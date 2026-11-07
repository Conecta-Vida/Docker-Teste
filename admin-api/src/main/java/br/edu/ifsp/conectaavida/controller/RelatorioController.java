package br.edu.ifsp.conectaavida.controller;

import br.edu.ifsp.conectaavida.service.RelatorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * CONTROLLER: RelatorioController
 * Rota Base: /api/relatorios
 * Objetivo: Disponibilizar os streams binários para download de arquivos no cliente.
 */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    /**
     * GET /api/relatorios/usuarios
     * Produz o download do arquivo binário PDF.
     */
    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> baixarRelatorioUsuarios() {
        byte[] pdf = relatorioService.gerarRelatorioUsuarios();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * GET /api/relatorios/usuarios/csv
     * Produz o download do arquivo planilhado CSV.
     */
    @GetMapping("/usuarios/csv")
    public ResponseEntity<byte[]> baixarCsvUsuarios() {
        byte[] csv = relatorioService.gerarCsvUsuarios();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_usuarios.csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}