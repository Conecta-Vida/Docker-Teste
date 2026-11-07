import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { HeartPulse, Lock, Mail, Loader2 } from "lucide-react";
import { authService } from "../services/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { toast } from "sonner";

export default function Login() {
  const navigate = useNavigate(); // Hook para redirecionar o usuário de página
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false); // Estado para animação de carregamento do botão

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault(); // Impede o recarregamento padrão da página ao enviar o formulário
    
    if (!email || !senha) {
      toast.warning("Por favor, preencha todos os campos.");
      return;
    }

    setLoading(true);
    try {
      // Consome o serviço de autenticação integrado à API Spring Boot
      const usuarioLogado = await authService.login(email, senha);
      
      // Armazena a sessão do administrador em formato string no LocalStorage da máquina
      localStorage.setItem("@conecta:admin", JSON.stringify(usuarioLogado));
      
      toast.success(`Bem-vindo de volta, ${usuarioLogado.nome}!`);
      navigate("/"); // Redireciona imediatamente para a home (Dashboard)
    } catch (error: any) {
      // 🟢 AJUSTE DE OURO: Captura a mensagem customizada em formato JSON enviada pelo AuthController do Spring Boot
      const mensagemServidor = error.response?.data?.mensagem;
      toast.error(mensagemServidor || error.message || "Erro ao tentar realizar o login.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md space-y-6 rounded-2xl border border-slate-200 bg-white p-8 shadow-xl">
        {/* LOGO E TÍTULO */}
        <div className="flex flex-col items-center text-center space-y-2">
          <div className="rounded-2xl bg-blue-600 p-3 text-white shadow-md shadow-blue-200">
            <HeartPulse className="h-8 w-8 animate-pulse" />
          </div>
          <h1 className="text-2xl font-black text-slate-900 tracking-tight">Conecta à Vida</h1>
          <p className="text-sm text-slate-500 font-medium">Painel Administrativo de Saúde Pública</p>
        </div>

        {/* FORMULÁRIO */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-1.5">
            <Label className="font-bold text-slate-700">E-mail Corporativo</Label>
            <div className="relative">
              <Mail className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
              <Input
                type="email"
                placeholder="nome@sistema.com"
                className="pl-10 bg-slate-50/50 focus-visible:ring-blue-600"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label className="font-bold text-slate-700">Senha de Acesso</Label>
            <div className="relative">
              <Lock className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
              <Input
                type="password"
                placeholder="••••••••"
                className="pl-10 bg-slate-50/50 focus-visible:ring-blue-600"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
              />
            </div>
          </div>

          <Button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 font-bold h-11 text-white hover:bg-blue-700 shadow shadow-blue-100"
          >
            {loading ? (
              <span className="flex items-center gap-2"><Loader2 className="h-4 w-4 animate-spin" /> Autenticando...</span>
            ) : (
              "Entrar no Sistema"
            )}
          </Button>
        </form>
      </div>
    </div>
  );
}