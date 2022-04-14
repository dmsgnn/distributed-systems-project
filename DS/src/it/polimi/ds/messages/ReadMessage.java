package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;

import java.sql.Timestamp;
import java.util.List;

public class ReadMessage extends Message {
    private Integer key;
    private List<Peer> peers;

    public ReadMessage(Integer key, List<Peer> peers) {
        this.peers = peers;
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    public List<Peer> getServers() {
        return peers;
    }

}
