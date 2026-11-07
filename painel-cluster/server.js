// =====================================================================
// Painel do Cluster Conecta Vida
// Visualizador próprio do Docker Swarm (diferencial do projeto).
//
// - Lê o estado do cluster pela API do Docker (socket do manager)
// - GET  /api/estado            -> nós, serviços e réplicas (JSON)
// - GET  /api/eventos           -> feed ao vivo (Server-Sent Events)
// - POST /api/escalar/:servico  -> {"replicas": N} altera as réplicas
//
// Sem dependências externas: só o Node.js.
// =====================================================================
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORTA = Number(process.env.PORT || 8090);
const SOCKET = process.env.DOCKER_SOCKET || '/var/run/docker.sock';
const STACK = process.env.STACK || 'conectavida';
// Serviços que podem ser escalados pela tela (o banco e o painel não)
const ESCALAVEIS = (process.env.ESCALAVEIS || 'admin-api,mobile-api,admin-web,mobile-web').split(',');
const MAX_REPLICAS = Number(process.env.MAX_REPLICAS || 8);

// ---------- Cliente mínimo da API do Docker (HTTP sobre socket Unix) ----------
function docker(metodo, caminho, corpo) {
  return new Promise((resolve, reject) => {
    const dados = corpo ? JSON.stringify(corpo) : null;
    const req = http.request({
      socketPath: SOCKET, method: metodo, path: caminho,
      headers: dados ? { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(dados) } : {},
    }, (res) => {
      let txt = '';
      res.on('data', (c) => (txt += c));
      res.on('end', () => {
        if (res.statusCode >= 400) return reject(new Error(`Docker ${res.statusCode}: ${txt}`));
        try { resolve(txt ? JSON.parse(txt) : {}); } catch { resolve(txt); }
      });
    });
    req.on('error', reject);
    if (dados) req.write(dados);
    req.end();
  });
}

// ---------- Estado do cluster ----------
const nomeCurto = (nome) => nome.replace(`${STACK}_`, '');

function tipoDoServico(nome) {
  if (/postgres|mysql|mongo|banco/.test(nome)) return 'banco';
  if (/-api$/.test(nome)) return 'api';
  if (/-web$/.test(nome)) return 'web';
  if (/visualizer|painel/.test(nome)) return 'monitor';
  if (/registry/.test(nome)) return 'registry';
  return 'outro';
}

async function lerEstado() {
  const [nos, servicos, tarefas] = await Promise.all([
    docker('GET', '/nodes'),
    docker('GET', '/services'),
    docker('GET', '/tasks?filters=' + encodeURIComponent(JSON.stringify({ 'desired-state': ['running'] }))),
  ]);

  const porId = Object.fromEntries(servicos.map((s) => [s.ID, s]));
  const nosOrdenados = nos
    .map((n) => ({
      id: n.ID,
      nome: n.Description.Hostname,
      papel: n.Spec.Role,
      lider: Boolean(n.ManagerStatus && n.ManagerStatus.Leader),
      status: n.Status.State,              // ready / down
      disponibilidade: n.Spec.Availability, // active / drain
      labels: n.Spec.Labels || {},
      cpus: Math.round((n.Description.Resources.NanoCPUs || 0) / 1e9),
      memoriaGB: +((n.Description.Resources.MemoryBytes || 0) / 1024 ** 3).toFixed(1),
      tarefas: [],
    }))
    .sort((a, b) => (a.papel === b.papel ? a.nome.localeCompare(b.nome) : a.papel === 'manager' ? -1 : 1));
  const no = Object.fromEntries(nosOrdenados.map((n) => [n.id, n]));

  for (const t of tarefas) {
    const s = porId[t.ServiceID];
    if (!s || !no[t.NodeID]) continue;
    const nome = s.Spec.Name;
    no[t.NodeID].tarefas.push({
      id: t.ID.slice(0, 10),
      servico: nomeCurto(nome),
      tipo: tipoDoServico(nome),
      slot: t.Slot || null,
      estado: t.Status.State,            // running / starting / preparing / ...
      desde: t.Status.Timestamp,
      imagem: (s.Spec.TaskTemplate.ContainerSpec.Image || '').split('@')[0].split('/').pop(),
    });
  }
  for (const n of nosOrdenados) n.tarefas.sort((a, b) => a.servico.localeCompare(b.servico) || (a.slot || 0) - (b.slot || 0));

  const resumo = servicos
    .filter((s) => s.Spec.Name.startsWith(STACK + '_') || s.Spec.Name === 'registry')
    .map((s) => {
      const nome = nomeCurto(s.Spec.Name);
      const desejadas = s.Spec.Mode.Replicated ? s.Spec.Mode.Replicated.Replicas : nosOrdenados.length;
      const rodando = tarefas.filter((t) => t.ServiceID === s.ID && t.Status.State === 'running').length;
      return { nome, tipo: tipoDoServico(nome), desejadas, rodando, escalavel: ESCALAVEIS.includes(nome) };
    })
    .sort((a, b) => a.nome.localeCompare(b.nome));

  return { stack: STACK, atualizadoEm: new Date().toISOString(), nos: nosOrdenados, servicos: resumo, maxReplicas: MAX_REPLICAS };
}

