package it.polimi.ds.messages;

import java.sql.Timestamp;

public class PersistMessage extends Message{
    private Timestamp persistTimestamp;

    public PersistMessage(Timestamp persistTimestamp) {
        this.persistTimestamp = persistTimestamp;
    }

    public Timestamp getPersistTimestamp() {
        return persistTimestamp;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() +") ts=" + this.persistTimestamp;
    }
}
