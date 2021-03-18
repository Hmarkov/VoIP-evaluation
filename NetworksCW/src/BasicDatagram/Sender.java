package BasicDatagram;


/**
 *
 * @author  abj
 */
import CMPC3M06.AudioRecorder;
import uk.ac.uea.cmp.voip.DatagramSocket2;
import uk.ac.uea.cmp.voip.DatagramSocket3;

import javax.sound.sampled.LineUnavailableException;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

class Sender implements Runnable{

    static DatagramSocket sending_socket;

    static int socketNum;
    public Sender(int sn){
        socketNum = sn;
    }

    public void start()
    {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run ()
    {
        //Port number
        int PORT = 8000;
        //IP address
        InetAddress clientIP = null;
        try
        {
            clientIP = InetAddress.getByName("localhost");
        }
        catch (UnknownHostException e)
        {
            System.out.println("Client IP not found");
            e.printStackTrace();
            System.exit(0);
        }
        try
        {
            switch(socketNum){
                case(1):
                    sending_socket = new DatagramSocket();
                    break;
                case(2):
                    sending_socket = new DatagramSocket2();
                    break;
                case(3):
                    sending_socket = new DatagramSocket3();
                    break;
            }
        }
        catch (SocketException e)
        {
            System.out.println("Socket UDP cannot be open");
            e.printStackTrace();
            System.exit(0);
        }
        try
        {
            AudioRecorder recorder = new AudioRecorder();
            int seqnum = 0;
            boolean running = true;
            long runningTime = 0;
            int packetsSent = 0;
            while (running == true)
            {
                try
                {
                    byte[] block  = new byte[512];
                    while(running ==  true)
                    {
                        Date startTime = new Date();
                        //Record sound
                        block = recorder.getBlock();

                        //Current time
                        Date date= new Date();
                        long time = date.getTime();

                        //byte buffer: 4 bytes for seq num, 8 bytes for timestamp,512 for sound
                        ByteBuffer b = ByteBuffer.allocate(524);
                        b.putInt(seqnum);
                        b.putLong(time);
                        b.put(block);

                        //byte array
                        byte [] tosend = b.array();//bytebuffer with info to send to the receiver

                        //Packet datagram with byte array, lenght of the array, destination IP, Port;
                        DatagramPacket packet = new DatagramPacket(tosend, tosend.length, clientIP, PORT);
                        sending_socket.send(packet);
                        //Gets running time
                        Date endTime = new Date();
                        runningTime += endTime.getTime() - startTime.getTime();
                        packetsSent += 1;
                        if(runningTime >= 1000)
                        {
                            System.out.println("-.-.-.-.-.-");
                            System.out.println("Bitrate= " + (packetsSent * tosend.length * 8));
                            System.out.println("-.-.-.-.-.-");
                            runningTime = 0;
                            packetsSent = 0;
                        }
                        seqnum ++;

                        //2000 for testing
                        if(seqnum == 2000){
                            running = false;
                        }
                    }
                }
                catch (IOException e)
                {
                    System.out.println("IO Exception error");
                    e.printStackTrace();
                }
            }
            recorder.close();
            sending_socket.close();
        }
        catch (LineUnavailableException ex)
        {
            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}