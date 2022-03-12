package it.polimi.ds.messages;

import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;

public class WriteRequest extends ClientRequest {
    private Tuple tuple;

    public WriteRequest(Tuple tuple, Timestamp timestamp) {
        super.timestamp = timestamp;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }
}
