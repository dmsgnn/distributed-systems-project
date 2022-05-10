package it.polimi.ds.tests;

import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.model.CommitInfo;
import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Workspace;
import it.polimi.ds.server.Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
