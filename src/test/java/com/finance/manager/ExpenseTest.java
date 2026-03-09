package com.finance.manager;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    private static final LocalDate TODAY = LocalDate.of(2024, 1, 15);

    @Test
    void constructor_createsExpenseWithCorrectValues() {
        Expense expense = new Expense(50.00, "Food", TODAY, "Lunch");
        assertEquals(50.00, expense.getAmount());
        assertEquals("Food", expense.getCategory());
        assertEquals(TODAY, expense.getDate());
        assertEquals("Lunch", expense.getDescription());
    }

    @Test
    void constructor_throwsOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Expense(-1.0, "Food", TODAY, ""));
    }

    @Test
    void constructor_throwsOnBlankCategory() {
        assertThrows(IllegalArgumentException.class,
                () -> new Expense(10.0, "  ", TODAY, "desc"));
    }

    @Test
    void constructor_throwsOnNullDate() {
        assertThrows(IllegalArgumentException.class,
                () -> new Expense(10.0, "Food", null, "desc"));
    }

    @Test
    void toCSV_producesValidCSVLine() {
        Expense expense = new Expense(12.50, "Transport", TODAY, "Bus ticket");
        assertEquals("12.50,Transport,2024-01-15,Bus ticket", expense.toCSV());
    }

    @Test
    void toCSV_quotesDescriptionWithComma() {
        Expense expense = new Expense(9.99, "Food", TODAY, "Coffee, bagel");
        String csv = expense.toCSV();
        assertTrue(csv.contains("\"Coffee, bagel\""),
                "Descriptions with commas should be quoted. Got: " + csv);
    }

    @Test
    void fromCSV_roundTripsSimpleExpense() {
        Expense original = new Expense(25.00, "Shopping", TODAY, "Books");
        Expense parsed = Expense.fromCSV(original.toCSV());
        assertEquals(original.getAmount(), parsed.getAmount(), 0.001);
        assertEquals(original.getCategory(), parsed.getCategory());
        assertEquals(original.getDate(), parsed.getDate());
        assertEquals(original.getDescription(), parsed.getDescription());
    }

    @Test
    void fromCSV_roundTripsDescriptionWithComma() {
        Expense original = new Expense(7.50, "Food", TODAY, "Coffee, muffin");
        Expense parsed = Expense.fromCSV(original.toCSV());
        assertEquals("Coffee, muffin", parsed.getDescription());
    }

    @Test
    void fromCSV_throwsOnTooFewFields() {
        assertThrows(IllegalArgumentException.class,
                () -> Expense.fromCSV("12.00,Food,2024-01-15"));
    }

    @Test
    void parseCsvLine_handlesQuotedFields() {
        String[] fields = Expense.parseCsvLine("1.00,\"A, B\",2024-01-01,desc");
        assertEquals("1.00", fields[0]);
        assertEquals("A, B", fields[1]);
        assertEquals("2024-01-01", fields[2]);
        assertEquals("desc", fields[3]);
    }
}
