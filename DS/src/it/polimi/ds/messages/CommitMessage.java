package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;
import it.polimi.ds.server.Store;
import it.polimi.ds.server.Workspace;

import java.sql.Timestamp;
import java.util.List;

public class CommitMessage extends Message {
    private List<Peer> peers;

    private Timestamp timestamp;

    private Workspace workspace;

    public CommitMessage(Workspace w, Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.timestamp = ts;
        this.workspace = w;
    }

    public CommitMessage(Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.timestamp = ts;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public List<Peer> getServers() {
        return peers;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace w) {
        this.workspace = w;
    }
}
