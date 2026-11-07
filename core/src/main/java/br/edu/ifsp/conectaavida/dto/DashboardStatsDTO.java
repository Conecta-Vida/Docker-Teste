package br.edu.ifsp.conectaavida.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO: DashboardStatsDTO
 * Objetivo: Consolidar as métricas volumétricas de quatro setores distintos da plataforma numa única resposta HTTP.
 * * Explicação para a Equipa:
 * Em vez de fazer o React disparar 4 requisições separadas para o servidor (o que deixaria o sistema lento),
 * o controlador junta as 4 contagens neste único DTO e envia um único objeto JSON otimizado.
 */
@Data // Lombok: Automatiza a infraestrutura de acessores (Getters e Setters) da classe
@AllArgsConstructor // Lombok: Cria o construtor ideal para fazermos o 'new DashboardStatsDTO(total, alertas, campanhas, noticias)'
public class DashboardStatsDTO {

    // Total de cidadãos cadastrados na tabela 'usuarios'
    private long totalUsuarios;

    // Alertas críticos do tipo 'ALERTA' que ainda não foram marcados como lidos
    private long alertasAtivos;

    // Ações comunitárias de saúde do tipo 'CAMPANHA' com status textual igual a 'Ativa'
    private long campanhasAtivas;

    // Quantidade total de informativos gerais publicados sob a classificação 'NOTICIA'
    private long noticiasPublicadas;
}