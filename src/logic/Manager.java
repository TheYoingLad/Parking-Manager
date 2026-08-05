package logic;

import data.Reservation;
import data.Spot;
import data.TimeInterval;
import data.Type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class Manager {

    private static final String SAVE_FILE = "parking_state.txt";
    private static final String DEFAULT_STATE = "default_state.txt";

    private final Spot[] spots = new Spot[10];
    private final Map<String, List<Reservation>> reservationMap = new HashMap<>();

    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        // initialization
        load();
        save();

        System.out.println("=================================");
        System.out.println("      Parking Manager CLI        ");
        System.out.println("=================================");

        while (running) {
            printMenu();
            System.out.print("Enter your choice: ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> showHelp();
                case "2" -> makeReservation(scanner);
                case "3" -> deleteReservation(scanner);
                case "4" -> showParkingSpotDetails(scanner);
                case "5" -> resetState();
                case "6" -> {
                    running = false;
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("[!] Invalid option. Please try again.\n");
            }
        }

        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Menu
    // -------------------------------------------------------------------------

    private void printMenu() {
        System.out.println();
        System.out.println("1. Help");
        System.out.println("2. Make Reservation");
        System.out.println("3. Delete Reservation");
        System.out.println("4. Show Parking Spot Details");
        System.out.println("5. Reset State");
        System.out.println("6. Exit");
    }

    // -------------------------------------------------------------------------
    // Option handlers
    // -------------------------------------------------------------------------

    /**
     * Displays a short description of every available command.
     */
    private void showHelp() {
        System.out.println();
        System.out.println("=== Help ===");
        System.out.println("1. Help                         - Show this help message.");
        System.out.println("2. Make Reservation             - Reserve an available parking spot.");
        System.out.println("3. Delete Reservation           - Cancel an existing reservation.");
        System.out.println("4. Show Parking Spot Details    - Display information about a specific spot.");
        System.out.println("5. Reset State                  - Reset state to default.");
        System.out.println("6. Exit                         - Quit the application.");
    }

    /**
     * Guides the user through creating a new parking reservation.
     */
    private void makeReservation(Scanner scanner) {
        System.out.println();
        System.out.println("=== Make Reservation ===");
        String input;

        Type type = null;
        while (type == null) {
            System.out.println("Do you require a special parking spot?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            System.out.print("Enter your choice: ");
            input = scanner.nextLine().trim();

            switch (input) {
                case "1" -> {
                    while (type == null) {
                        System.out.println("Please choose from the following types:");
                        System.out.println("1. Disabled");
                        System.out.println("2. Family (at least 3 children)");
                        System.out.print("Enter your choice: ");
                        input = scanner.nextLine().trim();

                        switch (input) {
                            case "1" -> type = Type.DISABLED;
                            case "2" -> type = Type.FAMILY;
                            default -> System.out.println("[!] Invalid option. Please try again.\n");
                        }
                    }
                }
                case "2" -> type = Type.STANDARD;
                default -> System.out.println("[!] Invalid option. Please try again.\n");
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime from = null;
        while (from == null) {
            System.out.print("Enter arrival date (yyyy-MM-dd HH:mm): ");
            input = scanner.nextLine().trim();
            try {
                from = LocalDateTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("[!] Invalid format. Please try again.\n");
            }
        }

        LocalDateTime to = null;
        while (to == null) {
            System.out.print("Enter departure date (yyyy-MM-dd HH:mm): ");
            input = scanner.nextLine().trim();
            try {
                to = LocalDateTime.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("[!] Invalid format. Please try again.\n");
            }
            if (to != null && (to.isBefore(from) || to.isEqual(from))) {
                to = null;
                System.out.println("[!] Invalid date, departure must be later than arrival. Please try again.\n");
            }
        }
        TimeInterval interval = new TimeInterval(from, to);

        String licencePlate = null;
        while (licencePlate == null) {
            System.out.print("Enter licence plate number: ");
            licencePlate = scanner.nextLine().trim().toUpperCase();
            if (licencePlate.isEmpty()) {
                licencePlate = null;
                System.out.println("[!] Licence plate number must be non-empty. Please try again.\n");
            }
        }

        if (reservationMap.containsKey(licencePlate)) {
            Optional<Reservation> existingReservation = reservationMap.get(licencePlate).stream().filter(res -> res.getInterval().overlaps(interval)).findAny();
            if (existingReservation.isPresent()) {
                System.out.println("[!] An existing reservation for this vehicle overlaps with the given interval. Reservation details:");
                System.out.println(existingReservation.get().toString(true, true));
                return;
            }
        }

        Reservation reservation = new Reservation(type, interval, licencePlate);
        int i;
        for (i = 0; i < spots.length; i++) {
            if (spots[i].makeReservation(reservation)) {
                reservation.setSpotNumber(i + 1);
                reservationMap.putIfAbsent(licencePlate, new ArrayList<>());
                reservationMap.get(licencePlate).add(reservation);
                break;
            }
        }

        if (i == spots.length){
            System.out.println("[!] All parking spots are occupied for the interval " + from + " - " + to);
            return;
        }

        save();
        System.out.println("[✓] Reservation successful! Details:");
        System.out.println(reservation.toString(true, true));
    }

    /**
     * Guides the user through cancelling an existing reservation.
     */
    private void deleteReservation(Scanner scanner) {
        System.out.println();
        System.out.println("=== Delete Reservation ===");

        String licencePlate = null;
        while (licencePlate == null) {
            System.out.print("Enter licence plate number: ");
            licencePlate = scanner.nextLine().trim().toUpperCase();
            if (licencePlate.isEmpty()) {
                licencePlate = null;
                System.out.println("[!] Licence plate number must be non-empty. Please try again.\n");
            }
        }

        if (!reservationMap.containsKey(licencePlate)) {
            System.out.println("[-] No reservation found for this vehicle.");
            return;
        }

        List<Reservation> reservations = reservationMap.get(licencePlate);
        int spot = 0;
        while (spot == 0) {
            System.out.println("Reservations for this vehicle:");
            for (int i = 0; i < reservations.size(); i++) {
                System.out.println(i + 1 + ".");
                System.out.println(reservations.get(i).toString(true, false));
            }

            System.out.print("Enter reservation number (1 - " + reservations.size() + "): ");
            String input = scanner.nextLine().trim();

            try {
                spot = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number. Please try again.\n");
                continue;
            }

            if (spot < 1 || spot > reservations.size()) {
                spot = 0;
                System.out.println("[!] Invalid option. Please try again.\n");
            }
        }

        Reservation toDelete = reservations.get(spot - 1);
        reservations.remove(spot - 1);
        if (reservations.isEmpty()) reservationMap.remove(licencePlate);
        spots[toDelete.getSpotNumber() - 1].deleteReservation(toDelete);
        save();
        System.out.println("[✓] Reservation successfully deleted.");
    }

    /**
     * Displays details for a given parking spot.
     */
    private void showParkingSpotDetails(Scanner scanner) {
        System.out.println();
        System.out.println("=== Parking Spot Details ===");

        int spot = 0;
        while (spot == 0) {
            System.out.print("Enter parking spot number (1 - 10): ");
            String input = scanner.nextLine().trim();

            try {
                spot = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number. Please try again.\n");
                continue;
            }

            if (spot < 1 || spot > 10) {
                spot = 0;
                System.out.println("[!] Invalid option. Please try again.\n");
            }
        }

        System.out.println("[✓] Details of spot #" + spot + ":");
        System.out.println(spots[spot - 1]);
    }

    /**
     * Resets the current state to the default defined in {@value #DEFAULT_STATE}.
     */
    private void resetState() {
        try {
            Files.copy(Path.of(DEFAULT_STATE), Path.of(SAVE_FILE));
            load();
            save();
            System.out.println("[✓] State reset successful.");
        } catch (IOException e) {
            System.out.println("[!] Failed to reset state: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Save / Load
    // -------------------------------------------------------------------------

    /**
     * Saves the current state to {@value #SAVE_FILE}.
     */
    private void save() {
        try {
            Serializer.save(spots, SAVE_FILE);
        } catch (IOException e) {
            System.out.println("[!] Failed to save state: " + e.getMessage());
        }
    }

    /**
     * Loads state from {@value #SAVE_FILE}.
     */
    private void load() {
        try {
            System.arraycopy(Serializer.load(SAVE_FILE), 0, spots, 0, spots.length);
        } catch (Exception e1) {
            System.out.println("[!] An error occurred during loading: " + e1.getMessage());
            System.out.println("Loading default state...");
            try {
                System.arraycopy(Serializer.load(DEFAULT_STATE), 0, spots, 0, spots.length);
            } catch (Exception e2) {
                System.out.println("[!] An error occurred during loading of the default state: " + e2.getMessage());
                System.out.println("Loading empty state...");

                for (int i = 0; i < spots.length; i++) {
                    spots[i] = new Spot(Type.STANDARD);
                }
                spots[0] = new Spot(Type.DISABLED);
            }
        }

        // rebuild the reservation map from the spot data
        reservationMap.clear();
        for (Spot spot : spots) {
            for (Reservation r : spot.reservations()) {
                reservationMap
                        .computeIfAbsent(r.getLicencePlate(), _ -> new ArrayList<>())
                        .add(r);
            }
        }
    }
}
