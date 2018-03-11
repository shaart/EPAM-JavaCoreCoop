package javacore.coop;

import javacore.coop.model.Book;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Layout for working with database
 */
public class Database implements AutoCloseable{
    /* TODO
        - add necessary methods
        - realize all methods
     */

    public static final String JDBC_DRIVER = "org.h2.Driver";

    private static final String DB_PREFIX = "jdbc:h2:";
    public static final String DEFAULT_DB_URL = "src/db/library";
    private static final String DB_POSTFIX = ";IFEXISTS=TRUE";

    //private final String DB_URL;
    //private final String USER;
    //private final char[] PASSWORD;

    private Connection connection = null;

    private Database (Connection connection){

            this.connection = connection;
    }


    private static Connection getConnection(String user, char[] password, String dbURL) {
        String url = DB_PREFIX + dbURL + DB_POSTFIX;
        Connection conn = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(url, user, new String(password));
        } catch (SQLException e) {
            System.out.println("Can't connect to current database");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static Database connect(String user, char[] password) {
        return connect(user, password, DEFAULT_DB_URL);
    }


    public static Database connect(String user, char[] password, String dbURL) {
        Connection connection;
        if ((connection = getConnection(user, password, dbURL)) != null) {
            return new Database(connection);
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

    @Override
    public void close() throws Exception {
        if (connection != null)
            connection.close();
    }
}
