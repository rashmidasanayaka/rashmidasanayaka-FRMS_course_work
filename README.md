# Fleet Rental Management System (FRMS)

ITS1033 - Object-Oriented Programming | Institute of Software Engineering | HDSE 77

A terminal (console) application for a small vehicle rental company. Users register and log in,
manage Cars / Vans / Trucks and Individual / Corporate customers, create and return rental
agreements, and view simple reports. Everything is in the single package `lk.ijse.fleet`.

## Requirements
- JDK 25 or later (the code only uses features available in much older versions, so JDK 17+ also compiles it)

## Build and run

From the project root folder (the folder that contains `src`):

```
javac -d bin $(find src -name "*.java")
java -cp bin lk.ijse.fleet.Main
```

On Windows PowerShell:

```
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d bin $files
java -cp bin lk.ijse.fleet.Main
```

Note: the background threads print a fleet status line every 5 seconds, so these lines can appear
in the middle of what you are typing. Your input is not lost - just keep typing and press Enter.

## Project structure

```
src/lk/ijse/fleet/
  Enums        : FuelType (rich enum), RentalStatus
  Interfaces   : Reportable, RentalEventListener (functional interface)
  Users        : SystemUser, UserManager
  Vehicles     : Vehicle (abstract) -> Car, Van, Truck
  Customers    : Customer (abstract) -> IndividualCustomer, CorporateCustomer
  Exceptions   : VehicleNotAvailableException, InsufficientCreditException (both checked)
  Business     : RentalAgreement, FleetManager (+ inner class FleetSnapshot)
  Threads      : VehicleStatusMonitor, OverdueReminderWorker
  UI           : Main
```

## Where each concept is used

| Concept | Where |
|---|---|
| Abstraction / inheritance / polymorphism | `Vehicle`, `Customer` and their subclasses (`calculateRentalCost`, `applyDiscount`) |
| Encapsulation / Java Bean | private fields + getters/setters in every class |
| Composition / association | `RentalAgreement` has a `Vehicle` and a `Customer`; `FleetManager` owns the lists; `Main` holds `FleetManager` and `UserManager` |
| Enums | `FuelType` (cost multiplier), `RentalStatus` |
| Static counters / auto IDs | `VEH-1001`, `CUS-1001`, `AGR-1001` |
| Custom checked exceptions | thrown in `FleetManager.createRentalAgreement`, caught in `Main.createRental` |
| Functional interface + lambda | `RentalEventListener`, lambda registered in the `Main` constructor |
| Interface polymorphism | `Reportable` implemented by `Vehicle` and `Customer`; used in `FleetManager.printAllReportables` |
| Inner class | `FleetManager.FleetSnapshot` (non-static) |
| Multithreading | `VehicleStatusMonitor` and `OverdueReminderWorker` run as daemon threads |
| Synchronized methods | `printFleetStatus`, `checkOverdueAgreements` (and the methods that change the lists) |
| Collections + Streams | `getAvailableVehicles`, `getVehiclesByFuelType`, `getCustomersSortedByName`, `generateRevenueReport` |

## Design notes

- The two daemon threads wake up every 5 seconds. `VehicleStatusMonitor` prints the fleet status line
  and `OverdueReminderWorker` checks for overdue rentals.
- One background check counts as one simulated day (so a 3-day rental becomes overdue after about
  20 seconds). Agreements are not removed when a vehicle is returned, so they keep counting.
- Passwords are stored in plain text because the assignment says not to implement hashing.
- `CorporateCustomer.outstandingBalance` is not increased automatically after a rental (the
  assignment does not ask for it); the credit check uses whatever the balance currently is.
- No role-based access control and no file persistence (out of scope). All data is lost when the program exits.

