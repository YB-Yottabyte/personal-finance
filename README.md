# Personal Finance Manager

![CI](https://github.com/YB-Yottabyte/personal-finance/actions/workflows/ci.yml/badge.svg)

A desktop application for tracking expenses, managing budgets, and analyzing spending patterns — built with Java 17, Swing, JFreeChart, and FlatLaf.

---

## Features

| Feature | Details |
|---|---|
| **Expense Tracking** | Add, search, sort, and clear expenses with real-time table filtering |
| **Budget Management** | Set weekly/monthly budgets; color-coded progress bar turns red above 90% |
| **Spending Analytics** | Category breakdown, monthly trends, next-month forecast, anomaly detection |
| **Data Visualization** | Pie chart (by category) and bar chart (monthly trend) via JFreeChart |
| **Dark Mode** | Toggle between light and dark FlatLaf themes at runtime |
| **CSV Import/Export** | RFC 4180-compliant; handles commas inside fields correctly |
| **Budget Persistence** | Budget settings saved and restored across sessions |

---

## Analytics Algorithms

The `SpendingAnalytics` class implements several algorithms on `List<Expense>`:

- **Top-K categories** — min-heap of size K; O(c log K) where c = number of categories
- **Sliding-window moving average** — two-pointer over sorted daily totals; O(n)
- **Linear regression forecast** — ordinary least-squares over monthly totals to project next month's spend
- **Budget burn rate** — estimates days until budget exhausted based on average daily spend
- **Z-score anomaly detection** — flags expenses more than 2 standard deviations from their category mean

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 (records, streams, sealed classes) |
| GUI | Java Swing + FlatLaf 3.4 |
| Charts | JFreeChart 1.5.4 |
| Build | Apache Maven |
| Testing | JUnit 5 (`@ParameterizedTest`, `@TempDir`) |
| CI | GitHub Actions |
| Storage | CSV + Java Properties |

---

## Project Structure

```
personal-finance/
├── pom.xml
└── src/
    ├── main/java/com/finance/manager/
    │   ├── MainApp.java                  Entry point; tabbed UI with FlatLaf + dark mode
    │   ├── Expense.java                  Immutable model with RFC 4180 CSV serialization
    │   ├── BudgetConfig.java             Java record for budget settings
    │   ├── ExpenseHandler.java           Expense list + auto-save
    │   ├── ExpenseTrackerService.java    Facade (constructor injection for tests)
    │   ├── BudgetHandler.java            Budget calculations
    │   ├── CSVHandler.java               Stateless CSV import/export
    │   ├── analytics/
    │   │   ├── SpendingAnalytics.java    Top-K, linear regression, moving average, burn rate
    │   │   └── SpendingAnomaly.java      Z-score anomaly detection
    │   └── ui/
    │       ├── DashboardPanel.java       Pie chart + live summary stats
    │       ├── TrendPanel.java           Monthly bar chart + forecast label
    │       └── ExpenseTablePanel.java    Searchable, sortable, color-coded table
    └── test/java/com/finance/manager/
        ├── ExpenseTest.java
        ├── BudgetHandlerTest.java
        ├── CSVHandlerTest.java           @TempDir round-trip tests
        └── analytics/
            ├── SpendingAnalyticsTest.java  @ParameterizedTest coverage
            └── SpendingAnomalyTest.java
```

---

## Getting Started

**Prerequisites:** Java 17+, Apache Maven 3.8+

```bash
git clone https://github.com/YB-Yottabyte/personal-finance.git
cd personal-finance

# Run all 42 tests
mvn test

# Build a single runnable JAR
mvn package

# Launch the app
java -jar target/personal-finance-manager.jar
```

---

## Academic Context

Originally developed as an Honors project for **CSE 205 (Object-Oriented Programming & Data Structures)** at **Arizona State University**.

[View Full Project Report (PDF)](Report.pdf)
