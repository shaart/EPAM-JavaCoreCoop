package javacore.coop;

import javacore.coop.model.Book;

import java.util.List;
import java.util.Scanner;

/**
 * Program's entry class for interaction with user
 */
public class UserInterface {
    static List<Book> result = null;

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

            System.out.print("Enter password: ");
            /* TODO
                Here must be reading password from console and storing at variable (Which type is more safe?)
            */
            password = new char[0];
            password = scanner.nextLine().toCharArray();

            System.out.println("Enter url to database. Leave this blank to use default connection.");
            databaseURL = scanner.nextLine();
            if (databaseURL.length() == 0) {
                databaseURL = Database.DEFAULT_DB_URL;
            }

            if ((db = Database.connect(username, password, databaseURL)) != null) {
                connected = true;
            }
        }

        boolean working = true;
        while (working) {
            // TODO
            // get user's input
            String input = scanner.nextLine();
            // process it
            // work with database

            // if user say "exit"
            working = false;
        }
    }
}
