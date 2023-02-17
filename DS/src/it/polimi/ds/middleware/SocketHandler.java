package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.Message;
import it.polimi.ds.model.Peer;

import java.net.Socket;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;

public abstract class SocketHandler implements Runnable {
    private boolean connected = false;
    private Peer peer;
    protected Timestamp creationTime; // used to identify the socket

    //socket attributes
    protected Socket socket;
    protected ObjectInputStream in;
    protected ObjectOutputStream out;

    public SocketHandler(Peer s) {
        this.creationTime = Timestamp.from(Instant.now());
        String host = s.getHost();
        int port = s.getPort();
        try { // socket creation
            this.peer = s;
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

    public SocketHandler(Socket s) {
        this.creationTime = Timestamp.from(Instant.now());
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
            e.printStackTrace();
        }
    }

    public void disconnect() throws IOException {
        this.in.close();
        this.out.close();
        this.socket.close();
        Thread.currentThread().interrupt();
    }

    public Peer getPeer() {
        return this.peer;
    }

    public boolean isConnected() {
        return this.connected;
    }

    public Timestamp getCreationTime() {
        return this.creationTime;
    }

    @Override
    abstract public void run();
}