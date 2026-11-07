import { useEffect, useState } from "react";
import { Home, Plus, Edit2, Trash2, MoreHorizontal, Phone, MapPin, Mail, Globe } from "lucide-react";
import { instituicaoService, type InstituicaoSaude } from "../services/api";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { toast } from "sonner";

export default function Instituicoes() {
  const [instituicoes, setInstituicoes] = useState<InstituicaoSaude[]>([]);
  const [openCadastro, setOpenCadastro] = useState(false);
  const [openEdicao, setOpenEdicao] = useState(false);
  const [instituicaoEditando, setInstituicaoEditando] = useState<InstituicaoSaude | null>(null);

  const carregarInstituicoes = async () => {
    try {
      const dados = await instituicaoService.listarTodas();
      setInstituicoes(dados);
    } catch {
      toast.error("Erro ao sincronizar mapa de unidades de saúde.");
    }
  };

  useEffect(() => {
    carregarInstituicoes();
  }, []);

  const handleCadastro = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const f = new FormData(e.currentTarget);

    const novaUnidade: InstituicaoSaude = {
      tipoInstituicao: f.get("tipoInstituicao") as string, 
      nome: f.get("nome") as string,
      email: f.get("email") as string || undefined,
      telefone: f.get("telefone") as string,
      linksite: f.get("linksite") as string || undefined,
      endereco: f.get("endereco") as string || undefined,
      horarioSegSex: f.get("horarioSegSex") as string || undefined,
      horarioSabado: f.get("horarioSabado") as string || undefined,
      horarioDomingo: f.get("horarioDomingo") as string || undefined,
    };

    try {
      await instituicaoService.cadastrar(novaUnidade);
      toast.success("Unidade de saúde integrada com sucesso!");
      setOpenCadastro(false);
      carregarInstituicoes();
    } catch {
      toast.error("Falha ao registar a nova instituição.");
    }
  };

  const handleEdicao = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!instituicaoEditando?.id) return;
    const f = new FormData(e.currentTarget);

    const dadosAtualizados: InstituicaoSaude = {
      tipoInstituicao: f.get("tipoInstituicao") as string,
      nome: f.get("nome") as string,
      email: f.get("email") as string || undefined,
      telefone: f.get("telefone") as string,
      linksite: f.get("linksite") as string || undefined,
      endereco: f.get("endereco") as string || undefined,
      horarioSegSex: f.get("horarioSegSex") as string || undefined,
      horarioSabado: f.get("horarioSabado") as string || undefined,
      horarioDomingo: f.get("horarioDomingo") as string || undefined,
    };

    try {
      await instituicaoService.atualizar(instituicaoEditando.id, dadosAtualizados);
      toast.success("Cadastro da unidade atualizado com sucesso!");
      setOpenEdicao(false);
      setInstituicaoEditando(null);
      carregarInstituicoes();
    } catch {
      toast.error("Erro ao aplicar as alterações cadastrais.");
    }
  };

  const handleDeletar = async (id: number) => {
    if (!confirm("Atenção! Remover esta unidade pode impactar alertas e campanhas vinculados a ela. Deseja prosseguir?")) return;
    try {
      await instituicaoService.deletar(id);
      toast.success("Unidade de saúde removida do ecossistema.");
      carregarInstituicoes();
    } catch {
      toast.error("Não foi possível excluir a instituição selecionada.");
    }
  };

  return (
    <div className="space-y-6 pb-10">
      <div className="flex justify-between items-center border-b pb-4 border-slate-100">
        <div>
          <h1 className="text-3xl font-black flex items-center gap-2 text-slate-900">
            <Home className="w-8 h-8 text-blue-600" /> Unidades de Saúde
          </h1>
          <p className="text-slate-400 text-xs font-bold mt-1">Gestão de infraestrutura médica, postos de vacinação e hospitais parceiros.</p>
        </div>

        <Dialog open={openCadastro} onOpenChange={setOpenCadastro}>
          <DialogTrigger asChild>
            <Button className="bg-blue-600 font-bold hover:bg-blue-700 shadow-sm gap-1 text-white border-none">
              <Plus className="w-4 h-4" /> Nova Unidade
            </Button>
          </DialogTrigger>
          <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl shadow-lg max-h-[90vh] overflow-y-auto border-none">
            <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3 flex items-center gap-2">
              <Plus className="w-5 h-5 text-blue-600" /> Cadastrar Ponto de Atendimento Médico
            </DialogTitle>
            <form onSubmit={handleCadastro} className="space-y-4 pt-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="col-span-1 grid gap-1.5">
                  <Label className="font-bold text-slate-700">Tipo</Label>
                  <select name="tipoInstituicao" required className="h-10 border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="UBS">UBS</option>
                    <option value="Hospital">Hospital</option>
                    <option value="Hemocentro">Hemocentro</option>
                  </select>
                </div>
                <div className="col-span-2 grid gap-1.5"><Label className="font-bold text-slate-700">Nome Oficial da Unidade</Label><Input name="nome" required placeholder="Ex: UBS Central Bragança" /></div>
              </div>
              <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Endereço Completo</Label><Input name="endereco" placeholder="Rua, Número, Bairro - Cidade/SP" /></div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Telefone de Contato</Label><Input name="telefone" required placeholder="(11) 4003-0000" /></div>
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">E-mail Institucional</Label><Input name="email" type="email" placeholder="unidade@saude.gov.br" /></div>
              </div>
              <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Link do Website Oficial (linksite)</Label><Input name="linksite" placeholder="https://saude.com.br" /></div>
              <div className="grid gap-3 bg-slate-50 p-3 rounded-lg border border-slate-100">
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Segunda a Sexta</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioSegSex" placeholder="Ex: 07:00 às 19:00" /></div>
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Sábado</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioSabado" placeholder="Ex: 08:00 às 12:00" /></div>
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Domingo</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioDomingo" placeholder="Ex: Fechado" /></div>
              </div>
              <Button type="submit" className="w-full bg-blue-600 font-bold text-white h-11 shadow mt-2 hover:bg-blue-700 border-none">Registar Unidade no Banco</Button>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {instituicoes.map((inst) => (
          <Card key={inst.id} className="bg-white border border-slate-100 shadow-sm overflow-hidden rounded-xl relative group">
            <CardContent className="p-5 space-y-4">
              
              <div className="absolute top-4 right-4 z-20">
                <DropdownMenu>
                  <DropdownMenuTrigger asChild>
                    <Button variant="ghost" className="h-8 w-8 p-0 text-slate-400 bg-slate-50 rounded-full hover:bg-slate-100"><MoreHorizontal className="w-4 h-4" /></Button>
                  </DropdownMenuTrigger>
                  <DropdownMenuContent align="end" className="w-32 border bg-white shadow-md rounded-lg p-1">
                    <DropdownMenuItem onClick={() => { setInstituicaoEditando(inst); setOpenEdicao(true); }} className="gap-2 cursor-pointer font-bold text-xs text-amber-600 px-3 py-2 rounded hover:bg-slate-50"><Edit2 className="w-3.5 h-3.5" /> Editar</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => inst.id && handleDeletar(inst.id)} className="gap-2 cursor-pointer font-bold text-xs text-red-600 px-3 py-2 rounded hover:bg-slate-50"><Trash2 className="w-3.5 h-3.5" /> Excluir</DropdownMenuItem>
                  </DropdownMenuContent>
                </DropdownMenu>
              </div>

              <div>
                <span className="text-[10px] font-black uppercase tracking-wider px-2 py-0.5 rounded bg-blue-50 text-blue-700 border border-blue-100">{inst.tipoInstituicao}</span>
                <h3 className="text-lg font-black text-slate-900 mt-2 pr-8 truncate">{inst.nome}</h3>
              </div>
              <div className="space-y-2 text-xs font-semibold text-slate-600 border-t pt-3 border-slate-50">
                {inst.endereco && <p className="flex items-start gap-1.5 text-slate-500"><MapPin className="w-4 h-4 text-blue-500 shrink-0 mt-0.5" /> {inst.endereco}</p>}
                <p className="flex items-center gap-1.5"><Phone className="w-3.5 h-3.5 text-slate-400" /> {inst.telefone}</p>
                {inst.email && <p className="flex items-center gap-1.5"><Mail className="w-3.5 h-3.5 text-slate-400" /> {inst.email}</p>}
                {inst.linksite && <p className="flex items-center gap-1.5 text-blue-600 truncate"><Globe className="w-3.5 h-3.5" /> <a href={inst.linksite} target="_blank" rel="noreferrer" className="hover:underline">{inst.linksite}</a></p>}
              </div>
              <div className="bg-slate-50 p-2.5 rounded-lg border border-slate-100 text-[11px] space-y-1 font-bold text-slate-500">
                <div className="flex justify-between"><span>Seg a Sex:</span><span className="text-slate-700">{inst.horarioSegSex || "-"}</span></div>
                <div className="flex justify-between"><span>Sábado:</span><span className="text-slate-700">{inst.horarioSabado || "Fechado"}</span></div>
                <div className="flex justify-between"><span>Domingo:</span><span className="text-slate-700">{inst.horarioDomingo || "Fechado"}</span></div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <Dialog open={openEdicao} onOpenChange={setOpenEdicao}>
        <DialogContent aria-describedby={undefined} className="sm:max-w-[550px] p-6 bg-white rounded-xl shadow-lg max-h-[90vh] overflow-y-auto border-none">
          <DialogTitle className="text-xl font-black text-slate-900 border-b pb-3 flex items-center gap-2"><Edit2 className="w-5 h-5 text-amber-500" /> Atualizar Unidade de Saúde</DialogTitle>
          {instituicaoEditando && (
            <form onSubmit={handleEdicao} className="space-y-4 pt-4">
              <div className="grid grid-cols-3 gap-4">
                <div className="grid gap-1.5">
                  <Label className="font-bold text-slate-700">Tipo</Label>
                  <select name="tipoInstituicao" defaultValue={instituicaoEditando.tipoInstituicao} className="h-10 border rounded-md px-3 bg-white text-sm font-semibold border-slate-200">
                    <option value="UBS">UBS</option>
                    <option value="Hospital">Hospital</option>
                    <option value="Hemocentro">Hemocentro</option>
                  </select>
                </div>
                <div className="col-span-2 grid gap-1.5"><Label className="font-bold text-slate-700">Nome Oficial</Label><Input name="nome" defaultValue={instituicaoEditando.nome} required /></div>
              </div>
              <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Endereço</Label><Input name="endereco" defaultValue={instituicaoEditando.endereco || ""} /></div>
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Telefone</Label><Input name="telefone" defaultValue={instituicaoEditando.telefone} required /></div>
                <div className="grid gap-1.5"><Label className="font-bold text-slate-700">E-mail</Label><Input name="email" type="email" defaultValue={instituicaoEditando.email || ""} /></div>
              </div>
              
              {/* 🟢 ADICIONADO: Campo linksite no formulário de edição */}
              <div className="grid gap-1.5">
                <Label className="font-bold text-slate-700">Link do Website Oficial (linksite)</Label>
                <Input name="linksite" defaultValue={instituicaoEditando.linksite || ""} placeholder="https://saude.com.br" />
              </div>

              <div className="grid gap-3 bg-amber-50/40 p-3 rounded-lg border border-amber-100">
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Seg a Sex</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioSegSex" defaultValue={instituicaoEditando.horarioSegSex || ""} /></div>
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Sábado</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioSabado" defaultValue={instituicaoEditando.horarioSabado || ""} /></div>
                <div className="grid grid-cols-3 items-center gap-2"><Label className="font-bold text-xs text-slate-600">Domingo</Label><Input className="col-span-2 bg-white h-8 text-xs font-semibold" name="horarioDomingo" defaultValue={instituicaoEditando.horarioDomingo || ""} /></div>
              </div>
              <Button type="submit" className="w-full bg-amber-500 hover:bg-amber-600 font-bold text-white h-11 shadow mt-2 border-none">Salvar Alterações Cadastrais</Button>
            </form>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}