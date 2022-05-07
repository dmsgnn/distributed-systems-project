package it.polimi.ds.model;

import it.polimi.ds.messages.CommitMessage;
import it.polimi.ds.middleware.ServerSocketHandler;

import java.sql.Timestamp;

public class CommitInfo {
    CommitMessage commitMessage;
    ServerSocketHandler commitManager;

    public CommitInfo (CommitMessage message, ServerSocketHandler sh) {
        this.commitMessage = message;
        this.commitManager = sh;
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
}
