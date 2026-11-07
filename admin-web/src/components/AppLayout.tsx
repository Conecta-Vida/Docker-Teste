import { useState } from "react";
import { Link, Outlet, useLocation } from "react-router-dom";
import { 
  LayoutDashboard, 
  Users, 
  Building2, 
  Megaphone, 
  AlertTriangle, 
  LogOut,
  HeartPulse,
  Menu,
  X
} from "lucide-react";

export default function AppLayout() {
  const location = useLocation();
  // 🟢 ADICIONADO: Estado lógico para abrir/fechar o menu em ecrãs de telemóvel
  const [menuCelularAberto, setMenuCelularAberto] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem("@conecta:admin");
    window.location.href = "/login";
  };

  const menuItens = [
    { caminho: "/", label: "Dashboard", icone: LayoutDashboard },
    { caminho: "/usuarios", label: "Usuários", icone: Users },
    { caminho: "/instituicoes", label: "Unidades de Saúde", icone: Building2 },
    { caminho: "/campanhas", label: "Campanhas", icone: Megaphone },
    { caminho: "/alertas", label: "Gestão de Alertas", icone: AlertTriangle },
  ];

  return (
    <div className="flex min-h-screen w-full bg-slate-50 font-sans antialiased text-slate-900 relative">
      
      {/* 🟢 ADICIONADO: Fundo escurecido que surge no telemóvel para fechar o menu ao clicar fora */}
      {menuCelularAberto && (
        <div 
          className="fixed inset-0 bg-slate-900/40 z-20 md:hidden animate-in fade-in duration-200"
          onClick={() => setMenuCelularAberto(false)}
        />
      )}
      
      {/* ⚠️ BARRA LATERAL (SIDEBAR) - DESIGN ORIGINAL PRESERVADO, LÓGICA RESPONSIVA INJETADA */}
      <aside className={`fixed inset-y-0 left-0 z-30 flex w-64 flex-col border-r border-slate-200 bg-white shadow-sm transition-transform duration-300 md:translate-x-0 ${
        menuCelularAberto ? "translate-x-0" : "-translate-x-full"
      }`}>
        {/* CABEÇALHO DO MENU */}
        <div className="flex h-16 items-center justify-between border-b border-slate-100 px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-blue-600 text-white shadow-md shadow-blue-100">
              <HeartPulse className="h-5 w-5 animate-pulse" />
            </div>
            <div>
              <span className="block text-sm font-black text-slate-900 leading-none">Conecta Vida</span>
              <span className="text-[10px] font-bold text-blue-600 uppercase tracking-wider">Painel Admin</span>
            </div>
          </div>
          
          {/* Botão de fechar visível apenas no telemóvel */}
          <button onClick={() => setMenuCelularAberto(false)} className="p-1 md:hidden text-slate-400 hover:text-slate-900">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* LINKS DE NAVEGAÇÃO */}
        <nav className="flex-1 space-y-1 px-4 py-6">
          {menuItens.map((item) => {
            const itemAtivo = location.pathname === item.caminho;
            const Icone = item.icone;

            return (
              <Link
                key={item.caminho}
                to={item.caminho}
                onClick={() => setMenuCelularAberto(false)} // Fecha o menu ao clicar num item no telemóvel
                className={`flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-bold transition-all ${
                  itemAtivo
                    ? "bg-blue-50 text-blue-600 shadow-sm"
                    : "text-slate-500 hover:bg-slate-50 hover:text-slate-900"
                }`}
              >
                <Icone className={`h-4 w-4 shrink-0 ${itemAtivo ? "text-blue-600" : "text-slate-400"}`} />
                {item.label}
              </Link>
            );
          })}
        </nav>

        {/* RODAPÉ DO MENU */}
        <div className="border-t border-slate-100 p-4">
          <button
            onClick={handleLogout}
            className="flex w-full items-center gap-3 rounded-lg px-4 py-3 text-sm font-bold text-red-600 transition-colors hover:bg-red-50"
          >
            <LogOut className="h-4 w-4 shrink-0 text-red-500" />
            Sair do Sistema
          </button>
        </div>
      </aside>

      {/* ÁREA DE CONTEÚDO PRINCIPAL FLUIDA */}
      {/* 🟢 OTIMIZADO: Substituído pl-64 fixo por md:pl-64 para libertar a margem esquerda no telemóvel */}
      <main className="flex-1 md:pl-64 flex flex-col min-w-0 w-full">
        {/* CABEÇALHO TOPO */}
        <header className="h-16 bg-white border-b border-slate-200/60 sticky top-0 z-10 flex items-center justify-between md:justify-end px-4 sm:px-8">
          
          {/* 🟢 ADICIONADO: Botão hambúrguer visível APENAS em ecrãs pequenos (celular) */}
          <button 
            onClick={() => setMenuCelularAberto(true)} 
            className="p-2 border border-slate-200 rounded-xl bg-slate-50 text-slate-700 md:hidden hover:bg-slate-100 transition-colors"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-3 bg-slate-50 border border-slate-100 rounded-full py-1.5 px-4 shadow-sm">
            <div className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="text-xs font-black text-slate-700 tracking-wide uppercase">Servidor Online</span>
          </div>
        </header>

        {/* CONTÊINER DE PÁGINAS EXPANSIVAS */}
        {/* 🟢 OTIMIZADO: Ajustado o preenchimento (padding) para ser menor em telas de telemóvel (p-4 sm:p-8) */}
        <div className="p-4 sm:p-8 w-full flex-1 max-w-none mx-0">
          <Outlet /> 
        </div>
      </main>

    </div>
  );
}