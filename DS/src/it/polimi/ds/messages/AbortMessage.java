package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;

import java.sql.Timestamp;
import java.util.List;

public class AbortMessage extends Message {
    private List<Peer> peers;


    public AbortMessage(List<Peer> peers) {
        this.peers = peers;
    }

    public List<Peer> getServers() {
        return peers;
    }
}
