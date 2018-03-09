package javacore.coop;

import javacore.coop.model.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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

    private static final String DB_PREFIX = "jdbc:h2:";
    public static final String DEFAULT_DB_URL = "src/db/library";
    private static final String DB_POSTFIX = ";IFEXISTS=TRUE";

    private final String DB_URL;
    private final String USER;
    private final char[] PASSWORD;

    private Database(String user, char[] password, String dbURL) {
        USER = user;
        PASSWORD = password;
        DB_URL = DB_PREFIX + dbURL + DB_POSTFIX;
    }

    private static boolean canConnect(String user, char[] password, String dbURL) {
        String url = DB_PREFIX + dbURL + DB_POSTFIX;
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(url, user, new String(password));
            return true;
        } catch (SQLException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* Can't do anything */ }
            }
        }
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

    /**
     *
     * @return Connection instance
     * @throws SQLException
     */
    public Connection getConnection () throws SQLException {
        return DriverManager.getConnection(DB_URL,USER,PASSWORD.toString());
    }

    /**
     *
     * @param connection
     * @throws SQLException
     */
    public void closeConnection (Connection connection) throws SQLException {
        if (connection == null)
            return;
        try {
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
