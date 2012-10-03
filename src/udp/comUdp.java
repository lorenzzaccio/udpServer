/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import udpserver.UdpServerView;

/**
 *
 * @author lorenzzaccio
 */
public class comUdp implements protocole{


    public static  void sendData(int cmd, String arg,String targetAddress,String sendPort){
        DatagramSocket socket = null;
         int lengthArg=0;
         if (arg==null)
             lengthArg=0;
         else
             lengthArg = arg.length();

         try {
            socket = new DatagramSocket();
            // send request
            byte[] buf = new byte[256];
            switch (cmd){

                case SET_VAR_CMD:
                    //lengthArg=1;
                    break;

                case SET_SPEED_CMD:

                    break;

                default:
                    break;
                         }

            String argString=arg;
            //logTxt.append("target address : " + targetAddrTxt.getText() + "\n");
            InetAddress address = InetAddress.getByName(targetAddress);
            buf = (Integer.toString(START_BYTE) +Integer.toString(cmd) + String.format("%04d",lengthArg) +argString + Integer.toString(END_BYTE)).getBytes();

            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, Integer.valueOf(sendPort));
            socket.send(packet);
        } catch (IOException ex) {
            Logger.getLogger(UdpServerView.class.getName()).log(Level.SEVERE, null, ex);
        }
         socket.close();
    }


}
