package BasicDatagramSecurity;

import BasicDatagramSecurity.Receiver;
import BasicDatagramSecurity.Sender;

import java.util.Scanner;

public class Duplex {
    public static void main (String[] args){
        Receiver receiver = new Receiver(1);
        Sender sender = new Sender(1);
        receiver.start();
        sender.start();

    }
}
