package it.polimi.ds.messages;

import java.sql.Timestamp;

public class VoteMessage extends Message {
    private int iter;
    private Timestamp commitTimestamp;

    public VoteMessage (Timestamp commitTimestamp, int iter) {
        this.commitTimestamp = commitTimestamp;
        this.iter = iter;
    }

    public Timestamp getCommitTimestamp() {
        return commitTimestamp;
    }

    public int getIter() {
        return iter;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() + ") iter="+iter + ", commit="+commitTimestamp;
    }
}
