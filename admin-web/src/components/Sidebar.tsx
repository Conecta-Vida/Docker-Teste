import React from "react";
import { Link, useLocation } from "react-router-dom";
import { LayoutDashboard, Users, Megaphone, Building2 } from "lucide-react";

export const Sidebar: React.FC = () => {
  const location = useLocation();

  // Ordem rigorosa solicitada: Dashboard, Usuários, Campanhas, Instituições
  const menuItems = [
    { name: "Dashboard", path: "/", icon: LayoutDashboard },
    { name: "Usuários", path: "/usuarios", icon: Users },
    { name: "Campanhas", path: "/campanhas", icon: Megaphone },
    { name: "Instituições", path: "/instituicoes", icon: Building2 },
  ];

  return (
    <aside className="w-64 bg-white border-r border-gray-100 flex flex-col h-screen sticky top-0 shrink-0">
      {/* Logotipo / Cabeçalho do Menu */}
      <div className="p-6 border-b border-gray-100">
        <h1 className="text-xl font-bold text-blue-600 tracking-tight">ConectaVida</h1>
        <p className="text-xs text-gray-400 mt-0.5">Painel Administrativo</p>
      </div>

      {/* Lista de Navegação */}
      <nav className="flex-1 p-4 space-y-1.5 overflow-y-auto">
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = location.pathname === item.path;

          return (
            <Link
              key={item.path}
              to={item.path}
              className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all ${
                isActive
                  ? "bg-blue-50 text-blue-600 shadow-sm"
                  : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
              }`}
            >
              <Icon className="w-5 h-5 shrink-0" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      {/* Rodapé opcional do menu lateral */}
      <div className="p-4 border-t border-gray-100">
        <div className="bg-gray-50 p-3 rounded-xl text-xs text-gray-500 text-center">
          Sistema ConectaVida v1.0
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;