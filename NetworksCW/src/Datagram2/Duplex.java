package Datagram2;

public class Duplex {
    public static void main (String[] args)
    {
        Receiver receiver = new Receiver();
        Sender sender = new Sender();

        receiver.start();
        sender.start();
    }
}
