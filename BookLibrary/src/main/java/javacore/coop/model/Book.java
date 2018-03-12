package javacore.coop.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;

/**
 * Book's model
 */
public class Book {

    public static final String TABLE_NAME = "books";
    public static final String AUTHOR_COLUMN = "AUTHOR";
    public static final String TITLE_COLUMN = "TITLE";
    public static final String ADDED_DATE_COLUMN = "added_date";
    public static final String REMAINED_AMOUNT = "remained_amount";
    public static final String ISBN_COLUMN ="ISBN";


    private String isbn;
    private String author;
    private String title;
    private Timestamp addedDate;
    private int remainedAmount;

    @Override
    public String toString() {
        return "Book{" +
                "isbn=" + isbn +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", addedDate=" + addedDate +
                ", remainedAmount=" + remainedAmount +
                '}';
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Timestamp addedDate) {
        this.addedDate = addedDate;
    }

    public int getRemainedAmount() {
        return remainedAmount;
    }

    public void setRemainedAmount(int remainedAmount) {
        this.remainedAmount = remainedAmount;
    }

    public Book() {
    }

    /**
     * Get info about book from remoted store by book's ISBN.<br>
     * Usage sample:<br>
     * <pre>
     * {@code try {
     *     long ISBN = 1118957407L;
     *     Book book = Book.request(ISBN);
     *     System.out.println(book);
     *   } catch (BookNotFoundException e) {
     *     System.out.println("Book not found");
     *   } catch (IOException e) {
     *     System.out.println("Can't connect to server");
     *   }}</pre>
     *
     * @param ISBN Book's ISBN
     * @return Found book
     * @throws BookNotFoundException if book by this ISBN not found
     * @throws IOException           if can't connect to remote server
     */
    public static Book request(String ISBN) throws BookNotFoundException, IOException {
        final String ISBN_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
        String response;

        BufferedReader responseStream = null;
        HttpURLConnection connectionRequest = null;
        try {
            URL obj = new URL(ISBN_API_URL + ISBN);
            connectionRequest = (HttpURLConnection) obj.openConnection();
            connectionRequest.setRequestMethod("GET");
            connectionRequest.setRequestProperty("Content-Type", "application/json");

            // Send request
            int responseCode = connectionRequest.getResponseCode();

            // Response
            responseStream = new BufferedReader(new InputStreamReader(connectionRequest.getInputStream()));
            String responseLine;
            StringBuffer content = new StringBuffer();
            while ((responseLine = responseStream.readLine()) != null) {
                content.append(responseLine);
            }
            response = content.toString();
        } catch (IOException e) {
            throw e;
        } finally {
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    /* Nothing to do */
                }
            }
            if (connectionRequest != null) {
                connectionRequest.disconnect();
            }
        }

        // Check response and get books from JSON
        JsonElement parsedResponse = new JsonParser().parse(response);
        if (parsedResponse.isJsonNull()) {
            throw new BookNotFoundException();
        }
        JsonObject responseJsonObject = parsedResponse.getAsJsonObject();
        final String RESPONSE_BOOKS_ARRAY = "items";
        JsonArray responseBooksArray = responseJsonObject
                .getAsJsonArray(RESPONSE_BOOKS_ARRAY);
        if (responseBooksArray == null) {
            throw new BookNotFoundException();
        }

        // Parse response books array
        JsonObject booksInfo = responseBooksArray.get(0).getAsJsonObject()
                .getAsJsonObject("volumeInfo");
        final String VOLUMEINFO_TITLE = "title";
        final String VOLUMEINFO_PUBLISH_DATE = "publishedDate";
        final String VOLUMEINFO_AUTHORS = "authors";
        String title = booksInfo.has(VOLUMEINFO_TITLE) ? booksInfo.get(VOLUMEINFO_TITLE).getAsString() : "";
        String authors;
        if (booksInfo.has(VOLUMEINFO_AUTHORS)) {
            JsonArray authorsArray = booksInfo.get(VOLUMEINFO_AUTHORS).getAsJsonArray();
            authors = "";
            for (JsonElement author : authorsArray) {
                authors += author.getAsString() + ", ";
            }
            if (authors.length() > 0) {
                authors = authors.substring(0, authors.lastIndexOf(", "));
            }
        } else {
            authors = "";
        }
        String publishDate = booksInfo.has(VOLUMEINFO_PUBLISH_DATE) ? booksInfo.get(VOLUMEINFO_PUBLISH_DATE).getAsString() : "";
        final int separatorIndex = publishDate.contains("-") ? publishDate.indexOf("-") : 0;

        // TODO Fill all necessary fields of the book
        Book foundBook = new Book();
        foundBook.setTitle(title);
        foundBook.setAuthor(authors);

        return foundBook;
    }
}

