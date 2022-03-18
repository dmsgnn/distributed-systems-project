package it.polimi.ds.messages;

import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.List;

public class WriteRequest extends ClientRequest {
    private Tuple tuple;
    private List<Server> servers;

    public WriteRequest(Tuple tuple, Timestamp timestamp, List<Server> servers) {
        this.servers = servers;
        super.timestamp = timestamp;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public List<Server> getServers() {
        return servers;
    }
}
