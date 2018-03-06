package javacore.coop;

import javacore.coop.model.Book;
import java.util.List;

/**
 * Layout for working with database
 */
public class Database {
    /* TODO
        - add necessary methods
        - realize all methods
     */
    public static final String JDBC_DRIVER = "org.h2.Driver";
    public static final String DEFAULT_DB_URL = "jdbc:h2:~db/library";
    private final String DB_URL;

    private final String USER;
    private final char[] PASSWORD;

    private Database(String user, char[] password, String dbURL) {
        USER = user;
        PASSWORD = password;
        DB_URL = dbURL;
    }

    private static boolean canConnect(String user, char[] password, String dbURL) {
        throw new UnsupportedOperationException();
    }

    public static Database connect(String user, char[] password) {
        return connect(user, password, DEFAULT_DB_URL);
    }

    public static Database connect(String user, char[] password, String dbURL) {
        if (canConnect(user, password, dbURL)) {
            return new Database(user, password, dbURL);
        } else {
            return null;
        }
    }

    public boolean addBook(Book newBook) {
        throw new UnsupportedOperationException();
    }

    public int booksRemained(Book book) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get number of books available for rent.
     *
     * @param bookId Id of book
     * @return Number of books available for rent
     */
    public int booksRemained(int bookId) {
        throw new UnsupportedOperationException();
    }

    public boolean deleteBook(int id) {
        throw new UnsupportedOperationException();
    }

    public Book getBook(int id) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get all books from database.
     *
     * @return List with all stored books
     */
    public List<Book> getBooks() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get list of books by <code>page</code> number with page's <code>size</code>.
     *
     * @param page Number of page
     * @param size Size of page
     * @return Specified list of books
     */
    public List<Book> getBooks(int page, int size) {
        throw new UnsupportedOperationException();
    }

}
