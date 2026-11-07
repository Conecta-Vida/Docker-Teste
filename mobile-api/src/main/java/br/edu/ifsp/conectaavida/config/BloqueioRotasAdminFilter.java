package br.edu.ifsp.conectaavida.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * As rotas compartilhadas vêm do módulo :core e, por isso, também existiriam
 * na mobile-api. Este filtro bloqueia na API pública do app as operações que
 * são exclusivas do painel administrativo (que roda na admin-api).
 *
 * Continuam liberadas para o app: login/cadastro, leitura de comunicações,
 * compartilhamento, perfil do próprio usuário (GET por e-mail, PUT e DELETE
 * por id) e inscrição em campanhas.
 */
@Component
public class BloqueioRotasAdminFilter extends OncePerRequestFilter {

    private static final Pattern CAMPANHA_NOTIFICAR_FIM =
            Pattern.compile("^/api/campanhas/\\d+/notificar-fim$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (exclusivaDoAdmin(request.getMethod(), request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"mensagem\":\"Operação disponível apenas na API administrativa.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    static boolean exclusivaDoAdmin(String metodo, String uri) {
        String caminho = uri.endsWith("/") && uri.length() > 1 ? uri.substring(0, uri.length() - 1) : uri;

        // Gestão de comunicações (criar, editar, excluir). Compartilhar continua liberado.
        if (caminho.startsWith("/api/comunicacoes")
                && !"GET".equals(metodo)
                && !caminho.equals("/api/comunicacoes/compartilhamentos")) {
            return true;
        }

        // Gestão de usuários: listar todos, criar pelo painel, importar/exportar CSV
        if (caminho.equals("/api/usuarios") && ("GET".equals(metodo) || "POST".equals(metodo))) {
            return true;
        }
        if (caminho.startsWith("/api/usuarios/importar-csv") || caminho.startsWith("/api/usuarios/exportar-csv")) {
            return true;
        }

        // Disparo de notificação de fim de campanha
        return "POST".equals(metodo) && CAMPANHA_NOTIFICAR_FIM.matcher(caminho).matches();
    }
}
