package br.edu.ifsp.conectaavida.mobile.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * SERVIÇO DE COMUNICAÇÕES (Específico do Mobile)
 *
 * Explicação para a Equipe:
 * O aplicativo celular não cadastra campanhas, mas precisa validar se uma
 * campanha está ativa ou se já passou da data de expiração antes de mostrá-la
 * na tela do cidadão. Essa validação temporal fica isolada aqui.
 */
@Service
public class ComunicacaoService {

    /**
     * Validação de segurança: impede que o App mostre mutirões de saúde
     * que já aconteceram no passado.
     */
    public boolean isCampanhaAtivaEValida(LocalDateTime dataFim) {
        if (dataFim == null) {
            return true; // Se não tem data de fim, é uma campanha contínua
        }
        return LocalDateTime.now().isBefore(dataFim);
    }
}