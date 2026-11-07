package br.edu.ifsp.conectaavida.core.service;

import br.edu.ifsp.conectaavida.core.dto.DashboardStatsDTO;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    // Sem cache: o dashboard sempre reflete o banco na hora
    public DashboardStatsDTO getStats() {
        long alertas = comunicacaoRepository.countByTipoIgnoreCase("Alerta");
        long alertasAtivos = comunicacaoRepository.countByTipoIgnoreCaseAndStatusIgnoreCase("Alerta", "Ativo");
        long campanhas = comunicacaoRepository.countByTipoIgnoreCase("Campanha");
        long noticias = comunicacaoRepository.countByTipoIgnoreCase("Noticia");

        return new DashboardStatsDTO(alertas, alertasAtivos, campanhas, noticias);
    }
}