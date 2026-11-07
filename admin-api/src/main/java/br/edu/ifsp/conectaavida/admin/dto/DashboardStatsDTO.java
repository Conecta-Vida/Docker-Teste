package br.edu.ifsp.conectaavida.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO DOS CARDS DO DASHBOARD
 * Carrega apenas os números totais que ficam nos "quadradinhos" do topo do painel.
 */
@Getter @Setter
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalAlertas;
    private long alertasAtivos;
    private long totalCampanhas;
    private long totalNoticias;
}