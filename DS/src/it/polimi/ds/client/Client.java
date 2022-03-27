package it.polimi.ds.client;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.ReadMessage;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.middleware.ClientSocketHandler;
import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Client {
    // servers
    private List<Peer> peers; // list of available servers
    private List<Peer> peersConnected; // list of connected servers

    // sockets
    private List<SocketHandler> connections; // list of connected sockets

    public Client(String configPath) {
        try {
            ConfigHelper ch = new ConfigHelper(configPath);
            peers = ch.getPeerList();
            peersConnected = new ArrayList<Peer>();
            connections = new ArrayList<SocketHandler>();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void connect(int peerId) {
        // Try to create a connection with the selected peer
        try {
            Peer peer = getPeer(peerId);
            SocketHandler s = new ClientSocketHandler(peer);
            if (s.isConnected()) {
                connections.add(s);
                peersConnected.add(peer);
                peers.remove(peer);
            }
        } catch (IndexOutOfBoundsException e) {
            PrintHelper.printError("Invalid input...");
        }
    }

    public void detach(int peerId) {
        try {
            SocketHandler target = null;
            for (SocketHandler s : connections) {
                if (s.getPeer().getId() == peerId) {
                    target = s;
                    break;
                }
            }
            if(target == null) {
                PrintHelper.printError("Invalid input...");
                return;
            }
            else {
                target.disconnect();
                connections.remove(target);
                Peer toRemove = this.getPeer(peerId);
                peers.add(toRemove);
                peersConnected.remove(toRemove);
            }
        } catch (Exception e) {
            PrintHelper.printError("An unexpected error occurred while detaching from server...");
        }
    }

    public List<Peer> getAvailablePeers() {
        return this.peers;
    }

    public int getConnectionsSize() {
        return this.connections.size();
    }

    public List<Peer> getPeersConnected() {
        return this.peersConnected;
    }

    public Peer getPeer(int peerId) {
        for (Peer elem : peers) {
            if (elem.getId() == peerId) {
                return elem;
            }
        }
        for (Peer elem : peersConnected) {
            if (elem.getId() == peerId) {
                return elem;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    public void write(int key, String value) {
        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all peers connected containing the list of peers to
        // which the request is sent, for coordination reasons
        for (SocketHandler s : connections) {
            s.send(new WriteMessage(new Tuple(key, value), ts, peersConnected));
        }
    }

    public void read(int key) {
        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all peers connected containing the list of peers to
        // which the request is sent, for coordination reasons
        for (SocketHandler s : connections) {
            s.send(new ReadMessage(key, ts, peersConnected));
        }
    }
}
