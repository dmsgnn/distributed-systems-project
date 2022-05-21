package it.polimi.ds.tests;

import it.polimi.ds.client.Client;
import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.*;
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
        //test5();
        isolationTest();
        //multiClientStressTest(10, 10, 1);
        //multiClientStressTest();
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
     * Commit [t2] is persisted and commit [t1] is then checked.
     * Commit [t1] is aborted because it arrived late and it is not valid anymore.
     */
    public static void test5() {

        TestSpecs ts = new TestSpecs();
        ts.addServer(0);
        ts.addDelay(new DelayMessageDelivery(CommitMessage.class, true, 2000));
        // ts = "server 0 has a 200ms delay when it receives a commit from a client"

        initializeServers(ts);
        Client c0 = new Client(FILENAME, "client0");
        Client c1 = new Client(FILENAME, "client1");

        c0.connect(0);
        c1.connect(1);

        c0.begin();
        c1.begin();

        c0.write(0, "first");
        c1.write(0, "second");

        c0.commit();
        c1.commit();

        while(!c0.isCommitOk() || !c1.isCommitOk()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(c0.logToString());
        System.out.println(c1.logToString());
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

    private static void isolationTest() {
        initializeServers(null);

        Client cBefore = new Client(FILENAME);
        cBefore.connect(0);
        cBefore.begin();
        cBefore.write(1, "20");
        cBefore.write(2, "25");
        cBefore.commit();
        while(!cBefore.isCommitOk()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Client c0 = new Client(FILENAME, "c0");
        Client c1 = new Client(FILENAME, "c1");

        c0.connect(0);
        c1.connect(0);
        //////////////////////////
        // Dirty reads
        //////////////////////////
        c0.begin();
                        c1.begin();
        c0.read(1);
        wait(1000);
                        c1.write(1, "21");
        wait(1000);
        c0.read(1);
        wait(1000);
                        c1.abort();
        c0.abort();
        while (!c1.isCommitOk() || !c0.isCommitOk()) {
            wait(100);
        }
        Map<Timestamp, Message> c0log = c0.getLog();
        for(Message m : c0log.values()) {
            if (m instanceof ReplyMessage) {
                if(((ReplyMessage) m).getValue().equals("21")) {
                    PrintHelper.printError("Dirty read!");
                    System.out.println(c0.logToString());
                    System.out.println(c1.logToString());
                    System.exit(0);
                }
            }
        }
        //////////////////////////
        // Non-repeatable read
        //////////////////////////
        c0.begin();
                        c1.begin();
        c0.read(1);
        wait(1000);
                        c1.write(1, "22");
                        c1.commit();
        while (!c1.isCommitOk()) {
            wait(100);
        }
        c0.read(1);
        wait(1000);
        c0.commit();
        while (!c0.isCommitOk()) {
            wait(100);
        }
        boolean read22 = false;
        for(Message m : c0log.values()) {
            if (m instanceof ReplyMessage) {
                if(((ReplyMessage) m).getValue().equals("22")) {
                    read22 = true;
                }
            }
            else if(m instanceof OutcomeMessage && read22) {
                if(((OutcomeMessage) m).isOutcome()) {
                    PrintHelper.printError("Non-repeatable read!");
                    System.out.println(c0.logToString());
                    System.out.println(c1.logToString());
                    System.exit(0);
                }
            }
        }
        //////////////////////////
        // Lost updates
        //////////////////////////
        c0.begin();
                        c1.begin();
        c0.read(1);
        wait(1000);
                        c1.read(1);
                        wait(1000);
                        c1.write(1, "50");
        wait(1000);
        c0.write(1, "100");
        wait(1000);
        c0.commit();
        while (!c0.isCommitOk()) {
            wait(100);
        }
                        c1.commit();
        while (!c1.isCommitOk()) {
            wait(100);
        }
        Map<Timestamp, Message> c1log = c1.getLog();
        int count = 0;
        for(Message m : c1log.values()) {
            if (m instanceof OutcomeMessage) {
                count++;
                if (((OutcomeMessage) m).isOutcome() && count == 2){
                    PrintHelper.printError("Lost update!");
                    System.out.println(c0.logToString());
                    System.out.println(c1.logToString());
                    System.exit(0);
                }
            }
        }
        //////////////////////////
        // Phantom reads
        //////////////////////////
        c0.begin();
                        c1.begin();
        c0.read(3);
        wait(1000);
                        c1.write(3, "ghost");
                        c1.commit();
        while (!c1.isCommitOk()) {
            wait(100);
        }
        wait(1000);
        c0.read(3);
        wait(1000);
        c0.commit();
        while (!c0.isCommitOk()) {
            wait(100);
        }
        boolean readGhost = false;
        for(Message m : c0log.values()) {
            if (m instanceof ReplyMessage) {
                if(((ReplyMessage) m).getValue().equals("ghost")) {
                    readGhost = true;
                }
            }
            else if(m instanceof OutcomeMessage && readGhost) {
                if(((OutcomeMessage) m).isOutcome()) {
                    PrintHelper.printError("Phantom read!");
                    System.out.println(c0.logToString());
                    System.out.println(c1.logToString());
                    System.exit(0);
                }
            }
        }
        System.out.println(c0.logToString());
        System.out.println(c1.logToString());
        PrintHelper.printConfirm("Test OK.");
        System.exit(0);
    }

    /**
     * method which simulates the commit of a single transaction with a given number of operation in order to test the time needed
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
                    Thread.sleep(20);
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
                clients.get(j).connect((j+1)%sz);
            }
        }

        // 10 tries for statistical purposes
        int numberOfTry = 10;
        int openTransactions = 0;
        int totalRead = 0;
        int totalWrite = 0;
        int totalAbort = 0;
        int totalCommit = 0;
        int numTrans=0;

        Random rand = new Random();
        int randomNumber = 0;

        long totalTime = 0;
        int clientTarget;

        for(int t=0; t<numberOfTry; t++) {
            numTrans = numTransaction;
            long startTime = System.nanoTime();
            while(numTrans - openTransactions > 0){
                randomNumber = rand.nextInt(50);
                clientTarget = (randomNumber * (numTrans*numClients)) % numClients;
                if (status.get(clientTarget)){
                    if(randomNumber > 35) { // end transaction
                        if (randomNumber % 7 == 0) {
                            clients.get(clientTarget).abort();
                            totalAbort++;
                            System.out.println("Abort executed");
                        }
                        else {
                            clients.get(clientTarget).commit();
                            totalCommit++;
                            System.out.println("Commit executed");
                        }
                        status.put(clientTarget, false);
                        numTrans--;
                        openTransactions--;
                    }
                    else { // do write or read
                        if(randomNumber % 2 == 0){ // do write
                            clients.get(clientTarget).write(randomNumber*numTrans +2, "test"+randomNumber);
                            totalWrite++;
                        }
                        else { // do read
                            clients.get(clientTarget).read(randomNumber*numTrans);
                            totalRead++;
                        }
                    }
                }
                else { // open transaction
                    clients.get(clientTarget).begin();
                    openTransactions++;
                    System.out.println("transaction opened");
                    status.put(clientTarget, true);
                }
            }
            // close transactions still open
            for(Map.Entry<Integer, Boolean> client : status.entrySet()){
                if(client.getValue()) {
                    clients.get(client.getKey()).commit();
                    totalCommit++;
                    status.put(client.getKey(), false);
                }
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
            totalRead = 0;
            totalWrite = 0;
            totalCommit = 0;
            totalAbort = 0;
            openTransactions=0;
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Duration in milliseconds of a simulation with " + numTransaction + " transactions" +
                " and " + numClients + " clients (average of "+ (numberOfTry-1) + " tries) -> " + totalTime/(Math.pow(10, 6) * (numberOfTry-1)));
    }

    public static void multiClientStressTest() {
        ConfigHelper ch;
        try {
            ch = new ConfigHelper(FILENAME);
        } catch (Exception ignored) {
            return;
        }
        int numClients = 500;
        int numOps = 100000;
        int numCommits = 0;

        Map<Integer, Boolean> activeTransactions = new HashMap<>();

        List<Client> clients = new ArrayList<>();
        for (int i = 0; i<numClients; i++) {
            clients.add(new Client(FILENAME));
            clients.get(i).connect(i % ch.getPeerList().size());
            clients.get(i).connect((i+1) % ch.getPeerList().size());
            activeTransactions.put(i, false);
        }
        long beg = System.nanoTime();
        for (int j = 0; j<numOps; j++) {
            int clientId = (int) (Math.random() * (numClients-1));
            if (activeTransactions.get(clientId)) {
                if (j % 10 == 0) {
                    try {
                        Thread.sleep((int) (Math.random() * 500));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (j % 100 == 0) {
                    try {
                        Thread.sleep((int) (Math.random() * 2000) + 1000 );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (j % 1000 == 0) {
                    try {
                        Thread.sleep((int) (Math.random() * 3000) + 2000 );
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                System.out.print(j + ": ");
                int op = (int) (Math.random() * 4);
                try {
                    Thread.sleep((int) (Math.random() * 20));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                switch (op) {
                    case 0 -> {
                        int tmp = (int) (Math.random() * j/10);
                        clients.get(clientId).write(tmp, "Rand" + tmp);
                        System.out.println("Write " + tmp);
                        break;
                    }
                    case 1 -> {
                        int tmp = (int) (Math.random() * j/10);
                        clients.get(clientId).read(tmp);
                        System.out.println("Read " + tmp);
                        break;
                    }
                    case 2 -> {
                        activeTransactions.replace(clientId, false);
                        clients.get(clientId).commit();
                        System.out.println("Commit");
                        numCommits++;
                        break;
                    }
                    case 3 -> {
                        activeTransactions.replace(clientId, false);
                        clients.get(clientId).abort();
                        System.out.println("Abort");
                        break;
                    }
                }
            }
            else {
                if (clients.get(clientId).isCommitOk()) {
                    System.out.println("Client" + clientId + "'s commit is not ok!");
                    activeTransactions.replace(clientId, true);
                    clients.get(clientId).begin();
                }
                else {
                    j--;
                }
            }
        }

        for (Integer index : activeTransactions.keySet()) {
            if (activeTransactions.get(index)) {
                clients.get(index).abort();
                numOps++;
            }
            while (!clients.get(index).isCommitOk()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        long end = System.nanoTime();
        boolean areWeDoneYet = true;
        for (Integer index : activeTransactions.keySet()) {
            if(clients.get(index).isCommitOk()) {
                clients.get(index).detachAll();
            }
            if(!clients.get(index).isCommitOk()) {
                areWeDoneYet = false;
            }
        }
        double totalTime = ((double)(end-beg))/1000000000; // seconds
        PrintHelper.printError("Are we done??? " + areWeDoneYet);
        System.out.println("Done " + numOps + " operations and " + numCommits + " commits in " + totalTime + " seconds.");
        System.out.println("Commit throughput: " + numCommits/totalTime + " commits per second." );
        System.out.println("Operations throughput: " + numOps/totalTime + " operations per second." );
        System.exit(0);

    }

    /**
     * method to test the efficiency of commit
     * @param numClients number of clients which will do a commit
     * @param numConnections number client-server connections for each client
     */
    private static void multiCommitStressTest(int numClients, int numConnections, int transactionSize) throws ParserConfigurationException, IOException, SAXException {

        initializeServers(null);

        int sz = new ConfigHelper(FILENAME).getPeerList().size();
        if(numConnections > sz){
            numConnections = sz;
        }

        // list of clients
        ArrayList<Client> clients = new ArrayList<>();

        // clients creation
        for (int j = 0; j<numClients; j++){
            clients.add(new Client(FILENAME));
            //clients connection
            for(int i=0; i<numConnections; i++){
                clients.get(j).connect((j+1)%sz);
            }
            clients.get(j).begin();
        }

        Random rand = new Random();

        for(int i = 0; i < transactionSize; i++){
            for(int k = 0; k < clients.size(); k++){
                int randomNumber = rand.nextInt(50);

                if(randomNumber%2 == 0){ // write
                    clients.get(k).write(randomNumber*k, "write"+randomNumber*k);
                }
                else { // read
                    clients.get(k).read(randomNumber*k);
                }

            }
        }

        long startCommits = System.nanoTime();
        // committing all transactions
        for(int i = 0; i < clients.size()/2; i++){
            clients.get(i).commit();
            clients.get(i + clients.size()/2).commit();
        }
        if(clients.size()%2 != 0){
            clients.get(clients.size()-1).commit();
        }
        // waiting for the outcome
        for(int i = 0; i<clients.size(); i++){
            while (!clients.get(i).isCommitOk()) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        long stopCommits = System.nanoTime();

        System.out.println("Time needed for commit -> " + (stopCommits-startCommits)/(Math.pow(10, 6)) + " [ms]");
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

    private static void wait(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
