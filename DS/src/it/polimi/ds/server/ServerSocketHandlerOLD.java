package it.polimi.ds.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;

/**
 * Helps to manage each client connection, pairing it with the server. It holds the input stream to
 * receive {@link Message} and holds an output stream to send the {@link ReplyMessage}
 */
public class ServerSocketHandlerOLD implements Runnable{
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
    public ServerSocketHandlerOLD(Socket socket, ServerMain server) {
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
                Message message = (Message) in.readObject(); // this is the client port
                System.out.println(socket.getInetAddress().toString() + ":" + socket.getPort());
                if (message instanceof ReadMessage) {
                    if(server.isContained(((ReadMessage) message).getKey())) {
                        sendReply(new ReplyMessage("[" + this.socket.getInetAddress().getHostAddress() + "] " + server.getValue(((ReadMessage) message).getKey())));
                    }
                    else{
                        PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadMessage) message).getKey() +" does not exists!");
                        //sendReply(new ServerReply("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadRequest) request).getKey() +" does not exists!" + "\u001B[0m"));
                    }
                }
                else if (message instanceof WriteMessage) {
                    server.setValue(((WriteMessage) message).getTuple().getKey(), ((WriteMessage) message).getTuple().getValue());
                    server.showStore();
                    server.forward(message);
                }
                else if (message instanceof HandshakeMessage) {
                    server.addAcceptedServer(((HandshakeMessage) message).getServerId(), this);
                }
                else {
                    PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored");
                    //System.out.println("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored" + "\u001B[0m");
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
    public void sendReply(Message reply) {
        try {
            out.writeObject(reply);
            out.flush();
        } catch (Exception e) {
            e.getMessage();
            //e.printStackTrace();
        }
    }
}