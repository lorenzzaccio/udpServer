/*
 * UdpServerView.java
 */
package udpserver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jdesktop.application.Action;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.TaskMonitor;
import quoteClient.quoteClient;
import quoteServer.QuoteServerThread;
import udp.comUdp;
import udp.protocole;
import util.*;
import dataTable.MyTableModel;
import dataTable.tableMap;
import dataTable.varTableModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import multicastQuoteServer.multicastQuoteServerThread;
import udp.globalCom;

/**
 * The application's main frame.
 */
public class UdpServerView extends FrameView implements protocole, tableMap {

    private static int MAX_IO = 100;
    private static int MAX_VAR = 25;
    quoteClient m_clientThread;
    boolean bSendReq = false;
    private boolean speedSliderSet = false;
    private String m_oldMagP1 = "0";
    public boolean bWriteFile = false;
    public boolean bWriteVarFile = false;
    public MyTableModel objTableModel;
    public varTableModel objVarModel;
    private int m_selectedRow;

    public UdpServerView(SingleFrameApplication app) throws IOException, InterruptedException {
        super(app);

        initComponents();

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                //statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                //statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        //statusAnimationLabel.setIcon(idleIcon);
        //progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        //statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    //statusAnimationLabel.setIcon(idleIcon);
                    //progressBar.setVisible(false);
                    //progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    //statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(false);
                    //progressBar.setValue(value);
                }
            }
        });
        magP1Txt.setText(m_oldMagP1);
        // Listen for changes in the text
        magP1Txt.getDocument().addDocumentListener(new DocumentListener() {

            public void changedUpdate(DocumentEvent e) {
                System.out.println("echo");
                // text was changed
                //check value
                if (!util.isNum((String) magP1Txt.getText())) {
                    magP1Txt.setText(m_oldMagP1);
                } else {
                    m_oldMagP1 = magP1Txt.getText();
                }
            }

            public void removeUpdate(DocumentEvent e) {
                // text was deleted
                System.out.println("echo1");
                // m_oldMagP1=magP1Txt.getText();
            }

            public void insertUpdate(DocumentEvent e) {
                System.out.println("echo2");
                // text was inserted
                if ((!util.isNum((String) magP1Txt.getText())) || (!util.isPositive((String) magP1Txt.getText()))) {
                    magP1Txt.setText(m_oldMagP1);
                } else {
                    m_oldMagP1 = magP1Txt.getText();
                }
            }
        });
        //init regAddrCombo
        for (int i = 30; i < 100; i++) {
            regAddrCombo.addItem(i);
        }
        //Turn on labels at major tick marks.
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setMinorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);

        //get local ip address
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException ex) {
            Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
        globalCom.m_localIp = addr.toString().split("/")[1];
        iplclTxt.setText(globalCom.m_localIp);

        globalCom.m_targetIp = ipRmtTxt.getText();
        globalCom.m_recptPort = portLclTxt.getText();
        globalCom.m_sendPort = portRmtTxt.getText();

        //table part
        String[] columnNames = {"Adresse", "Nom", "Valeur"};
        ioTable.getSelectionModel().addListSelectionListener(new RowListener());
        ioTable.setRowSelectionAllowed(true);
        ioTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        objTableModel = new MyTableModel(columnNames);
        m_selectedRow = 0;
        ioTable.getModel().addTableModelListener(objTableModel);

        //varTable part
        String[] varColumnNames = {"Nom", "Desc", "Valeur"};

        objVarModel = new varTableModel(varColumnNames);
        for (int i = 1; i < 20; i++) {
            Object[] data = {String.format("var%02d", i), "no desc", "0"};
            objVarModel.addRow(data);
        }
        varTable.setModel(objVarModel);
        varTable.getModel().addTableModelListener(objVarModel);

        //background task for filling register table
        Thread bkTh = new Thread() {

            boolean bLoop = true;

            @Override
            public void run() {
                while (bLoop == true) {
                    try {
                        bWriteFile = objTableModel.getChangeEvent();
                        if (bWriteFile) {
                            try {
                                writeFile("../conf/descRegister.txt");
                                bWriteFile = false;
                            } catch (IOException ex) {
                                Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        bkTh.start();

        //background task for filling register table
        Thread varBkTh = new Thread() {

            boolean bLoop = true;

            @Override
            public void run() {
                while (bLoop == true) {
                    try {
                        bWriteVarFile = objVarModel.getChangeEvent();
                        if (bWriteVarFile) {
                            try {
                                writeVarFile("../conf/varDescRegister.txt");
                                bWriteVarFile = false;
                            } catch (IOException ex) {
                                Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };

        varBkTh.start();
        Thread connectionTh = new Thread() {
            @Override
            public void run() {
                connectClient();
                while (globalCom.m_searchingRobot == true) {
                    try {
                        sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                ;
                loadIoTable();
                loadVarTable();

            }
        };
        connectionTh.start();

    }

    private class RowListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            m_selectedRow = 0;
            for (int c : ioTable.getSelectedRows()) {
                System.out.println(String.format(" %d", c));
                m_selectedRow = c;
            }
        }
    }

    /********************
     * 
     */
    public void readFile(String filename) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.length() > 0) {
                    process(str);
                }
            }
            ioTable.setModel(objTableModel);
            in.close();
        } catch (IOException e) {
        }
    }

    /********************
     *
     */
    public void readVarFile(String filename) {
        int rowIndex = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = in.readLine()) != null) {
                processVar(str, rowIndex);
                rowIndex++;
            }
            in.close();
        } catch (IOException e) {
        }
    }

    public void writeFile(String filename) throws IOException {
        File file = new File(filename);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        processWriting(bw);
        bw.close();
    }

    /***********************************
     *
     */
    public void writeVarFile(String filename) throws IOException {
        File file = new File(filename);
        // if file doesnt exists, then create it
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        processVarWriting(bw);
        bw.close();
    }

    /*********************************
     * 
     * @param bw
     * @throws IOException
     */
    private void processWriting(BufferedWriter bw) throws IOException {
        int i;
        MyTableModel tm = (MyTableModel) ioTable.getModel();
        for (i = 0; i < tm.getRowCount(); i++) {
            String szVal = (String) tm.getValueAt(i, ADDRESS_COL);
            String str = (String) tm.getValueAt(i, DESC_COL);
            String content = String.format("%s=%s \r\n", szVal, str);
            bw.write(content);
        }
    }

    /*********************************
     *
     * @param bw
     * @throws IOException
     */
    private void processVarWriting(BufferedWriter bw) throws IOException {
        int i;
        varTableModel tm = (varTableModel) varTable.getModel();
        for (i = 0; i < tm.getRowCount(); i++) {
            String szVal = (String) tm.getValueAt(i, ADDRESS_COL);
            String str = (String) tm.getValueAt(i, DESC_COL);
            String content = String.format("%s=%s \r\n", szVal, str);
            bw.write(content);
        }
    }

    /*********************
     *
     */
    private void process(String str) {
        String[] splitter = new String[2];
        splitter = str.split("=");


        //for (int i = 1; i < MAX_IO; i++) {
        int address = Integer.valueOf(splitter[0]);
        Object[] data = {String.format("%02d", address), splitter[1].trim(), new Boolean(false)};
        objTableModel.addRow(data);
        //}
        //ioTable.setModel(objTableModel);
/*
        MyTableModel tm = (MyTableModel) ioTable.getModel();
        for (i = 0; i < tm.getRowCount(); i++) {
        if (((String) tm.getValueAt(i, ADDRESS_COL)).matches(splitter[0].trim())) {
        break;
        }
        }
        tm.setValueAt(splitter[1].trim(), i, DESC_COL);
         */
    }

    /*********************
     *
     */
    private void processVar(String str, int i) {
        String[] splitter = new String[2];
        splitter = str.split("=");
        //int i;
        varTableModel tm = (varTableModel) varTable.getModel();
        /*for (i = 0; i < tm.getRowCount(); i++) {
        if (((String) tm.getValueAt(i, ADDRESS_COL)).matches(splitter[0].trim())) {
        break;
        }
        }*/
        tm.setValueAt(splitter[0].trim(), i, ADDRESS_COL);
        tm.setValueAt(splitter[1].trim(), i, DESC_COL);
    }

    /**********************************
     *
     */
    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = UdpServerApp.getApplication().getMainFrame();
            aboutBox = new UdpServerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        UdpServerApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        connectBtn = new javax.swing.JButton();
        speedSlider = new javax.swing.JSlider();
        getEnvBtn = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTxt = new javax.swing.JTextArea();
        finBatonToggleBtn = new javax.swing.JToggleButton();
        fastModeToggleBtn = new javax.swing.JToggleButton();
        posReposToggleBtn = new javax.swing.JToggleButton();
        jLabel5 = new javax.swing.JLabel();
        magP1Txt = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cadenceTxt = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        videsTxt = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        capsBonnesTxt = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        dechetsTxt = new javax.swing.JTextField();
        resteAProduireTxt = new javax.swing.JTextField();
        poubelleTxt = new javax.swing.JTextField();
        totalAProduireTxt = new javax.swing.JTextField();
        controlLevelBtn = new javax.swing.JToggleButton();
        plusMagTxt = new javax.swing.JButton();
        minusMagTxt = new javax.swing.JButton();
        plusDechetsTxt = new javax.swing.JButton();
        minusDechetsTxt = new javax.swing.JButton();
        minusVidesTxt = new javax.swing.JButton();
        plusVidesTxt = new javax.swing.JButton();
        minuxPoubelleTxt = new javax.swing.JButton();
        plusPoubelleTxt = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        iplclTxt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        portLclTxt = new javax.swing.JTextField();
        ipRmtTxt = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        portRmtTxt = new javax.swing.JTextField();
        regAddrCombo = new javax.swing.JComboBox();
        regTxt = new javax.swing.JTextField();
        setBtn = new javax.swing.JButton();
        getBtn = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ioTable = new javax.swing.JTable();
        getVarBtn = new javax.swing.JButton();
        writeBtn = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        varTable = new javax.swing.JTable();
        plusBtn = new javax.swing.JButton();
        delBtn = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();

        mainPanel.setMaximumSize(new java.awt.Dimension(327, 327));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(800, 712));
        mainPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                mainPanelComponentShown(evt);
            }
        });

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(udpserver.UdpServerApp.class).getContext().getResourceMap(UdpServerView.class);
        connectBtn.setFont(resourceMap.getFont("connectBtn.font")); // NOI18N
        connectBtn.setText(resourceMap.getString("connectBtn.text")); // NOI18N
        connectBtn.setName("connectBtn"); // NOI18N
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        speedSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        speedSlider.setValue(0);
        speedSlider.setName("speedSlider"); // NOI18N
        speedSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                speedSliderMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                speedSliderMouseReleased(evt);
            }
        });
        speedSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                speedSliderStateChanged(evt);
            }
        });

        getEnvBtn.setFont(resourceMap.getFont("getEnvBtn.font")); // NOI18N
        getEnvBtn.setText(resourceMap.getString("getEnvBtn.text")); // NOI18N
        getEnvBtn.setName("getEnvBtn"); // NOI18N
        getEnvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getEnvBtnActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        logTxt.setColumns(20);
        logTxt.setRows(5);
        logTxt.setEnabled(false);
        logTxt.setName("logTxt"); // NOI18N
        jScrollPane1.setViewportView(logTxt);

        finBatonToggleBtn.setFont(resourceMap.getFont("finBatonToggleBtn.font")); // NOI18N
        finBatonToggleBtn.setText(resourceMap.getString("finBatonToggleBtn.text")); // NOI18N
        finBatonToggleBtn.setName("finBatonToggleBtn"); // NOI18N
        finBatonToggleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finBatonToggleBtnActionPerformed(evt);
            }
        });

        fastModeToggleBtn.setFont(resourceMap.getFont("fastModeToggleBtn.font")); // NOI18N
        fastModeToggleBtn.setText(resourceMap.getString("fastModeToggleBtn.text")); // NOI18N
        fastModeToggleBtn.setName("fastModeToggleBtn"); // NOI18N
        fastModeToggleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fastModeToggleBtnActionPerformed(evt);
            }
        });

        posReposToggleBtn.setFont(resourceMap.getFont("posReposToggleBtn.font")); // NOI18N
        posReposToggleBtn.setText(resourceMap.getString("posReposToggleBtn.text")); // NOI18N
        posReposToggleBtn.setName("posReposToggleBtn"); // NOI18N
        posReposToggleBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posReposToggleBtnActionPerformed(evt);
            }
        });

        jLabel5.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        magP1Txt.setEditable(false);
        magP1Txt.setFont(resourceMap.getFont("jLabel5.font")); // NOI18N
        magP1Txt.setText(resourceMap.getString("magP1Txt.text")); // NOI18N
        magP1Txt.setName("magP1Txt"); // NOI18N
        magP1Txt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                magP1TxtMouseClicked(evt);
            }
        });
        magP1Txt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                magP1TxtActionPerformed(evt);
            }
        });
        magP1Txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                magP1TxtFocusLost(evt);
            }
        });
        magP1Txt.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                magP1TxtInputMethodTextChanged(evt);
            }
        });
        magP1Txt.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                magP1TxtPropertyChange(evt);
            }
        });

        jLabel6.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        jLabel6.setText(resourceMap.getString("jLabel6.text")); // NOI18N
        jLabel6.setName("jLabel6"); // NOI18N

        cadenceTxt.setEditable(false);
        cadenceTxt.setFont(resourceMap.getFont("jLabel6.font")); // NOI18N
        cadenceTxt.setText(resourceMap.getString("cadenceTxt.text")); // NOI18N
        cadenceTxt.setName("cadenceTxt"); // NOI18N
        cadenceTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cadenceTxtActionPerformed(evt);
            }
        });

        jLabel7.setFont(resourceMap.getFont("magP1Txt3.font")); // NOI18N
        jLabel7.setText(resourceMap.getString("jLabel7.text")); // NOI18N
        jLabel7.setName("jLabel7"); // NOI18N

        jLabel8.setFont(resourceMap.getFont("magP1Txt3.font")); // NOI18N
        jLabel8.setText(resourceMap.getString("jLabel8.text")); // NOI18N
        jLabel8.setName("jLabel8"); // NOI18N

        videsTxt.setEditable(false);
        videsTxt.setFont(resourceMap.getFont("videsTxt.font")); // NOI18N
        videsTxt.setText(resourceMap.getString("videsTxt.text")); // NOI18N
        videsTxt.setName("videsTxt"); // NOI18N
        videsTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                videsTxtMouseClicked(evt);
            }
        });
        videsTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videsTxtActionPerformed(evt);
            }
        });
        videsTxt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                videsTxtFocusLost(evt);
            }
        });

        jLabel9.setFont(resourceMap.getFont("magP1Txt6.font")); // NOI18N
        jLabel9.setText(resourceMap.getString("jLabel9.text")); // NOI18N
        jLabel9.setName("jLabel9"); // NOI18N

        jLabel10.setFont(resourceMap.getFont("magP1Txt6.font")); // NOI18N
        jLabel10.setText(resourceMap.getString("jLabel10.text")); // NOI18N
        jLabel10.setName("jLabel10"); // NOI18N

        jLabel11.setFont(resourceMap.getFont("magP1Txt6.font")); // NOI18N
        jLabel11.setText(resourceMap.getString("jLabel11.text")); // NOI18N
        jLabel11.setName("jLabel11"); // NOI18N

        capsBonnesTxt.setEditable(false);
        capsBonnesTxt.setFont(resourceMap.getFont("capsBonnesTxt.font")); // NOI18N
        capsBonnesTxt.setText(resourceMap.getString("capsBonnesTxt.text")); // NOI18N
        capsBonnesTxt.setName("capsBonnesTxt"); // NOI18N

        jLabel12.setFont(resourceMap.getFont("magP1Txt6.font")); // NOI18N
        jLabel12.setText(resourceMap.getString("jLabel12.text")); // NOI18N
        jLabel12.setName("jLabel12"); // NOI18N

        dechetsTxt.setEditable(false);
        dechetsTxt.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        dechetsTxt.setText(resourceMap.getString("dechetsTxt.text")); // NOI18N
        dechetsTxt.setName("dechetsTxt"); // NOI18N
        dechetsTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dechetsTxtMouseClicked(evt);
            }
        });
        dechetsTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dechetsTxtActionPerformed(evt);
            }
        });
        dechetsTxt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                dechetsTxtFocusLost(evt);
            }
        });

        resteAProduireTxt.setEditable(false);
        resteAProduireTxt.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        resteAProduireTxt.setText(resourceMap.getString("resteAProduireTxt.text")); // NOI18N
        resteAProduireTxt.setName("resteAProduireTxt"); // NOI18N

        poubelleTxt.setEditable(false);
        poubelleTxt.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        poubelleTxt.setText(resourceMap.getString("poubelleTxt.text")); // NOI18N
        poubelleTxt.setName("poubelleTxt"); // NOI18N
        poubelleTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                poubelleTxtMouseClicked(evt);
            }
        });
        poubelleTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                poubelleTxtActionPerformed(evt);
            }
        });
        poubelleTxt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                poubelleTxtFocusLost(evt);
            }
        });

        totalAProduireTxt.setEditable(false);
        totalAProduireTxt.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        totalAProduireTxt.setText(resourceMap.getString("totalAProduireTxt.text")); // NOI18N
        totalAProduireTxt.setName("totalAProduireTxt"); // NOI18N
        totalAProduireTxt.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                totalAProduireTxtMouseClicked(evt);
            }
        });
        totalAProduireTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                totalAProduireTxtActionPerformed(evt);
            }
        });
        totalAProduireTxt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                totalAProduireTxtFocusLost(evt);
            }
        });

        controlLevelBtn.setText(resourceMap.getString("controlLevelBtn.text")); // NOI18N
        controlLevelBtn.setName("controlLevelBtn"); // NOI18N
        controlLevelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                controlLevelBtnActionPerformed(evt);
            }
        });

        plusMagTxt.setFont(resourceMap.getFont("plusMagTxt.font")); // NOI18N
        plusMagTxt.setText(resourceMap.getString("plusMagTxt.text")); // NOI18N
        plusMagTxt.setName("plusMagTxt"); // NOI18N
        plusMagTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusMagTxtActionPerformed(evt);
            }
        });

        minusMagTxt.setFont(resourceMap.getFont("minusMagTxt.font")); // NOI18N
        minusMagTxt.setText(resourceMap.getString("minusMagTxt.text")); // NOI18N
        minusMagTxt.setName("minusMagTxt"); // NOI18N
        minusMagTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusMagTxtActionPerformed(evt);
            }
        });

        plusDechetsTxt.setFont(resourceMap.getFont("plusDechetsTxt.font")); // NOI18N
        plusDechetsTxt.setText(resourceMap.getString("plusDechetsTxt.text")); // NOI18N
        plusDechetsTxt.setName("plusDechetsTxt"); // NOI18N
        plusDechetsTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusDechetsTxtActionPerformed(evt);
            }
        });

        minusDechetsTxt.setFont(resourceMap.getFont("minusDechetsTxt.font")); // NOI18N
        minusDechetsTxt.setText(resourceMap.getString("minusDechetsTxt.text")); // NOI18N
        minusDechetsTxt.setName("minusDechetsTxt"); // NOI18N
        minusDechetsTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusDechetsTxtActionPerformed(evt);
            }
        });

        minusVidesTxt.setFont(resourceMap.getFont("minusVidesTxt.font")); // NOI18N
        minusVidesTxt.setText(resourceMap.getString("minusVidesTxt.text")); // NOI18N
        minusVidesTxt.setName("minusVidesTxt"); // NOI18N
        minusVidesTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minusVidesTxtActionPerformed(evt);
            }
        });

        plusVidesTxt.setFont(resourceMap.getFont("plusVidesTxt.font")); // NOI18N
        plusVidesTxt.setText(resourceMap.getString("plusVidesTxt.text")); // NOI18N
        plusVidesTxt.setName("plusVidesTxt"); // NOI18N
        plusVidesTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusVidesTxtActionPerformed(evt);
            }
        });

        minuxPoubelleTxt.setFont(resourceMap.getFont("minuxPoubelleTxt.font")); // NOI18N
        minuxPoubelleTxt.setText(resourceMap.getString("minuxPoubelleTxt.text")); // NOI18N
        minuxPoubelleTxt.setName("minuxPoubelleTxt"); // NOI18N
        minuxPoubelleTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minuxPoubelleTxtActionPerformed(evt);
            }
        });

        plusPoubelleTxt.setFont(resourceMap.getFont("plusPoubelleTxt.font")); // NOI18N
        plusPoubelleTxt.setText(resourceMap.getString("plusPoubelleTxt.text")); // NOI18N
        plusPoubelleTxt.setName("plusPoubelleTxt"); // NOI18N
        plusPoubelleTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusPoubelleTxtActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        iplclTxt.setText(resourceMap.getString("iplclTxt.text")); // NOI18N
        iplclTxt.setName("iplclTxt"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        portLclTxt.setText(resourceMap.getString("portLclTxt.text")); // NOI18N
        portLclTxt.setName("portLclTxt"); // NOI18N

        ipRmtTxt.setText(resourceMap.getString("ipRmtTxt.text")); // NOI18N
        ipRmtTxt.setName("ipRmtTxt"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        portRmtTxt.setText(resourceMap.getString("portRmtTxt.text")); // NOI18N
        portRmtTxt.setName("portRmtTxt"); // NOI18N

        regAddrCombo.setName("regAddrCombo"); // NOI18N

        regTxt.setText(resourceMap.getString("regTxt.text")); // NOI18N
        regTxt.setName("regTxt"); // NOI18N

        setBtn.setMnemonic('S');
        setBtn.setText(resourceMap.getString("setBtn.text")); // NOI18N
        setBtn.setToolTipText(resourceMap.getString("setBtn.toolTipText")); // NOI18N
        setBtn.setName("setBtn"); // NOI18N
        setBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setBtnActionPerformed(evt);
            }
        });

        getBtn.setMnemonic('G');
        getBtn.setText(resourceMap.getString("getBtn.text")); // NOI18N
        getBtn.setToolTipText(resourceMap.getString("getBtn.toolTipText")); // NOI18N
        getBtn.setName("getBtn"); // NOI18N
        getBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getBtnActionPerformed(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        ioTable.setName("ioTable"); // NOI18N
        jScrollPane2.setViewportView(ioTable);

        getVarBtn.setText(resourceMap.getString("getVarBtn.text")); // NOI18N
        getVarBtn.setName("getVarBtn"); // NOI18N
        getVarBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getVarBtnActionPerformed(evt);
            }
        });

        writeBtn.setText(resourceMap.getString("writeBtn.text")); // NOI18N
        writeBtn.setName("writeBtn"); // NOI18N
        writeBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeBtnActionPerformed(evt);
            }
        });

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        varTable.setName("varTable"); // NOI18N
        jScrollPane3.setViewportView(varTable);

        plusBtn.setFont(resourceMap.getFont("plusBtn.font")); // NOI18N
        plusBtn.setText(resourceMap.getString("plusBtn.text")); // NOI18N
        plusBtn.setName("plusBtn"); // NOI18N
        plusBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plusBtnActionPerformed(evt);
            }
        });

        delBtn.setFont(resourceMap.getFont("delBtn.font")); // NOI18N
        delBtn.setText(resourceMap.getString("delBtn.text")); // NOI18N
        delBtn.setName("delBtn"); // NOI18N
        delBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 453, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(posReposToggleBtn)
                                        .add(6, 6, 6)
                                        .add(finBatonToggleBtn)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(fastModeToggleBtn)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(getEnvBtn))
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(17, 17, 17)
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel12)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)))
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(40, 40, 40)
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel5)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel6)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel7)
                                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel8))))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(magP1Txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(cadenceTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(dechetsTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(videsTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(poubelleTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(totalAProduireTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(capsBonnesTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                                            .add(resteAProduireTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
                                        .add(18, 18, 18)
                                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(setBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .add(regAddrCombo, 0, 77, Short.MAX_VALUE))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(regTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 81, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                    .add(getBtn)))
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(plusPoubelleTxt)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(minuxPoubelleTxt))
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(plusVidesTxt)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(minusVidesTxt))
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(plusDechetsTxt)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(minusDechetsTxt))
                                            .add(mainPanelLayout.createSequentialGroup()
                                                .add(plusMagTxt)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(minusMagTxt)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                    .add(writeBtn, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 67, Short.MAX_VALUE)
                                                    .add(mainPanelLayout.createSequentialGroup()
                                                        .add(getVarBtn)
                                                        .add(22, 22, 22)))))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(39, 39, 39)
                                        .add(controlLevelBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(85, 85, 85)
                                        .add(speedSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(connectBtn)
                                .add(18, 18, 18)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(iplclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(16, 16, 16)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(portLclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ipRmtTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(portRmtTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(59, 59, 59))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(plusBtn)
                            .add(delBtn))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(44, 44, 44))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(16, 16, 16)
                        .add(connectBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(posReposToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(finBatonToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fastModeToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(getEnvBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                            .add(jLabel5)
                            .add(magP1Txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plusMagTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(minusMagTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(2, 2, 2)
                                .add(getVarBtn)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                            .add(jLabel6)
                            .add(cadenceTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(writeBtn))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(dechetsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plusDechetsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(minusDechetsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel8)
                            .add(videsTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plusVidesTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(minusVidesTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel9)
                            .add(poubelleTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plusPoubelleTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(minuxPoubelleTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel11)
                            .add(totalAProduireTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel12)
                            .add(capsBonnesTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel10)
                            .add(resteAProduireTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(158, 158, 158))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(iplclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2)
                            .add(portLclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3)
                            .add(ipRmtTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4)
                            .add(portRmtTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                    .add(controlLevelBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(plusBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(mainPanelLayout.createSequentialGroup()
                                        .add(delBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(145, 145, 145)
                                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                            .add(regAddrCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(regTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE, false)
                                            .add(setBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                            .add(getBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                        .add(18, 18, 18)
                                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(speedSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 338, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(29, 29, 29)
                                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 224, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(udpserver.UdpServerApp.class).getContext().getActionMap(UdpServerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        jMenuItem1.setAction(actionMap.get("connectSettings")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void speedSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_speedSliderStateChanged
        JSlider source = (JSlider) evt.getSource();
        if (!source.getValueIsAdjusting()) {
            int fps = (int) source.getValue();
            comUdp.sendData(SET_SPEED_CMD, String.format("%4d", fps), globalCom.m_targetIp, globalCom.m_sendPort);
        }
    }//GEN-LAST:event_speedSliderStateChanged

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        connectClient();
        loadIoTable();
        loadVarTable();
    }//GEN-LAST:event_connectBtnActionPerformed

    public void loadIoTable() {
        readFile("../conf/descRegister.txt");
        Thread updateIoTh = new Thread() {

            @Override
            public void run() {
                for (int i = 1; i < MAX_IO; i++) {
                    try {
                        comUdp.sendData(GET_REG_CMD, String.format("%04d", (Integer) i), globalCom.m_targetIp, globalCom.m_sendPort);
                        sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        updateIoTh.start();
    }

    public void loadVarTable() {
        readVarFile("../conf/varDescRegister.txt");
        Thread updateIoTh = new Thread() {

            @Override
            public void run() {
                varTableModel tm = (varTableModel) varTable.getModel();
                for (int i = 0; i < tm.getRowCount(); i++) {
                    try {
                        comUdp.sendData(GET_VAR_CMD, String.format("%s", (String) tm.getValueAt(i, ADDRESS_COL)), globalCom.m_targetIp, globalCom.m_sendPort);
                        sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        updateIoTh.start();
    }

    private void connectClient() {
        try {
            //init socket
            DatagramSocket socketRecpt;
            int portRecpt;
            //reception
            globalCom.m_recptPort = portLclTxt.getText();
            //adresse cible
            globalCom.m_targetIp = ipRmtTxt.getText();
            portRecpt = Integer.valueOf(globalCom.m_recptPort);
            globalCom.m_sendPort = portRmtTxt.getText();
            InetAddress laddrRecept = InetAddress.getByName(globalCom.m_localIp);
            socketRecpt = new DatagramSocket(portRecpt, laddrRecept);
            //start server thread
            QuoteServerThread m_serverThread = new QuoteServerThread(socketRecpt, this.getFrame());
            m_serverThread.setRefLogui(logTxt);
            m_serverThread.setToken(bSendReq);
            m_serverThread.setRefParent(this);
            m_serverThread.setRefUI(cadenceTxt, totalAProduireTxt, magP1Txt, capsBonnesTxt, dechetsTxt, videsTxt, poubelleTxt, speedSlider, speedSliderSet);

            m_serverThread.start();

        } catch (IOException ex) {
            Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ping() throws SocketException, UnknownHostException, IOException {
        int portRecpt = Integer.valueOf(globalCom.m_recptPort);
        globalCom.m_sendPort = portRmtTxt.getText();
        InetAddress laddrRecept = InetAddress.getByName(globalCom.m_localIp);
        DatagramSocket socketRecpt = new DatagramSocket(portRecpt, laddrRecept);
        multicastQuoteServerThread m_pingServerThread = new multicastQuoteServerThread(socketRecpt, this.getFrame());
        m_pingServerThread.start();
    }

    private void getEnvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getEnvBtnActionPerformed
        comUdp.sendData(GET_ENV_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
    }//GEN-LAST:event_getEnvBtnActionPerformed

    private void finBatonToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finBatonToggleBtnActionPerformed
        if (finBatonToggleBtn.isSelected()) {
            comUdp.sendData(SET_FIN_BATON_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
        } else {
            comUdp.sendData(SET_FIN_BATON_CMD, "01", globalCom.m_targetIp, globalCom.m_sendPort);
        }
    }//GEN-LAST:event_finBatonToggleBtnActionPerformed

    private void fastModeToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastModeToggleBtnActionPerformed

        if (fastModeToggleBtn.isSelected()) {
            comUdp.sendData(SET_FAST_MODE_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
        } else {
            comUdp.sendData(SET_FAST_MODE_CMD, "01", globalCom.m_targetIp, globalCom.m_sendPort);
        }

    }//GEN-LAST:event_fastModeToggleBtnActionPerformed

    private void posReposToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posReposToggleBtnActionPerformed
        if (posReposToggleBtn.isSelected()) {
            comUdp.sendData(SET_POS_REPOS_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
        } else {
            comUdp.sendData(SET_POS_REPOS_CMD, "01", globalCom.m_targetIp, globalCom.m_sendPort);
        }
    }//GEN-LAST:event_posReposToggleBtnActionPerformed

    private void magP1TxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_magP1TxtMouseClicked
        if (!magP1Txt.isEditable()) {
            magP1Txt.setEditable(true);
        }
    }//GEN-LAST:event_magP1TxtMouseClicked

    private void magP1TxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_magP1TxtFocusLost
        magP1Txt.setEditable(false);
    }//GEN-LAST:event_magP1TxtFocusLost

    private void magP1TxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magP1TxtActionPerformed
        comUdp.sendData(SET_MAGP1_CMD, magP1Txt.getText(), globalCom.m_targetIp, globalCom.m_sendPort);
        magP1Txt.setEditable(false);
    }//GEN-LAST:event_magP1TxtActionPerformed

    private void mainPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainPanelComponentShown
    }//GEN-LAST:event_mainPanelComponentShown

    private void cadenceTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cadenceTxtActionPerformed
    }//GEN-LAST:event_cadenceTxtActionPerformed

    private void dechetsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dechetsTxtActionPerformed
        dechetsTxt.setEditable(false);
    }//GEN-LAST:event_dechetsTxtActionPerformed

    private void videsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videsTxtActionPerformed
        videsTxt.setEditable(false);
    }//GEN-LAST:event_videsTxtActionPerformed

    private void poubelleTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_poubelleTxtActionPerformed
        poubelleTxt.setEditable(false);
    }//GEN-LAST:event_poubelleTxtActionPerformed

    private void totalAProduireTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_totalAProduireTxtActionPerformed
        totalAProduireTxt.setEditable(false);
    }//GEN-LAST:event_totalAProduireTxtActionPerformed

    private void dechetsTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dechetsTxtMouseClicked
        if (!dechetsTxt.isEditable()) {
            dechetsTxt.setEditable(true);
        }
    }//GEN-LAST:event_dechetsTxtMouseClicked

    private void videsTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_videsTxtMouseClicked
        if (!videsTxt.isEditable()) {
            videsTxt.setEditable(true);
        }
    }//GEN-LAST:event_videsTxtMouseClicked

    private void poubelleTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_poubelleTxtMouseClicked
        if (!poubelleTxt.isEditable()) {
            poubelleTxt.setEditable(true);
        }
    }//GEN-LAST:event_poubelleTxtMouseClicked

    private void totalAProduireTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_totalAProduireTxtMouseClicked
        if (!totalAProduireTxt.isEditable()) {
            totalAProduireTxt.setEditable(true);
        }
    }//GEN-LAST:event_totalAProduireTxtMouseClicked

    private void totalAProduireTxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_totalAProduireTxtFocusLost
        totalAProduireTxt.setEditable(false);
    }//GEN-LAST:event_totalAProduireTxtFocusLost

    private void poubelleTxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_poubelleTxtFocusLost
        poubelleTxt.setEditable(false);
    }//GEN-LAST:event_poubelleTxtFocusLost

    private void videsTxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_videsTxtFocusLost
        videsTxt.setEditable(false);
    }//GEN-LAST:event_videsTxtFocusLost

    private void dechetsTxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_dechetsTxtFocusLost
        dechetsTxt.setEditable(false);
    }//GEN-LAST:event_dechetsTxtFocusLost

    private void speedSliderMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speedSliderMousePressed
        speedSliderSet = true;
    }//GEN-LAST:event_speedSliderMousePressed

    private void speedSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speedSliderMouseReleased
        speedSliderSet = false;
    }//GEN-LAST:event_speedSliderMouseReleased

    private void controlLevelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controlLevelBtnActionPerformed
        if (controlLevelBtn.isSelected()) {
            comUdp.sendData(START_SUBMIT_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
        } else {
            comUdp.sendData(STOP_SUBMIT_CMD, "00", globalCom.m_targetIp, globalCom.m_sendPort);
        }
    }//GEN-LAST:event_controlLevelBtnActionPerformed

    private void plusMagTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusMagTxtActionPerformed
        int magP1 = Integer.valueOf(magP1Txt.getText()) + 1;
        magP1Txt.setText(Integer.toString(magP1));
    }//GEN-LAST:event_plusMagTxtActionPerformed

    private void minusMagTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusMagTxtActionPerformed
        int magP1 = Integer.valueOf(magP1Txt.getText()) - 1;
        magP1Txt.setText(Integer.toString(magP1));
    }//GEN-LAST:event_minusMagTxtActionPerformed

    private void plusDechetsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusDechetsTxtActionPerformed
        int tmp = Integer.valueOf(dechetsTxt.getText()) + 1;
        dechetsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusDechetsTxtActionPerformed

    private void minusDechetsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusDechetsTxtActionPerformed
        int tmp = Integer.valueOf(dechetsTxt.getText()) - 1;
        dechetsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minusDechetsTxtActionPerformed

    private void plusVidesTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusVidesTxtActionPerformed
        int tmp = Integer.valueOf(videsTxt.getText()) + 1;
        videsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusVidesTxtActionPerformed

    private void minusVidesTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusVidesTxtActionPerformed
        int tmp = Integer.valueOf(videsTxt.getText()) - 1;
        videsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minusVidesTxtActionPerformed

    private void plusPoubelleTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusPoubelleTxtActionPerformed
        int tmp = Integer.valueOf(poubelleTxt.getText()) + 1;
        poubelleTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusPoubelleTxtActionPerformed

    private void minuxPoubelleTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minuxPoubelleTxtActionPerformed
        int tmp = Integer.valueOf(poubelleTxt.getText()) - 1;
        poubelleTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minuxPoubelleTxtActionPerformed

    private void magP1TxtPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_magP1TxtPropertyChange
    }//GEN-LAST:event_magP1TxtPropertyChange

    private void magP1TxtInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_magP1TxtInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_magP1TxtInputMethodTextChanged

    private void setBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setBtnActionPerformed
        int addr = Integer.valueOf((String) regAddrCombo.getSelectedItem());
        int val = Integer.valueOf((String) regTxt.getText());
        comUdp.sendData(SET_REG_CMD, String.format("%04d%04d", addr, val), globalCom.m_targetIp, globalCom.m_sendPort);
    }//GEN-LAST:event_setBtnActionPerformed

    private void getBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getBtnActionPerformed
        Object szVal = regAddrCombo.getSelectedItem();
        comUdp.sendData(GET_REG_CMD, String.format("%04d", (Integer) szVal), globalCom.m_targetIp, globalCom.m_sendPort);
    }//GEN-LAST:event_getBtnActionPerformed

    private void getVarBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getVarBtnActionPerformed
        Object szVal = regTxt.getText();
        comUdp.sendData(GET_VAR_CMD, String.format("%s", (String) szVal), globalCom.m_targetIp, globalCom.m_sendPort);
    }//GEN-LAST:event_getVarBtnActionPerformed

    private void writeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeBtnActionPerformed
        try {
            writeFile("../conf/descRegister.txt");
        } catch (IOException ex) {
            Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_writeBtnActionPerformed

    private void plusBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusBtnActionPerformed
        if (objTableModel.getRowCount() > 0) {
            Object[] data = {String.format("%02d", m_selectedRow + 1), "no desc", new Boolean(false)};
            objTableModel.insertRow(m_selectedRow + 1, data);
        } else {
            Object[] data = {String.format("%02d", 0), "no desc", new Boolean(false)};
            objTableModel.insertRow(0, data);
        }
    }//GEN-LAST:event_plusBtnActionPerformed

    private void delBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delBtnActionPerformed
        if (objTableModel.getRowCount() > 0) {
            objTableModel.removeRow(m_selectedRow);
        }
    }//GEN-LAST:event_delBtnActionPerformed

    @Action
    public void connectSettings() {
        connectDlg obj = new connectDlg(this, true);
        obj.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cadenceTxt;
    private javax.swing.JTextField capsBonnesTxt;
    private javax.swing.JButton connectBtn;
    private javax.swing.JToggleButton controlLevelBtn;
    private javax.swing.JTextField dechetsTxt;
    private javax.swing.JButton delBtn;
    private javax.swing.JToggleButton fastModeToggleBtn;
    private javax.swing.JToggleButton finBatonToggleBtn;
    private javax.swing.JButton getBtn;
    private javax.swing.JButton getEnvBtn;
    private javax.swing.JButton getVarBtn;
    private javax.swing.JTable ioTable;
    public javax.swing.JTextField ipRmtTxt;
    private javax.swing.JTextField iplclTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextArea logTxt;
    private javax.swing.JTextField magP1Txt;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton minusDechetsTxt;
    private javax.swing.JButton minusMagTxt;
    private javax.swing.JButton minusVidesTxt;
    private javax.swing.JButton minuxPoubelleTxt;
    private javax.swing.JButton plusBtn;
    private javax.swing.JButton plusDechetsTxt;
    private javax.swing.JButton plusMagTxt;
    private javax.swing.JButton plusPoubelleTxt;
    private javax.swing.JButton plusVidesTxt;
    private javax.swing.JTextField portLclTxt;
    public javax.swing.JTextField portRmtTxt;
    private javax.swing.JToggleButton posReposToggleBtn;
    private javax.swing.JTextField poubelleTxt;
    private javax.swing.JComboBox regAddrCombo;
    private javax.swing.JTextField regTxt;
    private javax.swing.JTextField resteAProduireTxt;
    private javax.swing.JButton setBtn;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JTextField totalAProduireTxt;
    private javax.swing.JTable varTable;
    private javax.swing.JTextField videsTxt;
    private javax.swing.JButton writeBtn;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
