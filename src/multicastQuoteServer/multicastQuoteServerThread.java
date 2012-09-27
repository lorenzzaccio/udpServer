/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package multicastQuoteServer;

/**
 *
 * @author laurentgarnier
 */

import java.io.*;

import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import udp.protocole;

 

public class multicastQuoteServerThread extends Thread implements protocole  {

 

    private long FIVE_SECONDS = 5000;

    public boolean bDevicePinged = false;
    DatagramSocket socket;
    private final JFrame parent;

    public multicastQuoteServerThread(DatagramSocket sock, JFrame parent) throws IOException {
       this("multicastQuoteServerThread", sock, parent);
       socket = sock;
    }

    public multicastQuoteServerThread(String name, DatagramSocket sock, JFrame par) throws IOException {
        super(name);
        socket = sock;
        parent = par;
    }
    
    @Override
    public void run() {

        while (bDevicePinged) {
            try {
                byte[] buf = new byte[256];
                    //init reception buffer
                    for (int i = 0; i < 256; i++) {
                        buf[i] = 00;
                    }
                // construct quote

                String dString = null;

                dString = "ping";

                buf = dString.getBytes();

 

                // send it

                InetAddress group = InetAddress.getByName("192.168.1.255");
                int portTarget = 5001;
                DatagramPacket packet = new DatagramPacket(buf, buf.length,group, portTarget);

                socket.send(packet);
                sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(multicastQuoteServerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(multicastQuoteServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        socket.close();

    }

}