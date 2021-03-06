/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icculus.chunky.wordsmith;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import javax.swing.JOptionPane;
import org.sqlite.SQLiteConfig;

/**
 *
 * @author chunky
 */
public class DBManager {
    /**
     * Open the specified database, and create structures if they don't already exist
     * 
     * @param dbPath
     * @return
     * @throws SQLException 
     */
    public Connection openDB(String dbPath) throws SQLException {
        Connection dbConn;
        
        if(!dbPath.startsWith(":")) {
            File parentDir = new File(new File(dbPath).getAbsoluteFile().getParent());
            if(!parentDir.exists()) {
                boolean mkdirs = parentDir.mkdirs();
                if(!mkdirs) {
                    JOptionPane.showMessageDialog(null, "Couldn't create directory for database");
                }
            }
        }
        
        SQLiteConfig sqlcf = new SQLiteConfig();
        sqlcf.setJournalMode(SQLiteConfig.JournalMode.DELETE);
        sqlcf.enforceForeignKeys(true);
        sqlcf.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
        
        dbConn = DriverManager.getConnection("jdbc:sqlite:" + dbPath, sqlcf.toProperties());
        dbConn.setAutoCommit(false);
        
        try(Statement stmt = dbConn.createStatement()) {
            stmt.executeUpdate("PRAGMA threads=4");
        }
        createDBStructures(dbConn);
        return dbConn;
    }
    
    public void closeDB(Connection dbConn) throws SQLException {
        dbConn.close();
    }
    
    public void createDummyData(Connection dbConn) throws SQLException {
        final String[] insertSQL = new String[] {
            "DELETE FROM author WHERE 1",
            
            "INSERT OR IGNORE INTO author (name) VALUES ('Chunky Kibbles')",
            "INSERT OR IGNORE INTO author (name) VALUES ('Gary Briggs')",
            
            "INSERT OR IGNORE INTO book (authorid, title, description) VALUES ("
                + "  (SELECT authorid FROM author WHERE name='Chunky Kibbles'), "
                + "   'Kitten Fury', 'In which a Kitten Destroys the World'"
                + ")",
            "INSERT OR IGNORE INTO book (authorid, title, description) VALUES ("
                + "  (SELECT authorid FROM author WHERE name='Chunky Kibbles'), "
                + "   'Raptor Fury', 'In which a Raptor Destroys the World'"
                + ")",
            "INSERT OR IGNORE INTO book (authorid, title, description) VALUES ("
                + "  (SELECT authorid FROM author WHERE name='Chunky Kibbles'), "
                + "   'Puppy Fury', 'In which a Puppy Destroys the World'"
                + ")",
            
            "INSERT OR IGNORE INTO book (authorid, title, description) VALUES ("
                + "  (SELECT authorid FROM author WHERE name='Gary Briggs'), "
                + "   'Husband of a Writer', 'A completely unnecessary book that adds nothing to anything'"
                + ")",
            
        };
        try(Statement stmt = dbConn.createStatement()) {
            for(String sql : insertSQL) {
                try {
                    stmt.executeUpdate(sql);
                } catch(SQLException ex) {
                    System.out.println(sql);
                    ex.printStackTrace();
                }
            }
            dbConn.commit();
        }
        
        Random rng = new Random(42l);
        
        String wordCountSQL = "INSERT INTO wordcount (bookid, delta, adddate) VALUES "
                + " ((SELECT bookid FROM book WHERE title=?), ?, ?)";
        
        try(PreparedStatement stmt = dbConn.prepareStatement(wordCountSQL)) {
            for(String bookName : new String[] { "Kitten Fury", "Husband of a Writer", "Raptor Fury" } ) {
                stmt.setString(1, bookName);
                
                int n_days_delta = 365 - rng.nextInt(180);
                int n_samples = 10 + rng.nextInt(100);
                LocalDateTime currentWorkDay = LocalDateTime.now().minus(n_days_delta, ChronoUnit.DAYS);
                LocalDateTime today = LocalDateTime.now();
                
                for(int i = 0; i < n_samples && currentWorkDay.isBefore(today); i++) {
                    int todaysProgress = 20 + rng.nextInt(4000);
                    stmt.setInt(2, todaysProgress);
                    stmt.setString(3, currentWorkDay.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    stmt.executeUpdate();
                    int skipDays = Math.max(1, rng.nextInt(8)-2);
                    currentWorkDay = currentWorkDay.plusDays(skipDays);
                }
            }
            dbConn.commit();
        }
    }
    
    protected void createDBStructures(Connection dbConn) throws SQLException {
        final String[] createSQL = new String[] {
            "CREATE TABLE IF NOT EXISTS author (authorid INTEGER PRIMARY KEY,"
                + " name TEXT NOT NULL,"
                + " createdate INTEGER DEFAULT CURRENT_TIMESTAMP,"
                + " deleted INTEGER DEFAULT 0,"
                + " lastselected INTEGER DEFAULT 0,"
                + " UNIQUE(name)"
                + ")",
            
            "CREATE TABLE IF NOT EXISTS book (bookid INTEGER PRIMARY KEY,"
                + " authorid INTEGER NOT NULL REFERENCES author(authorid) ON DELETE CASCADE,"
                + " title TEXT NOT NULL,"
                + " description TEXT,"
                + " createdate INTEGER DEFAULT CURRENT_TIMESTAMP,"
                + " deleted INTEGER DEFAULT 0,"
                + " lastselected INTEGER DEFAULT 0, "
                + " UNIQUE(authorid, title)"
                + ")",
            
            "CREATE TABLE IF NOT EXISTS wordcount (wordcountid INTEGER PRIMARY KEY,"
                + " bookid INTEGER NOT NULL REFERENCES book(bookid) ON DELETE CASCADE,"
                + " delta INTEGER NOT NULL,"
                + " adddate INTEGER DEFAULT CURRENT_TIMESTAMP,"
                + " deleted INTEGER DEFAULT 0"
                + ")",
            
            "CREATE INDEX IF NOT EXISTS idx_wordcount_book_date ON wordcount(bookid, adddate)",
            
            "CREATE INDEX IF NOT EXISTS idx_wordcount_date_book ON wordcount(adddate, bookid)",
            
            "CREATE TABLE IF NOT EXISTS target (targetid INTEGER PRIMARY KEY,"
                + " bookid INTEGER NOT NULL REFERENCES book(bookid) ON DELETE CASCADE,"
                + " wordcount INTEGER NOT NULL,"
                + " adddate INTEGER DEFAULT CURRENT_TIMESTAMP,"
                + " targetdate DATETIME,"
                + " deleted INTEGER DEFAULT 0,"
                + " UNIQUE(bookid, targetdate)"
                + ")"
        };
        
        try(Statement stmt = dbConn.createStatement()) {
            boolean create_exception = false;
            for(String sql : createSQL) {
                try {
                    stmt.executeUpdate(sql);
                } catch(SQLException ex) {
                    System.out.println(sql);
                    ex.printStackTrace();
                    create_exception = true;
                }
            }
            dbConn.commit();
        }
    }
}
