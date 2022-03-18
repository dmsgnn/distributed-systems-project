package it.polimi.ds.server;

import it.polimi.ds.client.ClientSocketHandler;
import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.ClientRequest;
import it.polimi.ds.messages.ConnectionRequest;
import it.polimi.ds.messages.WriteRequest;
import it.polimi.ds.model.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
    private final ExecutorService executor;
    List<ClientSocketHandler> serverConnections = new ArrayList<>();

    private static final String FILENAME = "DS/src/config.xml";

    // store
    private Map<Integer, String> store = new HashMap<>();
    private Map<Integer, Timestamp> storeChronology = new HashMap<>();

    //constructor
    public ServerMain(int port){
        this.portNumber = port;
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server to listen to clients and to accept their connection
     * creates a ServerSocketHandler for each client
     */
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
                ServerSocketHandler connection = new ServerSocketHandler(clientSocket, this);
                executor.submit(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
     * parsing of the server.xml file, useful because all the servers must be connected with each other in order to communicate
     * @return the list of servers which represent the distributed store
     */
    private static ArrayList<Server> getServers() {
        ArrayList<Server> res = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document serversDoc = db.parse(new File(FILENAME));
            NodeList sList = serversDoc.getElementsByTagName("server");
            for (int i = 0; i < sList.getLength(); i++) {
                Node n = sList.item(i);
                if(n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Server s = new Server(
                            e.getElementsByTagName("host").item(0).getTextContent(),
                            Integer.parseInt(e.getElementsByTagName("port").item(0).getTextContent())
                    );
                    res.add(s);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return res;
    }

    /**
     * used to forward messages between servers
     * @param request is the request received by one or more servers which must be forwarded to other servers
     */
    // wip
    public void forward(ClientRequest request, List<Server> servers){
        WriteRequest r = (WriteRequest) request;
        WriteRequest r1 = new WriteRequest(r.getTuple(), r.getTimestamp(), ch.getServerList());
        for (ClientSocketHandler sock : serverConnections) {
            if(! servers.contains(sock.getServer())) {
               sock.send(r1);
            }
        }
    }

    /**
     * used when server starts, it connects with all available servers
     */
    // wip
    private void serverStartingConnection() throws InterruptedException {
        List<Server> servers = ch.getServerList();
        // TODO : use ip instead of port number
        for(Server server: servers) {
            if (server.getPort() != portNumber && isSocketAlive(server.getHost(), server.getPort())) {
                out.println("connecting with other servers...");
                ClientSocketHandler s = new ClientSocketHandler(server);
                serverConnections.add(s);
                s.send(new ConnectionRequest("127.0.0.1", portNumber));
            }
        }
    }

    /**
     * used to connect to another server who get up after us
     * @param ip
     * @param port
     */
    public void serverRequestConnection(String ip, int port) {
        Server server = new Server(ip, port);
        ClientSocketHandler s = new ClientSocketHandler(server);
        serverConnections.add(s);
    }

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
        int portNumber;
        InetAddress inetAddress = null;
        /*
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        */

        if(inetAddress != null)
            System.out.println("Hello, I'm on: " + inetAddress.getHostAddress());
        else
            System.out.println("Hello, I'm on: 127.0.0.1");

        if(args.length >= 1 && Integer.parseInt(args[0])>1024)
            portNumber = Integer.parseInt(args[0]);
        else
            portNumber = 4000;

        ServerMain server = new ServerMain(portNumber);
        server.startServer();
    }
}
