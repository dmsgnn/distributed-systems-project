package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;

import java.sql.Timestamp;
import java.util.ArrayList;
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

    /**
     * @return the list of the IDs of receivers
     */
    public ArrayList<Integer> getIDs (){
        ArrayList<Integer> idList = new ArrayList<>();
        getServers().stream().forEach((temp) -> idList.add(temp.getId()));
        return idList;
    }
}
