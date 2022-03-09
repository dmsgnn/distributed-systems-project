package it.polimi.ds.messages;

import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class WriteRequest implements ClientRequest {
    private Tuple tuple;
    private Timestamp timestamp;

    public WriteRequest(Tuple tuple, Timestamp timestamp) {
        this.timestamp = timestamp;
        this.tuple = tuple;
    }

    public Tuple getTuple() {
        return tuple;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
