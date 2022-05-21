package it.polimi.ds.messages;

import java.sql.Timestamp;

public class AckMessage extends Message{
    private boolean ack;
    private Timestamp commitTimestamp;

    private int iter;

    public AckMessage (boolean ack, Timestamp commitTimestamp) {
        this.ack = ack;
        this.commitTimestamp = commitTimestamp;
    }

    public AckMessage(Timestamp commitTimestamp) {
        this.ack = true;
        this.commitTimestamp = commitTimestamp;
    }

    public AckMessage (boolean ack, Timestamp commitTimestamp, int iter) {
        this.ack = ack;
        this.commitTimestamp = commitTimestamp;
        this.iter = iter;
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

    public int getIter() {
        return iter;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() + ") " + "ack=" + ack + ", iter="+iter + ", commit="+commitTimestamp;
    }
}
