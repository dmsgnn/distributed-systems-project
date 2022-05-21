package it.polimi.ds.messages;

import java.sql.Timestamp;

public class AbortTransactionMessage extends Message{
    private Timestamp abortTimestamp;

    public AbortTransactionMessage(Timestamp abortTimestamp) {
        this.abortTimestamp = abortTimestamp;
    }

    public Timestamp getAbortTimestamp() {
        return abortTimestamp;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() + ") " + abortTimestamp;
    }
}
