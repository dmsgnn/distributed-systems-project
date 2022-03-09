package it.polimi.ds.messages;

import java.io.Serializable;

public class ServerReply implements Serializable {
    private String value;

    public ServerReply(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
