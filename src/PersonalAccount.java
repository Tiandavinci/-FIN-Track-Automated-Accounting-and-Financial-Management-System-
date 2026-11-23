package fintrack;

/** Personal account: no tax. */
public class PersonalAccount extends Account {
    public PersonalAccount(String username, String passwordHash) { super(username, passwordHash); }

    @Override public String type() { return "PERSONAL"; }
    @Override public double tax() { return 0; }
}
