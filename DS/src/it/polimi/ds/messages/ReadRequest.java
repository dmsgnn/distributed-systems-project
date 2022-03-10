package it.polimi.ds.messages;

import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;

public class ReadRequest extends ClientRequest {
    private Integer key;

    public ReadRequest(Integer key, Timestamp timestamp) {
        super.timestamp = timestamp;
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }
}
