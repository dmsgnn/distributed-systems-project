package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;
import it.polimi.ds.server.Workspace;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CommitMessage extends Message {
    private List<Peer> peers;

    private Timestamp commitTimestamp;

    private Workspace workspace;

    public CommitMessage(Workspace w, Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.commitTimestamp = ts;
        this.workspace = w;
    }

    public CommitMessage(Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.commitTimestamp = ts;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
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

    /**
     * @return the list of the IDs of receivers
     */
    public ArrayList<Integer> getIDs (){
        ArrayList<Integer> idList = new ArrayList<>();
        getServers().stream().forEach((temp) -> idList.add(temp.getId()));
        return idList;
    }
}
