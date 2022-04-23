package it.polimi.ds.server;

import it.polimi.ds.model.Tuple;

import java.sql.Timestamp;
import java.util.Collection;

public class Workspace extends Store {
    private final Timestamp beginTimestamp;
    private Timestamp commitTimestamp;

    public Workspace (Timestamp beginTimestamp) {
        super();
        this.beginTimestamp = beginTimestamp;
    }

    public void setCommitTimestamp(Timestamp timestamp) {
        this.commitTimestamp = timestamp;
    }

    public Timestamp getBeginTimestamp() {
        return beginTimestamp;
    }

    public Timestamp getCommitTimestamp() {
        return commitTimestamp;
    }

    public Collection<Integer> getSavedIDs() {
        return this.store.keySet();
    }
}
