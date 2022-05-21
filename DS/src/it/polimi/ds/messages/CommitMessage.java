package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Workspace;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommitMessage extends Message {
    private List<Peer> peers;

    private Timestamp commitTimestamp;
    private ArrayList<Integer> idList = new ArrayList<>();

    private Workspace workspace;

    public CommitMessage(Workspace w, Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.commitTimestamp = ts;
        this.workspace = w;

    }

    public CommitMessage(Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.commitTimestamp = ts;
        peers.forEach((temp) -> idList.add(temp.getId()));
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public ArrayList<Integer> getIDs() {
        return idList;
    }

    public Timestamp getCommitTimestamp() {
        return commitTimestamp;
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

    @Override
    public String toString() {
        return "(" + this.getClass() +") " + this.peers.toString() + ", ts="+commitTimestamp;
    }
}
