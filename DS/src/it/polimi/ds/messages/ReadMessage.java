package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ReadMessage extends Message {
    private Integer key;
    private List<Peer> peers;
    private ArrayList<Integer> idList = new ArrayList<>();


    public ReadMessage(Integer key, List<Peer> peers) {
        this.peers = peers;
        this.key = key;
        peers.forEach((temp) -> idList.add(temp.getId()));
    }

    public Integer getKey() {
        return key;
    }

    public ArrayList<Integer> getIDs() {
        return idList;
    }

    public List<Peer> getServers() {
        return peers;
    }
}
