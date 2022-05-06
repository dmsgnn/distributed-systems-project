package it.polimi.ds.messages;

public class AckMessage extends Message{
    private boolean ack;


    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public boolean isAck() {
        return ack;
    }
}
