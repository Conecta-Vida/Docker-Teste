import { useEffect, useState } from "react";
import { Megaphone, Calendar, Plus, Users, Layers, Edit2, Trash2, MoreHorizontal } from "lucide-react"; 
import { campanhaService, type Campanha } from "../services/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { toast } from "sonner";

export default function Campanhas() {
  const [campanhas, setCampanhas] = useState<Campanha[]>([]);
  const [openCadastro, setOpenCadastro] = useState(false);
  const [openEdicao, setOpenEdicao] = useState(false);
  const [campanhaEditando, setCampanhaEditando] = useState<Campanha | null>(null);

  const carregarCampanhas = async () => {
    try {
      const dados = await campanhaService.listarTodas();
      setCampanhas(dados);
    } catch {
      toast.error("Erro ao sincronizar campanhas com a base Java.");
    }
  };

  useEffect(() => {
    carregarCampanhas();
  }, []);

  const handleCadastro = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);

    const nova: Campanha = {
      titulo: f.get("titulo") as string,
      descricao: f.get("descricao") as string,
      categoria: f.get("categoria") as string,
      publicoAlvo: f.get("publicoAlvo") as string,
      localizacao: f.get("localizacao") as string,
      status: f.get("status") as string, 
      dataInicio: f.get("dataInicio") as string,
      dataFim: f.get("dataFim") as string,
      linkimagem: (f.get("linkimagem") as string) || "https://images.unsplash.com/photo-1576091160550-2173dba999ef?q=80&w=800"
    };

    try {
      await campanhaService.cadastrar(nova);
      toast.success("Campanha comunitária lançada com sucesso!");
      setOpenCadastro(false);
      carregarCampanhas();
    } catch {
      toast.error("Erro ao salvar a campanha no Supabase.");
    }
  };

  const handleUpdate = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!campanhaEditando?.id) return;
    const f = new FormData(e.currentTarget);

    const dados: Campanha = {
      titulo: f.get("titulo") as string,
      descricao: f.get("descricao") as string,
      categoria: f.get("categoria") as string,
      publicoAlvo: f.get("publicoAlvo") as string,
      localizacao: f.get("localizacao") as string,
      status: f.get("status") as string,
      dataInicio: f.get("dataInicio") as string,
      dataFim: f.get("dataFim") as string,
      linkimagem: campanhaEditando.linkimagem
    };

    try {
      await campanhaService.atualizar(campanhaEditando.id, dados);
      toast.success("Campanha atualizada com sucesso!");
      setOpenEdicao(false);
      carregarCampanhas();
    } catch {
      toast.error("Falha ao modificar os dados da campanha.");
    }
  };

  const handleDeletar = async (id: number) => {
    if (!confirm("Deseja realmente remover esta campanha do ecossistema?")) return;
    try {
      await campanhaService.deletar(id);
      toast.success("Campanha removida da base.");
      carregarCampanhas();
    } catch {
      toast.error("Erro ao deletar o registro polimórfico.");
    }
  };

  return (
    <div className="space-y-6 pb-10">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-black flex items-center gap-2 text-slate-900">
          <Megaphone className="w-8 h-8 text-blue-600" /> Campanhas de Saúde
        </h1>

        <Dialog open={openCadastro} onOpenChange={setOpenCadastro}>
          <DialogTrigger asChild>
            <Button className="bg-blue-600 font-bold hover:bg-blue-700 shadow-sm gap-1 text-white border-none">
              <Plus className="w-4 h-4" /> Nova Campanha
            </Button>
          </DialogTrigger>
          <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl border-none shadow-lg max-h-[90vh] overflow-y-auto">
            <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3 flex items-center gap-2">
              <Megaphone className="w-5 h-5 text-blue-600" /> Cadastrar Mutirão Comunitário
            </DialogTitle>
            <form onSubmit={handleCadastro} className="space-y-4 pt-4">
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Título da Ação</Label>
                <Input name="titulo" required placeholder="Ex: Campanha de Doação de Sangue O-" />
              </div>
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Descrição / Instruções aos Cidadãos</Label>
                <textarea name="descricao" required rows={3} placeholder="Instruções completas sobre o mutirão de saúde..." className="w-full border rounded-md p-2.5 text-sm border-slate-200 outline-none focus:ring-2 focus:ring-blue-600 font-medium" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Foco Temático</Label>
                  <select name="categoria" required className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="Vacinação">Campanha de Vacinação</option>
                    <option value="Doação de Sangue">Doação de Sangue</option>
                    <option value="Saúde da Mulher">Saúde da Mulher</option>
                    <option value="Saúde do Homem">Saúde do Homem</option>
                    <option value="Check-up Médico">Check-up Médico</option>
                    <option value="Saúde Mental">Saúde Mental</option>
                    <option value="Combate a Surtos">Combate a Surtos</option>
                  </select>
                </div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Público-Alvo</Label>
                  <Input name="publicoAlvo" required placeholder="Ex: População Geral, Idosos" />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Local de Atendimento</Label><Input name="localizacao" required placeholder="Ex: UBS Central" /></div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Estado Inicial</Label>
                  <select name="status" required className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="ATIVA">ATIVA (Visível no Mobile)</option>
                    <option value="CONCLUÍDA">CONCLUÍDA (Histórico)</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Data de Início</Label><Input name="dataInicio" type="datetime-local" required /></div>
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Previsão de Término</Label><Input name="dataFim" type="datetime-local" required /></div>
              </div>
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Link da Imagem de Capa (Opcional)</Label>
                <Input name="linkimagem" placeholder="https://exemplo.com/foto.jpg" />
              </div>
              <Button type="submit" className="w-full bg-blue-600 font-bold text-white h-11 shadow mt-4 border-none hover:bg-blue-700">
                Publicar Campanha e Sincronizar Mobile
              </Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {campanhas.map((c) => (
          <Card key={c.id} className="border border-slate-200 shadow-sm bg-white overflow-hidden flex flex-col justify-between rounded-xl">
            <CardContent className="p-6 space-y-4">
              <div className="flex justify-between items-start">
                <div className="space-y-1">
                  <h3 className="font-black text-lg text-slate-900 leading-snug">{c.titulo}</h3>
                  <div className="flex flex-wrap items-center gap-2 text-xs font-semibold text-slate-400">
                    <span className="flex items-center gap-1"><Calendar className="w-3.5 h-3.5" /> {c.dataInicio ? new Date(c.dataInicio).toLocaleDateString() : "-"}</span>
                    <span>•</span>
                    <span className="flex items-center gap-1"><Users className="w-3.5 h-3.5" /> Alvo: {c.publicoAlvo}</span>
                  </div>
                </div>

                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-slate-400 hover:text-slate-600 rounded-md">
                      <MoreHorizontal className="w-4 h-4" />
                    </Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-32 border bg-white shadow-md rounded-lg p-1">
                    <DropdownMenuItem onClick={() => { setCampanhaEditando(c); setOpenEdicao(true); }} className="gap-2 cursor-pointer font-bold text-xs text-amber-600 px-3 py-2 rounded hover:bg-slate-50">
                      <Edit2 className="w-3.5 h-3.5" /> Editar
                    </DropdownMenuItem>
                    <DropdownMenuItem onClick={() => c.id && handleDeletar(c.id)} className="gap-2 cursor-pointer font-bold text-xs text-red-600 px-3 py-2 rounded hover:bg-slate-50">
                      <Trash2 className="w-3.5 h-3.5" /> Excluir
                    </DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              <p className="text-sm text-slate-600 font-medium line-clamp-3 leading-relaxed">{c.descricao}</p>

              <div className="flex items-center justify-between border-t pt-4 mt-2">
                <span className="text-xs font-bold text-slate-400 flex items-center gap-1">
                  <Layers className="w-3.5 h-3.5" /> {c.categoria || "Geral"}
                </span>
                <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider ${
                  c.status === "ATIVA" || c.status === "Ativa"
                    ? "bg-emerald-100 text-emerald-700 border border-emerald-200"
                    : "bg-slate-100 text-slate-500 border border-slate-200"
                }`}>
                  {c.status}
                </span>
              </div>
            </CardContent>
          </Card>
        ))}

        {campanhas.length === 0 && (
          <div className="col-span-2 text-center py-12 text-slate-400 font-medium italic bg-white border rounded-xl shadow-sm">
            Nenhuma campanha ou mutirão comunitário cadastrado no Supabase.
          </div>
        )}
      </div>

      <Dialog open={openEdicao} onOpenChange={setOpenEdicao}>
        <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl border-none shadow-lg max-h-[90vh] overflow-y-auto">
          <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3">Modificar Parâmetros da Campanha</DialogTitle>
          {campanhaEditando && (
            <form onSubmit={handleUpdate} className="space-y-4 pt-4">
              <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Título da Ação</Label><Input name="titulo" defaultValue={campanhaEditando.titulo} required /></div>
              <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Descrição / Instruções</Label><textarea name="descricao" defaultValue={campanhaEditando.descricao} required rows={3} className="w-full border rounded-md p-2.5 text-sm border-slate-200 outline-none focus:ring-2 focus:ring-blue-600 font-medium" /></div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Foco Temático</Label>
                  <select name="categoria" defaultValue={campanhaEditando.categoria} className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="Vacinação">Campanha de Vacinação</option>
                    <option value="Doação de Sangue">Doação de Sangue</option>
                    <option value="Saúde da Mulher">Saúde da Mulher</option>
                    <option value="Saúde do Homem">Saúde do Homem</option>
                    <option value="Check-up Médico">Check-up Médico</option>
                    <option value="Saúde Mental">Saúde Mental</option>
                    <option value="Combate a Surtos">Combate a Surtos</option>
                  </select>
                </div>
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Público-Alvo</Label><Input name="publicoAlvo" defaultValue={campanhaEditando.publicoAlvo} required /></div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Local de Atendimento</Label><Input name="localizacao" defaultValue={campanhaEditando.localizacao} required /></div>
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Estado da Campanha</Label>
                  <select name="status" defaultValue={campanhaEditando.status} className="h-10 w-full border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="ATIVA">ATIVA (Visível no Mobile)</option>
                    <option value="CONCLUÍDA">CONCLUÍDA (Histórico)</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Data de Início</Label><Input name="dataInicio" type="datetime-local" defaultValue={campanhaEditando.dataInicio ? campanhaEditando.dataInicio.substring(0, 16) : ""} required /></div>
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Data de Fim</Label><Input name="dataFim" type="datetime-local" defaultValue={campanhaEditando.dataFim ? campanhaEditando.dataFim.substring(0, 16) : ""} required /></div>
              </div>
              <Button type="submit" className="w-full bg-amber-500 font-bold text-white h-11 shadow mt-4 border-none hover:bg-amber-600">
                Salvar Alterações
              </Button>
            </form>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}