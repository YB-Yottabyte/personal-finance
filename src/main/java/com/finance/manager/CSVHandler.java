package com.finance.manager;

import org.springframework.stereotype.Component;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV import and export. Stateless — does not reference ExpenseHandler
 * directly, so it is independently testable.
 */
@Component
public class CSVHandler {

    public void exportExpensesToCSV(String filePath, List<Expense> expenseList) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Amount,Category,Date,Description\n");
            for (Expense expense : expenseList) {
                writer.write(expense.toCSV());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error exporting CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses a CSV file and returns the expenses as a list.
     * The header row is skipped automatically; invalid lines are logged and skipped.
     *
     * @throws RuntimeException if the file cannot be opened
     */
    public List<Expense> loadExpensesFromCSV(String filePath) {
        List<Expense> loaded = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("Amount,")) continue; // skip header
                }
                if (line.isBlank()) continue;
                try {
                    loaded.add(Expense.fromCSV(line));
                } catch (Exception ex) {
                    System.err.println("Skipping invalid CSV line: " + line + " (" + ex.getMessage() + ")");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV: " + e.getMessage(), e);
        }
        return loaded;
    }
}
