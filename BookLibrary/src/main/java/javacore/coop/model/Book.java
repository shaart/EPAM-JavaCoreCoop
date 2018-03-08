package javacore.coop.model;


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
}
