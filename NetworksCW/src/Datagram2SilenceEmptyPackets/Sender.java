package Datagram2SilenceEmptyPackets;

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
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender implements Runnable{
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

            Vector<DatagramPacket> uninterleaved = new Vector<DatagramPacket>();
            Vector<DatagramPacket> interleaved = new Vector<DatagramPacket>();

            int d = 3;
            int totalSize = 9;
            int seqnum = 0;
            int interleaveCount = 0;
            int totalNumPacketsSent = 0;
            long runningTime = 0;
            int packetsSent = 0;
            boolean running = true;
            while (running == true) {
                try {
                    byte[] block = new byte[512];
                    while (running == true) {
                        Date startTime = new Date();
                        //record sound
                        block = recorder.getBlock();

                        //get current time
                        Date date = new Date();
                        long time = date.getTime();

                        //byte buffer: 4 bytes for seq num, 8 bytes for timestamp,512 for sound
                        ByteBuffer b = ByteBuffer.allocate(524);

                        b.putInt(seqnum);
                        b.putLong(time);
                        b.put(block);

                        //byte array
                        byte[] tosend = b.array();

                        //Packet datagram with byte array, lenght of the array, destination IP, Port;
                        DatagramPacket packet = new DatagramPacket(tosend, tosend.length, clientIP, PORT);
                        //sending_socket3.send(packet);

                        /**
                         * --INTERLEAVING SECTION-- *
                         */
                        if (uninterleaved.size() < totalSize) {
                            //add packet to uninterleaved block;
                            uninterleaved.add(interleaveCount, packet);
                            interleaveCount++;
                            seqnum++;

                        } else if (uninterleaved.size() == totalSize) {
                            interleaveCount = 0; //reset counter for next batch

                            //do interleaving
                            interleaved = interleave(d, uninterleaved);
                            //send the packets;
                            for (int i = 0; i < totalSize; i++) {
                                sending_socket2.send(interleaved.get(i));
                                Date endTime = new Date();
                                runningTime += endTime.getTime() - startTime.getTime();
                                packetsSent += 1;
                                if(runningTime >= 1000)
                                {
                                    System.out.println("-_-_-_-_-_-");
                                    System.out.println("BitRate= " + (packetsSent * tosend.length * 8));
                                    System.out.println("-_-_-_-_-_-");
                                    runningTime = 0;
                                    packetsSent = 0;
                                }
                                totalNumPacketsSent++;
                                if (totalNumPacketsSent == 2000) {
                                    running = false;
                                }
                            }

                            //reset vectors for next packet batch
                            interleaved = new Vector<DatagramPacket>();
                            uninterleaved = new Vector<DatagramPacket>();

                            //reset sequence number back to 0
                            seqnum = 0;
                        }
                    }
                } catch (IOException e) {
                    System.out.println("ERROR: TextSender: Some random IO error occured!");
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

    /*
     * Interleaves a Vector containing Datagram Packets 90 degrees anti-clockwise
     * @param d size of vector
     * @param unInterleaved vector to interleave
     * @return an interleaved vector.
     */
    public Vector<DatagramPacket> interleave(int d, Vector<DatagramPacket> uninterleaved) {
        Vector<DatagramPacket> interleaved = new Vector<DatagramPacket>();
        for (int i = 0; i < d; i++) {
            for (int j = 0; j < d; j++) {
                int index = i * d + j;
                int indexUnInterleaved = j * d + (d - 1 - i);
                interleaved.add(index, uninterleaved.get(indexUnInterleaved));
            }
        }

        return interleaved;
    }
}
