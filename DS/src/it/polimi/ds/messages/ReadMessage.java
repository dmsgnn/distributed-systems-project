package it.polimi.ds.messages;

import java.sql.Timestamp;

public class ReadMessage extends Message {
    private Integer key;

    public ReadMessage(Integer key, Timestamp timestamp) {
        super.timestamp = timestamp;
        this.key = key;
    }

    public Integer getKey() {
        return key;
    }
}
