package it.polimi.ds.messages;

public class ServerReply extends Message {
    private String value;

    public ServerReply(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
