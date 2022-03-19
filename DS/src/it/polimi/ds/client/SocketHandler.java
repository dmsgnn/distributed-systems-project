package it.polimi.ds.client;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.model.Server;
import it.polimi.ds.server.ServerMain;

import java.net.Socket;
import java.io.*;

import static java.lang.System.exit;

public class SocketHandler implements Runnable {
    private boolean connected = false;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Server server;
    private ServerMain serverMain;

    public SocketHandler(Server s) {
        String host = s.getHost();
        int port = s.getPort();
        try { // socket creation
            this.server = s;
            Socket sock = new Socket(host, port);
            this.socket = sock;
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
            connected = true;
            new Thread(this).start();
        } catch (Exception e) {
            PrintHelper.printError("Failed to connect to " + host + ":" + port);
        }
    }

    public SocketHandler(Server s, ServerMain sm) {
        this.serverMain = sm;
        String host = s.getHost();
        int port = s.getPort();
        try { // socket creation
            this.server = s;
            Socket sock = new Socket(host, port);
            this.socket = sock;
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
            connected = true;
            new Thread(this).start();
        } catch (Exception e) {
            PrintHelper.printError("Failed to connect to " + host + ":" + port);
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

    public Server getServer() {
        return this.server;
    }

    public boolean isConnected() {
        return this.connected;
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
}