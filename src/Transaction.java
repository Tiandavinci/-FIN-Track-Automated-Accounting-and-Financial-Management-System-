package fintrack;

import java.time.LocalDate;

/** One money movement: either income or an expense. Fields are final, so it can't be changed after creation. */
public class Transaction {
    public final int id;
    public final LocalDate date;
    public final String description;
    public final double amount;       // always positive
    public final Category category;
    public final boolean income;      // true = income, false = expense

    public Transaction(int id, LocalDate date, String description, double amount, Category category, boolean income) {
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required.");
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        this.id = id;
        this.date = date;
        this.description = description.trim().replace("|", " ");   // "|" is the file separator
        this.amount = amount;
        this.category = category;
        this.income = income;
    }

    /** Positive for income, negative for expense. */
    public double signedAmount() { return income ? amount : -amount; }
}
