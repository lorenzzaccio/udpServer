/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package quoteClient;

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
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import udp.comUdp;
import udp.protocole;

public class quoteClient extends Thread implements protocole {
    boolean abort=false;
    protected DatagramSocket socket = null;
    private String m_strAddress;
    private JTextArea m_trace;
    public boolean m_bSendReq=false;
    private JComboBox m_cmdCombo;
    private String m_strPort;

    public quoteClient(DatagramSocket sock) throws IOException {
	this("quoteClient",sock);
    }

    public quoteClient(String name,DatagramSocket sock) throws IOException {
        super(name);
        socket=sock;
    }

    @Override
    public void run() {
        m_trace.append("Client thread started\n");
        
        while (!abort) {
            
                try {
                    //if (m_bSendReq) {
                        
                        // get a datagram socket
                        //socket = new DatagramSocket();
                        // send request
                        //byte[] buf = new byte[256];
                        //m_trace.append("target address : " + m_strAddress.getText() + "\n");
                        //InetAddress address = InetAddress.getByName(m_strAddress.getText());
                        //buf = (Integer.toString(GET_ENV_CMD)).getBytes();
                        //DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5000);
                        //socket.send(packet);
                        comUdp.sendData(GET_ENV_CMD, "00", m_strAddress, m_strPort);
                        
                        // get response
                        //byte[] bufRecept = new byte[256];
                        //m_trace.setText("waiting for answer ...\n");
                        //m_trace.append("waiting for answer ...\n");
                        //DatagramPacket packet = new DatagramPacket(bufRecept, bufRecept.length);
                        //socket.receive(packet);
                        // display response
                        //String received = new String(packet.getData()); //new String(packet.getData(), 0, packet.getLength());
                        //m_trace.append("Quote of the Moment: " + received + "\n");
                       // m_bSendReq = false;
                    //}
                
                sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(quoteClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        m_trace.setText("Client thread finished\n");
        socket.close();
    }

    public void setRefAddress(String addressTxt) {
        m_strAddress = addressTxt;
    }
    public void setRefPort(String portTxt) {
        m_strPort = portTxt;
    }

    public void setRefLogUi(JTextArea logTxt) {
        m_trace = logTxt;
    }

    public void setRefCmdUi(JComboBox cmdCombo) {
        m_cmdCombo = cmdCombo;
    }

    public void setToken(boolean bSendReq) {
        m_bSendReq= bSendReq;
    }


}