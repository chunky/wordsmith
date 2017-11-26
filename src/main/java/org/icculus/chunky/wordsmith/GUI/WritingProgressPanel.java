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
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;

/**
 *
 * @author chunky
 */
public class WritingProgressPanel extends javax.swing.JPanel implements DBChangeListener {

    MainWindow mw;
    JFreeChart chart;
    TimeTableXYDataset progressDataset = new TimeTableXYDataset();
    TimeSeriesCollection targetsDataset = new TimeSeriesCollection();
    
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

    private synchronized void createChart() {
        createDatasets();
//        chart = ChartFactory.createStackedBarChart("Progress", "Words", "Date", null,
//                PlotOrientation.VERTICAL, true, true, true);

        chart = ChartFactory.createXYLineChart("Progress", "Date", "Words", targetsDataset, PlotOrientation.VERTICAL, true, true, true);
        
        XYPlot plot = (XYPlot) chart.getPlot();
        StackedXYBarRenderer xyBarRenderer = new StackedXYBarRenderer(0.40);
        xyBarRenderer.setShadowVisible(false);
        xyBarRenderer.setBarPainter(new StandardXYBarPainter());
        plot.setDataset(1, progressDataset);
        plot.setRenderer(1, xyBarRenderer);
        
        DateAxis dateAxis = new DateAxis();
        dateAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, 7));
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
        dateAxis.setVerticalTickLabels(true);
        plot.setDomainAxis(dateAxis);
        plot.setNoDataMessage("No progress on this book, yet!");
//        
//        CategoryPlot categoryPlot = chart.getCategoryPlot();
//        
//        StackedBarRenderer stackedBarRenderer = new StackedBarRenderer();
//        stackedBarRenderer.setShadowVisible(false);
//        categoryPlot.setRenderer(stackedBarRenderer);
//        
        ChartPanel cp = new org.jfree.chart.ChartPanel(chart);

        cp.setMaximumDrawHeight(5000);
        cp.setMinimumDrawHeight(10);
        cp.setMaximumDrawWidth(5000);
        cp.setMinimumDrawWidth(10);
        chartPanel.add(cp);
    }
    
    public void updateChart() {
        createDatasets();
    }
    
    public void populateBookList() {
        Connection dbConn = mw.getDbConn();
        DefaultListModel<BookListItem> m = new DefaultListModel<>();
        try {
            List<BookListItem> bookListFromDB = BookListItem.getBookListFromDB(dbConn);
            for(BookListItem i : bookListFromDB) {
                m.addElement(i);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        bookSelectionList.setModel(m);
        bookSelectionList.setSelectedIndex(0);
    }
    
    public synchronized TimeTableXYDataset createDatasets() {
        progressDataset.clear();
        targetsDataset.removeAllSeries();
        
        String tmptblcreate = "CREATE TEMPORARY TABLE IF NOT EXISTS tmp_booklist (bookid INTEGER NOT NULL, UNIQUE(bookid))";
        String tmptblempty = "DELETE FROM tmp_booklist WHERE 1";
        String tmptblinsert = "INSERT OR IGNORE INTO tmp_booklist(bookid) VALUES (?)";
        String tmptbldrop = "DROP TABLE IF EXISTS tmp_booklist";
        
        int[] selectedIndices = bookSelectionList.getSelectedIndices();
        ListModel<BookListItem> bookListModel = bookSelectionList.getModel();
        final Connection dbConn = mw.getDbConn();
        try(Statement stmt = dbConn.createStatement()) {
            stmt.executeUpdate(tmptblcreate);
            stmt.executeUpdate(tmptblempty);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        try(PreparedStatement stmt = dbConn.prepareStatement(tmptblinsert)) {
            for(int i : selectedIndices) {
                stmt.setInt(1, bookListModel.getElementAt(i).getBookid());
                stmt.executeUpdate();
            }
            dbConn.commit();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String progresssql = "WITH days AS (SELECT STRFTIME('%Y-%m-%d', MIN(adddate)) AS start, "
                + "                    STRFTIME('%Y-%m-%d', MIN(adddate)) AS curr, "
                + "                    STRFTIME('%Y-%m-%d', MAX(adddate)) AS last "
                + "              FROM wordcount INNER JOIN tmp_booklist ON tmp_booklist.bookid=wordcount.bookid"
                + "            UNION ALL "
                + "              SELECT start, date(curr, '+1 day'), last FROM days WHERE curr<last) "
                + "SELECT days.curr AS day, book.bookid AS bookid, book.title AS title, SUM(wordcount.delta) AS accumWords "
                + "    FROM days "
                + "    INNER JOIN wordcount ON days.curr>=STRFTIME('%Y-%m-%d', wordcount.adddate)"
                + "    INNER JOIN book ON book.bookid=wordcount.bookid "
                + "    INNER JOIN tmp_booklist ON tmp_booklist.bookid=book.bookid"
                + "    GROUP BY book.bookid, days.curr"
                + "    ORDER BY days.curr, book.bookid "
                + "LIMIT 10000 ";

        DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                
        try(PreparedStatement stmt = dbConn.prepareStatement(progresssql)) {
            try(ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    String day = rs.getString("day");
                    String title = rs.getString("title");
                    Integer accumWords = rs.getInt("accumWords");
                    Date parsedDate = isoDateFormat.parse(day);
                    Day d = new Day(parsedDate);
                    progressDataset.add(d, accumWords, title);
                    
                }
            }
        } catch (SQLException|ParseException ex) {
            ex.printStackTrace();
        }
        
        try(Statement stmt = dbConn.createStatement()) {
            stmt.executeUpdate(tmptbldrop);
            dbConn.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        return progressDataset;
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

    @Override
    public void notifyDBChanges() {
        populateBookList();
        updateChart();
    }
}
