package javacore.coop;

import javacore.coop.model.Book;
import javacore.coop.model.BookNotFoundException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Program entry class without UI
 */
public class Main {
    public static void main(String[] args) {
        Database db = null;
        try {
            db = Database.connect("", "".toCharArray(), Database.DEFAULT_DB_URL);
            final String ISBN_OCA = "1118957407";
            final String ISBN_PATTERNS = "9780201485370";
            final String BOOK_NOT_FOUND_FORMAT = "Book with ISBN %d not found";

            List<String> isbns = new ArrayList<String>() {{
                add(ISBN_OCA);
                add(ISBN_PATTERNS);
            }};

            for (String bookISBN : isbns) {
                try {
                    db.addBook(bookISBN);
                    System.out.println("Added book with ISBN " + bookISBN + " to database");
                } catch (BookNotFoundException e) {
                    System.out.println("Book with ISBN " + bookISBN + " not found");
                } catch (Exception e) {
                    System.out.println("Database already contains book with ISBN " + bookISBN);
                }
            }

            System.out.println("\n== Books sorted by ISBN");
            List<Book> books = db.getBooks(Book.ISBN_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("\n== Books sorted by Upload Date");
            books = db.getBooks(Book.ADDED_DATE_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("\n== Book \"OCA\"");
            Book ocaBook;
            try {
                ocaBook = db.getBook(ISBN_OCA);
                System.out.println(ocaBook.toString());
            } catch (SQLException e) {
                System.out.println("Can't get book with ISBN " + ISBN_OCA);
            }

            Random random = new Random();
            int min = 1;
            int max = 100;
            int newNumber = random.nextInt(max - min + 1) + min;
            try {
                db.changeBooksRemained(ISBN_OCA, newNumber);
                System.out.println("\n-> \"OCA\" count updated to " + newNumber);
            } catch (SQLException e) {
                System.out.println("Can't change books amount by ISBN " + ISBN_OCA);
            }

            System.out.println("\n== Now Book \"OCA\"");
            try {
                ocaBook = db.getBook(ISBN_OCA);
                System.out.println(ocaBook.toString());
            } catch (SQLException e) {
                System.out.println("Can't get book with ISBN " + ISBN_OCA);
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
