package com.financeiro.backend.features.reports.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName;
import org.springframework.stereotype.Service;

import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;

@Service
public class StatementExportService {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] csv(StatementReport report) {
        // UTF-8 BOM, semicolon delimiter and decimal comma for Brazilian spreadsheet programs.
        var out = new StringBuilder("\uFEFF");
        row(out, "FinShare - Extrato", report.walletName());
        row(out, "Moeda", report.currency(), "Início", report.startDate().toString(), "Fim", report.endDate().toString());
        row(out, "Receitas pagas", number(report.income()), "Despesas pagas", number(report.expense()));
        row(out, "Transferências recebidas", number(report.transferIn()), "Transferências enviadas", number(report.transferOut()));
        row(out, "Variação realizada", number(report.netChange()), "Pendentes", "" + report.pendingCount(), "Cancelados", "" + report.canceledCount());
        row(out, "Critério", "Somente PAID compõe os totais. Sem saldo inicial. Datas sem fuso, conforme cadastro.");
        row(out, "Data", "Título", "Categoria", "Movimento", "Situação", "Valor", "Impacto realizado");
        for (var entry : report.entries()) {
            row(out, TIME.format(entry.date()), entry.title(), entry.category(), movement(entry.movement()),
                    status(entry.status()), number(entry.amount()), number(entry.impact()));
        }
        return out.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void row(StringBuilder out, String... cells) {
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) out.append(';');
            String value = cells[i] == null ? "" : cells[i];
            // Quoting alone does not prevent spreadsheet formula injection.
            String leading = value.stripLeading();
            if (!leading.isEmpty() && "=+-@".indexOf(leading.charAt(0)) >= 0 && !value.matches("-?\\d+(,\\d+)?")) {
                value = "'" + value;
            }
            out.append('"').append(value.replace("\"", "\"\"")).append('"');
        }
        out.append("\r\n");
    }

    private static String number(BigDecimal amount) { return amount.toPlainString().replace('.', ','); }

    public static String movement(String value) {
        return switch (value) {
            case "INCOME" -> "Receita";
            case "EXPENSE" -> "Despesa";
            case "TRANSFER_IN" -> "Transferência recebida";
            case "TRANSFER_OUT" -> "Transferência enviada";
            default -> value;
        };
    }

    private static String status(TransactionStatus value) {
        return switch (value) {
            case PAID -> "Pago";
            case PENDING -> "Pendente";
            case CANCELED -> "Cancelado";
        };
    }

    public byte[] pdf(StatementReport report) {
        try (var doc = new PDDocument(); var output = new ByteArrayOutputStream()) {
            doc.getDocumentInformation().setTitle("FinShare - Extrato financeiro");
            try (var table = new PdfTable(doc, report)) {
                for (var entry : report.entries()) {
                    table.addRow(new String[]{DATE.format(entry.date()), movement(entry.movement()),
                            entry.title(), entry.category(), status(entry.status()), number(entry.amount()), number(entry.impact())});
                }
                if (report.entries().isEmpty()) table.text("Nenhum lançamento no período selecionado.");
            }
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                try (var stream = new PDPageContentStream(doc, doc.getPage(i), PDPageContentStream.AppendMode.APPEND, true)) {
                    write(stream, new PDType1Font(FontName.HELVETICA), 8, 36, 24,
                            "FinShare | " + report.entries().size() + " lançamentos | Página " + (i + 1) + " de " + doc.getNumberOfPages());
                }
            }
            doc.save(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível gerar o PDF.", ex);
        }
    }

    private static void write(PDPageContentStream stream, PDFont font, float size, float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
    }

    private static class PdfTable implements AutoCloseable {
        private static final float[] WIDTHS = {66, 115, 180, 102, 65, 105, 105};
        private static final String[] HEADERS = {"Data", "Movimento", "Título", "Categoria", "Situação", "Valor", "Impacto"};
        private final PDDocument doc;
        private final StatementReport report;
        private final PDFont font = new PDType1Font(FontName.HELVETICA);
        private final PDFont bold = new PDType1Font(FontName.HELVETICA_BOLD);
        private PDPageContentStream stream;
        private float y;

        PdfTable(PDDocument doc, StatementReport report) throws IOException {
            this.doc = doc;
            this.report = report;
            newPage(true);
        }

        private void newPage(boolean first) throws IOException {
            if (stream != null) stream.close();
            var page = new PDPage(new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth()));
            doc.addPage(page);
            stream = new PDPageContentStream(doc, page);
            stream.setNonStrokingColor(new Color(23, 49, 85));
            write(stream, bold, 19, 36, 551, "FinShare | Extrato financeiro");
            stream.setNonStrokingColor(Color.DARK_GRAY);
            String name = clean(report.walletName());
            while (font.getStringWidth(name) / 1000 * 10 > 650) name = name.substring(0, name.length() - 1);
            write(stream, font, 10, 36, 528, name + " | " + clean(report.currency()));
            write(stream, font, 9, 36, 510, DATE.format(report.startDate()) + " a " + DATE.format(report.endDate())
                    + " | Gerado em " + TIME.format(report.generatedAt()));
            y = 487;
            if (first) {
                text("Receitas pagas: " + number(report.income()) + "   |   Despesas pagas: " + number(report.expense()));
                text("Transferências recebidas: " + number(report.transferIn()) + "   |   Enviadas: " + number(report.transferOut()));
                text("Variação realizada: " + number(report.netChange()) + " " + clean(report.currency())
                        + "   |   Pendentes: " + report.pendingCount() + "   |   Cancelados: " + report.canceledCount());
                text("Somente pagos compõem os totais. Variação do período, sem saldo inicial. Datas conforme cadastro (sem fuso).");
                y -= 8;
            }
            stream.setNonStrokingColor(new Color(234, 240, 248));
            stream.addRect(36, y - 6, 738, 22);
            stream.fill();
            stream.setNonStrokingColor(new Color(23, 49, 85));
            float x = 40;
            for (int i = 0; i < HEADERS.length; i++) {
                write(stream, bold, 9, x, y, HEADERS[i]);
                x += WIDTHS[i];
            }
            y -= 24;
            stream.setNonStrokingColor(Color.DARK_GRAY);
        }

        void text(String value) throws IOException {
            for (String line : wrap(value, 738)) {
                write(stream, font, 8.5f, 36, y, line);
                y -= 15;
            }
        }

        void addRow(String[] values) throws IOException {
            var cells = new ArrayList<List<String>>();
            int lines = 1;
            for (int i = 0; i < values.length; i++) {
                var wrapped = wrap(values[i], WIDTHS[i] - 10);
                cells.add(wrapped);
                lines = Math.max(lines, wrapped.size());
            }
            float height = lines * 12 + 12;
            if (y - height < 45) newPage(false);
            float x = 40;
            for (int i = 0; i < cells.size(); i++) {
                for (int j = 0; j < cells.get(i).size(); j++) {
                    String line = cells.get(i).get(j);
                    float offset = i >= 5 ? WIDTHS[i] - 10 - font.getStringWidth(line) / 1000 * 8.5f : 0;
                    write(stream, font, 8.5f, x + offset, y - j * 12, line);
                }
                x += WIDTHS[i];
            }
            y -= height;
            stream.setStrokingColor(new Color(225, 230, 237));
            stream.moveTo(36, y + 7);
            stream.lineTo(774, y + 7);
            stream.stroke();
        }

        private List<String> wrap(String value, float width) throws IOException {
            String cleaned = clean(value);
            var lines = new ArrayList<String>();
            var line = new StringBuilder();
            for (char c : cleaned.toCharArray()) {
                if (font.getStringWidth(line.toString() + c) / 1000 * 8.5f > width && !line.isEmpty()) {
                    int space = line.lastIndexOf(" ");
                    if (space > 0) {
                        lines.add(line.substring(0, space));
                        String remainder = line.substring(space + 1);
                        line.setLength(0);
                        line.append(remainder);
                    } else {
                        lines.add(line.toString());
                        line.setLength(0);
                    }
                }
                line.append(c);
            }
            lines.add(line.toString());
            return lines;
        }

        private String clean(String value) {
            if (value == null) return "";
            var out = new StringBuilder();
            value.codePoints().forEach(cp -> {
                String character = Character.isISOControl(cp) ? " " : new String(Character.toChars(cp));
                try { font.encode(character); out.append(character); }
                catch (IOException | IllegalArgumentException ex) { out.append('?'); }
            });
            return out.toString();
        }

        @Override public void close() throws IOException { if (stream != null) stream.close(); }
    }
}
