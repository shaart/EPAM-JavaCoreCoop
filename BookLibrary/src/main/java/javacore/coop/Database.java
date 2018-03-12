package javacore.coop;

import javacore.coop.model.Book;
import javacore.coop.model.BookNotFoundException;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Layout for working with database
 */
public class Database implements AutoCloseable {
    public static final String JDBC_DRIVER = "org.h2.Driver";

    private static final String DB_PREFIX = "jdbc:h2:";
    public static final String DEFAULT_DB_URL = "./db/library";
    private static final String DB_POSTFIX = ";IFEXISTS=TRUE";

    private static final String SELECT_ALL = "SELECT * FROM BOOKS ";
    private static final String SELECT_ALL_ORDER_BY = SELECT_ALL + " order by ";
    private static final String ADD_BOOK = "INSERT INTO BOOKS (TITLE, AUTHOR, ISBN, ADDED_DATE, " +
            " REMAINED_AMOUNT) VALUES (?,?,?,?,?)";
    private static final String FIND_BOOK_ISBN = "SELECT ADDED_DATE,AUTHOR,TITLE,REMAINED_AMOUNT " +
            "FROM BOOKS WHERE ISBN=?";

    private Connection connection = null;

    private Database(Connection connection) {
        this.connection = connection;
    }

    /**
     * Create table "BOOKS" at database if not exists using <code>connection</code>.
     *
     * @param connection Connection to database
     */
    private static void createBooksTableIfNotExists(Connection connection) {
        final String CREATE_BOOKS_TABLE_IF_NOT_EXIST = "CREATE TABLE IF NOT EXISTS BOOKS" +
                "(" +
                "  ID INTEGER DEFAULT 0 AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                "  ISBN VARCHAR(2147483647) NOT NULL," +
                "  ADDED_DATE TIMESTAMP DEFAULT NOW() NOT NULL," +
                "  AUTHOR VARCHAR(2147483647)," +
                "  TITLE VARCHAR(2147483647) NOT NULL," +
                "  REMAINED_AMOUNT INTEGER DEFAULT 0 NOT NULL" +
                "); CREATE UNIQUE INDEX IF NOT EXISTS BOOKS_ISBN_UINDEX ON BOOKS (ISBN)";
        try {
            PreparedStatement prst = connection.prepareStatement(CREATE_BOOKS_TABLE_IF_NOT_EXIST);
            boolean result = prst.execute();
            System.out.println("Table \"BOOKS\" " + (result ? "successfully created" : "found"));
        } catch (SQLException e) {
            // Can't do anything
            e.printStackTrace();
        }
    }

    /**
     * Get connection to database with <code>dbURL</code> by <code>user</code> and <code>password</code>.
     *
     * @param user     Username
     * @param password Password
     * @param dbURL    Database URL
     * @return Connection to database
     * @throws SQLException If something gone wrong
     */
    private static Connection getConnection(String user, char[] password, String dbURL) throws SQLException {
        String url = DB_PREFIX + dbURL + DB_POSTFIX;

        try {
            Class.forName(JDBC_DRIVER);
            return DriverManager.getConnection(url, user, new String(password));
        } catch (SQLException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new SQLException("Database driver not registered");
        }
    }

    /**
     * Connect to database with default DB URL by <code>user</code> and <code>password</code>.
     *
     * @param user     Username
     * @param password Password
     * @return Instance of Database for working with specified database
     * @throws SQLException If something gone wrong (DB is busy, incorrect username and password,
     *                      not registered DB driver)
     */
    public static Database connect(String user, char[] password) throws SQLException {
        try {
            return connect(user, password, DEFAULT_DB_URL);
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Connect to database with <code>dbURL</code> by <code>user</code> and <code>password</code>.
     *
     * @param user     Username
     * @param password Password
     * @param dbURL    Database URL
     * @return Instance of Database for working with specified database
     * @throws SQLException If something gone wrong (DB is busy, incorrect username and password,
     *                      not registered DB driver)
     */
    public static Database connect(String user, char[] password, String dbURL) throws SQLException {
        Connection connection;
        try {
            connection = getConnection(user, password, dbURL);

            createBooksTableIfNotExists(connection);

            return new Database(connection);
        } catch (SQLException e) {
            throw e;
        }
    }

    public void addBook(String isbn) throws BookNotFoundException, SQLException {
        try {
            Book book = Book.request(isbn);

            PreparedStatement prst = connection.prepareStatement(ADD_BOOK);
            prst.setString(1, book.getTitle());
            prst.setString(2, book.getAuthor());
            prst.setString(3, isbn);
            prst.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            prst.setInt(5, 10);

            prst.executeUpdate();

        } catch (SQLException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BookNotFoundException e) {
            throw e;
        }
    }

    public void changeBooksRemained(String isbn, int number) throws SQLException {
        try {
            final String UPDATE_BOOKS_AMOUNT_BY_ISBN = "UPDATE BOOKS SET REMAINED_AMOUNT=?  WHERE ISBN=?";
            PreparedStatement prst = connection.prepareStatement(UPDATE_BOOKS_AMOUNT_BY_ISBN);
            prst.setInt(1, number);
            prst.setString(2, isbn);
            prst.executeUpdate();

        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Get number of books available for rent.
     *
     * @param isbn Book's ISBN
     * @return Number of books available for rent
     */
    @Deprecated
    public int booksRemained(String isbn) {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    public boolean deleteBook(String isbn) {
        throw new UnsupportedOperationException();
    }

    public Book getBook(String isbn) throws SQLException {
        Book book = new Book();
        try {
            PreparedStatement prst = connection.prepareStatement(SELECT_ALL + " WHERE ISBN=?");

            prst.setString(1, isbn);
            ResultSet rs = prst.executeQuery();
            while (rs.next()) {
                book = new Book();
                book.setIsbn(rs.getString(Book.ISBN_COLUMN));
                book.setAuthor(rs.getString(Book.AUTHOR_COLUMN));
                book.setTitle(rs.getString(Book.TITLE_COLUMN));
                book.setAddedDate(rs.getTimestamp(Book.ADDED_DATE_COLUMN));
                book.setRemainedAmount(rs.getInt(Book.REMAINED_AMOUNT));
            }
        } catch (SQLException e) {
            throw e;
        }

        return book;
    }

    /**
     * Get all books from database sorted by <code>sortingColumn</code>.
     *
     * @param sortingColumn Column for order
     * @return List with all stored books
     */
    public List<Book> getBooks(String sortingColumn) throws SQLException {
        Book book = null;
        List<Book> books = new LinkedList<>();

        try {
            PreparedStatement prst = connection.prepareStatement(SELECT_ALL_ORDER_BY + sortingColumn);

            ResultSet rs = prst.executeQuery();
            while (rs.next()) {
                book = new Book();
                book.setIsbn(rs.getString(Book.ISBN_COLUMN));
                book.setAuthor(rs.getString(Book.AUTHOR_COLUMN));
                book.setTitle(rs.getString(Book.TITLE_COLUMN));
                book.setAddedDate(rs.getTimestamp(Book.ADDED_DATE_COLUMN));
                book.setRemainedAmount(rs.getInt(Book.REMAINED_AMOUNT));
                books.add(book);
            }
        } catch (SQLException e) {
            throw e;
        }

        return books;
    }

    @Override
    public void close() throws Exception {
        if (connection != null)
            connection.close();
    }
}
