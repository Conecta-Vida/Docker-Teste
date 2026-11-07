package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.domain.InstituicaoSaude;
import br.edu.ifsp.conectaavida.repository.InstituicaoSaudeRepository;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoSaudeController {

    @Autowired
    private InstituicaoSaudeRepository repository;

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @GetMapping
    public ResponseEntity<List<InstituicaoSaude>> listarTodas() {
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping
    public ResponseEntity<InstituicaoSaude> cadastrar(@RequestBody InstituicaoSaude instituicao) {
        InstituicaoSaude salva = repository.save(instituicao);
        return ResponseEntity.ok(salva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody InstituicaoSaude dadosNovos) {
        return repository.findById(id)
                .map(inst -> {
                    inst.setNome(dadosNovos.getNome());
                    inst.setTipoInstituicao(dadosNovos.getTipoInstituicao());
                    inst.setTelefone(dadosNovos.getTelefone());
                    inst.setEmail(dadosNovos.getEmail());
                    inst.setEndereco(dadosNovos.getEndereco());
                    inst.setLinksite(dadosNovos.getLinksite());
                    inst.setHorarioSegSex(dadosNovos.getHorarioSegSex());
                    inst.setHorarioSabado(dadosNovos.getHorarioSabado());
                    inst.setHorarioDomingo(dadosNovos.getHorarioDomingo());
                    InstituicaoSaude atualizada = repository.save(inst);
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarInstituicao(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // ADIÇÃO DEFENSIVA: Desvincula as comunicações desta instituição (seta a FK para NULL)
        // Isso impede o erro de integridade e mantém a notícia visível no mobile, apenas sem instituição atrelada.
        comunicacaoRepository.findAll().stream()
                .filter(c -> c.getInstituicao() != null && c.getInstituicao().getId().equals(id))
                .forEach(c -> {
                    c.setInstituicao(null);
                    comunicacaoRepository.save(c);
                });

        
        repository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}