package br.edu.ifsp.conectaavida.core.dto;

import java.io.Serializable;

// Implementa Serializable para que o Redis consiga guardar o objeto em memória com segurança
public class DashboardStatsDTO implements Serializable {
    private long totalAlertas;
    private long alertasAtivos;
    private long totalCampanhas;
    private long totalNoticias;

    public DashboardStatsDTO(long totalAlertas, long alertasAtivos, long totalCampanhas, long totalNoticias) {
        this.totalAlertas = totalAlertas;
        this.alertasAtivos = alertasAtivos;
        this.totalCampanhas = totalCampanhas;
        this.totalNoticias = totalNoticias;
    }

    // Getters
    public long getTotalAlertas() { return totalAlertas; }
    public long getAlertasAtivos() { return alertasAtivos; }
    public long getTotalCampanhas() { return totalCampanhas; }
    public long getTotalNoticias() { return totalNoticias; }
}