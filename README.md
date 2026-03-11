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
