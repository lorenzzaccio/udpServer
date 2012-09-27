/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataTable;

/**
 *
 * @author Lorenzzaccio
 */
import javax.swing.table.DefaultTableModel ;

public class otherTableModel extends DefaultTableModel {
    private static final int ROW = 0 ;

    public otherTableModel(String [] columnNames) {
        super (columnNames, ROW) ;
    }
}
