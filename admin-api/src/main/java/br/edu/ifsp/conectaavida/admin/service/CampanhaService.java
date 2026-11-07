package br.edu.ifsp.conectaavida.admin.service;

import br.edu.ifsp.conectaavida.core.domain.Comunicacao;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioCampanhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * SERVIÇO DE CAMPANHAS DE SAÚDE
 *
 * Explicação para a Equipe:
 * Este serviço cuida das regras exclusivas de Mutirões/Campanhas.
 * O @Transactional garante que, se der erro no meio da operação,
 * o banco de dados desfaz tudo (Rollback) para não deixar dados pela metade.
 */
@Service
public class CampanhaService {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private UsuarioCampanhaRepository usuarioCampanhaRepository;

    /**
     * Encerra uma campanha (ex: Mutirão de Vacinação acabou) e limpa as inscrições.
     */
    @Transactional
    public void encerrarCampanha(Long campanhaId) {
        Comunicacao campanha = comunicacaoRepository.findById(campanhaId)
                .orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada."));

        campanha.setStatus("Encerrada");
        comunicacaoRepository.save(campanha);

        // Apaga as inscrições para limpar espaço no banco, já que o evento acabou
        usuarioCampanhaRepository.deleteByComunicacaoId(campanhaId);
    }
}