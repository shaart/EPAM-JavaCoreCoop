package javacore.coop;

import javacore.coop.model.Book;
import javacore.coop.model.BookNotFoundException;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.LinkedList;
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
    public static final String DEFAULT_DB_URL = "./db/library";
    private static final String DB_POSTFIX = ";IFEXISTS=TRUE";

    private static final String GET_ALL = "SELECT * FROM BOOKS";
    String SORT_BY_SQL = GET_ALL + " order by "+"?";
    String ADD_BOOK = "INSERT INTO BOOKS (TITLE, AUTHOR, ISBN, ADDED_DATE, REMAINED_AMOUNT) VALUES (?,?,?,?,?)";
    String FIND_BOOK_ISBN = "SELECT ADDED_DATE,AUTHOR,TITLE,REMAINED_AMOUNT FROM BOOKS WHERE ISBN=?";

    //private final String DB_URL;
    //private final String USER;
    //private final char[] PASSWORD;

    public Connection connection = null;

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


    public void addBook (String isbn){
        try {
            Book book = Book.request(isbn);

            PreparedStatement prst = connection.prepareStatement(ADD_BOOK);
            prst.setString(1,book.getTitle());
            prst.setString(2,book.getAuthor());
            prst.setString(3, isbn);
            prst.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            prst.setInt(5,10);

            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BookNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void changeBooksRemained(String isbn, int number) {

        try {
            PreparedStatement prst = connection.prepareStatement("UPDATE BOOKS SET REMAINED_AMOUNT=" + number +" WHERE ISBN=" + isbn);
            prst.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }

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

    public Book getBook(String isbn) {
        Book book = new Book();
        try {
            PreparedStatement prst = connection.prepareStatement("SELECT * FROM BOOKS WHERE ISBN=" + isbn);
            //prst.setString(1, isbn);
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
            e.printStackTrace();
        }
        return book;
    }

    /**
     * Get all books from database.
     *
     * @return List with all stored books
     */
    public List<Book> getBooks(String column) {
        Book book = null;
        List<Book> books = new LinkedList<>();

        try {

            PreparedStatement prst = connection.prepareStatement("select * from books order by "+ column);
            //prst.setString(1,column.toUpperCase());

            ResultSet rs = prst.executeQuery();

            //System.out.println(SORT_BY_SQL);

            while (rs.next()){
                book = new Book();
                book.setIsbn(rs.getString(Book.ISBN_COLUMN));
                book.setAuthor(rs.getString(Book.AUTHOR_COLUMN));
                book.setTitle(rs.getString(Book.TITLE_COLUMN));
                book.setAddedDate(rs.getTimestamp(Book.ADDED_DATE_COLUMN));
                book.setRemainedAmount(rs.getInt(Book.REMAINED_AMOUNT));
                books.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return books;
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

    public void updateBooksNumber(long isbn_oca, int newNumber) {
    }
}
