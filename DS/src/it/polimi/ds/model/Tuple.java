package it.polimi.ds.model;

public class Tuple implements java.io.Serializable{
    private Integer key;
    private String value;

    public Tuple(int k, String v) {
        this.key = k;
        this.value = v;
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

    public String toString() {
        return key + ": " + value;
    }
}
