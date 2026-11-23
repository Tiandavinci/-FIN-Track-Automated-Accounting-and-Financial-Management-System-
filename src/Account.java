package fintrack;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// A user's ledger. Personal and Business accounts extend this and differ only in the tax they estimate.
public abstract class Account {
    public final String username;
    public final String passwordHash;
    public double budget;                                   // monthly budget (0 = none)
    private final List<Transaction> transactions = new ArrayList<>();   // private: changed only through the methods below

    protected Account(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    //pass
    public static String hash(String password) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }

    public boolean checkPassword(String password) { return passwordHash.equals(hash(password)); }

    //transac
    public void add(Transaction t) { transactions.add(t); }

    public void delete(int id) { transactions.removeIf(t -> t.id == id); }

    public int nextId() {
        int max = 0;
        for (Transaction t : transactions) max = Math.max(max, t.id);
        return max + 1;
    }

    /** Copy of the list, so outside code can't change the real one. */
    public List<Transaction> list() { return new ArrayList<>(transactions); }

    /** Transactions whose description or category contains the keyword (empty keyword = all). */
    public List<Transaction> search(String keyword) {
        String k = keyword == null ? "" : keyword.toLowerCase().trim();
        List<Transaction> found = new ArrayList<>();
        for (Transaction t : transactions)
            if (t.description.toLowerCase().contains(k) || t.category.name().toLowerCase().contains(k)) found.add(t);
        return found;
    }

    //total
    public double total(boolean income) {
        double sum = 0;
        for (Transaction t : transactions) if (t.income == income) sum += t.amount;
        return sum;
    }

    public double balance() { return total(true) - total(false); }

    /** Expense total for each category (array index = the category's position in the enum). */
    public double[] expensesByCategory() {
        double[] totals = new double[Category.values().length];
        for (Transaction t : transactions) if (!t.income) totals[t.category.ordinal()] += t.amount;
        return totals;
    }

    public double spentThisMonth() {
        LocalDate now = LocalDate.now();
        double sum = 0;
        for (Transaction t : transactions)
            if (!t.income && t.date.getYear() == now.getYear() && t.date.getMonth() == now.getMonth()) sum += t.amount;
        return sum;
    }

    // ----- what each account type decides for itself -----
    public abstract String type();
    public abstract double tax();
    public String businessName() { return ""; }
}
