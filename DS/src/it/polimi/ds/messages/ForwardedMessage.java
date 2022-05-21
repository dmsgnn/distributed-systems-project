package it.polimi.ds.messages;

import it.polimi.ds.model.Peer;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public class ForwardedMessage extends Message {

    private Message message;
    private Timestamp sourceSocketId;

    public ForwardedMessage(Message message, Timestamp sourceSocketId) {
        this.sourceSocketId = sourceSocketId;
        this.message = message;
    }

    public ForwardedMessage(Timestamp sourceSocketId) {
        this.sourceSocketId = sourceSocketId;
    }

    public Message getMessage() {
        return message;
    }

    public Timestamp getSourceSocketId() {
        return sourceSocketId;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() +") " + "{" + message +"}" + "sourceTs=" + this.sourceSocketId;
    }
}
