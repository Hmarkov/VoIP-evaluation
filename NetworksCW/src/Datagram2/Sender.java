package Datagram2;

import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender implements Runnable {
    static DatagramSocket2 sending_socket2;


    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        //Port number
        int PORT = 8000;
        //IP address
        InetAddress clientIP = null;
        try {
            clientIP = InetAddress.getByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println("ERROR: TextSender: Could not find client IP");
            e.printStackTrace();
            System.exit(0);
        }

        try {
            sending_socket2 = new DatagramSocket2();

        } catch (SocketException e) {
            System.out.println("ERROR: TextSender: Could not open UDP socket to send from.");
            e.printStackTrace();
            System.exit(0);
        }
        try {
            AudioRecorder recorder = new AudioRecorder();
            int seqnum = 0;
            long runningTime = 0;
            int packetsSent = 0;

            boolean running = true;
            while (running == true) {
                try {
                    byte[] block = new byte[512];
                    while (running == true) {
                        Date startTime = new Date();
                        //Record sound
                        block = recorder.getBlock();

                        ///Current time
                        Date date = new Date();
                        long time = date.getTime();

                        //byte buffer: 4 bytes for seq num, 8 bytes for timestamp,512 for sound
                        ByteBuffer b = ByteBuffer.allocate(524);
                        b.putInt(seqnum);
                        b.putLong(time);
                        b.put(block);

                        //byte array
                        byte[] tosend = b.array();//bytebuffer with info to send to the receiver

                        //Packet datagram with byte array, lenght of the array, destination IP, Port;
                        DatagramPacket packet = new DatagramPacket(tosend, tosend.length, clientIP, PORT);
                        sending_socket2.send(packet);
                        //Gets running time
                        Date endTime = new Date();
                        runningTime += endTime.getTime() - startTime.getTime();
                        packetsSent += 1;
                        if(runningTime >= 1000)
                        {
                            System.out.println("-.-.-.-.-.-");
                            System.out.println("BitRate = " + (packetsSent * tosend.length * 8));
                            System.out.println("-.-.-.-.-.-");
                            runningTime = 0;
                            packetsSent = 0;
                        }
                        seqnum++;

                        if (seqnum == 2000) {
                            running = false;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("IO Exception error");
                    e.printStackTrace();
                }
            }
            //recorder.close();

            //Close the socket
            sending_socket2.close();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
