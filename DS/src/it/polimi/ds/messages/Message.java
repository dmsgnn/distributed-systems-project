package it.polimi.ds.messages;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * this abstract class is used for communication between client and server of serializable objects
 */
public abstract class Message implements Serializable {
    public abstract String toString();
}
