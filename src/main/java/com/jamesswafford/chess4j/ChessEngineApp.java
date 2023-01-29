package com.jamesswafford.chess4j;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jamesswafford.chess4j.book.AbstractOpeningBook;
import com.jamesswafford.chess4j.book.OpeningBookSQLiteImpl;

public final class ChessEngineApp {

    private static final Log LOGGER = LogFactory.getLog(ChessEngineApp.class);

    private static AbstractOpeningBook openingBook;
    private static String bookPath = null;
    private static String testSuiteFile = null;
    private static int testSuiteTime = 5; // default to five seconds

    private ChessEngineApp() {}

    public static AbstractOpeningBook getOpeningBook() {
        return openingBook;
    }

    private static void initBook() throws Exception {
        LOGGER.info("# initializing book: " + bookPath);

        File bookFile = new File(bookPath);
        boolean initBook = !bookFile.exists();

        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:" + bookPath);
        OpeningBookSQLiteImpl sqlOpeningBook = new OpeningBookSQLiteImpl(conn);

        if (initBook) {
            LOGGER.info("# could not find " + bookPath + ", creating...");
            sqlOpeningBook.initializeBook();
            LOGGER.info("# ... finished.");
        } else {
            sqlOpeningBook.loadZobristKeys();
        }

        openingBook = sqlOpeningBook;

        LOGGER.info("# book initialization complete. " + openingBook.getTotalMoveCount() + " moves in book file.");
    }
}
