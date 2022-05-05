package it.polimi.ds.messages;

import it.polimi.ds.model.Tuple;

public class ReplyMessage extends Message {
    private Tuple tuple;

    public ReplyMessage(String value) {
        this.tuple = new Tuple(0, value);
    }
    public ReplyMessage(Tuple t) {
        this.tuple = t;
    }

    public String getValue() {
        return this.tuple.getValue();
    }

    public Tuple getTuple() {
        return this.tuple;
    }
}
