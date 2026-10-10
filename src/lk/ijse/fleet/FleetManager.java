package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetManager {

    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<RentalAgreement> agreements = new ArrayList<>();
    private RentalEventListener rentalEventListener;
    private String latestStatus = "Waiting for the first fleet check...";
    private List<String> overdueAlerts = new ArrayList<>();

    public synchronized void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public synchronized void addCustomer(Customer c) {
        customers.add(c);
    }

    public Vehicle findVehicleById(String id) {
        for (Vehicle v : vehicles) {
            if (v.getVehicleId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public Customer findCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getCustomerId().equals(id)) {
                return c;
            }
        }
        return null;
    }

    public void setRentalEventListener(RentalEventListener listener) {
        this.rentalEventListener = listener;
    }

    public List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(v -> v.getStatus() == RentalStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public List<RentalAgreement> getAllAgreements() {
        return new ArrayList<>(agreements);
    }

    public synchronized RentalAgreement createRentalAgreement(String vehicleId, String customerId, int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {

        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle == null) {
            System.out.println("Vehicle " + vehicleId + " was not found.");
            return null;
        }

        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException("Vehicle " + vehicleId + " is not available.");
        }

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer " + customerId + " was not found.");
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double total = customer.applyDiscount(subtotal);

        if (customer instanceof CorporateCustomer) {
            CorporateCustomer corporate = (CorporateCustomer) customer;
            if (!corporate.hasSufficientCredit(total)) {
                throw new InsufficientCreditException("Customer " + customerId
                        + " does not have enough credit for this rental.");
            }
        }

        RentalAgreement agreement = new RentalAgreement(vehicle, customer, rentalDays);
        vehicle.setStatus(RentalStatus.RENTED);
        agreements.add(agreement);

        if (rentalEventListener != null) {
            rentalEventListener.onRentalCreated(agreement);
        }

        return agreement;
    }

    public synchronized void returnVehicle(String agreementId) {
        for (RentalAgreement agreement : agreements) {
            if (agreement.getAgreementId().equals(agreementId)) {
                Vehicle vehicle = agreement.getVehicle();

                if (vehicle.getStatus() == RentalStatus.AVAILABLE) {
                    System.out.println("Vehicle " + vehicle.getVehicleId() + " was already returned.");
                } else {
                    vehicle.setStatus(RentalStatus.AVAILABLE);
                    System.out.println("Vehicle " + vehicle.getVehicleId() + " returned successfully.");
                }
                return;
            }
        }
        System.out.println("Agreement " + agreementId + " was not found.");
    }

    public FleetSnapshot generateSnapshot() {
        return new FleetSnapshot();
    }

    public synchronized void printFleetStatus() {
        latestStatus = generateSnapshot().getSummaryText();
    }

    public synchronized void checkOverdueAgreements() {
        overdueAlerts.clear();

        for (RentalAgreement agreement : agreements) {
            if (agreement.getVehicle().getStatus() == RentalStatus.RENTED) {
                agreement.setDaysElapsed(agreement.getDaysElapsed() + 1);

                if (agreement.getDaysElapsed() > agreement.getRentalDays()) {
                    overdueAlerts.add("[OVERDUE ALERT] Agreement " + agreement.getAgreementId()
                            + " has exceeded its " + agreement.getRentalDays() + "-day rental period.");
                }
            }
        }
    }

    public synchronized String getLatestStatus() {
        return latestStatus;
    }

    public synchronized List<String> getOverdueAlerts() {
        return new ArrayList<>(overdueAlerts);
    }

    public List<Vehicle> getVehiclesByFuelType(FuelType type) {
        return vehicles.stream()
                .filter(v -> v.getFuelType() == type)
                .collect(Collectors.toList());
    }

    public List<Customer> getCustomersSortedByName() {
        return customers.stream()
                .sorted(Comparator.comparing(c -> c.getFullName()))
                .collect(Collectors.toList());
    }

    public double generateRevenueReport() {
        return agreements.stream()
                .mapToDouble(a -> a.getTotalCost())
                .sum();
    }

    public void printAllReportables() {
        List<Reportable> reportables = new ArrayList<>();
        reportables.addAll(vehicles);
        reportables.addAll(customers);

        if (reportables.size() == 0) {
            System.out.println("  Nothing to report yet.");
            return;
        }

        for (Reportable item : reportables) {
            System.out.println("  " + item.getReportCategory() + ": " + item.getSummary());
        }
    }

    public static String formatMoney(double amount) {
        return String.format("%,.2f", amount);
    }

    public class FleetSnapshot {

        private int availableCount;
        private int rentedCount;
        private double totalRevenue;

        public FleetSnapshot() {
            availableCount = 0;
            rentedCount = 0;
            totalRevenue = 0;

            for (Vehicle v : vehicles) {
                if (v.getStatus() == RentalStatus.AVAILABLE) {
                    availableCount++;
                } else {
                    rentedCount++;
                }
            }

            for (RentalAgreement a : agreements) {
                totalRevenue = totalRevenue + a.getTotalCost();
            }
        }

        public String getSummaryText() {
            return "[FLEET STATUS] Available: " + availableCount
                    + "  Rented: " + rentedCount
                    + "  Total Revenue So Far: " + formatMoney(totalRevenue);
        }

        public void printSummary() {
            System.out.println(getSummaryText());
        }
    }
}
