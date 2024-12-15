**Personal Finance Manager Application**

**Overview**

The Personal Finance Manager Application is a desktop-based Java application designed to assist users in effectively managing their personal finances. The application offers tools to track daily expenses, set budgets, and export or import financial data in CSV format. With its user-friendly graphical interface, this application simplifies the process of monitoring expenses and analyzing budgets, empowering users to take control of their finances.

**Features**

Expense Tracking: Add and view daily expenses with attributes such as amount, category, date, and description.

Budget Management: Set and monitor your budget with real-time updates on remaining amounts.

Data Import/Export: Easily export expenses to CSV files or load existing data from a CSV file.

Simplified User Interface: A single-window application with intuitive buttons for essential actions such as adding expenses, setting budgets, and exporting data.

Data Security: Operates entirely within a local environment, ensuring user data is private and secure.

**Problem Statement**

Managing day-to-day financial transactions is becoming increasingly complex. Existing tools often present challenges such as high costs, unnecessary complexity, and concerns about data security. This project addresses these issues by providing:

A free and user-friendly platform.

Essential features without overwhelming complexity.

Secure, local storage for all user data.

**Application Design**

**User Interface**

The user interface is built for simplicity and ease of use:

Buttons for key actions: Adding expenses, setting budgets, clearing expenses, exporting, and loading data.

Remaining Budget Display: Dynamically updates based on user input.

Expense Table: Displays recorded expenses with details including amount, category, date, and description.

**Architecture**

The application is structured with the following key components:

**MainApp**

Entry point of the application, handling user interface interactions.

**Key Methods:**

setBudget(): Sets a new budget.

addExpense(): Adds an expense to the tracker.

exportCSV() and loadExpensesFromCSV(): Manage file operations for saving and loading data.

**ExpenseHandler**

Manages a collection of expenses and performs operations like adding, clearing, and calculating totals.

**Attributes:**

expenses: List of Expense objects.

**Methods:**

getTotalSpent(): Calculates the total expenses.

addExpense(Expense): Adds a new expense to the list.

**BudgetHandler**

Handles budget calculations and updates.

**Key Methods:**

getRemainingBudget(double, double): Calculates the remaining budget based on expenses.

getTotalSpent(List<Expense>): Summarizes all expenses for a given period.

**ExpenseTrackerService**

Central service that interacts with other classes to manage expenses, budgets, and file operations.

**Methods:**

getTotalSpent(): Calculates the total expenses.

getRemainingBudget(double): Returns the updated remaining budget.

exportExpensesToCSV(): Calls the CSVHandler to export data.

**Expense**

Represents an individual transaction with attributes like description, category, date, and amount.

**Attributes:**

description: A description of the expense.

category: The type of expense.

amount: The amount spent.

date: The date of the expense.

**Methods:**

toCSV(): Converts the expense data into a CSV-compatible format.

getAmount(): Returns the amount spent.

**CSVHandler**

Handles reading and writing expense data from/to CSV files.

**Methods:**

exportExpensesToCSV(String, List<Expense>): Exports the list of expenses to a file.

loadExpensesFromCSV(String): Loads expense data from a file.

**Installation and Setup**

**Prerequisites:**

Java Development Kit (JDK) 8 or higher.

A Java IDE such as IntelliJ IDEA, Eclipse, or NetBeans.

**Clone the Repository:**

git clone https://github.com/YB-Yottabyte/personal-finance.git

Build and Run:

Open the project in your preferred IDE.

Build the project to resolve dependencies.

Run the MainApp class to start the application.

**Usage**

Adding Expenses:

Enter details such as amount, category, date, and description.

Click the "Add Expense" button to save the entry.

Setting a Budget:

Click the "Set Budget" button and enter your desired budget.

The remaining budget will update dynamically based on your expenses.

Exporting Data:

Click "Export" to save your expenses as a CSV file.

Importing Data:

Click "Load" to import expenses from an existing CSV file.


**License**

This project is licensed under the MIT License. See the LICENSE file for more details.

**Contact**

For questions, feel free to reach out:

GitHub: https://github.com/YB-Yottabyte

LinkedIn: https://www.linkedin.com/in/sai-rithwik-kukunuri-b5084527b/

