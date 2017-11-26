/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icculus.chunky.wordsmith.GUI;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import org.icculus.chunky.wordsmith.DBManager;

/**
 *
 * @author chunky
 */
public class MainWindow extends javax.swing.JFrame {

    Connection dbConn = null;
    AddWordsPanel addWordsPanel;
    WritingProgressPanel writingProgressPanel;
    EditAuthorsBooksPanel authorsBooksPanel;
    Collection<DBChangeListener> dbChangeListeners = new HashSet<>();
    
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
        writingProgressPanel = new WritingProgressPanel(this);
        chartHostPanel.add(writingProgressPanel);
        dbChangeListeners.add(writingProgressPanel);
        
        addWordsPanel = new AddWordsPanel(this);
        addWordsHostPanel.add(addWordsPanel);
        dbChangeListeners.add(addWordsPanel);
        
        authorsBooksPanel = new EditAuthorsBooksPanel(this);
        authorsBooksHostPanel.add(authorsBooksPanel);
        dbChangeListeners.add(authorsBooksPanel);
        
        
        setLocationRelativeTo(null);
    }
    
    public void notifyDBChange() {
        for(DBChangeListener l : dbChangeListeners) {
            l.notifyDBChanges();
        }
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

        mainTabs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        addWordsHostPanel = new javax.swing.JPanel();
        chartHostPanel = new javax.swing.JPanel();
        authorsBooksHostPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("WordSmith");
        setPreferredSize(new java.awt.Dimension(1024, 768));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jPanel1.setLayout(new java.awt.GridBagLayout());

        addWordsHostPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(addWordsHostPanel, gridBagConstraints);

        chartHostPanel.setLayout(new java.awt.GridLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel1.add(chartHostPanel, gridBagConstraints);

        mainTabs.addTab("Progress", jPanel1);

        authorsBooksHostPanel.setLayout(new java.awt.GridLayout());
        mainTabs.addTab("Authors & Books", authorsBooksHostPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        getContentPane().add(mainTabs, gridBagConstraints);

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
    private javax.swing.JPanel addWordsHostPanel;
    private javax.swing.JPanel authorsBooksHostPanel;
    private javax.swing.JPanel chartHostPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTabbedPane mainTabs;
    // End of variables declaration//GEN-END:variables
}
