package br.edu.ifsp.conectaavida.core.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * SERVIÇO DE SEGURANÇA: Criptografia de Senhas (Requisito CR8)
 *
 * Explicação para a Equipe:
 * Nunca devemos salvar senhas em texto puro (ex: "123456") no banco de dados.
 * Esta classe usa o algoritmo SHA-256 nativo do Java para embaralhar a senha.
 * Se o banco vazar, os hackers só verão um código ilegível de 64 caracteres.
 */
@Service
public class PasswordEncryptionService {

    // Lê o "Salt" do arquivo application.properties. Se não achar, usa um valor padrão.
    // O Salt é uma palavra secreta misturada na senha antes de criptografar, blindando o sistema contra ataques de "Rainbow Tables".
    @Value("${app.security.salt:ConectaVida_SecretSalt_2026_IFSP}")
    private String salt;

    /**
     * Pega a senha digitada pelo usuário, mistura com o nosso Salt secreto e gera o Hash seguro.
     * Usado no cadastro e no momento de verificar o Login.
     */
    public String criptografarSenha(String senhaPura) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Mistura a senha pura com o Salt antes de jogar no "liquidificador" criptográfico
            String senhaComSalt = senhaPura + salt;

            byte[] hash = digest.digest(senhaComSalt.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            // Converte o resultado de bytes para uma String legível em Hexadecimal
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro de segurança ao processar credencial.", e);
        }
    }

    /**
     * MANTIDO PARA RETROCOMPATIBILIDADE:
     * Criptografa a senha sem o Salt. Utilizado apenas para permitir que usuários
     * antigos (cadastrados antes da atualização de segurança) consigam logar.
     */
    public String criptografarSenhaLegada(String senhaPura) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senhaPura.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar credencial legada.", e);
        }
    }
}