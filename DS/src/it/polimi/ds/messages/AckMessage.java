package it.polimi.ds.messages;

import java.sql.Timestamp;

public class AckMessage extends Message{
    private boolean ack;
    private Timestamp commitTimestamp;

    public AckMessage (boolean ack, Timestamp commitTimestamp) {
        this.ack = ack;
        this.commitTimestamp = commitTimestamp;
    }

    public AckMessage(Timestamp commitTimestamp) {
        this.ack = true;
        this.commitTimestamp = commitTimestamp;
    }
    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public boolean isAck() {
        return ack;
    }

    public Timestamp getCommitTimestamp() {
        return this.commitTimestamp;
    }
}
