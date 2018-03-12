package javacore.coop;

import javacore.coop.model.Book;

import java.util.ArrayList;
import java.util.List;


 //* Program entry class without UI

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
                db.addBook(bookISBN);
            }

            System.out.println("Books sorted by ISBN: ");
            List<Book> books = db.getBooks(Book.ISBN_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("Books sorted by upload date: ");
            books = db.getBooks(Book.ADDED_DATE_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("OCA book: ");
            Book ocaBook = db.getBook(ISBN_OCA);
            System.out.println(ocaBook.toString());

            int newNumber = 15;
            db.changeBooksRemained(ISBN_OCA, newNumber);
            ocaBook = db.getBook(ISBN_OCA);
            System.out.println("OCA count updated to " + newNumber);
            System.out.println(ocaBook.toString());

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
