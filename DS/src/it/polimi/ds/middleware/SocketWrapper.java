package it.polimi.ds.middleware;

import it.polimi.ds.messages.ClientRequest;
import it.polimi.ds.messages.ReadRequest;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.model.Operation;
import it.polimi.ds.model.Request;
import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;

import java.net.Socket;
import java.io.*;

import static java.lang.System.exit;

public class SocketWrapper implements Runnable{
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
        // socket creation
        Socket sock = null;
        try {
            sock = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("\n\nserver not available: ip or port number might be wrong\n\n");
            System.exit(0);
            //e.printStackTrace();
        }
        this.socket = sock;

        // stream creation
        try {
            out = new ObjectOutputStream(this.socket.getOutputStream());
            in = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[!] Failed to connect to " + host + ":" + port);
        }
        new Thread(this).start();
    }
    /*
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
     */

    public void send(ClientRequest r) {
        try {
            out.writeObject(r);
            out.flush();
        } catch (IOException e) {
            e.getMessage();
            //e.printStackTrace();
        }
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
            e.getMessage();
            System.out.println("\nServer shut down \n ");
            exit(0);
            //e.printStackTrace();
        }
    }
}