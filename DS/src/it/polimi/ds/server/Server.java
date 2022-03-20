package it.polimi.ds.server;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.middleware.ServerSocketHandler;
import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

public class Server {
    private int R;
    private Peer peerData; // contains peer data (id, inetAddress, port)
    private List<Peer> peers;

    private ServerSocket socket;
    private List<SocketHandler> connections;

    private final ExecutorService executor;

    private Store store;

    public Server(int id, String configPath) {
        executor = Executors.newCachedThreadPool();
        try {
            ConfigHelper ch = new ConfigHelper(configPath);
            this.peers = ch.getPeerList();
            for (Peer elem : peers) {
                System.out.println(elem.getId());
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
                SocketHandler s = new ServerSocketHandler(peer);
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
        store.put(t);
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
            executor.submit(connection);
        }
    }

    public Peer getPeerData() {
        return this.peerData;
    }
}
