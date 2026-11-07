import { useEffect, useState } from "react";
import { ShieldAlert, Radio, CheckCircle2, MapPin, BellRing, Layers, Edit2, Trash2, MoreHorizontal } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { toast } from "sonner";
import { alertaService, type Alerta } from "../services/api";

export default function Alertas() {
  const [alertas, setAlertas] = useState<Alerta[]>([]);
  const [openCadastro, setOpenCadastro] = useState(false);
  const [openEdicao, setOpenEdicao] = useState(false);
  const [alertaEditando, setAlertaEditando] = useState<Alerta | null>(null);

  const carregarAlertas = async () => {
    try {
      const dados = await alertaService.listarTodos();
      setAlertas(dados);
    } catch {
      toast.error("Falha ao sincronizar alertas com a API unificada.");
    }
  };

  useEffect(() => {
    carregarAlertas();
  }, []);

  const handleCadastro = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);

    const novoAlerta: Alerta = {
      titulo: f.get("titulo") as string,
      descricao: f.get("descricao") as string,
      categoria: f.get("categoria") as string,
      localizacao: f.get("localizacao") as string,
      lido: false
    };

    try {
      await alertaService.cadastrar(novoAlerta);
      toast.success("Alerta emergencial emitido e enviado para os dispositivos móveis!");
      setOpenCadastro(false);
      carregarAlertas();
    } catch {
      toast.error("Erro ao publicar o alerta no Supabase.");
    }
  };

  const handleUpdate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!alertaEditando?.id) return;
    const f = new FormData(e.currentTarget);

    const dados: Alerta = {
      titulo: f.get("titulo") as string,
      descricao: f.get("descricao") as string,
      categoria: f.get("categoria") as string,
      localizacao: f.get("localizacao") as string,
      lido: alertaEditando.lido
    };

    try {
      await alertaService.atualizar(alertaEditando.id, dados);
      toast.success("Parâmetros do alerta modificados.");
      setOpenEdicao(false);
      carregarAlertas();
    } catch {
      toast.error("Erro ao atualizar o registro no banco de dados.");
    }
  };

  const handleMarcarComoLido = async (id: number) => {
    try {
      await alertaService.marcarComoLido(id);
      toast.success("Alerta arquivado e removido do painel ativo.");
      carregarAlertas();
    } catch {
      toast.error("Falha ao atualizar o estado do alerta.");
    }
  };

  const handleDeletar = async (id: number) => {
    if (!confirm("Remover este alerta apagará o histórico permanentemente do Supabase. Deseja continuar?")) return;
    try {
      await alertaService.deletar(id);
      toast.success("Registro removido com sucesso.");
      carregarAlertas();
    } catch {
      toast.error("Falha ao deletar a publicação polimórfica.");
    }
  };

  return (
    <div className="space-y-6 pb-10">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-black flex items-center gap-2 text-slate-900">
          <ShieldAlert className="w-8 h-8 text-amber-500" /> Gestão de Alertas Críticos
        </h1>

        <Dialog open={openCadastro} onOpenChange={setOpenCadastro}>
          <DialogTrigger asChild>
            <Button className="bg-amber-500 font-bold hover:bg-amber-600 shadow-sm gap-1 text-white">
              <Radio className="w-4 h-4 animate-pulse" /> Emitir Alerta
            </Button>
          </DialogTrigger>
          {/* 🟢 ADICIONADO: aria-describedby={undefined} */}
          <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl">
            <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3 flex items-center gap-2">
              <Radio className="w-5 h-5 text-amber-500" /> Lançar Transmissão de Emergência
            </DialogTitle>
            <form onSubmit={handleCadastro} className="space-y-4 pt-4">
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Título do Alerta (Notificação)</Label>
                <Input name="titulo" required placeholder="Ex: Surto de Dengue Detectado" />
              </div>
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Instruções de Contingência para a População</Label>
                <textarea name="descricao" required rows={3} placeholder="Descreva os cuidados e orientações médicas que os cidadãos devem seguir no smartphone..." className="w-full border rounded-md p-2.5 text-sm border-slate-200 outline-none focus:ring-2 focus:ring-amber-500 font-medium" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Grau de Risco (Gatilho Push)</Label>
                  <select name="categoria" required className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="Epidemia">Epidemia (Surtos / Vírus)</option>
                    <option value="Urgência">Urgência Técnica (Bloqueio de Leitos)</option>
                    <option value="Informativo">Aviso Geral (Informativos de Saúde)</option>
                  </select>
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Foco Geográfico (Cidade/Bairro)</Label>
                  <Input name="localizacao" required placeholder="Ex: Zona Norte / Geral" />
                </div>
              </div>
              <Button type="submit" className="w-full bg-amber-500 hover:bg-amber-600 font-bold text-white h-11 shadow mt-4">
                Disparar Alerta para o Banco e Mobile
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {alertas.map((a) => (
          <Card key={a.id} className="border border-slate-200 shadow-sm bg-white overflow-hidden flex flex-col justify-between rounded-xl">
            <CardContent className="p-6 space-y-4">
              <div className="flex justify-between items-start">
                <div className="space-y-1">
                  <h3 className="font-black text-lg text-slate-900 leading-snug flex items-center gap-1.5">
                    <BellRing className="w-4 h-4 text-amber-500 shrink-0" /> {a.titulo}
                  </h3>
                  <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-slate-400">
                    <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" /> Foco: {a.localizacao || "Geral"}</span>
                    <span>•</span>
                    <span className="flex items-center gap-1"><Layers className="w-3.5 h-3.5" /> Nível: {a.categoria}</span>
                  </div>
                </div>

                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-slate-400 hover:text-slate-600 rounded-md">
                      <MoreHorizontal className="w-4 h-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-36 border bg-white shadow-md rounded-lg">
                    <DropdownMenuItem onClick={() => a.id && handleMarcarComoLido(a.id)} className="gap-2 cursor-pointer font-bold text-xs text-emerald-600">
                      <CheckCircle2 className="w-3.5 h-3.5" /> Arquivar (Lido)
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => { setAlertaEditando(a); setOpenEdicao(true); }} className="gap-2 cursor-pointer font-bold text-xs text-amber-600">
                      <Edit2 className="w-3.5 h-3.5" /> Modificar
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => a.id && handleDeletar(a.id)} className="gap-2 cursor-pointer font-bold text-xs text-red-600">
                      <Trash2 className="w-3.5 h-3.5" /> Remover
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              <p className="text-sm text-slate-600 font-medium leading-relaxed">{a.descricao}</p>

              <div className="flex items-center justify-between border-t pt-4 mt-2">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  Postado em: {a.dataPostada ? new Date(a.dataPostada).toLocaleString() : "Recentemente"}
                </span>
                <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-red-100 text-red-700 border border-red-200 animate-pulse">
                  Ativo no Celular
                </span>
              </div>
            </CardContent>
          </Card>
        ))}

        {alertas.length === 0 && (
          <div className="col-span-2 text-center py-12 text-slate-400 font-medium italic bg-white border rounded-xl shadow-sm">
            Nenhum alerta crítico ou surto ativo pendente no Supabase.
          </div>
        )}
      </div>

      <Dialog open={openEdicao} onOpenChange={setOpenEdicao}>
        {/* 🟢 ADICIONADO: aria-describedby={undefined} */}
        <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl">
          <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3">Modificar Parâmetros do Alerta</DialogTitle>
          {alertaEditando && (
            <form onSubmit={handleUpdate} className="space-y-4 pt-4">
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Título do Alerta</Label>
                <Input name="titulo" defaultValue={alertaEditando.titulo} required />
              </div>
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Instruções de Contingência</Label>
                <textarea name="descricao" defaultValue={alertaEditando.descricao} required rows={3} className="w-full border rounded-md p-2.5 text-sm border-slate-200 outline-none focus:ring-2 focus:ring-amber-500 font-medium" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Grau de Risco</Label>
                  <select name="categoria" defaultValue={alertaEditando.categoria} className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="Epidemia">Epidemia</option>
                    <option value="Urgência">Urgência Técnica</option>
                    <option value="Informativo">Aviso Geral</option>
                  </select>
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Foco Geográfico</Label>
                  <Input name="localizacao" defaultValue={alertaEditando.localizacao} required />
                </div>
              </div>
              <Button type="submit" className="w-full bg-amber-500 hover:bg-amber-600 font-bold text-white h-11 shadow mt-4">
                Salvar Alterações Técnico-Operacionais
              </Button>
            </form>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}