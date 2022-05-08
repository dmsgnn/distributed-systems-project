package it.polimi.ds.model;

import java.sql.Time;
import java.sql.Timestamp;

public class Tuple implements java.io.Serializable{
    private Integer key;
    private String value;

    private Timestamp timestamp;

    public Tuple(int k, String v) {
        this.key = k;
        this.value = v;
    }

    public Tuple(int k, String v, Timestamp t) {
        this.key = k;
        this.value = v;
        this.timestamp = t;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
    public String toString() {
        return key + ": " + value;
    }
}
