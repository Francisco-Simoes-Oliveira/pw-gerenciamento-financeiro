package com.financeiro.backend.features.reports;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.reports.service.StatementExportService;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;

class StatementExportServiceTest {
    final StatementExportService exports = new StatementExportService();
    static StatementReport fixture(List<StatementReport.Entry> entries) {
        return new StatementReport(UUID.randomUUID(), "Casa - João e Maria", "BRL", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                LocalDateTime.of(2026, 9, 30, 18, 0), new BigDecimal("100.10"), new BigDecimal("20.05"),
                new BigDecimal("30.25"), new BigDecimal("10.00"), new BigDecimal("100.30"), 1, 1, entries);
    }
    static StatementReport.Entry entry(String title) {
        return new StatementReport.Entry(UUID.randomUUID(), LocalDateTime.of(2026, 9, 2, 12, 30), title, "Alimentação",
                "EXPENSE", TransactionStatus.PAID, new BigDecimal("20.05"), new BigDecimal("-20.05"));
    }

    @Test void csvQuotesAccentsDelimitersNewlinesAndNeutralizesFormulas() {
        var report = fixture(List.of(entry("=HYPERLINK(\"evil\")"), entry("Café; pão\nJoão"), entry("\t+SUM(A1:A2)")));
        var csv = new String(exports.csv(report), StandardCharsets.UTF_8);
        assertTrue(csv.startsWith("\uFEFF"));
        assertTrue(csv.contains("\"'=HYPERLINK(\"\"evil\"\")\""));
        assertTrue(csv.contains("\"Café; pão\nJoão\""));
        assertTrue(csv.contains("\"'\t+SUM(A1:A2)\""));
        assertTrue(csv.contains("\"100,30\""));
        assertTrue(csv.contains("\"-20,05\""));
        assertEquals(3, csv.split("02/09/2026 12:30", -1).length - 1);
    }

    @Test void pdfIsReadableMultipageWithEveryRowAndPageNumbers() throws Exception {
        var rows = IntStream.range(0, 80).mapToObj(i -> entry(String.format("Lançamento %03d - Café e alimentação; descrição longa para testar quebra de linha", i))).toList();
        byte[] bytes = exports.pdf(fixture(rows));
        try (var doc = Loader.loadPDF(bytes)) {
            assertTrue(doc.getNumberOfPages() > 1);
            String text = new PDFTextStripper().getText(doc);
            for (int i = 0; i < 80; i++) assertTrue(text.contains(String.format("Lançamento %03d", i)));
            assertTrue(text.contains("100,30"));
            assertTrue(text.contains("Casa - João e Maria"));
            assertTrue(text.contains("Página " + doc.getNumberOfPages() + " de " + doc.getNumberOfPages()));
            // Optional visual QA artifact. Normal test runs produce no files.
            if (System.getProperty("statement.qa.dir") != null) {
                Path directory = Path.of(System.getProperty("statement.qa.dir"));
                Files.createDirectories(directory);
                Files.write(directory.resolve("extrato-exemplo.pdf"), bytes);
                var renderer = new PDFRenderer(doc);
                for (int i = 0; i < doc.getNumberOfPages(); i++) {
                    javax.imageio.ImageIO.write(renderer.renderImageWithDPI(i, 100), "png", directory.resolve("page-" + (i + 1) + ".png").toFile());
                }
            }
        }
    }

    @Test void emptyPdfAndUnsupportedGlyphsDoNotFail() throws Exception {
        try (var doc = Loader.loadPDF(exports.pdf(fixture(List.of())))) {
            assertEquals(1, doc.getNumberOfPages());
            assertTrue(new PDFTextStripper().getText(doc).contains("Nenhum lançamento"));
        }
        try (var doc = Loader.loadPDF(exports.pdf(fixture(List.of(entry("Café ☕ 中文")))))) {
            assertTrue(new PDFTextStripper().getText(doc).contains("Café"));
        }
    }
}
