package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.model.Peer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static java.lang.System.exit;

public class ClientSocketHandler extends SocketHandler {
    public ClientSocketHandler(Peer s) {
        super(s);
    }

    /**
     * Thread to remain open to accept server replies
    */
    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                // print to test the received value
                if(message instanceof ServerReply) {
                    System.out.println(((ServerReply) message).getValue());
                }
            }
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            e.getMessage();
            PrintHelper.printError("\nServer shut down \n ");
            exit(0);
        }
    }
}