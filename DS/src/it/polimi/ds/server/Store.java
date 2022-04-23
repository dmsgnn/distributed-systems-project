package it.polimi.ds.server;

import it.polimi.ds.model.Tuple;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

public class Store implements Serializable {
    // Map<Integer, Tuple> containing the data, the key is the key of the tuple for query speedup
    protected Map<Integer, Tuple> store;

    public Store() {
         store = new HashMap<>();
    }

    public boolean contains (int key) {
        return store.containsKey(key);
    }

    public Tuple getTuple(int key) {
        return store.get(key);
    }

    public void put(Tuple t) {
        store.put(t.getKey(), t);
    }

    public String toString() {
        String res = "Store status\n";
        res+="Keys      Values\n";
        for(Map.Entry<Integer, Tuple> entry : store.entrySet()){
            int space = 10 - String.valueOf(entry.getKey()).length();
            String s = String.format("%"+space+"s", "");
            res+=entry.getKey() + s + entry.getValue().getValue() +"\n";
        }
        return res;
    }
}
