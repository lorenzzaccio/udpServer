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
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class QuoteServerThread extends Thread implements protocole {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean recptLoop = true;
    private JTextArea m_trace;
    private String m_strAddress;
    private String m_strLocalPort;
    private boolean m_bSendReq = false;
    public String[] getVal = new String[20];
    public byte[] bVal = new byte[20];
    private int[] m_getVal;
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
                    m_trace.append("Packet Received \n");
                    m_trace.append("Data is :" + new String(packet.getData()) + "\n");
                    byte[] toto = packet.getData();
                    getVal[0] = String.valueOf((char) toto[0]);
                    getVal[1] = String.valueOf((char) toto[1]);
                    getVal[2] = String.valueOf((char) toto[2]);
                    getVal[3] = String.valueOf((char) toto[3]);
                    getVal[4] = String.valueOf((char) toto[4]);
                    getVal[5] = String.valueOf((char) toto[5]);
                    getVal[6] = String.valueOf((char) toto[6]);
                    getVal[7] = String.valueOf((char) toto[7]);
                    getVal[8] = String.valueOf((char) toto[8]);
                    getVal[9] = String.valueOf((char) toto[9]);
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

    protected String getNextQuote() {
        String returnValue = null;
        try {
            if ((returnValue = in.readLine()) == null) {
                in.close();
                recptLoop = false;
                returnValue = "No more quotes. Goodbye.";
            }
        } catch (IOException e) {
            returnValue = "IOException occurred in server.";
        }
        return returnValue;
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

    public void setRefAddress(String localAddrTxt,String localPortTxt) {
        m_strAddress = localAddrTxt;
        m_strLocalPort = localPortTxt;
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

    public void setToken(boolean bSendReq) {
        m_bSendReq = bSendReq;
    }

    public void updateUi(String[] val) {
        m_cadenceTxt.setText(getVal[0]);
        if (!m_bonnesTxt.isEditable()) {
            m_bonnesTxt.setText(getVal[3]);
        }

        if (!m_dechetsTxt.isEditable()) {
            m_dechetsTxt.setText(getVal[4]);
        }

        if (!m_videsTxt.isEditable()) {
            m_videsTxt.setText(getVal[5]);
        }

        if (!m_poubelleTxt.isEditable()) {
            m_poubelleTxt.setText(getVal[6]);
        }

        //m_resteAProduireTxt.setText(  getVal[7]);
        if (!m_totalAProduireTxt.isEditable()) {
            m_totalAProduireTxt.setText(getVal[1]);
        }

        if (!m_magP1Txt.isEditable()) {
            m_magP1Txt.setText(getVal[2]);
        }

        if (!m_speedSliderSet) {
            String szval = getVal[7].trim();
            m_speedSlider.setValue(Integer.valueOf(szval));
        }
    }
}
