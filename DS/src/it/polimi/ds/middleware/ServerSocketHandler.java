package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.model.Peer;
import it.polimi.ds.server.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    private boolean SendReadReply(Message message){
        // making the idList
        ReadMessage m = (ReadMessage) message;
        ArrayList<Integer> idList = new ArrayList<>();
        m.getServers().stream().forEach((temp) -> idList.add(temp.getId()));
        int key = m.getKey();
        for (int i = 0; i<server.getR(); i++) {
            int targetId = ((key % server.getPeers().size()) + i) % server.getPeers().size();
            idList.remove(targetId);
        }
        return server.getPeerData().getId() == Collections.max(idList);
    }

    @Override
    public void run() {
// TODO: using strings for errors is not good, it would be better doing an error enumeration
        try {
            while(true) {
                Message message = (Message) in.readObject();
                //System.out.println(socket.getInetAddress().toString() + ":" + socket.getPort());
                if (message instanceof ReadMessage) {
                    // if this sever has the key and its id is the biggest one of the receivers which have the key -> reply
                    if(server.containsKey(((ReadMessage) message).getKey()) && SendReadReply(message)) {
                        send(new ServerReply("[" + this.socket.getInetAddress().getHostAddress() + "] " + server.getValue(((ReadMessage) message).getKey())));
                    }
                    else if(server.containsKey(((ReadMessage) message).getKey())){
                        PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadMessage) message).getKey() +" exists but it is not my job to answer!");
                    }
                    else{
                        // TODO : in this case (if I am the server with the highest ID between the receivers - it is already verified that no one have the key)
                        //  the key must be requested from another server, R % peers.size, and used to reply
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
            //e.printStackTrace();
            //e.getMessage();
            try {
                System.out.println("Closing connection with " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "...");
                socket.close();
                in.close();
                out.close();
                System.out.println("Connection closed successfully!");
            } catch (IOException ioException) {
                PrintHelper.printError("Unexpected error while closing the connection.");
                //ioException.printStackTrace();
            }
        }
    }
}
