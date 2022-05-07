package it.polimi.ds.model;

import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.middleware.ServerSocketHandler;

import java.sql.Timestamp;

public class CommitInfo {
    CommitMessage commitMessage;
    ServerSocketHandler commitManager;

    int iter;

    public CommitInfo (CommitMessage message, ServerSocketHandler sh) {
        this.commitMessage = message;
        this.commitManager = sh;
        this.iter = 0;
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
}
