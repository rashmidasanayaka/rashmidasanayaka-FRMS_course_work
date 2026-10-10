package lk.ijse.fleet;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String LINE = "============================================================================";
    private static final String DASH = "----------------------------------------------------------------------------";
    private static final String BOX_TOP = "+---------------------------------------------------------------------+";
    private static final String BOX_MID = "+---------------------------------------------------------------------+";
    private static final String BOX_BOTTOM = "+---------------------------------------------------------------------+";

    private FleetManager fleetManager;
    private UserManager userManager;
    private Scanner scanner;
    private SystemUser loggedInUser;

    public Main() {
        fleetManager = new FleetManager();
        userManager = new UserManager();
        scanner = new Scanner(System.in);
        loggedInUser = null;

        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId())
        );

        startBackgroundWorkers();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    public void run() {
        while (true) {
            showAuthMenu();
            showMainMenu();
        }
    }

    private void startBackgroundWorkers() {
        fleetManager.printFleetStatus();

        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();
    }

    public void showAuthMenu() {
        boolean loggedIn = false;

        while (!loggedIn) {
            clearConsole();
            printHeader("FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println("  [1] Login                       [2] Register New User");
            System.out.println("  [0] Exit");
            System.out.println(BOX_BOTTOM);
            System.out.print("  Enter choice > ");

            int choice = readMenuChoice();

            if (choice == 1) {
                loggedIn = loginScreen();
            } else if (choice == 2) {
                registerScreen();
            } else if (choice == 0) {
                System.out.println();
                System.out.println("  Thank you for using FRMS. Goodbye!");
                System.exit(0);
            } else if (choice != -1) {
                System.out.println("  Invalid choice. Please pick a number from the menu.");
                pause();
            }
        }
    }

    private boolean loginScreen() {
        clearConsole();
        printHeader("LOGIN");

        String username = readText("  Username : ");
        String password = readText("  Password : ");

        SystemUser user = userManager.login(username, password);
        if (user == null) {
            System.out.println();
            System.out.println("  Invalid username or password.");
            pause();
            return false;
        }

        loggedInUser = user;
        return true;
    }

    private void registerScreen() {
        clearConsole();
        printHeader("REGISTER NEW USER");

        String username = readText("  Username  : ");
        String password = readText("  Password  : ");
        String fullName = readText("  Full name : ");

        System.out.println();
        userManager.registerUser(username, password, fullName);
        pause();
    }

    public void showMainMenu() {
        boolean running = true;

        while (running) {
            clearConsole();
            printMainMenuFrame();

                System.out.println(fleetManager.getLatestStatus());
            List<String> alerts = fleetManager.getOverdueAlerts();
            if (alerts.size() == 0) {
                System.out.println("No overdue rentals.");
            } else {
                for (String alert : alerts) {
                    System.out.println(alert);
                }
            }

            System.out.println(DASH);
            System.out.println("[1] Add Vehicle                     [6] View Revenue Report");
            System.out.println("[2] Add Customer                    [7] View Vehicles by Fuel Type");
            System.out.println("[3] View Available Vehicles         [8] View Combined Fleet & Customer Report");
            System.out.println("[4] Create Rental Agreement         [9] View All Customers");
            System.out.println("[5] Return Vehicle                 [10] View All Rental Agreements");
            System.out.println("[0] Logout");
            System.out.println(DASH);
            System.out.print("Enter choice > ");

            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    addVehicleScreen();
                    break;
                case 2:
                    addCustomerScreen();
                    break;
                case 3:
                    viewAvailableVehiclesScreen();
                    break;
                case 4:
                    createRentalScreen();
                    break;
                case 5:
                    returnVehicleScreen();
                    break;
                case 6:
                    revenueReportScreen();
                    break;
                case 7:
                    vehiclesByFuelTypeScreen();
                    break;
                case 8:
                    combinedReportScreen();
                    break;
                case 9:
                    viewAllCustomersScreen();
                    break;
                case 10:
                    viewAllAgreementsScreen();
                    break;
                case 0:
                    loggedInUser = null;
                    running = false;
                    break;
                case -1:
                    break;
                default:
                    System.out.println("  Invalid choice. Please pick a number from the menu.");
                    pause();
                    break;
            }
        }
    }

    private void addVehicleScreen() {
        clearConsole();
        printHeader("ADD VEHICLE");

        System.out.println("  Vehicle type:");
        System.out.println("    [1] Car      [2] Van      [3] Truck");
        int type = readInt("  Choose type > ", 1, 3);

        String registrationNumber = readText("  Registration number : ");
        String brand = readText("  Brand               : ");
        String model = readText("  Model               : ");
        double dailyRate = readPositiveDouble("  Daily rate          : ");
        FuelType fuelType = readFuelType();

        Vehicle vehicle;
        if (type == 1) {
            vehicle = new Car(registrationNumber, brand, model, dailyRate, fuelType);
        } else if (type == 2) {
            int seats = readInt("  Passenger capacity  : ", 1, 100);
            vehicle = new Van(registrationNumber, brand, model, dailyRate, fuelType, seats);
        } else {
            double tons = readPositiveDouble("  Cargo capacity (tons): ");
            vehicle = new Truck(registrationNumber, brand, model, dailyRate, fuelType, tons);
        }

        fleetManager.addVehicle(vehicle);

        System.out.println();
        System.out.println("  Vehicle added successfully: " + vehicle.getVehicleId());
        pause();
    }

    private void addCustomerScreen() {
        clearConsole();
        printHeader("ADD CUSTOMER");

        System.out.println("  Customer type:");
        System.out.println("    [1] Individual      [2] Corporate");
        int type = readInt("  Choose type > ", 1, 2);

        String fullName = readText("  Full name      : ");
        String contactNumber = readText("  Contact number : ");

        Customer customer;
        if (type == 1) {
            int points = readInt("  Loyalty points : ", 0, 1000000);
            customer = new IndividualCustomer(fullName, contactNumber, points);
        } else {
            double creditLimit = readPositiveDouble("  Credit limit   : ");
            customer = new CorporateCustomer(fullName, contactNumber, creditLimit);
        }

        fleetManager.addCustomer(customer);

        System.out.println();
        System.out.println("  Customer added successfully: " + customer.getCustomerId());
        pause();
    }

    private void viewAvailableVehiclesScreen() {
        clearConsole();
        printTitle("AVAILABLE VEHICLES");

        List<Vehicle> list = fleetManager.getAvailableVehicles();
        if (list.size() == 0) {
            System.out.println("  No vehicles are available right now.");
        } else {
            for (Vehicle v : list) {
                System.out.println("  " + v);
            }
        }
        pause();
    }

    private void createRentalScreen() {
        clearConsole();
        printTitle("CREATE RENTAL AGREEMENT");

        System.out.println("  Available vehicles:");
        List<Vehicle> vehicles = fleetManager.getAvailableVehicles();
        if (vehicles.size() == 0) {
            System.out.println("    (none)");
        }
        for (Vehicle v : vehicles) {
            System.out.println("    " + v);
        }

        System.out.println();
        System.out.println("  Customers:");
        List<Customer> customers = fleetManager.getAllCustomers();
        if (customers.size() == 0) {
            System.out.println("    (none)");
        }
        for (Customer c : customers) {
            System.out.println("    " + c);
        }

        System.out.println(DASH);
        String vehicleId = readText("  Vehicle ID  (e.g. VEH-1001) : ").toUpperCase();
        String customerId = readText("  Customer ID (e.g. CUS-1001) : ").toUpperCase();
        int days = readInt("  Rental days                 : ", 1, 365);

        System.out.println();
        try {
            RentalAgreement agreement = fleetManager.createRentalAgreement(vehicleId, customerId, days);
            if (agreement != null) {
                System.out.println(agreement);
            }
        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
        pause();
    }

    private void returnVehicleScreen() {
        clearConsole();
        printTitle("RETURN VEHICLE");

        List<RentalAgreement> agreements = fleetManager.getAllAgreements();
        if (agreements.size() == 0) {
            System.out.println("  There are no rental agreements yet.");
            pause();
            return;
        }

        printAgreementList(agreements);
        System.out.println(DASH);

        String agreementId = readText("  Agreement ID (e.g. AGR-1001) : ").toUpperCase();

        System.out.println();
        fleetManager.returnVehicle(agreementId);
        pause();
    }

    private void revenueReportScreen() {
        clearConsole();
        printTitle("FLEET REVENUE REPORT");

        double total = fleetManager.generateRevenueReport();
        System.out.println("  Total Revenue: " + FleetManager.formatMoney(total));
        pause();
    }

    private void vehiclesByFuelTypeScreen() {
        clearConsole();
        printTitle("VEHICLES BY FUEL TYPE");

        FuelType type = readFuelType();
        List<Vehicle> list = fleetManager.getVehiclesByFuelType(type);

        System.out.println();
        if (list.size() == 0) {
            System.out.println("  No " + type + " vehicles found.");
        } else {
            for (Vehicle v : list) {
                System.out.println("  " + v);
            }
        }
        pause();
    }

    private void combinedReportScreen() {
        clearConsole();
        printTitle("COMBINED FLEET & CUSTOMER REPORT");

        fleetManager.printAllReportables();
        pause();
    }

    private void viewAllCustomersScreen() {
        clearConsole();
        printTitle("ALL CUSTOMERS");

        List<Customer> list = fleetManager.getAllCustomers();
        if (list.size() == 0) {
            System.out.println("  No customers added yet.");
        } else {
            for (Customer c : list) {
                System.out.println("  " + c);
            }
        }
        pause();
    }

    private void viewAllAgreementsScreen() {
        clearConsole();
        printTitle("ALL RENTAL AGREEMENTS");

        List<RentalAgreement> list = fleetManager.getAllAgreements();
        if (list.size() == 0) {
            System.out.println("  No rental agreements yet.");
        } else {
            printAgreementList(list);
        }
        pause();
    }

    private void printAgreementList(List<RentalAgreement> list) {
        for (RentalAgreement a : list) {
            System.out.println("  " + a.getAgreementId()
                    + " | " + a.getVehicle().getVehicleId()
                    + " | " + a.getCustomer().getCustomerId()
                    + " | Days: " + a.getRentalDays()
                    + " | Total: " + FleetManager.formatMoney(a.getTotalCost())
                    + " | Vehicle: " + a.getVehicle().getStatus());
        }
    }

    private void clearConsole() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J\033[3J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 35; i++) {
                System.out.println();
            }
        }
    }

    private void pause() {
        System.out.println();
        System.out.print("  Press Enter to continue...");
        scanner.nextLine();
    }

    private void printTitle(String title) {
        printHeader(title);
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println(BOX_TOP);
        System.out.println("| " + padCenter(title, 68) + " |");
        System.out.println(BOX_MID);
    }

    private void printMainMenuFrame() {
        String header = "FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName();
        System.out.println("+" + "-".repeat(69) + "+");
        System.out.println("| " + padCenter(header, 67) + " |");
        System.out.println("+" + "-".repeat(69) + "+");
    }

    private String padCenter(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }

        int spaces = width - text.length();
        int left = spaces / 2;
        int right = spaces - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private int readMenuChoice() {
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            pause();
            return -1;
        }
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (!text.equals("")) {
                return text;
            }
            System.out.println("  This cannot be empty. Please try again.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(text);
                if (number >= min && number <= max) {
                    return number;
                }
                System.out.println("  Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a whole number.");
            }
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                double number = Double.parseDouble(text);
                if (number > 0) {
                    return number;
                }
                System.out.println("  The number must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a number.");
            }
        }
    }

    private FuelType readFuelType() {
        System.out.println("  Fuel type:");
        System.out.println("    [1] PETROL      [2] DIESEL");
        System.out.println("    [3] ELECTRIC    [4] HYBRID");
        int choice = readInt("  Choose fuel type > ", 1, 4);

        if (choice == 1) {
            return FuelType.PETROL;
        } else if (choice == 2) {
            return FuelType.DIESEL;
        } else if (choice == 3) {
            return FuelType.ELECTRIC;
        } else {
            return FuelType.HYBRID;
        }
    }
}
