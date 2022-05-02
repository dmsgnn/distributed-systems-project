package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.ErrorMessage;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.ReplyMessage;
import it.polimi.ds.model.Peer;

import java.io.*;
import java.net.SocketException;

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
                try {
                    Message message = (Message) in.readObject();
                    // print to test the received value
                    if(message instanceof ReplyMessage) {
                        System.out.println(((ReplyMessage) message).getValue());
                    }
                    if(message instanceof ErrorMessage) {
                        PrintHelper.printError((ErrorMessage) message, this.getPeer().getHost());
                    }
                } catch (EOFException e) {
                    System.out.println("Nothing to read...");
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection closed successfully!");
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            e.getMessage();
            PrintHelper.printError("\nServer shut down \n ");
            exit(0);
        }
    }
}