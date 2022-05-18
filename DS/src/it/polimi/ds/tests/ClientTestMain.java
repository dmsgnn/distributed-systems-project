package it.polimi.ds.tests;

import it.polimi.ds.client.Client;
import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.AbortMessage;
import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.messages.PersistMessage;
import it.polimi.ds.messages.VoteMessage;
import it.polimi.ds.model.Peer;
import it.polimi.ds.tests.helpers.DelayMessageDelivery;
import it.polimi.ds.tests.helpers.ServerThread;
import it.polimi.ds.tests.helpers.TestSpecs;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

public class ClientTestMain {
    private static final String FILENAME = "DS/src/it/polimi/ds/tests/config/config_test.xml";

    private static ArrayList<ServerThread> threads = new ArrayList<>();

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        //test6();
        multiClientStressTest(10, 30, 1);
    }

    /**
     * Server0 riceve due commit, il primo a tempo x+1 e il secondo a tempo x. Possiamo vedere che il primo
     * tentativo di commit (x+1) che fa viene messo in pausa per gestire il commit che ha ricevuto da un altro
     * server con timestamp x.
     *
     * Entrambi vanno a buon fine perché sono eseguiti su due chiavi diverse.
     */
    public static void test1_1() {
        TestSpecs ts = new TestSpecs();
        ts.addServer(0);
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, false, 200));
        // ts = "server 0 has a 200ms delay when it receives a commit from a server"

        initializeServers(ts);
        Client c0 = new Client(FILENAME);
        // c0 deve fare due attempts in quanto server0 riceve prima il commit di c0 e poi quello di c1 (che ha un timestamp minore)
        Client c1 = new Client(FILENAME);


        c0.connect(0);
        c1.connect(1);

        c1.begin();
        c1.write(0, "primo"); // sul server 1

        c0.begin();
        c0.write(1, "secondo"); // sul server 0

        c1.commit();
        c0.commit();
        // t1 < t2
    }

    /**
     * Server0 riceve due commit, il primo a tempo x+1 e il secondo a tempo x. Possiamo vedere che il primo
     * tentativo di commit (x+1) che fa viene messo in pausa per gestire il commit che ha ricevuto da un altro
     * server con timestamp x.
     *
     * Solo quello a tempo x va a buon fine in quanto lavorano entrambi sulla stessa chiave.
     */
    public static void test2_1() {
        TestSpecs ts = new TestSpecs();
        ts.addServer(0);
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, false, 200));
        // ts = "server 0 has a 200ms delay when it receives a commit from a server"

        initializeServers(ts);
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);


        c0.connect(0);
        c1.connect(1);

        c1.begin();
        c1.write(0, "primo"); // sul server 1

        c0.begin();
        c0.write(0, "secondo"); // sul server 0

        c1.commit();
        c0.commit();
        // t1 < t2
    }

    private static void test3() {
        TestSpecs ts = new TestSpecs();
        ts.addServer(0); // problem with 0
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, true, 200));
        // ts = "server 0 has a 200ms delay when it receives a commit from a server"
        initializeServers(ts);

        Client c0 = new Client(FILENAME);

        c0.connect(0);
        c0.connect(1);

        c0.begin();
        c0.write(0, "secondo");

        c0.commit();
    }

    public static void test4() {
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);


        c0.connect(0);
        c1.connect(1);

        c1.begin();
        c1.write(0, "primo"); // sul server 1

        c0.begin();
        c0.write(0, "secondo"); // sul server 0

        c0.commit();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        c1.commit();
        // t1 < t2
    }

    /**
     * Server 0 has 200ms delay when it receives a commit from a client.
     * Client c0 and c1 write the same tuple.
     * [t1] Client c0 commits at server 0.
     * [t2] Client c1 commits at server 1.
     *
     * Server 1 manages the commit of c1 and asks for the vote, Server 0 hasn't yet received the commit [t1] and thus
     *  ACKs the commit [t2].
     * Server 0 then receives commit [t1] but cannot ask for vote.
     * Commit [t1] is persisted and commit [t2] is then checked.
     * Commit [t2] is aborted because it
     */
    public static void test5() {

        TestSpecs ts = new TestSpecs();
        ts.addServer(0);
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, true, 2000));
        // ts = "server 0 has a 200ms delay when it receives a commit from a client"

        initializeServers(ts);
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);

        c0.connect(0);
        c1.connect(1);

        c0.begin();
        c1.begin();

        c0.write(0, "first");
        c1.write(0, "second");

        c0.commit();
        c1.commit();
    }

    /**
     * Clients c0 and c1 perform two commits on the same tuple not interleaved. Only the commit from c0 should be performed.
     * c0 is connected to server s0.
     * c1 is connected to both server s1 and s2, but connection with server s1 has some delay.
     *
     * While s0 and s2 are performing the abort of the transaction of c1.
     *
     * Server s1 has not yet received the first commit from c1, when it receives an abort message it does not yet have the commit
     * of c1 in its commitBuffer, therefore it stores the abort in the abortBuffer until the commit from c1 is received. At such
     * point the commit is ignored and the buffer is emptied.
     */
    public static void test6() {
        TestSpecs ts = new TestSpecs();
        ts.addServer(1);
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, true, 200));
        // ts = "server 0 has a 200ms delay when it receives a commit from a client"

        initializeServers(ts);
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);

        c0.connect(0);
        c1.connect(2);
        c1.connect(1);

        // let s0 and s2 abort the transaction
        c0.begin();
        c1.begin();
        c0.write(10, "dieci");
        c1.write(10, "ten");

        c0.commit();
        c1.commit();
        // only 10: "dieci" should be in the storage

            c0.begin();
            c1.begin();
            c0.write(10, "dieci");
            c1.write(10, "ten");

            c0.commit();
            c1.commit();
    }

    /**
     * method which realize simulate the commit of a single transaction with a given number of operation in order to test the time needed
     * @param numOps number of operation of the transaction
     * @throws IOException
     */
    public static void singleOperationStressTest (int numOps) throws IOException {

        initializeServers(null);
        Client client = new Client(FILENAME);
        client.connect(0);

        int numberOfTry = 10;

        Random rand = new Random();
        int randomNumber = rand.nextInt(50);

        int numberOperations = numOps;
        long totalTime = 0;

        for(int j=0; j<numberOfTry; j++) {
            long startTime = System.nanoTime();
            client.begin();
            for (int i = 0; i < numberOperations; i++) {
                if((randomNumber * i) % 2 == 0){
                    client.write(randomNumber*i, "test"+i);
                }
                else {
                    client.read(randomNumber+1);
                }
            }
            client.commit();
            while (!client.isCommitOk()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            long stopTime = System.nanoTime();
            System.out.println("Test" + j + " -> " + (stopTime-startTime)/Math.pow(10, 6) );
            if (j != 0){
                totalTime += (stopTime - startTime);
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Duration in milliseconds of a Transaction with "+ numberOperations + " operations (average of "+ (numberOfTry-1) + " tries) -> " + totalTime/(Math.pow(10, 6) * (numberOfTry-1)));

        //shutdownServers();
        //client.deleteConnections();

    }


    public static void multiClientStressTest(int numClients, int numTransaction, int numConnections) throws ParserConfigurationException, IOException, SAXException {
        initializeServers(null);

        int sz = new ConfigHelper(FILENAME).getPeerList().size();
        if(numConnections > sz){
            numConnections = sz;
        }

        // map of client
        Map<Integer, Client> clients = new HashMap<>();
        // map of status, 1 -> in transaction
        Map<Integer, Boolean> status = new HashMap<>();

        // clients creation
        for (int j = 0; j<numClients; j++){
            clients.put(j, new Client(FILENAME));
            status.put(j, false);
            //clients connection
            for(int i=0; i<numConnections; i++){
                clients.get(j).connect((j+1)%3);
            }
        }

        // 10 tries for statistical purposes
        int numberOfTry = 10;
        int openTransactions = 0;
        int totalOps = 0;
        int totalRead = 0;
        int totalWrite = 0;
        int totalAbort = 0;
        int totalCommit = 0;

        Random rand = new Random();
        int randomNumber = 0;

        long totalTime = 0;
        int ct;

        for(int t=0; t<numberOfTry; t++) {
            int numTrans = numTransaction;
            long startTime = System.nanoTime();
            while(numTrans - openTransactions > 0){
                randomNumber = rand.nextInt(50);
                ct = (randomNumber * (numTrans*numClients)) % numClients;
                if (status.get(ct)){
                    if(randomNumber > 35) { // end transaction
                        if (randomNumber % 7 == 0) {
                            clients.get(ct).abort();
                            totalAbort++;
                            System.out.println("Abort executed");
                        }
                        else {
                            clients.get(ct).commit();
                            totalCommit++;
                            System.out.println("Commit executed");
                        }
                        status.put(ct, false);
                        numTrans--;
                        openTransactions--;
                    }
                    else { // do write or read
                        totalOps++;
                        if(randomNumber % 2 == 0){ // do write
                            clients.get(ct).write(randomNumber*numTrans +2, "test"+randomNumber);
                            totalWrite++;
                        }
                        else { // do read
                            totalRead++;
                            clients.get(ct).read(randomNumber*numTrans);
                        }
                    }
                }
                else { // open transaction
                    clients.get(ct).begin();
                    openTransactions++;
                    System.out.println("transaction opened");
                    status.put(ct, true);
                }
            }
            // close transactions still open
            for(Map.Entry<Integer, Boolean> client : status.entrySet()){
                if(client.getValue())
                    clients.get(client.getKey()).commit();
            }

            for(Map.Entry<Integer, Client> client : clients.entrySet()) {
                while (!client.getValue().isCommitOk()) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            long stopTime10Ops = System.nanoTime();

            System.out.println("Test" + t + " -> " + (stopTime10Ops-startTime)/Math.pow(10, 6) + " [ms]" );
            System.out.println("Test" + t + " -> " + totalRead + " total reads");
            System.out.println("Test" + t + " -> " + totalWrite + " total writes");
            System.out.println("Test" + t + " -> " + totalAbort + " total aborts");
            System.out.println("Test" + t + " -> " + totalCommit + " total commits");
            if (t != 0){
                totalTime += (stopTime10Ops - startTime);
            }
            totalOps = 0;
            totalRead = 0;
            totalWrite = 0;
            totalCommit = 0;
            totalAbort = 0;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Duration in milliseconds of a simulation with " + numTransaction + " transactions" +
                " and " + numClients + " clients (average of "+ (numberOfTry-1) + " tries) -> " + totalTime/(Math.pow(10, 6) * (numberOfTry-1)));
    }

    public static List<ServerThread> initializeServers(TestSpecs ts) {
        List<Peer> peerList;
        try {
            ConfigHelper ch = new ConfigHelper(FILENAME);
            peerList = ch.getPeerList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (Peer p : peerList) {
            ServerThread s = new ServerThread(p.getId(), FILENAME, ts);
            (new Thread(s)).start();
            threads.add(s);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return threads;
    }

    // does not work
    private static void shutdownServers () throws IOException {
        for(ServerThread st : threads){
            st.close();
        }
        threads.clear();
    }
}
