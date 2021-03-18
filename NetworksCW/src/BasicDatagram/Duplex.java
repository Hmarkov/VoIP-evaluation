package BasicDatagram;/*
 * TextDuplex.java
 */


import java.util.Scanner;

/**
 *
 * @author  abj
 */
class Duplex {

    public static void main (String[] args){
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter Datagram Socket Number (1-3): ");
        int socketNum = reader.nextInt();
        Receiver receiver = new Receiver(socketNum);
        Sender sender = new Sender(socketNum);

        receiver.start();
        sender.start();

    }

}