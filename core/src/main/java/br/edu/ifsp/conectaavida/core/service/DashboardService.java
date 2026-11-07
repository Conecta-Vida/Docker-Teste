package br.edu.ifsp.conectaavida.core.service;

import br.edu.ifsp.conectaavida.core.dto.DashboardStatsDTO;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Cacheable("dashboardStats") // 🚀 O Redis guarda o DTO puro em cache com sucesso
    public DashboardStatsDTO getStats() {
        long alertas = comunicacaoRepository.countByTipo("Alerta");
        long alertasAtivos = comunicacaoRepository.countByTipoAndStatus("Alerta", "Ativo");
        long campanhas = comunicacaoRepository.countByTipo("Campanha");
        long noticias = comunicacaoRepository.countByTipo("Noticia");

        return new DashboardStatsDTO(alertas, alertasAtivos, campanhas, noticias);
    }
}