CREATE TABLE BOOKS
(
  ISBN            VARCHAR(2147483647)                                                                                                     NOT NULL,
  ID              INTEGER DEFAULT 0 AUTO_INCREMENT PRIMARY KEY NOT NULL,
  ADDED_DATE      TIMESTAMP DEFAULT NOW()                                                                                                 NOT NULL,
  AUTHOR          VARCHAR(2147483647),
  TITLE           VARCHAR(2147483647)                                                                                                     NOT NULL,
  REMAINED_AMOUNT INTEGER DEFAULT 0                                                                                                       NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS BOOKS_ISBN_UINDEX
  ON BOOKS (ISBN)
