package br.edu.ifsp.conectaavida.service;

import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.domain.Comunicacao;
import br.edu.ifsp.conectaavida.repository.UsuarioRepository;
import br.edu.ifsp.conectaavida.repository.ComunicacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

@Service // Indica ao Spring que esta classe contém lógica de serviço (regras de negócio)
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Value("${app.security.salt:ConectaVida_SecretSalt_2026_IFSP}")
    private String salt;

    /**
     * CRIPTOGRAFIA REQUISITO CR8 (SHA-256 NATIVO + SALT INTEGRADO):
     * Explicação para o grupo: Como nosso build.gradle não possui dependências pesadas do Spring Security,
     * criamos um hash criptográfico unilateral SHA-256 usando pacotes nativos do próprio Java.
     * Isso converte senhas limpas (Ex: "123456") em hashes ilegíveis de 64 caracteres salvos no Supabase.
     * 🟢 EVOLUÇÃO: Agora com Salt dinâmico para evitar ataques de tabelas de arco-íris (Rainbow Tables).
     */
    public String criptografarSenha(String senhaPura) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Combina a senha pura digitada com o sal secreto antes de gerar o hash
            String senhaComSalt = senhaPura + salt;

            byte[] hash = digest.digest(senhaComSalt.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
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
     * Compatibilidade com registros antigos que foram salvos sem salt.
     * Ajuda a manter login funcionando após a evolução do esquema de segurança.
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

    /**
     * CADASTRO UNIFICADO WITH VALIDAÇÃO:
     * Intercepta a tentativa de cadastro do painel ou do app mobile, valida se o e-mail é único,
     * passa a senha pelo liquidificador criptográfico e define o nível inicial se vazio.
     */
    public Usuario cadastrarUsuario(Usuario novoUsuario) {
        if (usuarioRepository.existsByEmail(novoUsuario.getEmail())) {
            throw new IllegalArgumentException("Este e-mail já está em uso por outro cidadão.");
        }

        // Aplica criptografia SHA-256 com proteção extra de Salt
        novoUsuario.setSenha(criptografarSenha(novoUsuario.getSenha()));

        // Se a chamada veio do app mobile, assume-se por padrão que é um usuário do ecossistema comum
        if (novoUsuario.getPermissao() == null || novoUsuario.getPermissao().isEmpty()) {
            novoUsuario.setPermissao("Usuário Comum");
        }

        return usuarioRepository.save(novoUsuario);
    }

    /**
     * ATIVAÇÃO DO CR7 (MUITOS-PARA-MUITOS VIA LOGICA):
     * Vincula fisicamente um cidadão (Mobile) a um mutirão/campanha de doação de sangue ou vacina (Web).
     * Carrega as entidades do banco, insere o ID na lista interna e salva. O Hibernate gera o SQL INSERT
     * na tabela "usuarios_campanhas" nos bastidores de forma transparente.
     */
    public void vincularCidadaoACampanha(Long usuarioId, Long campanhaId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Cidadão não localizado no banco."));

        Comunicacao campanha = comunicacaoRepository.findById(campanhaId)
                .orElseThrow(() -> new IllegalArgumentException("Campanha de saúde não localizada no banco."));

        // Insere na lista relacional gerenciada pela tabela de junção
        usuario.getCampanhasInscritas().add(campanha);
        usuarioRepository.save(usuario); // Faz o commit no banco do Supabase
    }
}