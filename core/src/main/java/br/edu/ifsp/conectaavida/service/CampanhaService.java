package br.edu.ifsp.conectaavida.service;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * CLASSE: CampanhaService
 * Objetivo: Concentrar as regras de negócio associadas às Campanhas de Saúde.
 * * Explicação para a Equipa: Como unificámos Alertas, Notícias e Campanhas na mesma tabela,
 * este serviço isola as regras das Campanhas antes de interagir com o banco de dados.
 */
@Service // Indica ao Spring que esta classe contém lógica de serviço (regras de negócio)
public class CampanhaService {

    // O repositório é declarado como final para garantir a imutabilidade da dependência
    private final ComunicacaoRepository repository;

    // Injeção via construtor: o padrão mais recomendado pelo Spring Boot
    public CampanhaService(ComunicacaoRepository repository) {
        this.repository = repository;
    }

    /**
     * Retorna exclusivamente as Campanhas cadastradas no sistema.
     */
    public List<Comunicacao> listarTodas() {
        return repository.findByTipo("CAMPANHA");
    }

    /**
     * Valida e insere uma nova campanha no Supabase.
     * * Detalhe Importante: Mesmo que o formulário do Frontend se esqueça de enviar
     * a classificação, o método força o tipo "CAMPANHA" por segurança antes do commit.
     */
    public Comunicacao salvar(Comunicacao campanha) {
        campanha.setTipo("CAMPANHA");
        return repository.save(campanha); // Persiste os dados na nuvem
    }

    /**
     * Remove uma campanha do sistema através do seu ID identificador único.
     */
    public void deletar(Long id) {
        repository.deleteById(id);
    }
}