import { LayoutDashboard, Users, Megaphone, Bell, Settings, LogOut, ShieldAlert } from "lucide-react";
import { Link, useLocation } from "react-router-dom";
import { VitalAlertLogo } from "./icons/VitalAlertLogo";
import { Sidebar, SidebarContent, SidebarGroup, SidebarGroupContent, SidebarGroupLabel, SidebarHeader, SidebarMenu, SidebarMenuButton, SidebarMenuItem, SidebarFooter } from "./ui/sidebar";

// PARA A EQUIPE: Se precisarem adicionar um novo link no menu, basta colocar aqui neste array!
const items = [
  { title: "Dashboard", url: "/", icon: LayoutDashboard },
  { title: "Usuarios", url: "/usuarios", icon: Users },
  { title: "Campanhas", url: "/campanhas", icon: Megaphone },
  { title: "Alertas", url: "/alertas", icon: Bell },
  { title: "Urgência", url: "/alerta-urgente", icon: ShieldAlert },
  { title: "Configurações", url: "/configuracoes", icon: Settings },
];

export function AppSidebar() {
  const location = useLocation(); // Hook para sabermos em qual página estamos e pintar o menu de azul

  return (
    <Sidebar className="border-r border-slate-200">
      <SidebarHeader className="p-6">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-blue-600 rounded-xl flex items-center justify-center text-white shadow-lg shadow-blue-100">
            <VitalAlertLogo className="w-6 h-6" />
          </div>
          <span className="font-bold text-xl text-slate-900 tracking-tight">Conecta Admin</span>
        </div>
      </SidebarHeader>
      
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="px-6 text-xs font-bold text-slate-400 uppercase tracking-widest">
            Menu Principal
          </SidebarGroupLabel>
          <SidebarGroupContent className="px-3">
            <SidebarMenu>
              {items.map((item) => {
                const isActive = location.pathname === item.url;
                return (
                  <SidebarMenuItem key={item.title}>
                    <SidebarMenuButton asChild isActive={isActive}>
                      <Link 
                        to={item.url} 
                        className={`flex items-center gap-3 px-3 py-2 rounded-lg transition-all ${
                          isActive ? 'bg-blue-50 text-blue-700' : 'text-slate-600 hover:bg-slate-50'
                        }`}
                      >
                        <item.icon className={`w-5 h-5 ${isActive ? 'text-blue-600' : 'text-slate-400'}`} />
                        <span className="font-semibold">{item.title}</span>
                      </Link>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      
      <SidebarFooter className="p-4 border-t border-slate-100">
        <button className="flex items-center gap-3 w-full p-3 text-slate-500 hover:text-red-600 transition-all font-bold text-sm">
          <LogOut className="w-5 h-5" />
          <span>Sair</span>
        </button>
      </SidebarFooter>
    </Sidebar>
  );
}