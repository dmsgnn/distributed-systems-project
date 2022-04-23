package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.List;

public class BeginMessage extends Message {
    private List<Peer> peers;

    private Timestamp timestamp;

    public BeginMessage(Timestamp ts, List<Peer> peers) {
        this.peers = peers;
        this.timestamp = ts;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public List<Peer> getServers() {
        return peers;
    }
}
