package logic;

import data.Spot;

import java.util.Scanner;

public class Manager {

    private final Spot[] spots = new Spot[10];

    public void start() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

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
                case "5" -> {
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
        System.out.println("5. Exit");
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
        System.out.println("5. Exit                         - Quit the application.");
    }

    /**
     * Guides the user through creating a new parking reservation.
     */
    private void makeReservation(Scanner scanner) {
        System.out.println();
        System.out.println("=== Make Reservation ===");
        // TODO: implement reservation logic
        System.out.println("[Stub] Reservation creation not yet implemented.");
    }

    /**
     * Guides the user through cancelling an existing reservation.
     */
    private void deleteReservation(Scanner scanner) {
        System.out.println();
        System.out.println("=== Delete Reservation ===");
        // TODO: implement deletion logic
        System.out.println("[Stub] Reservation deletion not yet implemented.");
    }

    /**
     * Displays details for a given parking spot.
     */
    private void showParkingSpotDetails(Scanner scanner) {
        System.out.println();
        System.out.println("=== Parking Spot Details ===");
        // TODO: implement spot detail lookup
        System.out.println("[Stub] Spot detail display not yet implemented.");
    }
}
