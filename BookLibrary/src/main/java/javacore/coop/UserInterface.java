package javacore.coop;

import javacore.coop.model.Book;

import java.util.List;
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

        boolean working = true;
        // TODO: Menu
        final String MENU_MESSAGE = "== MENU == \n1. Add book\n" +
                "--------------\n0. Exit\n";
        final int OPTION_ADD_BOOK = 1;
        final int OPTION_EXIT = 0;

        List<Book> queryResult = null;
        while (working) {
            System.out.println(MENU_MESSAGE);
            System.out.print("Option: ");
            int userOption = scanner.nextInt();

            // TODO: Working with database
            switch (userOption) {
                case OPTION_ADD_BOOK:
                    Book newBook = createBookUI(scanner);
                    if (newBook != null) {
                        db.addBook(newBook);
                    }
                    break;
                case OPTION_EXIT:
                    working = false;
            }
        }
        scanner.close();
    }

    private static Book createBookUI(Scanner scanner) {
        if (scanner == null) scanner = new Scanner(System.in);


        final int OPTION_AUTHOR = 1;
        final int OPTION_TITLE = 2;
        final int OPTION_YEAR = 3;
        final int OPTION_SHOW_CURRENT_BOOK = 8;
        final int OPTION_CONFIRM = 9;
        final int OPTION_CANCEL = 0;
        final String BOOK_MENU = "== New book ==\n1. Set author\n2. Set title\n3. Set year\n" +
                "8. Show current book\n--------------\n9. Confirm\n0. Cancel\n";

        int userOption = 1;
        String author = "";
        String title = "";
        Integer year = null;

        System.out.println(BOOK_MENU);
        while (userOption != OPTION_CONFIRM && userOption != OPTION_CANCEL) {
            System.out.print("Option: ");
            userOption = scanner.nextInt();
            scanner.nextLine();
            switch (userOption) {
                case OPTION_AUTHOR:
                    System.out.print("Enter Author: ");
                    author = scanner.nextLine();
                    break;
                case OPTION_TITLE:
                    System.out.print("Enter Title: ");
                    title = scanner.nextLine();
                    break;
                case OPTION_YEAR:
                    System.out.print("Enter Year: ");
                    try {
                        year = scanner.nextInt();
                    } catch (Exception e) {
                        System.out.println("Incorrect year");
                    }
                    scanner.nextLine();
                    break;
                case OPTION_SHOW_CURRENT_BOOK:
                    System.out.format("Author: %s\nTitle: %s\nYear: %d\n", author, title, year);
                    break;
                case OPTION_CONFIRM:
                    if (author.isEmpty()) {
                        System.out.println("Author is empty");
                        userOption = -1;
                    }
                    if (title.isEmpty()) {
                        System.out.println("Title is empty");
                        userOption = -1;
                    }
                    if (year == null) {
                        System.out.println("Year is empty");
                        userOption = -1;
                    }
                    break;
                case OPTION_CANCEL:
                    break;
            }
        }
        if (userOption == OPTION_CONFIRM) {
            return new Book(author, title, year);
        } else {
            return null;
        }
    }
}

