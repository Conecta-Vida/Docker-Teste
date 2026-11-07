import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Megaphone, ArrowLeft, Loader2, Save, Trash2 } from "lucide-react";
import { campanhaService, type Campanha } from "../services/api";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";

export default function CampanhaDetalhes() {
  const { id } = useParams(); 
  const navigate = useNavigate();
  const [campanha, setCampanha] = useState<Campanha | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    campanhaService.buscarPorId(Number(id))
      .then(setCampanha)
      .catch(() => toast.error("Campanha não localizada."))
      .finally(() => setLoading(false));
  }, [id]);

  const handleSalvar = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!campanha || !id) return;
    const f = new FormData(e.currentTarget);
    const atualizada: Campanha = {
      ...campanha,
      titulo: f.get("titulo") as string,
      descricao: f.get("descricao") as string,
      status: f.get("status") as string, 
    };

    try {
      await campanhaService.atualizar(Number(id), atualizada);
      toast.success("Alterações salvas!");
      navigate("/campanhas");
    } catch {
      toast.error("Erro ao salvar.");
    }
  };

  const handleDeletar = async () => {
    if (!id || !confirm("Remover esta campanha definitivamente do feed público?")) return;
    try {
      await campanhaService.deletar(Number(id));
      toast.success("Campanha apagada do Supabase.");
      navigate("/campanhas");
    } catch {
      toast.error("Erro ao deletar.");
    }
  };

  if (loading) return <div className="flex h-[50vh] items-center justify-center"><Loader2 className="animate-spin text-blue-600" /></div>;
  if (!campanha) return <div className="text-center py-20 text-slate-400 font-bold">Nenhum dado encontrado.</div>;

  return (
    <div className="space-y-6 max-w-3xl mx-auto pb-10">
      <Button variant="ghost" size="sm" onClick={() => navigate("/campanhas")} className="gap-1 font-bold text-slate-500 hover:text-slate-900"><ArrowLeft className="w-4 h-4" /> Voltar</Button>
      
      <Card className="border-none shadow-sm bg-white overflow-hidden rounded-xl">
        <CardHeader className="bg-slate-50 border-b border-slate-100 flex flex-row items-center justify-between">
          <CardTitle className="text-lg font-black flex items-center gap-1.5"><Megaphone className="w-5 h-5 text-blue-600" /> Detalhes operacionais</CardTitle>
          <Button variant="destructive" size="sm" onClick={handleDeletar} className="font-bold gap-1 border-none shadow-sm"><Trash2 className="w-4 h-4" /> Encerrar</Button>
        </CardHeader>
        <CardContent className="p-6">
          <form onSubmit={handleSalvar} className="space-y-4">
            <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Título da Campanha</Label><Input name="titulo" defaultValue={campanha.titulo} required /></div>
            <div className="grid gap-1.5"><Label className="font-bold text-slate-700">Instruções Médicas / Descrição</Label><textarea name="descricao" defaultValue={campanha.descricao} required className="w-full min-h-[120px] border p-3 text-sm rounded-md bg-white border-slate-200 outline-none focus:ring-2 focus:ring-blue-600 font-medium" /></div>
            <div className="grid gap-1.5">
              <Label className="font-bold text-slate-700">Status Operacional</Label>
              <select name="status" defaultValue={campanha.status?.toUpperCase()} className="h-10 border bg-white border-slate-200 rounded-md px-3 text-sm font-semibold focus:ring-2 focus:ring-blue-600 w-full">
                <option value="ATIVA">Ativa (Visível no App)</option>
                <option value="ENCERRADA">Encerrada (Histórico)</option>
                <option value="AGENDADA">Agendada (Oculta)</option>
              </select>
            </div>
            <Button type="submit" className="w-full bg-blue-600 hover:bg-blue-700 font-bold text-white h-11 gap-1 shadow mt-4 border-none"><Save className="w-4 h-4" /> Salvar Modificações</Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}