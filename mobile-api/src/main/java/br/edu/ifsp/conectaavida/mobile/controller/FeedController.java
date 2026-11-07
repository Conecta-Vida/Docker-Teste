package br.edu.ifsp.conectaavida.mobile.controller;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLLER DO FEED DO APLICATIVO
 *
 * Explicação para a Equipe:
 * Diferente do Admin (que vê tudo e edita tudo), este controller é apenas
 * de LEITURA. Ele devolve a lista de alertas e notícias da cidade para o Flutter
 * desenhar de forma bonita na tela do cidadão.
 */
@RestController
@RequestMapping("/api/mobile/feed")
public class FeedController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    // A rota que o Flutter vai chamar para carregar a aba de "Alertas"
    @GetMapping("/alertas")
    public ResponseEntity<List<Comunicacao>> buscarAlertasAtivos() {
        // Traz apenas alertas que ainda não foram marcados como lidos/inativos
        List<Comunicacao> alertas = comunicacaoRepository.findByTipoAndLidoFalseOrderByDataPostadaDesc("Alerta");
        return ResponseEntity.ok(alertas);
    }

    // A rota que o Flutter vai chamar para carregar a aba de "Notícias"
    @GetMapping("/noticias")
    public ResponseEntity<List<Comunicacao>> buscarNoticiasRecentes() {
        // Traz as notícias ordenadas das mais novas para as mais antigas
        List<Comunicacao> noticias = comunicacaoRepository.findByTipoOrderByDataInicioDesc("Noticia");
        return ResponseEntity.ok(noticias);
    }
}