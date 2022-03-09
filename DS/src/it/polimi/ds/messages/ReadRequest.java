package it.polimi.ds.messages;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class ReadRequest implements ClientRequest {
    private Integer key;
    private Timestamp timestamp;

    public ReadRequest(Integer key, Timestamp timestamp) {
        this.timestamp = timestamp;
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
