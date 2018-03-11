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
import java.time.LocalDateTime;

/**
 * Book's model
 */
public class Book {

    private static final String TABLE_NAME = "books";
    private static final String ID_COLUMN = "id";
    private static final String AUTHOR_ID_COLUMN = "author_id";
    private static final String TITLE_COLUMN = "title";
    private static final String YEAR_COLUMN = "year";
    private static final String UPLOAD_DATE_COLUMN = "upload_date";
    private static final String IS_AVALIBLE_COLUMN = "is_available";


    private int id;
    private String author_id;
    private String title;
    private int year;
    private LocalDateTime uploadDate;
    private boolean isAvailable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", author_id='" + author_id + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", uploadDate=" + uploadDate +
                ", isAvailable=" + isAvailable +
                '}';
    }


    public Book() {
    }

    public Book(int id, String author_id, String title, int year, LocalDateTime uploadDate, boolean isAvailable) {
        this.id = id;
        this.author_id = author_id;
        this.title = title;
        this.year = year;
        this.uploadDate = uploadDate;
        this.isAvailable = isAvailable;
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
    public static Book request(long ISBN) throws BookNotFoundException, IOException {
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
        int year = Integer.valueOf(publishDate.substring(0, separatorIndex));

        // TODO Fill all necessary fields of the book
        Book foundBook = new Book();
        foundBook.setTitle(title);
        foundBook.setYear(year);
        foundBook.setAuthor_id(authors);
//        foundBook.setUploadDate(LocalDateTime.now());

        return foundBook;
    }
}