// ---------- Feed de eventos: compara o estado a cada 2 s ----------
const clientes = new Set();
const historico = [];
let anterior = null;

function emitir(tipo, texto) {
  const ev = { hora: new Date().toISOString(), tipo, texto };
  historico.push(ev);
  if (historico.length > 50) historico.shift();
  for (const res of clientes) res.write(`data: ${JSON.stringify(ev)}\n\n`);
}

async function observar() {
  try {
    const atual = await lerEstado();
    const mapa = (e) => {
      const m = {};
      for (const n of e.nos) for (const t of n.tarefas) m[t.id] = { ...t, no: n.nome };
      return m;
    };
    if (anterior) {
      const a = mapa(anterior), b = mapa(atual);
      for (const id in b) {
        if (!a[id]) emitir('nova', `Réplica de ${b[id].servico} criada em ${b[id].no}`);
        else if (a[id].estado !== b[id].estado && b[id].estado === 'running') emitir('ok', `${b[id].servico} em ${b[id].no} está rodando`);
      }
      for (const id in a) if (!b[id]) emitir('removida', `Réplica de ${a[id].servico} removida de ${a[id].no}`);
      const nosA = Object.fromEntries(anterior.nos.map((n) => [n.nome, n.status]));
      for (const n of atual.nos) {
        if (nosA[n.nome] && nosA[n.nome] !== n.status) {
          emitir(n.status === 'ready' ? 'ok' : 'alerta', n.status === 'ready' ? `Nó ${n.nome} voltou (Ready)` : `Nó ${n.nome} caiu (${n.status})`);
        }
      }
    } else {
      emitir('info', `Painel conectado ao cluster: ${atual.nos.length} nós, ${atual.servicos.length} serviços`);
    }
    anterior = atual;
  } catch (e) {
    console.error('Erro ao ler o cluster:', e.message);
  }
}
setInterval(observar, 2000);
observar();

// ---------- Escalar um serviço ----------
async function escalar(servico, replicas) {
  if (!ESCALAVEIS.includes(servico)) throw Object.assign(new Error(`O serviço ${servico} não pode ser escalado pela tela.`), { status: 400 });
  if (!Number.isInteger(replicas) || replicas < 0 || replicas > MAX_REPLICAS) {
    throw Object.assign(new Error(`Réplicas devem ficar entre 0 e ${MAX_REPLICAS}.`), { status: 400 });
  }
  const nome = `${STACK}_${servico}`;
  const s = await docker('GET', `/services/${encodeURIComponent(nome)}`);
  const spec = s.Spec;
  const antes = spec.Mode.Replicated.Replicas;
  spec.Mode.Replicated.Replicas = replicas;
  await docker('POST', `/services/${s.ID}/update?version=${s.Version.Index}`, spec);
  emitir('escala', `${servico}: ${antes} → ${replicas} réplicas (pelo painel)`);
  return { servico, antes, depois: replicas };
}

// ---------- Servidor HTTP ----------
const PUBLICO = path.join(__dirname, 'public');
const TIPOS = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript', '.css': 'text/css', '.svg': 'image/svg+xml' };

function json(res, status, obj) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store' });
  res.end(JSON.stringify(obj));
}

http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://x');
  try {
    if (req.method === 'GET' && url.pathname === '/api/estado') return json(res, 200, await lerEstado());
    if (req.method === 'GET' && url.pathname === '/healthz') return json(res, 200, { status: 'ok' });

    if (req.method === 'GET' && url.pathname === '/api/eventos') {
      res.writeHead(200, { 'Content-Type': 'text/event-stream', 'Cache-Control': 'no-cache', Connection: 'keep-alive' });
      for (const ev of historico.slice(-15)) res.write(`data: ${JSON.stringify(ev)}\n\n`);
      clientes.add(res);
      req.on('close', () => clientes.delete(res));
      return;
    }

    const m = url.pathname.match(/^\/api\/escalar\/([\w-]+)$/);
    if (req.method === 'POST' && m) {
      let corpo = '';
      for await (const c of req) { corpo += c; if (corpo.length > 1000) break; }
      const { replicas } = JSON.parse(corpo || '{}');
      return json(res, 200, await escalar(m[1], Number(replicas)));
    }

    // Arquivos estáticos (index.html)
    const arq = path.join(PUBLICO, url.pathname === '/' ? 'index.html' : path.normalize(url.pathname).replace(/^(\.\.[/\\])+/, ''));
    if (arq.startsWith(PUBLICO) && fs.existsSync(arq) && fs.statSync(arq).isFile()) {
      res.writeHead(200, { 'Content-Type': TIPOS[path.extname(arq)] || 'application/octet-stream' });
      return fs.createReadStream(arq).pipe(res);
    }
    json(res, 404, { erro: 'Não encontrado' });
  } catch (e) {
    json(res, e.status || 500, { erro: e.message });
  }
}).listen(PORTA, () => console.log(`Painel do Cluster Conecta Vida em http://0.0.0.0:${PORTA}`));
