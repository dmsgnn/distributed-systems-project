package it.polimi.ds.middleware;

import it.polimi.ds.client.Client;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.model.Peer;

import java.io.*;
import java.net.SocketException;

import static java.lang.System.exit;

public class ClientSocketHandler extends SocketHandler {
    Client client;

    public ClientSocketHandler(Peer s, Client client) {
        super(s);
        this.client = client;
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
                    if(message instanceof ForwardedMessage) {
                        PrintHelper.printError("Houston abbiamo un problema");
                        exit(0);
                    }
                    if(message instanceof OutcomeMessage){
                        client.setCommitOk(true);
                        if(((OutcomeMessage) message).isOutcome())
                            System.out.println("Commit successful!");
                        else
                            PrintHelper.printError("Commit was not possible, transaction has been aborted.");
                    }
                } catch (EOFException e) {
                    e.printStackTrace();
                    return;
                    //System.out.println("Nothing to read...");
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection closed successfully!");
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
            PrintHelper.printError("\nServer shut down \n ");
            exit(0);
        }
    }
}