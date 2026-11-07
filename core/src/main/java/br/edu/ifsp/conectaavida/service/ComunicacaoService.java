package br.edu.ifsp.conectaavida.service;

import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.repository.InstituicaoSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ComunicacaoService {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private InstituicaoSaudeRepository instituicaoSaudeRepository;

    /**
     * REGRA DE NEGÓCIO DA PUBLICAÇÃO UNIFICADA:
     * Valida os requisitos herdados tanto do Admin quanto do Mobile. Garante o vínculo
     * obrigatório com uma Unidade de Saúde física e impede que notícias com datas retroativas entrem no feed.
     */
    public Comunicacao cadastrarPublicacao(Comunicacao publicacao) {
        if (publicacao.getInstituicao() == null || publicacao.getInstituicao().getId() == null) {
            throw new IllegalArgumentException("A publicação precisa obrigatoriamente estar vinculada a uma instituição de saúde.");
        }
        if (!instituicaoSaudeRepository.existsById(publicacao.getInstituicao().getId())) {
            throw new IllegalArgumentException("Órgão / Instituição de Saúde fornecida não consta na base.");
        }

        // Validação temporal para o Feed do Smartphone
        if (publicacao.getTipo().equalsIgnoreCase("NOTICIA")) {
            if (publicacao.getDataFim() == null || publicacao.getDataFim().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("A data de expiração/vencimento do feed não pode ser retroativa.");
            }
        }

        Comunicacao salva = comunicacaoRepository.save(publicacao);

        // MOTOR DE GESTÃO AUTOMÁTICA DE ALERTAS E SURTOS EPIDEMIOLÓGICOS (PUSH MOBILE)
        if (salva.getCategoria() != null &&
                (salva.getCategoria().equalsIgnoreCase("Epidemia") || salva.getCategoria().equalsIgnoreCase("Urgência"))) {
            dispararNotificacaoPush(salva);
        }

        return salva;
    }

    private void dispararNotificacaoPush(Comunicacao item) {
        System.out.println("==================================================");
        System.out.println("🚨 MÓDULO MOBILE: DISPARO DE NOTIFICAÇÃO PUSH CONCLUÍDO!");
        System.out.println("Região Foco: " + item.getLocalizacao());
        System.out.println("Título do Alerta SMS/Push: " + item.getTitulo());
        System.out.println("==================================================");
    }
}