package it.polimi.ds.server;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.middleware.ServerSocketHandler;
import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.model.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;
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
    private Map<Integer, ServerSocketHandler> connectionsToServers = new HashMap<>();

    private Map<ServerSocketHandler, Workspace> workspaces = new HashMap<>();
    private final ExecutorService executor;

    private Store store;

    // commit attributes
    // this arraylist is the buffer which contains the commit received, ordered by commit timestamp -> the element in the first position is the older one
    public ArrayList<CommitInfo> commitBuffer = new ArrayList<>();
    // this map contains a pair of timestamp (which is the one of the commit to manage) and a list of ack messages
    // (responses by all other servers related to the commit with that specific timestamp)
    private HashMap<Timestamp, Integer> commitResponses = new HashMap<>();


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
                ServerSocketHandler s = new ServerSocketHandler(peer, this);
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
        while (true) {
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
            // create the list of id who received the message
            ArrayList<Integer> idList = new ArrayList<>();
            m.getServers().stream().forEach((temp) -> idList.add(temp.getId()));
            // If I am the server with the highest ID
            int key = m.getTuple().getKey();
            for (int i = 0; i<R; i++) { // Compute list of recipients
                int targetId = ((key % peers.size()) + i) % peers.size();
                if(targetId != this.peerData.getId() && !idList.contains(targetId)) {
                    connectionsToServers.get(targetId).send(message); // do the forwarding
                }
            }
        }
    }

    public void forwardRead(ForwardedMessage message) {
        int key = ((ReadMessage) message.getMessage()).getKey();
        connectionsToServers.get(key % peers.size()).send(message);
    }

    public void addConnectedServer(int serverId, ServerSocketHandler s) {
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
        for (Map.Entry<Integer, ServerSocketHandler> elem : connectionsToServers.entrySet()) {
            if (elem.getValue() == sock) {
                return elem.getKey();
            }
        }
        return -1;
    }

    public Peer getPeerData() {
        return peerData;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public int getR() {
        return R;
    }

    public Map<Integer, ServerSocketHandler> getConnectionsToServers() {
        return connectionsToServers;
    }

    private void manageNextCommit() {
        // if the buffer is not empty
        if(commitBuffer.size() > 0) {
            CommitInfo commitInfo = commitBuffer.get(0);
            // I have to check whether I am the manager
            if (!connectionsToServers.values().contains((commitInfo.getCommitManager()))){
                System.out.println("Managing commit " + commitInfo.getCommitTimestamp());
                boolean isValid = isWorkspaceValid(commitInfo.getCommitMessage().getWorkspace());
                // if for me the workspace is valid, I need to request the vote
                if (isValid) {
                    System.out.println("For me it is valid :)");
                    commitInfo.updateIter();
                    System.out.println("Request vote for "+commitInfo.getCommitTimestamp() + "... (attempt #" + commitInfo.getIter()+")");
                    for (SocketHandler connection : connectionsToServers.values()) {
                        connection.send(new VoteMessage(commitInfo.getCommitTimestamp(), commitInfo.getIter()));
                    }
                }
                else {
                    // TODO abort
                }
            }
        }
    }

    /**
     *
     * @param m the CommitMessage that required the commit
     * @param clientSender true if the sender is a client, false otherwise
     * @param sourceConnection the socket that has a connection to the source of the commit,
     *                         if I am the manager I save the connection to the client,
     *                         otherwise I save the connection to the manager.
     */
    public synchronized void commitTransaction(CommitMessage m, boolean clientSender, ServerSocketHandler sourceConnection) {
        // if I am the one with the biggest id I am elected to forward the commit and to manage replies (i.e. I am the commit manager)
        System.out.println("Commit received!");
        if(clientSender && (getPeerData().getId() == Collections.max(m.getIDs()))) {
            for (Peer peer : peers) {
                if (!m.getIDs().contains(peer.getId())) {
                    System.out.println("io server " + getPeerData().getId() + " forwardo a " + peer.getId());
                    connectionsToServers.get(peer.getId()).send(m);
                }
            }
            enqueueCommit(new CommitInfo(m, sourceConnection), true);
        }
        else if (clientSender) { // if the client is the sender but I am not elected as manager => I need to save the connection to the manager
            ServerSocketHandler manager = connectionsToServers.get(Collections.max(m.getIDs())); // get the manager
            enqueueCommit(new CommitInfo(m, manager)); // save the commit
        }
        else { // if I received the message from a server, such server is the manager
            enqueueCommit(new CommitInfo(m, sourceConnection));
        }
    }

    public void enqueueCommit(CommitInfo commit, boolean isManager) {
        enqueueCommit(commit);
        if (isManager) {
            commitResponses.put(commit.getCommitTimestamp(), 0);
        }
    }
    public void enqueueCommit(CommitInfo commit) {
        commitBuffer.add(commit);
        if(commitBuffer.size() > 1) {
            commitBuffer.sort(Comparator.comparing(CommitInfo::getCommitTimestamp));
        }
        System.out.println("Commit message with timestamp " + commit.getCommitTimestamp() + " added to buffer");
        //System.out.println(commit.getWorkspace().toString());
        manageNextCommit();
    }
    public CommitInfo dequeueCommit(boolean isManager) {
        CommitInfo removed = dequeueCommit();
        if (isManager) {
            commitResponses.remove(removed.getCommitTimestamp());
        }
        //System.out.println("Commit message with timestamp " + removed.getCommitTimestamp() + " removed from buffer");
        return removed;
    }
    public CommitInfo dequeueCommit() {
        CommitInfo removed = commitBuffer.remove(0);
        if(commitBuffer.size() > 1) {
            commitBuffer.sort(Comparator.comparing(CommitInfo::getCommitTimestamp));
        }
        System.out.println("Commit message with timestamp " + removed.getCommitTimestamp() + " removed from buffer");
        manageNextCommit();
        return removed;
    }

    public boolean isWorkspaceValid(Workspace w) {
        for (Integer id : w.getSavedIDs()) { // for each id stored in the workspace
            // if the saved timestamp is higher than the one at which the transaction began then data has been changed under the hood
            if (store.contains(id) && store.getTuple(id).getTimestamp().compareTo(w.getBeginTimestamp()) > 0) {
                return false;
            }
        }
        return true;
    }

    public SocketHandler getSourceSocket(Timestamp sourceSocketId) {
        for (SocketHandler s : connections) {
            if (s.getCreationTime().equals(sourceSocketId)) {
                return s;
            }
        }
        return null;
    }

    public void handleAckMessage(AckMessage message) {
        //System.out.println("Handling AckMessage");
        if(message.isAck()) {
            //System.out.println("Positive ack");
            // if the first commit in the buffer is actually the one that got acknowledged
            // and the first commit in the buffer has the same iter number as the one in the ack
            if(commitBuffer.get(0).getCommitTimestamp().equals(message.getCommitTimestamp())
                    && commitBuffer.get(0).getIter() == message.getIter()) {
                // add it to the list of acks
                commitResponses.replace(message.getCommitTimestamp(), commitResponses.get(message.getCommitTimestamp()) + 1);
                // if the size of the list of acks is the same as the size of the list of connections to servers
                if (commitResponses.get(message.getCommitTimestamp()) == connectionsToServers.size()) {
                    // validate and eventually persist
                    if (isWorkspaceValid(commitBuffer.get(0).getCommitMessage().getWorkspace())) {
                        System.out.println("This workspace can be persisted! "+ commitBuffer.get(0).getCommitTimestamp());
                        System.out.println(commitBuffer.get(0).getCommitMessage().getWorkspace());
                        persistTransaction(commitBuffer.get(0).getCommitMessage().getWorkspace());
                        forwardPersist();
                        //notify the client
                        commitBuffer.get(0).getCommitManager().send(new OutcomeMessage(true));
                        dequeueCommit(true);
                    }
                }
            }
        }
        else {
            // Abort the transaction
            for(Map.Entry<Integer, ServerSocketHandler>  sv : connectionsToServers.entrySet()){
                sv.getValue().send(new AbortTransactionMessage(commitBuffer.get(0).getCommitTimestamp()));
            }
            dequeueCommit(true);
        }
    }

    private void forwardPersist(){
        for(Map.Entry<Integer, ServerSocketHandler>  sv : connectionsToServers.entrySet()){
            sv.getValue().send(new PersistMessage(commitBuffer.get(0).getCommitTimestamp()));
        }
    }

    public void persistTransactionRequest (Timestamp ts){
        if(ts == commitBuffer.get(0).getCommitTimestamp()) {
            System.out.println("This workspace can be persisted! "+ commitBuffer.get(0).getCommitTimestamp());
            persistTransaction(commitBuffer.get(0).getCommitMessage().getWorkspace());
            dequeueCommit();
        }
        else
            PrintHelper.printError("The commit transaction is not the first in my commit buffer.");
    }

    private void persistTransaction (Workspace workspace){
        for(Map.Entry<Integer, Tuple> entry : workspace.getStore().entrySet()){
            // if one of the target id is equal to mine I have to persist the tuple
            for (int i = 0; i<R; i++) {
                int targetId = ((entry.getKey() % peers.size()) + i) % peers.size();
                if(targetId == peerData.getId()){
                    store.put(entry.getValue());
                }
            }
        }

        System.out.println("-----STORE-----");
        System.out.println(store.toString());
        System.out.println("---------------");
    }

    public void abortTransaction (Timestamp ts){
        if(ts == commitBuffer.get(0).getCommitTimestamp())
            dequeueCommit();
        else
            PrintHelper.printError("The aborted transaction is not the first in my commit buffer.");
    }

    public void printCommitBuffer() {
        for (CommitInfo elem : commitBuffer) {
            ServerSocketHandler s = elem.getCommitManager();
            System.out.print(connectionsToServers.containsValue(s) + ", ");
        }
        System.out.println("\n");
    }

    public void doVote(VoteMessage message) {
        // somebody asked me to vote
        while (commitBuffer.size() == 0) {
            PrintHelper.printError("Trying to vote but commitBuffer empty. Are you trying to vote before committing?");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // if the first element in the queue is actually the one that I have to vote
        if (commitBuffer.get(0).getCommitTimestamp().equals(message.getCommitTimestamp())) {
            boolean isValid = isWorkspaceValid(commitBuffer.get(0).getCommitMessage().getWorkspace());
            commitBuffer.get(0).getCommitManager().send(new AckMessage(isValid, message.getCommitTimestamp(), message.getIter()));
        }
    }
}
