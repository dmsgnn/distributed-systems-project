package it.polimi.ds.messages;

import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.ArrayList;

public class WriteRequest extends ClientRequest {
    private Tuple tuple;
    private ArrayList<Server> servers;

    public WriteRequest(Tuple tuple, Timestamp timestamp, ArrayList<Server> servers) {
        this.servers = servers;
        super.timestamp = timestamp;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public ArrayList<Server> getServers() {
        return servers;
    }
}
