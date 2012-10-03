/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dataTable;

import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import udp.comUdp;
import udp.globalCom;
import udp.protocole;

/**
 *
 * @author Lorenzzaccio
 */

public class varTableModel extends MyTableModel implements TableModelListener, protocole, tableMap {
    private static final int ROW = 0;
    private String[] columnNames = {"Nom", "Desc", "Valeur"};
    private boolean m_writeFile = false;
    private boolean m_FireEvent = true;

    public varTableModel(String[] colNames) {
        super(colNames);
        columnNames = colNames;
    }
    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
            return true;
    }

    public void setValueVarAt(Object value, int row, int col,boolean bFireEvent) {
        if (row < dataVector.size()) {
            Vector rowVector = (Vector) dataVector.elementAt(row);
            rowVector.setElementAt(value, col);
            m_FireEvent = bFireEvent;
            fireTableCellUpdated(row, col);
            m_writeFile = true;
        }
    }
    /****************************
     *
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if ((column == VALUE_COL)||(column==ADDRESS_COL)) {
            TableModel model = (TableModel) e.getSource();
            Object objData = model.getValueAt(row, column);
            Object objAddress = model.getValueAt(row, ADDRESS_COL);
            if (m_FireEvent) {
                setVar((String)objAddress, (String)objData);
            } else //reset value
            {
                m_FireEvent = true;
            }
        }
    }

    public void setVar(String address, String val) {
        comUdp.sendData(SET_VAR_CMD, String.format("%s=%s", address, val), globalCom.m_targetIp, globalCom.m_sendPort);
    }
}
