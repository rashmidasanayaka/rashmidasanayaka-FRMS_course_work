package lk.ijse.fleet;

public abstract class Customer implements Reportable {

    private static int customerCounter = 1001;

    private String customerId;
    private String fullName;
    private String contactNumber;

    public Customer(String fullName, String contactNumber) {
        this.customerId = "CUS-" + customerCounter;
        customerCounter++;

        this.fullName = fullName;
        this.contactNumber = contactNumber;
    }

    public abstract double applyDiscount(double subtotal);

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @Override
    public String getSummary() {
        return customerId + " | " + fullName;
    }

    @Override
    public String getReportCategory() {
        return "CUSTOMER";
    }

    @Override
    public String toString() {
        return customerId + " | " + fullName + " | " + contactNumber;
    }
}
