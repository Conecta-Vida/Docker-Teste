package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CONTROLLER DE NOTÍCIAS (FEED)
 */
@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @GetMapping
    public ResponseEntity<List<Comunicacao>> listarNoticias() {
        // Usa o nosso método customizado no repositório para buscar apenas Notícias
        List<Comunicacao> noticias = comunicacaoRepository.findByTipo("Noticia");
        return ResponseEntity.ok(noticias);
    }

    @PostMapping
    public ResponseEntity<Comunicacao> postarNoticia(@RequestBody Comunicacao noticia) {
        noticia.setTipo("Noticia");
        noticia.setDataPostada(LocalDateTime.now());
        return ResponseEntity.ok(comunicacaoRepository.save(noticia));
    }
}