package it.polimi.ds.server;

import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.HandshakeMessage;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.model.Peer;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.*;

/**
 * Main class of the server
 */
public class ServerMain {
    //needed
    private ConfigHelper ch;
    private int R;
    private final int portNumber;
    private int id;
    private final ExecutorService executor;

    // sockets between servers
    List<SocketHandler> serverConnections = new ArrayList<>();
    private Map<Integer, ServerSocketHandlerOLD> servers = new HashMap<>();

    private static final String FILENAME = "DS/src/config.xml";

    // store
    private Map<Integer, String> store = new HashMap<>();
    private Map<Integer, Timestamp> storeChronology = new HashMap<>();

    //constructor
    public ServerMain(int port){
        this.portNumber = port;
        executor = Executors.newCachedThreadPool();
    }

    /*
    /**
     * Starts the server to listen to clients and to accept their connection
     * creates a ServerSocketHandler for each client
     *
    public void startServer() {

        try {
            this.ch = new ConfigHelper(FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Port number: " + portNumber);
        ServerSocket serverSocket = null;
        System.out.println("Server started!");
        try {
            try{
                serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.out.println("\nPort already in use!\n");
                exit(0);
                //e.printStackTrace();
            }
            serverStartingConnection();
            while(true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Accepted!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert clientSocket != null;
                ServerSocketHandlerOLD connection = new ServerSocketHandlerOLD(clientSocket, this);
                executor.submit(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    // store access

    public String getValue(int key) {
        return store.get(key);
    }

    public void setValue(int key, String value){
        store.put(key, value);
    }

    public boolean isContained (int key) {
        return store.containsKey(key);
    }

    /**
     * prints the status of the store with all the keys and values, it is called whenever a change happens
     */
    public void showStore() {
        out.println("Store status");
        out.println("Keys      Values");
        for(Map.Entry<Integer, String> entry : store.entrySet()){
            int space = 10 - String.valueOf(entry.getKey()).length();
            String s = String.format("%"+space+"s", "");
            out.println(entry.getKey() + s + entry.getValue());
        }
    }

    /**
     * used to forward messages between servers
     * @param message is the request received by one or more servers which must be forwarded to other servers
     */
    // wip
    public void forward(Message message){
        WriteMessage r = (WriteMessage) message;
        WriteMessage r1 = new WriteMessage(r.getTuple(), r.getTimestamp(), ch.getPeerList());
        for (SocketHandler sock : serverConnections) {
            if(! ((WriteMessage) message).getServers().contains(sock.getPeer())) {
               sock.send(r1);
            }
        }
        for (var entry : servers.entrySet()) {
            if(! ((WriteMessage) message).getServers().contains(entry.getValue())) { // TODO fix!
                entry.getValue().sendReply(r1);
            }
        }
    }

    /*
    /**
     * used when server starts, it connects with all available servers
     *
    // wip
    private void serverStartingConnection() throws InterruptedException {
        List<Peer> peers = ch.getPeerList();
        // TODO : use ip instead of port number
        for(Peer peer : peers) {
            if (peer.getPort() != portNumber && isSocketAlive(peer.getHost(), peer.getPort())) {
                out.println("connecting with other servers...");
                SocketHandler s = new SocketHandler(peer, this);
                serverConnections.add(s);
                List<Peer> l = ch.getPeerList();
                for (Peer elem : l) {
                    InetAddress inetAddress = null;
                    try {
                        inetAddress = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    if (elem.getPort() == portNumber && elem.getHost().equals(inetAddress.getHostAddress())) {
                        this.id = elem.getId();
                        break;
                    }
                }
                s.send(new HandshakeMessage(this.id));
            }
        }
    }*/

    public static boolean isSocketAlive(String ip, int port){
        boolean isAlive = false;
        SocketAddress socketAddress = new InetSocketAddress(ip, port);
        Socket socket = new Socket();
        int timeout = 2000;
        try {
            socket.connect(socketAddress, timeout);
            socket.close();
            isAlive = true;
        } catch (IOException e) {
            out.println("waiting other servers to start running...");
            //e.printStackTrace();
        }
        return isAlive;
    }

    public static void main(String[] args) throws IOException {
        int id;
        if(args.length >= 1)
            id = Integer.parseInt(args[0]);
        else
            id = 0;

        Server server = new Server(id, FILENAME);
        //server.startServer();
    }

    public void addAcceptedServer(int id, ServerSocketHandlerOLD s) {
        this.servers.put(id, s);
    }


}
