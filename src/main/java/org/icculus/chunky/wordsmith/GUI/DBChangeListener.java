/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.icculus.chunky.wordsmith.GUI;

/** Every time database data [eg author or books] is changed
 *
 * @author chunky
 */
public interface DBChangeListener {
    /**
     * This gets called when main data is changed
     */
    public void notifyDBChanges();
}
