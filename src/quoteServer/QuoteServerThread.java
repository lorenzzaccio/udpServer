/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package quoteServer;

/**
 *
 * @author lorenzzaccio
 */
/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import dataTable.tableMap;
import dataTable.varTableModel;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import udp.comUdp;
import udp.globalCom;
import udp.protocole;
import udpserver.UdpServerView;

public class QuoteServerThread extends Thread implements protocole, tableMap {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean recptLoop = true;
    private JTextArea m_trace;
    private boolean m_bSendReq = false;
    public String[] getVal;
    public byte[] bVal = new byte[20];
    private JTextField m_cadenceTxt;
    private JTextField m_bonnesTxt;
    private JTextField m_magP1Txt;
    private JTextField m_totalAProduireTxt;
    private JTextField m_videsTxt;
    private JTextField m_dechetsTxt;
    private JTextField m_poubelleTxt;
    private JTextField m_resteAProduireTxt;
    private JSlider m_speedSlider;
    private boolean m_speedSliderSet;
    JFrame parent;
    private UdpServerView m_parent;
    private String m_startByte;
    private int m_cmd;
    private int m_length;
    private String[] m_val;
    private String m_endByte;
    private InetAddress m_hostAddress;
    private int m_hostPort;

    public QuoteServerThread(DatagramSocket sock, JFrame parent) throws IOException {
        this("QuoteServerThread", sock, parent);
    }

    public QuoteServerThread(String name, DatagramSocket sock, JFrame par) throws IOException {
        super(name);
        socket = sock;
        parent = par;

        try {
            in = new BufferedReader(new FileReader("one-liners.rtf"));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open quote file. Serving time instead.");
        }
    }

    @Override
    public void run() {
        while (recptLoop) {
            try {
                try {
                    byte[] buf = new byte[256];
                    //init reception buffer
                    for (int i = 0; i < 256; i++) {
                        buf[i] = 00;
                    }
                    // receive request
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    m_trace.append("Data Received :" + new String(packet.getData()) + "\n");
                    byte[] toto = packet.getData();
                    m_hostAddress = packet.getAddress();
                    m_hostPort = packet.getPort();
                    m_val = new String[packet.getLength() - 8];

                    m_startByte = String.valueOf((char) toto[0]) + String.valueOf((char) toto[1]);
                    m_cmd = Integer.valueOf(String.valueOf((char) toto[2]) + String.valueOf((char) toto[3]));
                    m_length = Integer.valueOf(String.valueOf((char) toto[4]) + String.valueOf((char) toto[5]) + String.valueOf((char) toto[6]) + String.valueOf((char) toto[7]));

                    char[] dataBuf = new char[m_length];
                    int k = 0;
                    for (int i = HEADER_SIZE; i < (HEADER_SIZE + m_length); i++) {
                        dataBuf[k] = (char) toto[i];
                        k++;
                    }

                    m_endByte = String.valueOf((char) toto[packet.getLength() - 2]) + String.valueOf((char) toto[packet.getLength() - 1]);
                    handleReception(dataBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                    recptLoop = false;
                }
                sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(QuoteServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        socket.close();
    }

    private void handleReception(char[] dataBuf) {
        String[] splitter;
        varTableModel tm = (varTableModel) m_parent.objVarModel;
        if ((Integer.valueOf(m_startByte) == 23) && (Integer.valueOf(m_endByte) == 32)) {
            String data = "";
            data = String.copyValueOf(dataBuf);

            switch (m_cmd) {
                case PING_CMD:
                    if (globalCom.m_searchingRobot) {
                        comUdp.sendData(SET_CLIENT_CONNECTION_CMD, globalCom.m_recptPort, m_hostAddress.getHostAddress(), Integer.toString(m_hostPort));
                        m_parent.portRmtTxt.setText(Integer.toString(m_hostPort));
                        globalCom.m_sendPort = Integer.toString(m_hostPort);
                        m_parent.ipRmtTxt.setText(m_hostAddress.getHostAddress());
                        globalCom.m_targetIp = m_hostAddress.getHostAddress();
                        globalCom.m_searchingRobot = false;
                    }
                    break;
                case GET_REG_CMD:
                    int line = Integer.valueOf(data.substring(0, 4));
                    String val = data.substring(4, 8);
                    m_parent.objTableModel.setValueBoolAt(val, line - 1, VALUE_COL, false);
                    break;
                case GET_VAR_CMD:
                    splitter = data.split("=");
                    for (int i = 0; i < tm.getRowCount(); i++) {
                        if (tm.getValueAt(i, ADDRESS_COL).equals(splitter[0])) {
                            tm.setValueVarAt(splitter[1], i, VALUE_COL, false);
                        }
                    }
                    break;
                case GET_ENV_CMD:
                    splitter = data.split("-");
                    updateUi(splitter);
                    break;
                case SET_CLIENT_CONNECTION_CMD:
                    globalCom.m_searchingRobot = false;
                    break;

            }
        }
    }

    public void setRefLogui(JTextArea logTxt) {
        m_trace = logTxt;
    }

    private String getCmdResult(String strNum) {
        int ret;

        switch (Integer.valueOf(strNum)) {
            case 0:
                ret = 15300;
                break;

            case 1:
                ret = 32;
                break;

            case 2:
                ret = 1;
                break;

            case 3:
                ret = 5;
                break;

            case 4:
                ret = 12;
                break;

            case 5:
                ret = 123;
                break;

            default:
                ret = 99999;
                break;

        }
        return Integer.toString(ret);
    }

    public void setRefUI(JTextField cadence, JTextField totalAProduire, JTextField magP1, JTextField bonnes, JTextField dechets, JTextField vides, JTextField poubelle, JSlider speedSlider, boolean speedSliderSet) {
        m_cadenceTxt = cadence;
        m_bonnesTxt = bonnes;
        m_dechetsTxt = dechets;
        m_videsTxt = vides;
        m_poubelleTxt = poubelle;
        m_speedSlider = speedSlider;
        m_totalAProduireTxt = totalAProduire;
        m_magP1Txt = magP1;
        m_speedSliderSet = speedSliderSet;
    }

    public void setRefParent(UdpServerView parent) {
        m_parent = parent;
    }

    public void setToken(boolean bSendReq) {
        m_bSendReq = bSendReq;
    }

    public void updateUi(String[] val) {
        m_cadenceTxt.setText(val[0].split("=")[1]);
        if (!m_bonnesTxt.isEditable()) {
            m_bonnesTxt.setText(val[3].split("=")[1]);
        }

        if (!m_dechetsTxt.isEditable()) {
            m_dechetsTxt.setText(val[4].split("=")[1]);
        }

        if (!m_videsTxt.isEditable()) {
            m_videsTxt.setText(val[5].split("=")[1]);
        }

        if (!m_poubelleTxt.isEditable()) {
            m_poubelleTxt.setText(val[6].split("=")[1]);
        }

        //m_resteAProduireTxt.setText(  getVal[7].split("=")[1]);
        if (!m_totalAProduireTxt.isEditable()) {
            m_totalAProduireTxt.setText(val[1].split("=")[1]);
        }

        if (!m_magP1Txt.isEditable()) {
            m_magP1Txt.setText(val[2].split("=")[1]);
        }

        if (!m_speedSliderSet) {
            String szval = val[7].trim().split("=")[1];
            m_speedSlider.setValue(Integer.valueOf(szval));
        }
    }
}
