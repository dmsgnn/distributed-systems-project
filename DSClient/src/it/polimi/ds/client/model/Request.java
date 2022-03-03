package it.polimi.ds.client.model;

import java.io.Serializable;

public class Request implements Serializable {
    private Operation operation;
    private Object payload;

    public Request (Operation op, Object pl) {
        this.operation = op;
        this.payload = pl;
    }
}
