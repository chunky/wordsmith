/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icculus.chunky.wordsmith.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import org.icculus.chunky.wordsmith.DBManager;

/**
 *
 * @author chunky
 */
public class MainWindow extends javax.swing.JFrame {

    Connection dbConn = null;
    
    /**
     * Creates new form WordSmith
     */
    public MainWindow() {
        try {
            DBManager dbm = new DBManager();
            dbConn = dbm.openDB("test.sqlite");
            dbm.createDummyData(dbConn);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        initComponents();
        configureCombos();
        chartPanel.add(new WritingProgressPanel(this));
        setLocationRelativeTo(null);
    }
    
    private String getSelectedAuthorName() {
        return (String) authorCombo.getSelectedItem();
    }
    
    private String getSelectedBookTitle() {
        return (String) bookCombo.getSelectedItem();
    }
    
    private void configureCombos() {
        
        authorCombo.setModel(new ComboBoxModel<String>() {
            Object selectedItem = "Choose an Author...";
            
            Set<ListDataListener> listeners = new LinkedHashSet<>();

            String[] authorList = new String[] {};
            @Override
            public void setSelectedItem(Object anItem) {
                selectedItem = anItem;
            }

            @Override
            public Object getSelectedItem() {
                return selectedItem;
            }

            private void populateAuthorList() {
                String sql = "SELECT name FROM author ORDER BY name";
                try(Statement stmt = dbConn.createStatement()) {
                    ArrayList<String> al = new ArrayList<>();
                    try(ResultSet rs = stmt.executeQuery(sql)) {
                        while(rs.next()) {
                            al.add(rs.getString("name"));
                        }
                    }
                    authorList = al.toArray(new String[al.size()]);
                } catch(SQLException ex) {
                    ex.printStackTrace();
                    authorList = new String[] {};
                }
            }
            
            @Override
            public int getSize() {
                if(null == dbConn) {
                    return 0;
                }
                populateAuthorList();
                return authorList.length;
            }

            @Override
            public String getElementAt(int index) {
                return authorList.length<index?null:authorList[index];
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                listeners.add(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                listeners.remove(l);
            }
        });
        
        bookCombo.setModel(new ComboBoxModel<String>() {
            Object selectedItem = "Choose a Book...";
            
            Set<ListDataListener> listeners = new LinkedHashSet<>();

            String[] bookList = new String[] {};
            @Override
            public void setSelectedItem(Object anItem) {
                selectedItem = anItem;
            }

            @Override
            public Object getSelectedItem() {
                return selectedItem;
            }

            private void populateBookList() {
                String sql = "SELECT title FROM book WHERE authorid=(SELECT authorid FROM author WHERE name=?) ORDER BY title";
                try(PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                    stmt.setString(1, getSelectedAuthorName());
                    ArrayList<String> al = new ArrayList<>();
                    
                    try(ResultSet rs = stmt.executeQuery()) {
                        while(rs.next()) {
                            String thisTitle = rs.getString("title");
                            al.add(thisTitle);
                        }
                    }
                    bookList = al.toArray(new String[al.size()]);
                } catch(SQLException ex) {
                    ex.printStackTrace();
                    bookList = new String[] {};
                }
            }
            
            @Override
            public int getSize() {
                if(null == dbConn) {
                    return 0;
                }
                populateBookList();
                return bookList.length;
            }

            @Override
            public String getElementAt(int index) {
                populateBookList();
                return bookList.length<index?null:bookList[index];
            }

            @Override
            public void addListDataListener(ListDataListener l) {
                listeners.add(l);
            }

            @Override
            public void removeListDataListener(ListDataListener l) {
                listeners.remove(l);
            }
        });
        
        
        bookCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Store for GUI information
                String sql1 = "UPDATE book SET lastselected=0";
                String sql2 = "UPDATE book SET lastselected=1 WHERE title=?";
                try(PreparedStatement stmt1 = dbConn.prepareStatement(sql1);
                        PreparedStatement stmt2 = dbConn.prepareStatement(sql2)) {
                    
                    stmt1.executeUpdate();
                    stmt2.setString(1, getSelectedBookTitle());
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        authorCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Store for GUI information
                String sql1 = "UPDATE author SET lastselected=0";
                String sql2 = "UPDATE author SET lastselected=1 WHERE name=?";
                try(PreparedStatement stmt1 = dbConn.prepareStatement(sql1);
                        PreparedStatement stmt2 = dbConn.prepareStatement(sql2)) {
                    
                    stmt1.executeUpdate();
                    stmt2.setString(1, getSelectedAuthorName());
                    stmt2.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                
                bookCombo.setSelectedIndex(0);
                bookCombo.repaint();
            }
        });
        
    }

    public Connection getDbConn() {
        return dbConn;
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

        authorCombo = new javax.swing.JComboBox<>();
        bookCombo = new javax.swing.JComboBox<>();
        chartPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WordSmith");
        setPreferredSize(new java.awt.Dimension(1024, 768));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        authorCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Author..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(authorCombo, gridBagConstraints);

        bookCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Book..." }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 3.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        getContentPane().add(bookCombo, gridBagConstraints);

        chartPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(chartPanel, gridBagConstraints);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> authorCombo;
    private javax.swing.JComboBox<String> bookCombo;
    private javax.swing.JPanel chartPanel;
    // End of variables declaration//GEN-END:variables
}
