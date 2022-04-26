package it.polimi.ds.middleware;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.model.Peer;
import it.polimi.ds.server.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
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

    /**
     * @param idList is the list of the receivers of the read message
     * @param key is the requested key
     * @return the list of the receiver of the read message who could have the requested key
     */
    public ArrayList<Integer> getPossibleKeyOwners (ArrayList<Integer> idList, int key){
        ArrayList<Integer> shouldHaveTheKey = new ArrayList<>();
        for (int i = 0; i<server.getR(); i++) {
            int targetId = ((key % server.getPeers().size()) + i) % server.getPeers().size();
            if(idList.contains(targetId)) {
                shouldHaveTheKey.add(targetId);
            }
        }
        return shouldHaveTheKey;
    }
    /**
     * @param idList is the list of the receivers of the read message
     * @param key is the requested key
     * @return true if between the receivers of the read message there is at least one server which could have the key, false otherwise
     */
    private boolean someoneShouldHaveTheKey(ArrayList<Integer> idList, int key) {
        if(getPossibleKeyOwners(idList, key).size() == 0)
            return false;
        else
            return true;
    }

    @Override
    public void run() {
// TODO: using strings for errors is not good, it would be better doing an error enumeration
        try {
            while(true) {
                Message message = (Message) in.readObject();
                //System.out.println(socket.getInetAddress().toString() + ":" + socket.getPort());
                if (message instanceof ReadMessage) {
                    if(someoneShouldHaveTheKey(((ReadMessage) message).getIDs(), ((ReadMessage) message).getKey())){ // in this case forwarding is not required
                        // if I am the biggest one of servers which could have the key between the receivers I manage the request
                        if (server.getPeerData().getId() == Collections.max(getPossibleKeyOwners(((ReadMessage) message).getIDs(), ((ReadMessage) message).getKey()))){
                            // the key exists and reply is sent
                            if(server.containsKey(((ReadMessage) message).getKey())){
                                send(new ServerReply("[" + this.socket.getInetAddress().getHostAddress() + "] " + server.getValue(((ReadMessage) message).getKey())));
                            }
                            // the key simply does not exists
                            else {
                                PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] Key " + ((ReadMessage) message).getKey() +" does not exists!");
                                // TODO create an error message
                            }
                        }
                        // I am not the biggest one and i have to do nothing
                        else {
                            PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] It is not my job to manage the read message with key " + ((ReadMessage) message).getKey() + "!");
                        }
                    }
                    // no one could have the key, forwarding needed
                    else{
                        if(server.getPeerData().getId() == Collections.max(((ReadMessage) message).getIDs())){
                            //TODO read forward
                            //server.getConnectionsToServers().get(((ReadMessage) message).getKey() % server.getPeers().size()).send(message);
                        }
                    }
                }
                else if (message instanceof WriteMessage) {
                    System.out.println("[" + server.getSocketId(this) + "] W " + ((WriteMessage) message).getTuple());
                    server.addToPrivateWorkspace(this, ((WriteMessage) message).getTuple());
                    //server.setValue(((WriteMessage) message).getTuple());
                    //server.forward(message, this);
                }
                else if (message instanceof HandshakeMessage) {
                    server.addConnectedServer(((HandshakeMessage) message).getServerId(), this);
                }
                else if (message instanceof BeginMessage) {
                    server.beginTransaction(this, ((BeginMessage) message).getTimestamp());
                }
                else if (message instanceof AbortMessage) {
                    server.abortTransaction(this);
                }
                else if (message instanceof CommitMessage) {
                    server.commitTransaction(this, (CommitMessage) message);
                }
                else {
                    PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored");
                    //System.out.println("\u001B[31m" + "["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and it has been ignored" + "\u001B[0m");
                }
            }
        } catch (Exception e) {
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
