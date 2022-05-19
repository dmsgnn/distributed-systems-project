package it.polimi.ds.model;

import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.middleware.ServerSocketHandler;

import java.sql.Timestamp;

public class CommitInfo {
    CommitMessage commitMessage;
    ServerSocketHandler commitManager;

    boolean isBeingPersisted; // set to true if the server already sent an ACK for this transaction

    int iter;

    public CommitInfo (CommitMessage message, ServerSocketHandler sh) {
        this.commitMessage = message;
        this.commitManager = sh;
        this.iter = 0;
        this.isBeingPersisted = false;
    }

    public CommitMessage getCommitMessage() {
        return commitMessage;
    }

    public ServerSocketHandler getCommitManager() {
        return commitManager;
    }

    public Timestamp getCommitTimestamp() {
        return commitMessage.getCommitTimestamp();
    }

    public int getIter() {
        return this.iter;
    }

    public int updateIter() {
        iter++;
        return this.iter;
    }

    public String toString() {
        return this.getCommitTimestamp().toString() + "(" + isBeingPersisted + ")";
    }

    public boolean isBeingPersisted() {
        return isBeingPersisted;
    }

    public void setBeingPersisted(boolean beingPersisted) {
        isBeingPersisted = beingPersisted;
    }
}
