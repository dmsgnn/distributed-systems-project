package it.polimi.ds.client.middleware;

import it.polimi.ds.client.model.Operation;
import it.polimi.ds.client.model.Request;
import it.polimi.ds.client.model.Server;
import it.polimi.ds.client.model.Tuple;

import java.net.Socket;
import java.io.*;

public class SocketWrapper {
    private boolean connected = false;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public SocketWrapper(String host, int port) {
        try {
            // Create the socket and bind i/o
            socket = new Socket(host, port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            connected = true;
        } catch (IOException e) {
            System.out.println("[!] Failed to connect to " + host + ":" + port);
        }
    }

    public SocketWrapper(Server s) {
        String host = s.getHost();
        int port = s.getPort();
        try {
            // Create the socket and bind i/o
            socket = new Socket(host, port);
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            connected = true;
        } catch (IOException e) {
            System.out.println("[!] Failed to connect to " + host + ":" + port);
        }
    }

    public String read(int key) throws IOException {
        Integer payload = key;
        Request r = new Request(Operation.READ, payload);
        return this.send(r);
    }

    public String write(int key, String value) throws IOException {
        Tuple payload = new Tuple(key, value);
        Request r = new Request(Operation.READ, payload);
        return this.send(r);
    }

    private String send(Request r) throws IOException {
        out.writeObject(r);
        return "";
    }


    public boolean isConnected() {
        return this.connected;
    }

}