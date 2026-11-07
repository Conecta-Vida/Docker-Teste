package br.edu.ifsp.conectaavida.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO: ChartDataDTO
 * Objetivo: Agrupar dados analíticos mensais para alimentar o gráfico de crescimento do Frontend.
 * * Explicação para a Equipa:
 * Este objeto não representa uma tabela física no Supabase. Ele existe apenas na memória
 * da aplicação para agrupar o resultado de contagens que o DashboardController processa.
 */
@Data // Lombok: Gera automaticamente Getters, Setters, toString, equals e hashCode em tempo de compilação
@AllArgsConstructor // Lombok: Cria o construtor preenchido exigindo todos os atributos (mes, quantidade)
public class ChartDataDTO {

    // Nome resumido ou completo do mês de referência (Ex: "Jan", "Fev", "Mar")
    private String mes;

    // Volume absoluto de utilizadores registados até este período
    // Usamos o tipo 'long' para suportar grandes volumes de contagem de dados (BigInt)
    private long quantidade;
}