/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icculus.chunky.wordsmith.GUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Class used in various models to represent a book
 * 
 * @author chunky
 */
public class BookListItem {
    private final Integer bookid;
    private final String title;
    private final String author;
    private final Integer authorid;

    public BookListItem(Integer bookid, String title, String author, Integer authorid) {
        this.bookid = bookid;
        this.title = title;
        this.author = author;
        this.authorid = authorid;
    }

    public static List<BookListItem> getBookListFromDB(Connection dbConn) throws SQLException {
        
        String sql = "SELECT bookid, author.name AS author, author.authorid AS authorid, book.title AS title"
                + " FROM book INNER JOIN author ON author.authorid=book.authorid"
                + " WHERE NOT author.deleted AND NOT book.deleted";
        
        ArrayList<BookListItem> l = new ArrayList<>();
        try(PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    int bookid = rs.getInt("bookid");
                    String author = rs.getString("author");
                    Integer authorid = rs.getInt("authorid");
                    String title = rs.getString("title");
                    BookListItem item = new BookListItem(bookid, title, author, authorid);
                    l.add(item);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return l;
    }

    public Integer getBookid() {
        return bookid;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Integer getAuthorid() {
        return authorid;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BookListItem)) {
            return false;
        }
        return ((BookListItem)obj).getBookid().equals(bookid);
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s)", title, author);
    }
}