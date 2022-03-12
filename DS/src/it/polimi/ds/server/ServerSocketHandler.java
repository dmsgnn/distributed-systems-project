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
     * The thread catches the events coming from the client to communicate to the server.
     */
    public void run() {
        // TODO: using strings for errors is not good, it would be better doing an error enumeration
        try {
            while(true) {
                ClientRequest request  = (ClientRequest) in.readObject();
                if (request instanceof ReadRequest) {
                    if(server.isContained(((ReadRequest) request).getKey())) {
                        sendReply(new ServerReply("[" + this.socket.getInetAddress().getHostAddress() + "] " + server.getValue(((ReadRequest) request).getKey())));
                    }
                    else{
                        sendReply(new ServerReply("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] This key does not exists!" + "\u001B[0m"));
                    }
                }
                else if (request instanceof WriteRequest) {
                    server.setValue(((WriteRequest) request).getTuple().getKey(), ((WriteRequest) request).getTuple().getValue());
                    server.showStore();
                }
                else {
                    System.out.println("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored" + "\u001B[0m");
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
