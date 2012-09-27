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
 public class  MyTableModel extends DefaultTableModel implements TableModelListener,protocole,tableMap{
     private boolean DEBUG = false;
     private static final int ROW = 0 ;
     
     private String[] columnNames = {"Adresse","Nom",
                                        "Valeur"};
     private Object data [][];
    private boolean m_writeFile=false;


    public MyTableModel(String[] colNames) {
        super (colNames, ROW) ;
        columnNames = colNames;
    }

  
        public boolean getChangeEvent() {
            boolean tmpWriteFile = m_writeFile;
            return tmpWriteFile;
        }
  /*
        public int getRowCount() {
            return data.length;
        }
 */
    @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }
 /*
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
 */
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
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }
            if(row<dataVector.size()){
                Vector rowVector = (Vector)dataVector.elementAt(row);
                rowVector.setElementAt(value, col);
                fireTableCellUpdated(row, col);
                m_writeFile = true;
            }
            //data[row][col] = value;
            //fireTableCellUpdated(row, col);
 
            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
        public void setValueBoolAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }
            boolean bVal = false;
            
            if(Integer.valueOf((String)value) == 1)
                bVal = true;
            else
                bVal = false;

            if(row<dataVector.size()){
                Vector rowVector = (Vector)dataVector.elementAt(row);
                rowVector.setElementAt(bVal, col);
                fireTableCellUpdated(row, col);
            }
            //data[row][col] = value;
            //fireTableCellUpdated(row, col);
 
            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
 
        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();
 
            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
        
            /****************************
     * 
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        String lclszData = "false";
        int row = e.getFirstRow();
        int column = e.getColumn();
        if(column==VALUE_COL){
            TableModel model = (TableModel)e.getSource();
            String columnName = model.getColumnName(column);
            Object objData = model.getValueAt(row, column);
            int lclData;
            if((Boolean)objData==true){
                lclData = 1;
                lclszData="true";
            } else{
                lclData = 0;
                lclszData="false";
            }
           // Do something with the data...
            Object objAddress = model.getValueAt(row, ADDRESS_COL);
            int address = Integer.valueOf((String)objAddress);
            //System.out.println("data is "+lclData);
            setReg(address,lclData);
            //setBoolReg(address,lclszData);
        }
    }
    public void setReg(int address,int val){
        comUdp.sendData(SET_REG_CMD, String.format("%04d%04d", address,val), globalCom.m_targetIp, globalCom.m_sendPort);
    }
    public void setBoolReg(int address,String val){
        comUdp.sendData(SET_REG_CMD, String.format("%04d%s", address,val), globalCom.m_targetIp, globalCom.m_sendPort);
    }
    
    }