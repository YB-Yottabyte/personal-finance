package com.finance.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVHandlerTest {

    private final CSVHandler handler = new CSVHandler();
    private static final LocalDate D = LocalDate.of(2024, 6, 15);

    // ---- Export -------------------------------------------------------------

    @Test
    void exportCSV_writesHeaderAndRows(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("out.csv");
        List<Expense> expenses = List.of(
                new Expense(12.50, "Food", D, "Lunch"),
                new Expense(45.00, "Transport", D, "Train")
        );

        handler.exportExpensesToCSV(file.toString(), expenses);

        List<String> lines = Files.readAllLines(file);
        assertEquals("Amount,Category,Date,Description", lines.get(0));
        assertTrue(lines.get(1).startsWith("12.50,Food"));
        assertTrue(lines.get(2).startsWith("45.00,Transport"));
    }

    @Test
    void exportCSV_quotesDescriptionContainingComma(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("out.csv");
        handler.exportExpensesToCSV(file.toString(),
                List.of(new Expense(5.0, "Food", D, "Coffee, bagel")));

        String content = Files.readString(file);
        assertTrue(content.contains("\"Coffee, bagel\""),
                "Comma in description should be quoted. Got:\n" + content);
    }

    // ---- Load ---------------------------------------------------------------

    @Test
    void loadCSV_roundTripsExpenses(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("expenses.csv");
        List<Expense> original = List.of(
                new Expense(20.0, "Food",      D, "Groceries"),
                new Expense(80.0, "Transport", D, "Monthly pass")
        );
        handler.exportExpensesToCSV(file.toString(), original);

        List<Expense> loaded = handler.loadExpensesFromCSV(file.toString());

        assertEquals(2, loaded.size());
        assertEquals(20.0, loaded.get(0).getAmount(), 0.001);
        assertEquals("Food", loaded.get(0).getCategory());
        assertEquals(80.0, loaded.get(1).getAmount(), 0.001);
    }

    @Test
    void loadCSV_skipsHeaderRow(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("expenses.csv");
        Files.writeString(file, "Amount,Category,Date,Description\n10.00,Food,2024-06-15,Test\n");

        List<Expense> loaded = handler.loadExpensesFromCSV(file.toString());
        assertEquals(1, loaded.size());
        assertEquals(10.0, loaded.get(0).getAmount(), 0.001);
    }

    @Test
    void loadCSV_skipsInvalidLinesWithoutThrowing(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("expenses.csv");
        Files.writeString(file,
                "Amount,Category,Date,Description\n" +
                "THIS_IS_NOT_A_NUMBER,Food,2024-06-15,bad\n" +
                "15.00,Food,2024-06-15,valid\n");

        List<Expense> loaded = handler.loadExpensesFromCSV(file.toString());
        assertEquals(1, loaded.size(), "Invalid line should be skipped, not thrown");
        assertEquals(15.0, loaded.get(0).getAmount(), 0.001);
    }

    @Test
    void loadCSV_throwsOnMissingFile() {
        assertThrows(RuntimeException.class,
                () -> handler.loadExpensesFromCSV("/nonexistent/path/file.csv"));
    }
}
