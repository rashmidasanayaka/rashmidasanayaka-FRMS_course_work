package lk.ijse.fleet;

public class RentalAgreement {

    private static int agreementCounter = 1001;

    private String agreementId;
    private Vehicle vehicle;
    private Customer customer;
    private int rentalDays;
    private double totalCost;
    private int daysElapsed;

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {
        double subtotal = vehicle.calculateRentalCost(rentalDays);
        this.totalCost = customer.applyDiscount(subtotal);

        this.agreementId = "AGR-" + agreementCounter;
        agreementCounter++;

        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;
        this.daysElapsed = 0;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(int daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    @Override
    public String toString() {
        String line = "============================================================================";

        String text = line + "\n";
        text = text + "  RENTAL AGREEMENT RECEIPT\n";
        text = text + line + "\n";
        text = text + "  Agreement ID : " + agreementId + "\n";
        text = text + "  Vehicle      : " + vehicle.getVehicleId() + " - " + vehicle.getBrand()
                + " " + vehicle.getModel() + " (" + getVehicleTypeName() + ")\n";
        text = text + "  Customer     : " + customer.getCustomerId() + " - " + customer.getFullName()
                + " (" + getCustomerTypeName() + ")\n";
        text = text + "  Rental Days  : " + rentalDays + "\n";
        text = text + "  Total Cost   : " + FleetManager.formatMoney(totalCost) + "\n";
        text = text + line;
        return text;
    }

    private String getVehicleTypeName() {
        if (vehicle instanceof Car) {
            return "Car";
        } else if (vehicle instanceof Van) {
            return "Van";
        } else if (vehicle instanceof Truck) {
            return "Truck";
        }
        return "Vehicle";
    }

    private String getCustomerTypeName() {
        if (customer instanceof CorporateCustomer) {
            return "Corporate";
        } else if (customer instanceof IndividualCustomer) {
            return "Individual";
        }
        return "Customer";
    }
}
