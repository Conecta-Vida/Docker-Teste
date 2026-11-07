package br.edu.ifsp.conectaavida.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * DTO DOS GRÁFICOS
 * Estrutura exata que as bibliotecas de gráficos do React (como Chart.js ou Recharts)
 * precisam para desenhar as barras na tela.
 */
@Getter @Setter
@AllArgsConstructor
public class ChartDataDTO {
    private List<String> labels; // Ex: ["Janeiro", "Fevereiro", "Março"]
    private List<Long> data;     // Ex: [15, 42, 10] (Quantidade de Alertas)
}