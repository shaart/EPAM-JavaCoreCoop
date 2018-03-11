package javacore.coop;

import javacore.coop.model.Book;

import java.util.ArrayList;
import java.util.List;

/**
 * Program entry class without UI
 */
public class Main {
    public static void main(String[] args) {
        Database db = null;
        try {
            db = Database.connect("", "".toCharArray(), Database.DEFAULT_DB_URL);
            if (db == null) {
                throw new Exception("Can't connect to database");
            }
            final long ISBN_OCA = 1118957407L;
            final long ISBN_PATTERNS = 9780201485370L;
            final String BOOK_NOT_FOUND_FORMAT = "Book with ISBN %d not found";

            List<Long> isbns = new ArrayList<Long>() {{
                add(ISBN_OCA);
                add(ISBN_PATTERNS);
            }};

            for (Long bookISBN : isbns) {
                db.addBook(bookISBN);
            }

            System.out.println("Books sorted by ISBN: ");
            List<Book> books = db.getBooks(Book.ISBN_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("Books sorted by upload date: ");
            books = db.getBooks(Book.UPLOAD_DATE_COLUMN);
            for (Book book : books) {
                System.out.println(book.toString());
            }

            System.out.println("OCA book: ");
            Book ocaBook = db.getBook(ISBN_OCA);
            System.out.println(ocaBook.toString());

            int newNumber = 15;
            db.updateBooksNumber(ISBN_OCA, newNumber);
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
