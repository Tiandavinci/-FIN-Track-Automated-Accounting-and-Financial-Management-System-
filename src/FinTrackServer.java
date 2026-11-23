package fintrack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Starts the web server: serves the page in web/ and answers the page's /api/ requests with JSON. */
public class FinTrackServer {
    private static final Path WEB = Paths.get("web");
    private final Map<String, Account> sessions = new HashMap<>();   // login token -> logged-in account
    private final FileStorage storage;

    public FinTrackServer() throws IOException { storage = new FileStorage(); }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.US);   // numbers always print as 12.50 in the JSON
        FinTrackServer app = new FinTrackServer();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/", app::handleApi);
        server.createContext("/", app::handleStatic);
        server.start();
        System.out.println("FIN Track is running at http://localhost:8080");
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:8080"));   // open the page automatically
        } catch (Exception e) {
            System.out.println("Open http://localhost:8080 in your browser.");
        }
    }

    // ===================== Web page files =====================

    private void handleStatic(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) path = "/index.html";
        Path file = WEB.resolve(path.substring(1)).normalize();
        if (!file.startsWith(WEB) || !Files.isRegularFile(file)) { send(ex, 404, "text/plain", "Not found"); return; }

        String name = file.toString();
        String type = name.endsWith(".html") ? "text/html" : name.endsWith(".css") ? "text/css"
                : name.endsWith(".js") ? "application/javascript" : name.endsWith(".png") ? "image/png"
                : "application/octet-stream";
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        byte[] bytes = Files.readAllBytes(file);
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    // ===================== API =====================

    private synchronized void handleApi(HttpExchange ex) throws IOException {
        try {
            String path = ex.getRequestURI().getPath();
            Map<String, String> in = parse(ex.getRequestURI().getRawQuery());
            in.putAll(parse(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));

            // Register / login: no token needed yet, we hand one out.
            if (path.equals("/api/register") || path.equals("/api/login")) {
                Account a = path.equals("/api/register") ? register(in) : login(in);
                String token = UUID.randomUUID().toString();
                sessions.put(token, a);
                send(ex, 200, "application/json", "{\"token\":\"" + token + "\"}");
                return;
            }

            // Everything else needs a logged-in user.
            String token = ex.getRequestHeaders().getFirst("X-Token");
            Account acc = sessions.get(token);
            if (acc == null) { send(ex, 401, "application/json", error("Please log in.")); return; }

            switch (path) {
                case "/api/data":
                    break;
                case "/api/logout":
                    sessions.remove(token);
                    send(ex, 200, "application/json", "{}");
                    return;
                case "/api/transactions":
                    addTransaction(acc, in);
                    storage.save(acc);
                    break;
                case "/api/delete":
                    acc.delete(whole(in.get("id")));
                    storage.save(acc);
                    break;
                case "/api/budget":
                    double b = number(in.getOrDefault("amount", "0"));
                    if (b < 0) throw new IllegalArgumentException("Budget cannot be negative.");
                    acc.budget = b;
                    storage.save(acc);
                    break;
                default:
                    send(ex, 404, "application/json", error("Unknown endpoint."));
                    return;
            }
            send(ex, 200, "application/json", report(acc, in.get("q")));

        } catch (IllegalArgumentException e) {          // bad input -> show the message on the page
            send(ex, 400, "application/json", error(e.getMessage()));
        } catch (Exception e) {
            send(ex, 500, "application/json", error("Server error: " + e.getMessage()));
        }
    }

    private Account register(Map<String, String> in) throws IOException {
        String user = username(in.get("username"));
        String pass = in.get("password");
        if (pass == null || pass.length() < 4) throw new IllegalArgumentException("Password must be at least 4 characters.");
        if (storage.exists(user)) throw new IllegalArgumentException("That username is already taken.");

        Account a = "business".equals(in.get("type"))
                ? new BusinessAccount(user, Account.hash(pass), in.get("businessName"))
                : new PersonalAccount(user, Account.hash(pass));
        storage.save(a);
        return a;
    }

    private Account login(Map<String, String> in) throws IOException {
        Account a = storage.load(username(in.get("username")));
        if (a == null || !a.checkPassword(in.getOrDefault("password", "")))
            throw new IllegalArgumentException("Wrong username or password.");
        return a;
    }

    private void addTransaction(Account acc, Map<String, String> in) {
        LocalDate date;
        Category category;
        try { date = LocalDate.parse(in.get("date").trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Date must use the format YYYY-MM-DD."); }
        try { category = Category.valueOf(in.get("category").trim().toUpperCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Please choose a valid category."); }

        acc.add(new Transaction(acc.nextId(), date, in.get("description"), number(in.get("amount")),
                category, "INCOME".equals(in.get("type"))));
    }

    // ===================== Reading input =====================

    private static String username(String s) {
        if (s == null || !s.trim().matches("[A-Za-z0-9_]{3,20}"))
            throw new IllegalArgumentException("Username must be 3-20 letters, numbers or underscores.");
        return s.trim().toLowerCase();
    }

    private static double number(String s) {
        try {
            double v = Double.parseDouble(s.trim());
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new NumberFormatException();
            return v;
        } catch (Exception e) { throw new IllegalArgumentException("Please enter a valid number."); }
    }

    private static int whole(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Please enter a whole number."); }
    }

    /** Turns "a=1&b=2" into a map. */
    private static Map<String, String> parse(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null || text.isBlank()) return map;
        for (String pair : text.split("&")) {
            String[] kv = pair.split("=", 2);
            map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                    kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }

    // ===================== JSON for the page =====================

    private static String report(Account a, String keyword) {
        return "{\"owner\":\"" + esc(a.username) + "\",\"type\":\"" + a.type()
                + "\",\"businessName\":\"" + esc(a.businessName()) + "\""
                + ",\"summary\":" + summaryJson(a)
                + ",\"categories\":" + categoriesJson(a)
                + ",\"budget\":" + budgetJson(a)
                + ",\"transactions\":" + transactionsJson(a.search(keyword)) + "}";
    }

    private static String summaryJson(Account a) {
        return String.format("{\"income\":%.2f,\"expenses\":%.2f,\"balance\":%.2f,\"tax\":%.2f}",
                a.total(true), a.total(false), a.balance(), a.tax());
    }

    private static String categoriesJson(Account a) {
        double[] totals = a.expensesByCategory();
        double all = a.total(false);
        List<String> items = new ArrayList<>();
        for (Category c : Category.values()) {
            double t = totals[c.ordinal()];
            if (t > 0) items.add(String.format("{\"name\":\"%s\",\"total\":%.2f,\"percent\":%.1f}", c, t, t / all * 100));
        }
        return "[" + String.join(",", items) + "]";
    }

    private static String budgetJson(Account a) {
        double spent = a.spentThisMonth();
        String status = a.budget <= 0 ? "none" : spent > a.budget ? "exceeded" : spent >= a.budget * 0.8 ? "warning" : "ok";
        return String.format("{\"budget\":%.2f,\"spent\":%.2f,\"status\":\"%s\"}", a.budget, spent, status);
    }

    private static String transactionsJson(List<Transaction> list) {
        List<String> items = new ArrayList<>();
        for (Transaction t : list)
            items.add(String.format("{\"id\":%d,\"date\":\"%s\",\"type\":\"%s\",\"description\":\"%s\",\"category\":\"%s\",\"amount\":%.2f}",
                    t.id, t.date, t.income ? "INCOME" : "EXPENSE", esc(t.description), t.category, t.signedAmount()));
        return "[" + String.join(",", items) + "]";
    }

    private static String error(String message) { return "{\"error\":\"" + esc(message) + "\"}"; }

    private static String esc(String s) { return String.valueOf(s).replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " "); }

    private static void send(HttpExchange ex, int code, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
