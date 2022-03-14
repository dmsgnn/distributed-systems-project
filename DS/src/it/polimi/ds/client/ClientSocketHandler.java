package it.polimi.ds.client;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.ClientRequest;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.model.Server;

import java.net.Socket;
import java.io.*;

import static java.lang.System.exit;

public class ClientSocketHandler implements Runnable {
    private boolean connected = false;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Server server;

    public ClientSocketHandler(String host, int port) {
        try {
            // Create the socket and bind i/o
            this.server = new Server(host, port);
            socket = new Socket(host, port);
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
            connected = true;
            new Thread(this).start();
        } catch (IOException e) {
            PrintHelper.printError("Failed to connect to " + host + ":" + port);
            //System.out.println("[!] Failed to connect to " + host + ":" + port);
        }
    }

    public ClientSocketHandler(Server s) {
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
            //System.out.println("\u001B[31m" + "[!] Failed to connect to " + host + ":" + port + );
        }
    }

    public void send(ClientRequest r) {
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
                ServerReply reply = (ServerReply) in.readObject();
                // print to test the received value
                System.out.println(reply.getValue());
            }
        } catch(IOException | ClassNotFoundException e) {
            //e.printStackTrace();
            e.getMessage();
            PrintHelper.printError("\nServer shut down \n ");
            exit(0);
        }
    }
}