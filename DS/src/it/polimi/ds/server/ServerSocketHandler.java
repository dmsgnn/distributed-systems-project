package it.polimi.ds.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import it.polimi.ds.messages.ClientRequest;
import it.polimi.ds.messages.ReadRequest;
import it.polimi.ds.messages.ServerReply;
import it.polimi.ds.messages.WriteRequest;

import static java.lang.System.out;

/**
 * Helps to manage each client connection, pairing it with the server. It holds the input stream to
 * receive {@link ClientRequest} and holds an output stream to send the {@link ServerReply}
 */
public class ServerSocketHandler implements Runnable{
    private static final Object lock = new Object();

    private Socket socket;
    private ServerMain server;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    // constructor
    /**
     * Opens a channel between the client and the server
     * @param socket client connection
     * @param server server to be connected with
     */
    public ServerSocketHandler(Socket socket, ServerMain server) {
        this.socket = socket;
        this.server = server;

        try {
            in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            //e.printStackTrace();
            e.getMessage();
        }
    }

    /**
     * The thread catches the events coming from the client to communicate to the it.polimi.ds.client.server.
     */
    public void run() {
        try {
            while(true) {
                ClientRequest request  = (ClientRequest) in.readObject();
                if (request instanceof ReadRequest) {
                    System.out.println("I received a read request with key " + ((ReadRequest) request).getKey() + " at time " + ((ReadRequest) request).getTimestamp());
                    sendReply(new ServerReply("["+this.socket.getInetAddress().getHostAddress()+ "] Hey, I received your Read request!"));
                }
                else if (request instanceof WriteRequest) {
                    System.out.println("I received a Write request with key " + ((WriteRequest) request).getTuple().getKey() +
                            " and value " + ((WriteRequest)request).getTuple().getValue()+ " at time " + ((WriteRequest) request).getTimestamp());
                    sendReply(new ServerReply("["+this.socket.getInetAddress().getHostAddress()+ "] Hey, I received your Write request!"));
                }
                else {
                    System.out.println("An unexpected type of request has been received and it has been ignored");
                }
            }
        }catch (Exception e) {
            //e.printStackTrace();
            e.getMessage();
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException ioException) {
                //ioException.printStackTrace();
            }
        }
    }

    /**
     * Sends a reply to the client
     * @param reply is the message to send
     */
    public void sendReply(ServerReply reply) {
        try {
            out.writeObject(reply);
            out.flush();
        } catch (Exception e) {
            e.getMessage();
            //e.printStackTrace();
        }
    }
}
