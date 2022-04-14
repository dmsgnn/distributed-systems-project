package it.polimi.ds.server;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.HandshakeMessage;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.middleware.ServerSocketHandler;
import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Server {
    private int R;
    private Peer peerData; // contains peer data (id, inetAddress, port)
    private List<Peer> peers;

    private ServerSocket socket;
    private List<SocketHandler> connections = new ArrayList<>();
    // Map containing ID, SocketHandler for each connection with the other servers.
    private Map<Integer, SocketHandler> connectionsToServers = new HashMap<>();

    private final ExecutorService executor;

    private Store store;

    public Server(int id, String configPath) {
        executor = Executors.newCachedThreadPool();
        try {
            ConfigHelper ch = new ConfigHelper(configPath);
            this.peers = ch.getPeerList();
            this.R = ch.getParamR();
            for (Peer elem : peers) {
                if (elem.getId() == id) {
                    this.peerData = elem;
                    break;
                }
            }
            // try to open socket
            try {
                socket = new ServerSocket(peerData.getPort());
            } catch (IOException e) {
                System.out.println("\nPort already in use!\n");
                exit(0);
            }
            // initialize store
            this.store = new Store();
            // initialize connection with already available servers
            initializeConnections();
            accept();
        } catch (Exception e) {
            e.printStackTrace();
            //System.exit(1);
        }
    }

    /**
     * Try to initialize connection with all the peers in config.xml
     */
    private void initializeConnections() {
        for (Peer peer : peers) {
            if(!peer.equals(peerData)) {
                SocketHandler s = new ServerSocketHandler(peer, this);
                if (s.isConnected()) {
                    connections.add(s);
                    addConnectedServer(peer.getId(), s);
                    s.send(new HandshakeMessage(this.peerData.getId()));
                }
            }
        }
    }

    public boolean containsKey(int key) {
        return store.contains(key);
    }

    public void showStore() {
        System.out.println(this.store.toString());
    }

    public void setValue(Tuple t) {
        int first = t.getKey() % peers.size(); // first on which to write
        int last = (first + R-1) % peers.size(); // last on which to write
        System.out.println(first + "<=" + peerData.getId() + "<" + last);
        boolean cond1 = first <= peerData.getId() && peerData.getId() <= last;
        boolean cond2 = first <= peerData.getId() || peerData.getId() <= last;
        if ((first < last && cond1) ||
             first > last && cond2) {
            store.put(t);
            showStore();
        }
    }

    public String getValue(int key) {
        return store.getTuple(key).getValue();
    }

    public void accept() {
        while(true) {
            Socket clientSocket = null;
            try {
                clientSocket = socket.accept();
                System.out.println("Accepted!");
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert clientSocket != null;
            ServerSocketHandler connection = new ServerSocketHandler(clientSocket, this);
            this.connections.add(connection);
            executor.submit(connection);
        }
    }

    public void forward(Message message, SocketHandler sourceSocket) {
        // if the message arrives from another server it has already been forwarded
        if(!connectionsToServers.values().contains(sourceSocket)) {
            WriteMessage m = (WriteMessage) message;
            for (Peer p : m.getServers()) { // If there is a server with higher ID, it has to do the forwarding
                if(p.getId() > peerData.getId()) {
                    return;
                }
            }
            // If I am the server with the highest ID
            int key = m.getTuple().getKey();
            for (int i = 0; i<R; i++) { // Compute list of recipients
                int targetId = ((key % peers.size()) + i) % peers.size();
                if(targetId != this.peerData.getId()) { // TODO add check if recipient was already in the list of recipients
                    connectionsToServers.get(targetId).send(message); // do the forwarding
                }
            }
        }
    }

    public void addConnectedServer(int serverId, SocketHandler s) {
        this.connectionsToServers.put(serverId, s);
        System.out.println("[i] Connection established with server " + serverId);
    }

    /**
     * Given a socket returns the ID of the server connected on that socket, if any.
     * @param sock
     * @return The ID of the server connected to that socket.
     * Returns -1 if the socket does not model a connection to a server (i.e. the connection is to a client).
     */
    public int getSocketId(SocketHandler sock) {
        for (Map.Entry<Integer, SocketHandler> elem : connectionsToServers.entrySet()) {
            if (elem.getValue() == sock) {
                return elem.getKey();
            }
        }
        return -1;
    }
}
