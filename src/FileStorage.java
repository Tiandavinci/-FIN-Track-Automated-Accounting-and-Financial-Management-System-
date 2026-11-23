package fintrack;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves each account to data/<username>.txt. File format:
 *   line 1:  TYPE|username|passwordHash|businessName|budget
 *   others:  T|INCOME or EXPENSE|id|date|description|amount|category
 */
public class FileStorage {
    private final Path folder = Paths.get("data");

    public FileStorage() throws IOException { Files.createDirectories(folder); }

    private Path file(String username) { return folder.resolve(username + ".txt"); }

    public boolean exists(String username) { return Files.exists(file(username)); }

    public void save(Account a) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(String.join("|", a.type(), a.username, a.passwordHash, a.businessName(), String.valueOf(a.budget)));
        for (Transaction t : a.list())
            lines.add(String.join("|", "T", t.income ? "INCOME" : "EXPENSE", String.valueOf(t.id), t.date.toString(),
                    t.description, String.valueOf(t.amount), t.category.name()));
        Files.write(file(a.username), lines);
    }

    /** Returns null if the user doesn't exist. */
    public Account load(String username) throws IOException {
        if (!exists(username)) return null;
        try {
            List<String> lines = Files.readAllLines(file(username));
            String[] h = lines.get(0).split("\\|", -1);
            Account a = h[0].equals("BUSINESS") ? new BusinessAccount(h[1], h[2], h[3]) : new PersonalAccount(h[1], h[2]);
            a.budget = Double.parseDouble(h[4]);
            for (int i = 1; i < lines.size(); i++) {
                String[] p = lines.get(i).split("\\|", -1);
                a.add(new Transaction(Integer.parseInt(p[2]), LocalDate.parse(p[3]), p[4],
                        Double.parseDouble(p[5]), Category.valueOf(p[6]), p[1].equals("INCOME")));
            }
            return a;
        } catch (RuntimeException e) {
            throw new IOException("Corrupted data file for " + username, e);
        }
    }
}
