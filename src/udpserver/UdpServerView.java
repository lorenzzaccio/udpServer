/*
 * UdpServerView.java
 */

package udpserver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import quoteClient.quoteClient;
import quoteServer.QuoteServerThread;
import udp.comUdp;
import udp.protocole;
import util.*;

/**
 * The application's main frame.
 */
public class UdpServerView extends FrameView implements protocole {

    quoteClient m_clientThread ;
    boolean bSendReq = false;
    private boolean speedSliderSet=false;
    public String m_localIp,m_targetIp,m_recptPort,m_sendPort;
    private String m_oldMagP1="0";
   // private MyVerifier verifier = new MyVerifier();

    public UdpServerView(SingleFrameApplication app) throws IOException {
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
                    String text = (String)(evt.getNewValue());
                    //statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    //progressBar.setVisible(true);
                    //progressBar.setIndeterminate(false);
                    //progressBar.setValue(value);
                }
            }
        });

        //magP1Txt.setInputVerifier(verifier);


         magP1Txt.setText(m_oldMagP1);
        // Listen for changes in the text
    magP1Txt.getDocument().addDocumentListener(new DocumentListener() {


        public void changedUpdate(DocumentEvent e) {
             System.out.println("echo");
        // text was changed
                    //check value
        if(!util.isNum((String) magP1Txt.getText()))
            magP1Txt.setText(m_oldMagP1);
        else
            m_oldMagP1=magP1Txt.getText();
        }
        public void removeUpdate(DocumentEvent e) {
        // text was deleted
             System.out.println("echo1");
             // m_oldMagP1=magP1Txt.getText();
        }
        public void insertUpdate(DocumentEvent e) {
            System.out.println("echo2");
        // text was inserted
            if((!util.isNum((String) magP1Txt.getText()))||(!util.isPositive((String) magP1Txt.getText())))
            magP1Txt.setText(m_oldMagP1);
        else
            m_oldMagP1=magP1Txt.getText();
        }
    });



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
        //localAddrTxt.setText(addr.toString().split("/")[1]);
        m_localIp = addr.toString().split("/")[1];
        iplclTxt1.setText(m_localIp);

        m_targetIp = ipRmtTxt1.getText();// + "." + ipRmtTxt2.getText() + "." + ipRmtTxt3.getText() + "." + ipRmtTxt4.getText(); //"192.168.0.2";
        m_recptPort = portLclTxt.getText();//
        m_sendPort=portRmtTxt.getText();

    }

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
        iplclTxt1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        portLclTxt = new javax.swing.JTextField();
        ipRmtTxt1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        portRmtTxt = new javax.swing.JTextField();
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

        iplclTxt1.setText(resourceMap.getString("iplclTxt1.text")); // NOI18N
        iplclTxt1.setName("iplclTxt1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        portLclTxt.setText(resourceMap.getString("portLclTxt.text")); // NOI18N
        portLclTxt.setName("portLclTxt"); // NOI18N

        ipRmtTxt1.setText(resourceMap.getString("ipRmtTxt1.text")); // NOI18N
        ipRmtTxt1.setName("ipRmtTxt1"); // NOI18N

        jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
        jLabel3.setName("jLabel3"); // NOI18N

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setName("jLabel4"); // NOI18N

        portRmtTxt.setText(resourceMap.getString("portRmtTxt.text")); // NOI18N
        portRmtTxt.setName("portRmtTxt"); // NOI18N

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(connectBtn)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(49, 49, 49)
                                .add(posReposToggleBtn)
                                .add(6, 6, 6)
                                .add(finBatonToggleBtn))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(iplclTxt1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 142, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(fastModeToggleBtn))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(16, 16, 16)
                                .add(jLabel2)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(portLclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 453, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(getEnvBtn)
                                .add(81, 81, 81)
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel12)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(190, 190, 190)
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
                                .add(plusPoubelleTxt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(minuxPoubelleTxt))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(plusVidesTxt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(minusVidesTxt))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(plusMagTxt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(minusMagTxt))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(plusDechetsTxt)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(minusDechetsTxt)))))
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(145, 145, 145)
                        .add(controlLevelBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 110, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(25, 25, 25)
                        .add(speedSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(221, 221, 221))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                        .add(14, 14, 14)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ipRmtTxt1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                        .add(18, 18, 18)
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(portRmtTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 59, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(59, 59, 59))))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(16, 16, 16)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(77, 77, 77)
                                .add(speedSlider, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 338, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(mainPanelLayout.createSequentialGroup()
                                .add(connectBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(52, 52, 52)
                                .add(controlLevelBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(32, 32, 32)
                                .add(getEnvBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(mainPanelLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel1)
                            .add(iplclTxt1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2)
                            .add(portLclTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3)
                            .add(ipRmtTxt1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel4)
                            .add(portRmtTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(posReposToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(finBatonToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(fastModeToggleBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 38, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel5)
                            .add(magP1Txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(plusMagTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(minusMagTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel6)
                            .add(cadenceTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        JSlider source = (JSlider)evt.getSource();
    if (!source.getValueIsAdjusting()) {
        int fps = (int)source.getValue();
        /*
         if (fps == 0) {
            if (!frozen) stopAnimation();
        } else {
            delay = 1000 / fps;
            timer.setDelay(delay);
            timer.setInitialDelay(delay * 10);
            if (frozen) startAnimation();
        }*/
        comUdp.sendData(SET_SPEED_CMD,String.format("%4d", fps),m_targetIp,m_sendPort );
    }
    }//GEN-LAST:event_speedSliderStateChanged

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        try {
            //m_clientThread.m_bSendReq=true;
            //init socket
            DatagramSocket socket;
            DatagramSocket socketRecpt;
            //int sendPort = 5000;
            int portRecpt ;
            //send
            //sendPort = Integer.valueOf(targetPortTxt.getText());
            InetAddress laddr = InetAddress.getByName(m_localIp);
            //socket = new DatagramSocket( sendPort,laddr);
            //reception
            m_recptPort = portLclTxt.getText();
            portRecpt = Integer.valueOf(m_recptPort);
            InetAddress laddrRecept = InetAddress.getByName(m_localIp);
            socketRecpt = new DatagramSocket(portRecpt, laddrRecept);
            //start server thread
            QuoteServerThread m_serverThread = new QuoteServerThread(socketRecpt,this.getFrame());
            m_serverThread.setRefLogui(logTxt);
            m_serverThread.setToken(bSendReq);
            m_serverThread.setRefAddress(m_localIp);
            m_serverThread.setRefUI( cadenceTxt, totalAProduireTxt, magP1Txt,  capsBonnesTxt, dechetsTxt, videsTxt, poubelleTxt, speedSlider,speedSliderSet);

            m_serverThread.start();


            //start client thread
         /*   m_clientThread = new quoteClient(socketRecpt);
            m_clientThread.setRefAddress(m_targetIp);
            m_clientThread.setRefPort(m_sendPort);
            m_clientThread.setRefLogUi(logTxt);
            //m_clientThread.setToken(bSendReq);
            
            m_clientThread.start();*/

        } catch (IOException ex) {
            Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }//GEN-LAST:event_connectBtnActionPerformed

    private void getEnvBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getEnvBtnActionPerformed
        comUdp.sendData(GET_ENV_CMD,"00",m_targetIp,m_sendPort);
    }//GEN-LAST:event_getEnvBtnActionPerformed

    private void finBatonToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finBatonToggleBtnActionPerformed
        if(finBatonToggleBtn.isSelected()){
            comUdp.sendData(SET_FIN_BATON_CMD,"00",m_targetIp,m_sendPort );
        }else{
            comUdp.sendData(SET_FIN_BATON_CMD,"01",m_targetIp,m_sendPort );
        }
    }//GEN-LAST:event_finBatonToggleBtnActionPerformed

    private void fastModeToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fastModeToggleBtnActionPerformed

        if(fastModeToggleBtn.isSelected()){
            comUdp.sendData(SET_FAST_MODE_CMD,"00",m_targetIp,m_sendPort );
        }else{
            comUdp.sendData(SET_FAST_MODE_CMD,"01",m_targetIp,m_sendPort );
        }

    }//GEN-LAST:event_fastModeToggleBtnActionPerformed

    private void posReposToggleBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posReposToggleBtnActionPerformed
        if(posReposToggleBtn.isSelected()){
            comUdp.sendData(SET_POS_REPOS_CMD,"00",m_targetIp,m_sendPort );
        }else{
            comUdp.sendData(SET_POS_REPOS_CMD,"01",m_targetIp,m_sendPort );
        }
    }//GEN-LAST:event_posReposToggleBtnActionPerformed

    private void magP1TxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_magP1TxtMouseClicked
       if(!magP1Txt.isEditable())
        magP1Txt.setEditable(true);
    }//GEN-LAST:event_magP1TxtMouseClicked

    private void magP1TxtFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_magP1TxtFocusLost
        magP1Txt.setEditable(false);
    }//GEN-LAST:event_magP1TxtFocusLost

    private void magP1TxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magP1TxtActionPerformed
        comUdp.sendData(SET_MAGP1_CMD,magP1Txt.getText(),m_targetIp,m_sendPort );
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
         if(!dechetsTxt.isEditable())
        dechetsTxt.setEditable(true);
    }//GEN-LAST:event_dechetsTxtMouseClicked

    private void videsTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_videsTxtMouseClicked
         if(!videsTxt.isEditable())
        videsTxt.setEditable(true);
    }//GEN-LAST:event_videsTxtMouseClicked

    private void poubelleTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_poubelleTxtMouseClicked
        if(!poubelleTxt.isEditable())
        poubelleTxt.setEditable(true);
    }//GEN-LAST:event_poubelleTxtMouseClicked

    private void totalAProduireTxtMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_totalAProduireTxtMouseClicked
        if(!totalAProduireTxt.isEditable())
        totalAProduireTxt.setEditable(true);
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
        speedSliderSet=true;
    }//GEN-LAST:event_speedSliderMousePressed

    private void speedSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_speedSliderMouseReleased
        speedSliderSet=false;
    }//GEN-LAST:event_speedSliderMouseReleased

    private void controlLevelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_controlLevelBtnActionPerformed
       if(controlLevelBtn.isSelected()){
            comUdp.sendData(START_SUBMIT_CMD,"00",m_targetIp,m_sendPort );
        }else{
            comUdp.sendData(STOP_SUBMIT_CMD,"00",m_targetIp,m_sendPort );
        }
    }//GEN-LAST:event_controlLevelBtnActionPerformed

    private void plusMagTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusMagTxtActionPerformed
        int magP1 = Integer.valueOf(magP1Txt.getText())+1;
        magP1Txt.setText(Integer.toString(magP1));
    }//GEN-LAST:event_plusMagTxtActionPerformed

    private void minusMagTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusMagTxtActionPerformed
        int magP1 = Integer.valueOf(magP1Txt.getText())-1;
        magP1Txt.setText(Integer.toString(magP1));
    }//GEN-LAST:event_minusMagTxtActionPerformed

    private void plusDechetsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusDechetsTxtActionPerformed
        int tmp = Integer.valueOf(dechetsTxt.getText())+1;
        dechetsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusDechetsTxtActionPerformed

    private void minusDechetsTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusDechetsTxtActionPerformed
       int tmp = Integer.valueOf(dechetsTxt.getText())-1;
        dechetsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minusDechetsTxtActionPerformed

    private void plusVidesTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusVidesTxtActionPerformed
        int tmp = Integer.valueOf(videsTxt.getText())+1;
        videsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusVidesTxtActionPerformed

    private void minusVidesTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minusVidesTxtActionPerformed
        int tmp = Integer.valueOf(videsTxt.getText())-1;
        videsTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minusVidesTxtActionPerformed

    private void plusPoubelleTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plusPoubelleTxtActionPerformed
        int tmp = Integer.valueOf(poubelleTxt.getText())+1;
        poubelleTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_plusPoubelleTxtActionPerformed

    private void minuxPoubelleTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minuxPoubelleTxtActionPerformed
        int tmp = Integer.valueOf(poubelleTxt.getText())-1;
        poubelleTxt.setText(Integer.toString(tmp));
    }//GEN-LAST:event_minuxPoubelleTxtActionPerformed

    private void magP1TxtPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_magP1TxtPropertyChange

    }//GEN-LAST:event_magP1TxtPropertyChange

    private void magP1TxtInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_magP1TxtInputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_magP1TxtInputMethodTextChanged

    @Action
    public void connectSettings() {
        connectDlg obj = new connectDlg(this,true);
        obj.setVisible(true);
    }


    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField cadenceTxt;
    private javax.swing.JTextField capsBonnesTxt;
    private javax.swing.JButton connectBtn;
    private javax.swing.JToggleButton controlLevelBtn;
    private javax.swing.JTextField dechetsTxt;
    private javax.swing.JToggleButton fastModeToggleBtn;
    private javax.swing.JToggleButton finBatonToggleBtn;
    private javax.swing.JButton getEnvBtn;
    private javax.swing.JTextField ipRmtTxt1;
    private javax.swing.JTextField iplclTxt1;
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
    private javax.swing.JTextArea logTxt;
    private javax.swing.JTextField magP1Txt;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton minusDechetsTxt;
    private javax.swing.JButton minusMagTxt;
    private javax.swing.JButton minusVidesTxt;
    private javax.swing.JButton minuxPoubelleTxt;
    private javax.swing.JButton plusDechetsTxt;
    private javax.swing.JButton plusMagTxt;
    private javax.swing.JButton plusPoubelleTxt;
    private javax.swing.JButton plusVidesTxt;
    private javax.swing.JTextField portLclTxt;
    private javax.swing.JTextField portRmtTxt;
    private javax.swing.JToggleButton posReposToggleBtn;
    private javax.swing.JTextField poubelleTxt;
    private javax.swing.JTextField resteAProduireTxt;
    private javax.swing.JSlider speedSlider;
    private javax.swing.JTextField totalAProduireTxt;
    private javax.swing.JTextField videsTxt;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}
