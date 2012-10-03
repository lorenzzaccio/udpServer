/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dataTable;

import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import udp.comUdp;
import udp.globalCom;
import udp.protocole;

/**
 *
 * @author laurentgarnier
 */
public class MyTableModel extends DefaultTableModel implements TableModelListener, protocole, tableMap {

    private static final int ROW = 0;
    private String[] columnNames = {"Adresse", "Nom", "Valeur"};
    private boolean m_writeFile = false;
    private boolean m_FireEvent = true;

    public MyTableModel(String[] colNames) {
        super(colNames, ROW);
        columnNames = colNames;
    }

    public boolean getChangeEvent() {
        boolean tmpWriteFile = m_writeFile;
        m_writeFile = false;
        return tmpWriteFile;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        if (col < 1) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        if (row < dataVector.size()) {
            Vector rowVector = (Vector) dataVector.elementAt(row);
            rowVector.setElementAt(value, col);
            fireTableCellUpdated(row, col);
            m_writeFile = true;
        }
    }

    public void setValueBoolAt(Object value, int row, int col, boolean bFireEvent) {
        boolean bVal = false;

        if (Integer.valueOf((String) value) == 1) {
            bVal = true;
        } else {
            bVal = false;
        }

        if (row < dataVector.size()) {
            Vector rowVector = (Vector) dataVector.elementAt(row);
            rowVector.setElementAt(bVal, col);
            m_FireEvent = bFireEvent;
            fireTableCellUpdated(row, col);
        }
    }

    /****************************
     * 
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column == VALUE_COL) {
            TableModel model = (TableModel) e.getSource();

            Object objData = model.getValueAt(row, column);
            int lclData;
            if ((Boolean) objData == true) {
                lclData = 1;
            } else {
                lclData = 0;
            }
            Object objAddress = model.getValueAt(row, ADDRESS_COL);
            int address = Integer.valueOf((String) objAddress);
            if (m_FireEvent) {
                setReg(address, lclData);
            } else //reset value
            {
                m_FireEvent = true;
            }
        }
    }

    public void setReg(int address, int val) {
        comUdp.sendData(SET_REG_CMD, String.format("%04d%04d", address, val), globalCom.m_targetIp, globalCom.m_sendPort);
    }

    public void setBoolReg(int address, String val) {
        comUdp.sendData(SET_REG_CMD, String.format("%04d%s", address, val), globalCom.m_targetIp, globalCom.m_sendPort);
    }
}
