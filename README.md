# -FIN-Track-Automated-Accounting-and-Financial-Management-System-

Team Name: FINDING MIMO
Team Number: 3
Section: BSIT - 2104

## Project Description
FIN Track is a Java-based Automated Accounting and Financial Management System designed to help individuals and small businesses manage their financial records more efficiently. The system allows users to record, categorize, monitor, and summarize income and expenses in one organized application.

It provides features such as transaction management, financial summaries, category reports, monthly budget monitoring, and data storage. By automating these accounting tasks, FIN Track aims to reduce manual recording errors, save time, and help users make better financial decisions.


## Problem Being Address
Many individuals and small businesses still rely on notebooks or scattered spreadsheets to track their financial transactions. This manual process can be time-consuming, difficult to organize, and prone to errors.

It can also be challenging to monitor spending habits, calculate balances, and determine where money is being spent. FIN Track addresses these problems by providing an organized system for recording and managing financial transactions.

## Project Objectives
To develop a Java-based application that will automate accounting tasks and manage personal and business finances by recording, organizing, and reporting financial transactions accurately and efficiently. 

## Target User
-Individual
-Small Business Owner
-Bookkeeper

## Main Features
Transaction Recording - Add income and expenses with date, description, amount, and category.

Transaction Management - View all transactions, search by keyword, and delete records.

Financial Summary - Total income, total expenses, net balance, and estimated tax for business 
accounts.
Category Report - Expense totals and percentages for each category.

Budget Monitoring - Monthly budget with warnings when spending nears or exceeds it.

Data Persistence - Records are saved to a file and reloaded on the next run.


## Technology Used
Java

Java Development Kit (JDK)

GitHub

Java IDE
## Team Members & Roles
Oquin, Edmel Christian - Project Leader

Ilagan, Jaybee Jr. - Lead Programmer

Martinez, Juan Carlo Gabriel - System/Design Lead

Matira, Adhyson - Documentation

## Team Responsibilities
Oquin, Edmel Christian - Project Leader: Responsible for coordinating the team, monitoring deadlines, checking progress reports, and overseeing the main menu and program flow.

Ilagan, Jaybee Jr. - Lead Programmer: Responsible for developing the transaction and account class hierarchies, implementing exception handling, and ensuring the program's functionality.

Martinez, Juan Carlo Gabriel - System/Design Lead: Responsible for gathering system requirements, creating the UML diagram, and developing the ReportGenerator and FileStorage components.

Matira, Adhyson - Documentation: Responsible for preparing the README, creating test cases, gathering screenshots, preparing the InputReader component, and making presentation materials.

## OOP Concepts Demonstrated
Class and Objects - Each real-world thing in the system is a class: Account, Transaction, FileStorage and so on. Objects are created at run time. Each registered user becomes a PersonalAccount or BusinessAccount object. Each income or expense entry becomes a Transaction object stored in that account.

Methods - Each account does its own work through methods such as add(), delete(), search(), total(), balance(), spentThisMonth() and expensesByCategory(). FileStorage has save() and load(). FinTrackServer has helper methods for registering, logging in and adding transactions.

Encapsulation - The transaction list in Account is private. It can only be changed through add() and delete(). list() returns a copy so outside code can’t alter the real list. Transaction fields are final, so an entry can’t be changed after it is created. Its constructor also rejects a blank description or an amount of zero or less.

Inheritance - PersonalAccount and BusinessAccount both extend Account. They reuse the password check, the transaction list, the totals and the budget, and add only what is different. BusinessAccount adds a business name and a tax rule.
## Program Flow



