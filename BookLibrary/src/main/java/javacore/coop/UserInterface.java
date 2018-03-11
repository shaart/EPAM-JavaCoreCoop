package javacore.coop;

import java.util.Scanner;

/**
 * Program's entry class for interaction with user
 */
public class UserInterface {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Database db = null;

        boolean connected = false;
        while (!connected) {
            String username;
            char[] password;
            String databaseURL;

            // Ask for username and password
            System.out.print("Enter username: ");
            username = scanner.nextLine();
            password = ConsoleUtil.readPassword("Enter password: ", scanner);
            if (password == null) {
                // Nothing to close before exit
                System.out.println("Canceled. Program will be closed.");
                return;
            }

            System.out.println("Enter URL to database. [Leave this blank to use default connection (" +
                    Database.DEFAULT_DB_URL + ")]");
            System.out.print("Database URL: ");
            databaseURL = scanner.nextLine();
            if (databaseURL.isEmpty()) {
                databaseURL = Database.DEFAULT_DB_URL;
            }

            if ((db = Database.connect(username, password, databaseURL)) != null) {
                connected = true;
                System.out.println("~ Connection with database established successfully.");
            } else {
                System.out.println("[!] Can't connect to database with this " +
                        "url using these username and password.");
                boolean getAnswer = false;
                while (!getAnswer) {
                    System.out.print("Try again? (Y/N)  ");
                    String answer = scanner.next();
                    if (answer.equalsIgnoreCase("n")
                            || answer.equalsIgnoreCase("no")
                            || answer.equalsIgnoreCase("exit")) {
                        return;
                    } else if (answer.equalsIgnoreCase("y")
                            || answer.equalsIgnoreCase("yes")) {
                        getAnswer = true;
                        scanner.nextLine();
                    }
                }
            }
        }
        if (db != null) {
            try {
                db.close();
            } catch (Exception e) {
            }
        }

        // TODO Here may be UI for working with DB
        throw new UnsupportedOperationException();
    }
}

