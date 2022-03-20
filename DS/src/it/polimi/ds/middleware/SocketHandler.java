package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.model.Peer;
import it.polimi.ds.server.ServerMain;

import java.net.Socket;
import java.io.*;

import static java.lang.System.exit;

public abstract class SocketHandler implements Runnable {
    private boolean connected = false;
    private Peer peer;

    //socket attributes
    protected Socket socket;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    public SocketHandler(Peer s) {
        String host = s.getHost();
        int port = s.getPort();
        try { // socket creation
            this.peer = s;
            Socket sock = new Socket(host, port);
            this.socket = sock;
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(/*new BufferedInputStream*/(this.socket.getInputStream()));
            connected = true;
            new Thread(this).start();
        } catch (Exception e) {
            PrintHelper.printError("Failed to connect to " + host + ":" + port);
        }
    }

    public SocketHandler(Socket s) {
        try {
            this.socket = s;
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
            connected = true;
        } catch (Exception e) {
            PrintHelper.printError("Failed to connect...");
        }
    }

    public void send(Message r) {
        try {
            out.writeObject(r);
            out.flush();
        } catch (IOException e) {
            e.getMessage();
            //e.printStackTrace();
        }
    }

    public Peer getPeer() {
        return this.peer;
    }

    public boolean isConnected() {
        return this.connected;
    }

    @Override
    abstract public void run();
    /*
    /**
     * Thread to remain open to accept server replies

    @Override
    public void run() {
        try {
            while (true) {
                Message message = (Message) in.readObject();
                // print to test the received value
                if(message instanceof ServerReply) {
                    System.out.println(((ServerReply) message).getValue());
                }
                else if (message instanceof WriteMessage) {
                    serverMain.setValue(((WriteMessage) message).getTuple().getKey(), ((WriteMessage) message).getTuple().getValue());
                    serverMain.showStore();
                    serverMain.forward(message);
                }
            }
        } catch(IOException | ClassNotFoundException e) {
            //e.printStackTrace();
            e.getMessage();
            PrintHelper.printError("\nServer shut down \n ");
            exit(0);
        }
    }
    */
}