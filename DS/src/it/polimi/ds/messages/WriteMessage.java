package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;
import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.List;

public class WriteMessage extends Message {
    private Tuple tuple;
    private List<Peer> peers;

    public WriteMessage(Tuple tuple, List<Peer> peers) {
        this.peers = peers;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public List<Peer> getServers() {
        return peers;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() +") " + this.peers.toString() + " tuple="+tuple;
    }
}
