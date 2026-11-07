package br.edu.ifsp.conectaavida.mensageria;

/**
 * Mensagem trafegada no RabbitMQ quando o painel publica uma comunicação
 * que precisa virar notificação push (ex.: um ALERTA epidemiológico).
 */
public record EventoComunicacao(Long id, String tipo, String titulo, String descricao) {
}
