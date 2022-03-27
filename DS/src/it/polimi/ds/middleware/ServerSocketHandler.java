package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.model.Peer;
import it.polimi.ds.server.Server;

import java.io.IOException;
import java.net.Socket;

public class ServerSocketHandler extends SocketHandler {

    private Server server;

    public ServerSocketHandler(Peer p, Server s) {
        super(p);
        this.server = s;
    }

    public ServerSocketHandler(Socket sock, Server s) {
        super(sock);
        this.server = s;
    }

    @Override
    public void run() {
// TODO: using strings for errors is not good, it would be better doing an error enumeration
        try {
            while(true) {
                Message message = (Message) in.readObject();
                //System.out.println(socket.getInetAddress().toString() + ":" + socket.getPort());
                if (message instanceof ReadMessage) {
                    if(server.containsKey(((ReadMessage) message).getKey())) {
                        send(new ServerReply("[" + this.socket.getInetAddress().getHostAddress() + "] " + server.getValue(((ReadMessage) message).getKey())));
                    }
                    else{
                        PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadMessage) message).getKey() +" does not exists!");
                        //sendReply(new ServerReply("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadRequest) request).getKey() +" does not exists!" + "\u001B[0m"));
                    }
                }
                else if (message instanceof WriteMessage) {
                    System.out.println("[" + server.getSocketId(this) + "] W " + ((WriteMessage) message).getTuple());
                    server.setValue(((WriteMessage) message).getTuple());
                    server.forward(message, this);
                }
                else if (message instanceof HandshakeMessage) {
                    server.addConnectedServer(((HandshakeMessage) message).getServerId(), this);
                }
                else {
                    PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored");
                    //System.out.println("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored" + "\u001B[0m");
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            //e.getMessage();
            try {
                socket.close();
                in.close();
                out.close();
            } catch (IOException ioException) {
                //ioException.printStackTrace();
            }
        }
    }
}
