package br.edu.ifsp.conectaavida.service;

import br.edu.ifsp.conectaavida.domain.Usuario;
import br.edu.ifsp.conectaavida.repository.UsuarioRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Service
public class RelatorioService {

    private final UsuarioRepository repository;

    public RelatorioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public byte[] gerarRelatorioUsuarios() {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("CONECTA À VIDA - RELATÓRIO GERENCIAL DE USUÁRIOS"));
            document.add(new Paragraph("Listagem oficial de cidadãos homologados no ecossistema de saúde."));
            document.add(new Paragraph(" "));

            float[] columnWidths = {1, 3, 4, 1};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            table.addHeaderCell("ID");
            table.addHeaderCell("Nome");
            table.addHeaderCell("Email");
            table.addHeaderCell("Idade");

            for (Usuario u : repository.findAll()) {
                table.addCell(u.getId().toString());
                table.addCell(u.getNome() != null ? u.getNome() : "-");
                table.addCell(u.getEmail() != null ? u.getEmail() : "-");

                // 🟢 Cálculo dinâmico para o iText PDF baseado no ano atual (2026)
                if (u.getDataNascimento() != null) {
                    table.addCell(String.valueOf(2026 - u.getDataNascimento()));
                } else {
                    table.addCell("-");
                }
            }

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Falha catastrófica ao compilar o documento PDF estruturado.", e);
        }
    }

    public byte[] gerarCsvUsuarios() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Nome;Email;Idade;Sexo;Localizacao;Permissao\n");

        for (Usuario u : repository.findAll()) {
            // 🟢 Cálculo dinâmico para a Planilha Excel/CSV baseado no ano atual (2026)
            String idadeCalculada = "-";
            if (u.getDataNascimento() != null) {
                idadeCalculada = String.valueOf(2026 - u.getDataNascimento());
            }

            csv.append(u.getId()).append(";")
                    .append(u.getNome() != null ? u.getNome() : "-").append(";")
                    .append(u.getEmail() != null ? u.getEmail() : "-").append(";")
                    .append(idadeCalculada).append(";")
                    .append(u.getSexo() != null ? u.getSexo() : "-").append(";")
                    .append(u.getLocalizacao() != null ? u.getLocalizacao() : "-").append(";")
                    .append(u.getPermissao() != null ? u.getPermissao() : "-").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}