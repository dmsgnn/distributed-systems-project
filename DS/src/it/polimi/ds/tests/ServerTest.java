package it.polimi.ds.tests;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.middleware.ServerSocketHandler;
import it.polimi.ds.model.CommitInfo;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;
import it.polimi.ds.model.Workspace;
import it.polimi.ds.server.Server;
import it.polimi.ds.tests.helpers.ServerThread;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {
    @Test
    public void dequeueCommitTest() {
        Server server = new Server();
        List<Peer> peers = new ArrayList<>();
        peers.add(new Peer("192.168.0.1", 4000, 0));

        Workspace w = new Workspace(Timestamp.from(Instant.now()));

        CommitMessage cm1 = new CommitMessage(w, Timestamp.from(Instant.now()), peers);
        CommitInfo ci1 = new CommitInfo(cm1, null);
        CommitMessage cm2 = new CommitMessage(w, Timestamp.from(Instant.now()), peers);
        CommitInfo ci2 = new CommitInfo(cm2, null);
        server.enqueueCommit(ci1);
        server.enqueueCommit(ci2);
        server.dequeueCommit(ci1, false);
        assertEquals(server.commitBuffer.size(), 1);
        assertEquals(server.commitBuffer.get(0), ci2);
    }

    @Test
    public void isWorkspaceValidTest() {
        // create local storage
        Timestamp t1 = Timestamp.from(Instant.now());
        Server server = new Server();
        server.putInStore(new Tuple(1, "1", t1));

        // create workspace1 to validate
        Timestamp t2 = Timestamp.from(Instant.now());
        Workspace w1 = new Workspace(t2);
        w1.put(new Tuple(1, "one"));
        w1.put(new Tuple(2, "two"));
        w1.put(new Tuple(3, "three"));

        assertTrue(server.isWorkspaceValid(w1)); // workspace1_timestamp > store_timestamp => Workspace valid

        server.putInStore(new Tuple(2, "2", t2)); // add tuple with timestamp t2

        Workspace w2 = new Workspace(t1); // workspace2_timestamp is t2 (> t2)
        w2.put(new Tuple(1, "uno"));
        w2.put(new Tuple(2, "due"));
        w2.put(new Tuple(3, "tre"));

        assertFalse(server.isWorkspaceValid(w2)); // workspace2_timestamp < store_timestamp => Workspace invalid
    }

    @Test
    public void keyOwnersTest() throws ParserConfigurationException, IOException, SAXException {
        List<ServerThread> sList = ClientTestMain.initializeServers(null);

        assertEquals(sList.size(), new ConfigHelper("DS/src/it/polimi/ds/tests/config/config_test.xml").getPeerList().size());
        assertNotNull(sList.get(0).getServer());
        ServerSocketHandler sh = sList.get(0).getServer().getConnectionsToServers().get(1);
        assertNotNull(sh);
        int R = sList.get(0).getServer().getR();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // no recipient has the key
        ArrayList<Integer> recipients1 = new ArrayList<>();
        for (int i=R; i<sList.size(); i++) {
            recipients1.add(i);
        }
        int key1 = sList.size();
        assertEquals(0, sh.getPossibleKeyOwners(recipients1, key1).size());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // every recipient has the key
        ArrayList<Integer> recipients2 = new ArrayList<>();
        for (int i=0; i<sList.size(); i++) {
            recipients2.add(i);
        }
        int key2 = key1;
        assertEquals(R, sh.getPossibleKeyOwners(recipients2, key2).size());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // one recipient has the key
        ArrayList<Integer> recipients3 = new ArrayList<>();
        for (int i=R; i<sList.size(); i++) {
            recipients3.add(i);
        }
        recipients3.add(0);
        int key3 = key1;
        assertEquals(1, sh.getPossibleKeyOwners(recipients3, key3).size());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // the list of recipients is empty
        ArrayList<Integer> recipients4 = new ArrayList<>();
        int key4 = key1;
        assertEquals(0, sh.getPossibleKeyOwners(recipients4, key4).size());
    }
}
