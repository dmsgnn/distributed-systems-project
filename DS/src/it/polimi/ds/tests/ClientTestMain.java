package it.polimi.ds.tests;

import it.polimi.ds.client.Client;
import it.polimi.ds.helpers.ConfigHelper;
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
import java.util.List;

public class ClientTestMain {
    private static final String FILENAME = "DS/src/it/polimi/ds/tests/config/config_test.xml";

    public static void main(String[] args) {
        test5();
    }

    /**
     * Server0 riceve due commit, il primo a tempo x+1 e il secondo a tempo x. Possiamo vedere che il primo
     * tentativo di commit (x+1) che fa viene messo in pausa per gestire il commit che ha ricevuto da un altro
     * server con timestamp x.
     *
     * Entrambi vanno a buon fine perché sono eseguiti su due chiavi diverse.
     */
    private static void test1() {
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);


        c0.connect(0);
        // c0 deve fare due attempts in quanto server0 riceve prima il commit di c0 e poi quello di c1 (che ha un timestamp minore)
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
     * Solo quello a tempo x va a buon fine perché lavorano entrambi sulla stessa chiave.
     */
    private static void test2() {
        Client c0 = new Client(FILENAME);
        Client c1 = new Client(FILENAME);


        c0.connect(0);
        // c0 deve fare due attempts in quanto server0 riceve prima il commit di c0 e poi quello di c1 (che ha un timestamp minore)
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

    private static int initializeServers(TestSpecs ts) {
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return peerList.size();
    }
}
