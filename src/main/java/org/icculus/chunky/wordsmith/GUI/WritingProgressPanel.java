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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.converter.LocalDateTimeStringConverter;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author chunky
 */
public class WritingProgressPanel extends javax.swing.JPanel {

    MainWindow mw;
    JFreeChart chart;
    DefaultCategoryDataset dataset = null;
    
    /**
     * Creates new form ProgressPanel
     */
    public WritingProgressPanel(MainWindow mw) {
        this.mw = mw;
        initComponents();
        populateBookList();
        createChart();
        bookSelectionList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateChart();
            }
        });
    }

    private void createChart() {
        dataset = createProgressDataset();
        chart = ChartFactory.createStackedBarChart("Progress", "Words", "Date", dataset,
                PlotOrientation.VERTICAL, true, true, true);

        ChartPanel cp = new org.jfree.chart.ChartPanel(chart);

        cp.setMaximumDrawHeight(5000);
        cp.setMinimumDrawHeight(10);
        cp.setMaximumDrawWidth(5000);
        cp.setMinimumDrawWidth(10);
        chartPanel.add(cp);
    }
    
    public void updateChart() {
        createProgressDataset();
    }
    
    public void populateBookList() {
        Connection dbConn = mw.getDbConn();
        String sql = "SELECT bookid, author.name AS author, book.title AS title"
                + " FROM book INNER JOIN author ON author.authorid=book.authorid";
        
        DefaultListModel<BookListItem> m = new DefaultListModel<>();
        try(PreparedStatement stmt = dbConn.prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    int bookid = rs.getInt("bookid");
                    String author = rs.getString("author");
                    String title = rs.getString("title");
                    BookListItem item = new BookListItem(bookid, author, title);
                    m.addElement(item);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        bookSelectionList.setModel(m);
        bookSelectionList.setSelectedIndex(0);
    }
    
    public DefaultCategoryDataset createProgressDataset() {
        if(null == dataset) {
            dataset = new DefaultCategoryDataset();
        }
        dataset.clear();
        
        String sql = "WITH formattedDay AS (SELECT bookid, STRFTIME('%Y-%m-%d', adddate) AS day, delta FROM wordcount) "
                + "  SELECT A.bookid AS bookid, A.day AS day, SUM(B.delta) AS accumWords, A.delta AS delta "
                + "   FROM formattedDay A "
                + "   INNER JOIN formattedDay B ON A.bookid = B.bookid AND A.day>=B.day "
                + "   GROUP BY A.bookid, A.day "
                + "   ORDER BY A.bookid, A.day ";
        
        DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        Set<Integer> selectedBooks = new HashSet<>();
        int[] selectedIndices = bookSelectionList.getSelectedIndices();
        ListModel<BookListItem> bookListModel = bookSelectionList.getModel();
        for(int i : selectedIndices) {
            selectedBooks.add(bookListModel.getElementAt(i).getBookid());
        }
        
        try(PreparedStatement stmt = mw.getDbConn().prepareStatement(sql)) {
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Integer bookid = rs.getInt("bookid");
                    if(!selectedBooks.contains(bookid)) {
                        continue;
                    }
                    String day = rs.getString("day");
                    Integer delta = rs.getInt("delta");
                    Integer accumWords = rs.getInt("accumWords");
                    Date parsedDate = isoDateFormat.parse(day);
                    dataset.addValue(accumWords, bookid, parsedDate);
                    
                }
            }
        } catch (SQLException|ParseException ex) {
            ex.printStackTrace();
        }
        
        return dataset;
    }
    
    private static class BookListItem {
        int bookid;
        String author;
        String title;

        public BookListItem(int bookid, String author, String title) {
            this.bookid = bookid;
            this.author = author;
            this.title = title;
        }
        
        public int getBookid() {
            return bookid;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%s)", title, author);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jScrollPane1 = new javax.swing.JScrollPane();
        bookSelectionList = new javax.swing.JList<>();
        chartPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        bookSelectionList.setModel(new DefaultListModel<BookListItem>
            ());
        jScrollPane1.setViewportView(bookSelectionList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(jScrollPane1, gridBagConstraints);

        chartPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 5.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        add(chartPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<BookListItem> bookSelectionList;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
