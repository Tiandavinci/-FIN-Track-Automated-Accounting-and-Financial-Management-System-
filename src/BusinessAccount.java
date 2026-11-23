package fintrack;

//business acc may 12% tax sa net balance
public class BusinessAccount extends Account {
    private final String businessName;

    public BusinessAccount(String username, String passwordHash, String businessName) {
        super(username, passwordHash);
        if (businessName == null || businessName.isBlank()) throw new IllegalArgumentException("Business name is required.");
        this.businessName = businessName.trim().replace("|", " ");
    }

    @Override public String type() { return "BUSINESS"; }
    @Override public double tax() { return Math.max(0, balance()) * 0.12; } //ito yung 12% tax
    @Override public String businessName() { return businessName; }
}
