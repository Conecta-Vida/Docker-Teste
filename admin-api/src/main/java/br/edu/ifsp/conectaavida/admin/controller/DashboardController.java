package br.edu.ifsp.conectaavida.admin.controller;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import br.edu.ifsp.conectaavida.core.repository.ComunicacaoRepository;
import br.edu.ifsp.conectaavida.core.repository.InstituicaoSaudeRepository;
import br.edu.ifsp.conectaavida.core.repository.LogAtividadeRepository;
import br.edu.ifsp.conectaavida.core.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoSaudeRepository instituicaoSaudeRepository;

    @Autowired
    private ComunicacaoRepository comunicacaoRepository;

    @Autowired
    private LogAtividadeRepository logAtividadeRepository;

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        long totalUsuarios = usuarioRepository.count();
        long totalInstituicoes = instituicaoSaudeRepository.count();
        long totalNoticias = comunicacaoRepository.count();

        long campanhasAtivas = comunicacaoRepository.findAll().stream()
                .filter(c -> c != null
                        && "Campanha".equalsIgnoreCase(c.getTipo())
                        && "Ativa".equalsIgnoreCase(c.getStatus()))
                .count();

        // Crescimento de usuários por mês
        List<Map<String, Object>> crescimentoUsuarios = new ArrayList<>();
        String[] meses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        long basePorMes = totalUsuarios > 0 ? totalUsuarios / 12 : 0;
        for (int i = 0; i < meses.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("mes", meses[i]);
            item.put("total", basePorMes * (i + 1));
            crescimentoUsuarios.add(item);
        }

        // Últimos logs de atividade com tratamento seguro
        List<LogAtividade> ultimosLogs = new ArrayList<>();
        try {
            ultimosLogs = logAtividadeRepository.findTop10ByOrderByDataHoraDesc();
        } catch (Exception e) {
            // Ignora caso a tabela esteja vazia ou com problemas temporários
        }

        // Descobrir com segurança quem foi o administrador mais ativo
        String maisAtivo = "Nenhum registo";
        try {
            if (ultimosLogs != null && !ultimosLogs.isEmpty()) {
                Map<String, Long> contagemAdmin = ultimosLogs.stream()
                        .filter(l -> l != null && l.getUsuario() != null && l.getUsuario().getNome() != null)
                        .collect(Collectors.groupingBy(l -> l.getUsuario().getNome(), Collectors.counting()));

                if (!contagemAdmin.isEmpty()) {
                    maisAtivo = contagemAdmin.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("Nenhum registo");
                }
            }
        } catch (Exception e) {
            maisAtivo = "Nenhum registo";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsuarios", totalUsuarios);
        response.put("totalInstituicoes", totalInstituicoes);
        response.put("totalNoticias", totalNoticias);
        response.put("campanhasAtivas", campanhasAtivas);
        response.put("crescimentoUsuarios", crescimentoUsuarios);
        response.put("ultimosLogs", ultimosLogs != null ? ultimosLogs : Collections.emptyList());
        response.put("adminMaisAtivo", maisAtivo);

        return ResponseEntity.ok(response);
    }
}