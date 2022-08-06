package it.polimi.ds.middleware;

import it.polimi.ds.enums.ErrorCode;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;
import it.polimi.ds.server.Server;
import it.polimi.ds.model.Workspace;
import it.polimi.ds.tests.helpers.DelayMessageDelivery;
import it.polimi.ds.tests.helpers.TestSpecs;

import java.io.IOException;
import java.net.Socket;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerSocketHandler extends SocketHandler {

    private Server server;
    private Workspace privateWorkspace;

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
        return getPossibleKeyOwners(idList, key).size() != 0;
    }

    @Override
    public void run() {
        try {
            while(true) {
                Message message = (Message) in.readObject();
                simulateNetworkDelay(server.getTestSpecs(), message.getClass());
                server.addToLog(Timestamp.from(Instant.now()), new LogMessage(message, false));
                if (message instanceof ReadMessage) {
                    this.doRead((ReadMessage) message);
                }
                else if (message instanceof WriteMessage) {
                    this.privateWorkspace.put(((WriteMessage) message).getTuple());
                }
                else if (message instanceof HandshakeMessage) {
                    server.addConnectedServer(((HandshakeMessage) message).getServerId(), this);
                }
                else if (message instanceof BeginMessage) {
                    this.privateWorkspace = new Workspace(((BeginMessage) message).getTimestamp());
                }
                else if (message instanceof AbortMessage) {
                    this.privateWorkspace = null;
                }
                else if (message instanceof CommitMessage) {
                    if(((CommitMessage) message).getWorkspace() == null) {
                        ((CommitMessage) message).setWorkspace(this.privateWorkspace);
                        server.commitTransaction((CommitMessage) message, true, this);
                    }
                    else{
                        server.commitTransaction((CommitMessage) message, false, this);
                    }
                }
                else if (message instanceof AckMessage) {
                    server.handleAckMessage((AckMessage) message, this);
                }
                else if (message instanceof ForwardedMessage) {
                    handleForwardedMessage((ForwardedMessage) message);
                }
                else if (message instanceof VoteMessage) {
                    //boolean done = server.doVote((VoteMessage) message);
                    //while (!done) {
                        //Thread.sleep(500);
                        server.doVote((VoteMessage) message);
                    //}
                }
                else if(message instanceof AbortTransactionMessage){
                    server.abortTransaction(((AbortTransactionMessage) message).getAbortTimestamp());
                }
                else if(message instanceof PersistMessage) {
                    server.persistTransactionRequest(((PersistMessage) message).getPersistTimestamp());
                }
                else {
                    PrintHelper.printError("["+this.socket.getInetAddress().getHostAddress()+ "] An unexpected type of request has been received and has been ignored");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public void doRead(ReadMessage message) {
        int key = (message).getKey();
        Tuple res;
        // Retrieve the tuple
        // if the key is in the private workspace
        if(privateWorkspace.contains(key) && privateWorkspace.getTuple(key).getValue() != null){
            res = new Tuple(key, privateWorkspace.getTuple(key).getValue());
            // if I am the biggest of the receivers
            if(server.getPeerData().getId() == Collections.max(message.getIDs())) {
                //send(new ReplyMessage("[" + this.socket.getInetAddress().getHostAddress() + "] " + privateWorkspace.getTuple(key).getValue()));
                // send the tuple as reply, the client manages the printing
                send(new ReplyMessage(res));
            }
            // in this case it is not necessary to put the tuple in the private workspace because it already exists there
        }
        else {
            // if the key is not in the private workspace but is stored locally
            if (server.containsKey(key)) {
                res = new Tuple(key, server.getValue(key));
            }
            // server does not own the key
            else {
                // if the server does not own the key it does not mean that it does not exist,
                // the check is performed while sending the reply
                res = new Tuple(key, null);
            }
            // Send the reply
            if (someoneShouldHaveTheKey(message.getIDs(), key)) { // in this case forwarding is not required
                //System.out.println("[info] Someone should have the key...");
                // if I am the biggest one of servers which could have the key between the receivers I manage the request
                if (server.getPeerData().getId() == Collections.max(getPossibleKeyOwners((message).getIDs(), key))) {
                    //System.out.println("[info] I should have the key " + key + " and I should reply.");
                    // the key exists and reply is sent
                    if (res.getValue() != null) {
                        System.out.println("Tuple with key " + key + " is present");
                        //send(new ReplyMessage("[" + this.socket.getInetAddress().getHostAddress() + "] " + res.getValue()));
                        send(new ReplyMessage(res));
                    }
                    // the key does not exist
                    else {
                        PrintHelper.printError("[" + this.socket.getInetAddress().getHostAddress() + "] Key " + key + " does not exists!");
                        send(new ErrorMessage(ErrorCode.INVALID_KEY, key));
                    }
                }
                // I am not the biggest one: do nothing
                else {
                    //PrintHelper.printError("[" + this.socket.getInetAddress().getHostAddress() + "] It is not my job to manage the read message with key " + key + "!");
                }
            }
            // Forwarding!
            else {
                // if I am the biggest one I perform the forwarding
                if (server.getPeerData().getId() == Collections.max((message).getIDs())) {
                    server.forwardRead(new ForwardedMessage(message, this.creationTime));
                }
            }
            // Store the tuple
            res.setValue(null); // all the Tuples stored in the workspace caused by reads are stored in the private workspace with null values
            privateWorkspace.put(res);
        }
    }

    public void handleForwardedMessage(ForwardedMessage message) {
        //System.out.println("Handling a forwarded read!");
        Message innerMessage = message.getMessage();
        Message reply;
        if (innerMessage instanceof ReadMessage) { // If the inner message is a read request I have to retrieve the tuple from storage
            int key = ((ReadMessage) innerMessage).getKey();
            if (server.containsKey(key)) {
                reply = new ForwardedMessage(new ReplyMessage(new Tuple(key, server.getValue(key))), message.getSourceSocketId());
            }
            else {
                System.out.println("Tuple with key " + key + "not found");
                reply = new ForwardedMessage(new ErrorMessage(ErrorCode.INVALID_KEY, key), message.getSourceSocketId());
            }
            send(reply);
        }
        else { // in all other cases I forward the inner message to the socket corresponding to sourceSocketId
            Timestamp sourceSocketId = message.getSourceSocketId();
            SocketHandler source = server.getSourceSocket(sourceSocketId);
            if (source != null) {
                reply = innerMessage;
                source.send(reply);
            }
            else {
                reply = new ForwardedMessage(new ErrorMessage(ErrorCode.UNKNOWN, null), sourceSocketId);
                this.send(reply);
            }
        }
    }

    public void simulateNetworkDelay(TestSpecs ts, Class messageClass) {
        if (ts == null) { // if no rules are specified I am not in the test scenario, thus I add some random delays to simulate the network
            int sleepTime = (int)(Math.random()*100);
            //int sleepTime = 100;
            //PrintHelper.printError("Sleep time: " + sleepTime + "ms");
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        List<DelayMessageDelivery> delays = ts.getDelays();
        if (ts.getServerIDs().contains(server.getPeerData().getId()) || ts.getServerIDs().size() == 0) { // if I am in the list of targets (if the list is empty I am a target by default)
            if (delays.size() != 0) { // if there are some delays in the rules
                for (DelayMessageDelivery d : delays) {
                    if (messageClass.equals(d.getMessageClass())) { // if the message I got is the one for which there is a delay
                        boolean cond0 = d.isFromClient() == null; // the rule does not specify fromClient => delay to all
                        boolean cond1 = d.isFromClient() && server.getSocketId(this) == -1; // the rules specify a delay from client and my connection is to a client
                        boolean cond2 = !d.isFromClient() && server.getSocketId(this) != -1; // the rules specify a delay from server and my connection is to a server
                        if (cond0 || cond1 || cond2) {
                            try {
                                //System.out.println("################# SERVER s" + server.getPeerData().getId() + " sleeping for " + d.getDelayMillis() + "ms! " + cond0 + " - " + cond1 + " - " + cond2 + " Source = " + server.getSocketId(this));
                                Thread.sleep(d.getDelayMillis());
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void send(Message m) {
        server.addToLog(Timestamp.from(Instant.now()), new LogMessage(m, true));
        super.send(m);
    }
}
