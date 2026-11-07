import { useEffect, useState, useMemo } from "react";
import { Users, FileText, Download, Upload, Plus, MoreHorizontal, Edit2, Trash2, UserCheck } from "lucide-react";
import { type Usuario, usuarioService, relatorioService } from "../services/api";
import { Card } from "@/components/ui/card";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { toast } from "sonner";

export default function Usuarios() {
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [busca, setBusca] = useState("");
  const [openCadastro, setOpenCadastro] = useState(false);
  const [openEdicao, setOpenEdicao] = useState(false);
  const [usuarioEditando, setUsuarioEditando] = useState<Usuario | null>(null);

  const carregarUsuarios = async () => {
    try {
      const dados = await usuarioService.listarTodos();
      setUsuarios(dados);
    } catch {
      toast.error("Erro ao carregar a lista de cidadãos.");
    }
  };

  useEffect(() => {
    carregarUsuarios();
  }, []);

  const filtrados = useMemo(() => {
    const termo = busca.toLowerCase();
    return usuarios.filter(
      (u) =>
        u.nome.toLowerCase().includes(termo) ||
        u.email.toLowerCase().includes(termo)
    );
  }, [usuarios, busca]);

  const handleCadastro = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);
    
    const novo: Usuario = {
      nome: f.get("nome") as string,
      email: f.get("email") as string,
      senha: (f.get("senha") as string) || "123456", 
      dataNascimento: f.get("dataNascimento") ? Number(f.get("dataNascimento")) : undefined, // 🟢 dataNascimento
      sexo: (f.get("sexo") as string) || undefined,
      localizacao: "Bragança Paulista", 
      permissao: f.get("permissao") as string, 
    };

    try {
      await usuarioService.cadastrar(novo);
      toast.success("Registro criado com sucesso!");
      setOpenCadastro(false);
      carregarUsuarios();
    } catch {
      toast.error("Falha ao salvar o novo registro.");
    }
  };

  const handleUpdate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!usuarioEditando?.id) return;
    const f = new FormData(e.currentTarget);

    const dados: Usuario = {
      nome: f.get("nome") as string,
      email: f.get("email") as string,
      dataNascimento: f.get("dataNascimento") ? Number(f.get("dataNascimento")) : undefined, // 🟢 dataNascimento
      sexo: (f.get("sexo") as string) || undefined,
      localizacao: usuarioEditando.localizacao || "Bragança Paulista",
      permissao: f.get("permissao") as string,
    };

    try {
      await usuarioService.atualizar(usuarioEditando.id, dados);
      toast.success("Dados atualizados com sucesso!");
      setOpenEdicao(false);
      carregarUsuarios();
    } catch {
      toast.error("Erro na atualização dos dados.");
    }
  };

  const handleDeletar = async (id: number) => {
    if (!confirm("Tem certeza que deseja remover este registro do sistema?")) return;
    try {
      await usuarioService.deletar(id);
      toast.success("Registro removido.");
      carregarUsuarios();
    } catch {
      toast.error("Não foi possível deletar o usuário.");
    }
  };

  const handleImportarCsv = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      const msg = await usuarioService.importarCsv(file);
      toast.success(msg || "Carga de dados processada com sucesso!");
      carregarUsuarios();
    } catch (err: any) {
      toast.error(err.message || "Erro ao processar o arquivo CSV.");
    }
  };

  return (
    <div className="space-y-6 pb-10">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h1 className="text-3xl font-black flex items-center gap-2 text-slate-900">
          <Users className="w-8 h-8 text-blue-600" /> Controle de Usuários
        </h1>
        
        <div className="flex flex-wrap items-center gap-2">
          <Button variant="outline" onClick={() => window.open(relatorioService.getUrlDownloadPdf(), '_blank')} className="font-bold text-slate-700 gap-1.5 border-slate-200 bg-white">
            <FileText className="w-4 h-4 text-red-500" /> Exportar PDF
          </Button>
          
          <Button variant="outline" onClick={() => window.open(relatorioService.getUrlExportarCsv(), '_blank')} className="font-bold text-slate-700 gap-1.5 border-slate-200 bg-white">
            <Download className="w-4 h-4 text-emerald-600" /> Exportar CSV
          </Button>

          <label className="cursor-pointer inline-flex h-10 items-center justify-center rounded-md border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 shadow-sm hover:bg-slate-50 gap-1.5">
            <Upload className="w-4 h-4 text-blue-600" /> Importar CSV
            <input type="file" accept=".csv" onChange={handleImportarCsv} className="hidden" />
          </label>

          <Dialog open={openCadastro} onOpenChange={setOpenCadastro}>
            <DialogTrigger asChild>
              <Button className="bg-blue-600 font-bold hover:bg-blue-700 shadow-sm gap-1">
                <Plus className="w-4 h-4" /> Novo Registro
              </Button>
            </DialogTrigger>
            <DialogContent aria-describedby={undefined} className="sm:max-w-[500px] p-6 bg-white rounded-xl">
              <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3 flex items-center gap-2">
                <UserCheck className="w-5 h-5 text-blue-600" /> Cadastrar Usuário ou Administrador
              </DialogTitle>
              <form onSubmit={handleCadastro} className="space-y-4 pt-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Nome Completo</Label>
                  <Input name="nome" required placeholder="Ex: Luiz Henrique" />
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">E-mail Institucional</Label>
                  <Input name="email" type="email" required placeholder="exemplo@conecta.com" />
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Senha de Acesso</Label>
                  <Input name="senha" type="password" placeholder="Defina uma senha (Padrão: 123456)" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="grid gap-1.5">
                    <Label className="font-bold text-slate-700">Ano de Nascimento</Label>
                    <Input name="dataNascimento" type="number" required placeholder="Ex: 2004" />
                  </div>
                  <div className="grid gap-1.5">
                    <Label className="font-bold text-slate-700">Gênero / Sexo</Label>
                    <Input name="sexo" placeholder="Ex: Masculino" />
                  </div>
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Nível de Permissão (Cargo)</Label>
                  <select name="permissao" required className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold focus:ring-2 focus:ring-blue-600">
                    <option value="Usuário Comum">Usuário Comum (Cidadão)</option>
                    <option value="Administrador">Administrador (Gestor de Saúde)</option>
                  </select>
                </div>
                <Button type="submit" className="w-full bg-blue-600 font-bold text-white h-11 shadow mt-4">
                  Salvar Registro no Banco
                </Button>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <div className="relative">
        <Input
          placeholder="Buscar cidadão por nome ou e-mail..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="bg-white shadow-sm pl-4 h-11"
        />
      </div>

      <Card className="border border-slate-200 shadow-sm overflow-hidden bg-white rounded-xl">
        <Table>
          <TableHeader className="bg-slate-50/70 border-b border-slate-100">
            <TableRow>
              <TableHead className="font-bold text-slate-600 px-6 py-4">Nome</TableHead>
              <TableHead className="font-bold text-slate-600 px-6 py-4">E-mail</TableHead>
              <TableHead className="font-bold text-slate-600 px-6 py-4">Idade / Sexo</TableHead>
              <TableHead className="font-bold text-slate-600 px-6 py-4">Permissão</TableHead>
              <TableHead className="w-[60px]"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtrados.map((u) => (
              <TableRow key={u.id} className="hover:bg-slate-50/40 transition-colors border-b border-slate-100 last:border-none">
                <TableCell className="font-semibold text-slate-900 px-6 py-4">{u.nome}</TableCell>
                <TableCell className="text-slate-600 font-medium px-6 py-4">{u.email}</TableCell>
                <TableCell className="text-slate-500 font-medium px-6 py-4">
                  {/* 🟢 Cálculo dinâmico da Idade baseado no ano atual (2026) */}
                  {u.dataNascimento ? `${2026 - u.dataNascimento} anos` : "-"} / {u.sexo || "-"}
                </TableCell>
                <TableCell className="px-6 py-4">
                  <span className={`px-2.5 py-1 rounded-md text-[11px] font-black uppercase tracking-wider ${
                    u.permissao === 'Administrador' || u.permissao === 'ADMINISTRADOR'
                      ? 'bg-purple-100 text-purple-700 border border-purple-200'
                      : 'bg-slate-100 text-slate-600 border border-slate-200'
                  }`}>
                    {u.permissao || "Usuário Comum"}
                  </span>
                </TableCell>
                <TableCell className="px-6 py-4">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-slate-400 hover:text-slate-600">
                        <MoreHorizontal className="w-4 h-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-32 border bg-white shadow-md rounded-lg">
                      <DropdownMenuItem onClick={() => { setUsuarioEditando(u); setOpenEdicao(true); }} className="gap-2 cursor-pointer font-bold text-xs text-amber-600">
                        <Edit2 className="w-3.5 h-3.5" /> Editar
                      </DropdownMenuItem>
                      <DropdownMenuItem onClick={() => u.id && handleDeletar(u.id)} className="gap-2 cursor-pointer font-bold text-xs text-red-600">
                        <Trash2 className="w-3.5 h-3.5" /> Excluir
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </TableCell>
              </TableRow>
            ))}
            {filtrados.length === 0 && (
              <TableRow>
                <TableCell colSpan={5} className="text-center py-10 text-slate-400 italic">
                  Nenhum cidadão ou administrador localizado na base de dados.
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>

      <Dialog open={openEdicao} onOpenChange={setOpenEdicao}>
        <DialogContent aria-describedby={undefined} className="sm:max-w-[500px] p-6 bg-white rounded-xl">
          <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3">Editar Dados de Registro</DialogTitle>
          {usuarioEditando && (
            <form onSubmit={handleUpdate} className="space-y-4 pt-4">
              <div className="grid gap-1.5"><Label>Nome Completo</Label><Input name="nome" defaultValue={usuarioEditando.nome} required /></div>
              <div className="grid gap-1.5"><Label>E-mail</Label><Input name="email" type="email" defaultValue={usuarioEditando.email} required /></div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5">
                  <Label>Ano de Nascimento</Label>
                  <Input name="dataNascimento" type="number" defaultValue={usuarioEditando.dataNascimento || ""} required />
                </div>
                <div className="grid gap-1.5"><Label>Gênero</Label><Input name="sexo" defaultValue={usuarioEditando.sexo || ""} /></div>
              </div>
              <div className="grid gap-1.5">
                <Label>Nível de Permissão (Cargo)</Label>
                <select name="permissao" defaultValue={usuarioEditando.permissao || "Usuário Comum"} className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold">
                  <option value="Usuário Comum">Usuário Comum (Cidadão)</option>
                  <option value="Administrador">Administrador (Gestor de Saúde)</option>
                </select>
              </div>
              <Button type="submit" className="w-full bg-amber-500 font-bold text-white h-11 mt-4 shadow">
                Salvar Alterações
              </Button>
            </form>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}