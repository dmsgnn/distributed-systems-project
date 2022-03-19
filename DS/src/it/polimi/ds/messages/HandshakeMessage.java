package it.polimi.ds.messages;

public class HandshakeMessage extends Message {
    private int id;

    public HandshakeMessage(int id) {
        this.id = id;
    }

    public int getServerId() {
        return id;
    }
}
