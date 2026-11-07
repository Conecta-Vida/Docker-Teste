package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.InstituicaoSaude;
import br.edu.ifsp.conectaavida.repository.InstituicaoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: UnidadeSaudeController
 * Rota Base: /api/unidade-saude
 */
@RestController
@RequestMapping("/api/unidade-saude")
public class UnidadeSaudeController {

    @Autowired private InstituicaoSaudeRepository repository;

    @GetMapping
    public InstituicaoSaude obter() {
        return repository.findTopByTipoInstituicaoOrderByIdAsc("UNIDADE").orElse(new InstituicaoSaude());
    }

    @PostMapping
    public InstituicaoSaude salvar(@RequestBody InstituicaoSaude dados) {
        dados.setTipoInstituicao("UNIDADE");
        // Verifica se a unidade central já existe na base de dados para fazer uma substituição (overwrite) mantendo o mesmo ID
        repository.findTopByTipoInstituicaoOrderByIdAsc("UNIDADE").ifPresent(u -> dados.setId(u.getId()));
        return repository.save(dados);
    }
}