package it.polimi.ds.messages;

import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

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
