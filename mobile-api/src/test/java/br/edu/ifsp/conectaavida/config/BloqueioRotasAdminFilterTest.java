package br.edu.ifsp.conectaavida.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BloqueioRotasAdminFilterTest {

    @ParameterizedTest(name = "{0} {1} -> bloqueada={2}")
    @CsvSource({
            // Rotas usadas pelo app (liberadas)
            "POST,   /api/auth/login,                     false",
            "POST,   /api/auth/register,                  false",
            "GET,    /api/comunicacoes,                   false",
            "POST,   /api/comunicacoes/compartilhamentos, false",
            "GET,    /api/usuarios/email/a@b.com,         false",
            "PUT,    /api/usuarios/7,                     false",
            "DELETE, /api/usuarios/7,                     false",
            "POST,   /api/campanhas/3/inscrever,          false",
            "DELETE, /api/campanhas/3/desinscrever,       false",
            "GET,    /api/campanhas/3/inscritos,          false",
            "POST,   /api/push/registrar-token,           false",
            // Operações do painel (bloqueadas na mobile-api)
            "POST,   /api/comunicacoes,                   true",
            "POST,   /api/comunicacoes/,                  true",
            "PUT,    /api/comunicacoes/2,                 true",
            "DELETE, /api/comunicacoes/2,                 true",
            "GET,    /api/usuarios,                       true",
            "POST,   /api/usuarios,                       true",
            "POST,   /api/usuarios/importar-csv,          true",
            "GET,    /api/usuarios/exportar-csv,          true",
            "POST,   /api/campanhas/3/notificar-fim,      true",
    })
    void classificaRotas(String metodo, String uri, boolean bloqueada) {
        assertEquals(bloqueada, BloqueioRotasAdminFilter.exclusivaDoAdmin(metodo, uri));
    }
}
