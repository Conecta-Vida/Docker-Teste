package br.edu.ifsp.conectaavida.admin.service;

import br.edu.ifsp.conectaavida.core.domain.LogAtividade;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SERVIÇO DE GERAÇÃO DE RELATÓRIOS (PDF e CSV)
 *
 * Explicação para a Equipe:
 * Este serviço usa a biblioteca iText7. Ele recebe listas de dados do banco e "desenha"
 * um arquivo PDF na memória RAM, devolvendo os bytes para o usuário fazer o download.
 */
@Service
public class RelatorioService {

    /**
     * Gera um PDF com a trilha de auditoria (quem fez o que no sistema).
     */
    public byte[] gerarRelatorioAuditoriaPdf(List<LogAtividade> logs) {

        // Cria um espaço na memória para segurar o arquivo PDF enquanto o desenhamos
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título do PDF
            document.add(new Paragraph("Relatório de Auditoria - Conecta Vida")
                    .setBold().setFontSize(18));
            document.add(new Paragraph("Ações recentes dos administradores no sistema.\n\n"));

            // Cria uma tabela com 3 colunas
            float[] columnWidths = {150f, 200f, 150f};
            Table table = new Table(columnWidths);
            table.addHeaderCell(new Paragraph("Data/Hora").setBold());
            table.addHeaderCell(new Paragraph("Administrador").setBold());
            table.addHeaderCell(new Paragraph("Ação Realizada").setBold());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            // Preenche a tabela iterando sobre os logs que vieram do banco de dados
            for (LogAtividade log : logs) {
                table.addCell(log.getDataHora().format(formatter));
                table.addCell(log.getUsuario().getNome()); // Puxa o nome de quem fez a ação
                table.addCell(log.getAcao());
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o relatório PDF de auditoria", e);
        }

        // Devolve o arquivo pronto em formato de bytes
        return out.toByteArray();
    }
}