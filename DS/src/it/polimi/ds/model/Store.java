package it.polimi.ds.model;

import it.polimi.ds.model.Tuple;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;

public class Store implements Serializable {
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
        //out.println(this); // uncomment to debug store status
    }

    public Map<Integer, Tuple> getStore() {
        return store;
    }

    public String toString() {
        int maxKeyLength = 3;
        int maxValueLength = 5;
        for(Map.Entry<Integer, Tuple> entry : store.entrySet()){
            if(entry.getValue().getValue().length() > maxValueLength){
                maxValueLength = entry.getValue().getValue().length();
            }
            String temp = String.valueOf(entry.getValue().getKey());
            if(temp.length() > maxKeyLength){
                maxKeyLength = temp.length();
            }
        }
        StringBuilder res = new StringBuilder("Store status\n");
        res.append(separator(maxKeyLength, maxValueLength));
        res.append("\n| KEY");
        for(int i = 0; i<maxKeyLength-3; i++){
            res.append(" ");
        }
        res.append("  | VALUE");

        for(int i = 0; i<maxValueLength-5; i++){
            res.append(" ");
        }
        res.append("  |\n");
        res.append(separator(maxKeyLength, maxValueLength));
        for(Map.Entry<Integer, Tuple> entry : store.entrySet()){
            res.append("\n");
            int space = maxKeyLength - String.valueOf(entry.getKey()).length();
            StringBuilder s = new StringBuilder();
            for(int i = 0; i<space; i++){
                s.append(" ");
            }
            int space1 = maxValueLength - entry.getValue().getValue().length();
            StringBuilder s1 = new StringBuilder();
            for(int i = 0; i<space1; i++){
                s1.append(" ");
            }
            res.append("| ").append(entry.getKey()).append(s).append("  | ").append(entry.getValue().getValue()).append(s1).append("  |\n");
            res.append(separator(maxKeyLength, maxValueLength));
        }
        return res.toString();
    }

    public String separator(int kl, int vl){
        StringBuilder res= new StringBuilder("+-");
        for(int i = 0; i<kl; i++){
            res.append("-");
        }
        res.append("--+-");
        for(int i = 0; i<vl; i++){
            res.append("-");
        }
        res.append("--+");
        return res.toString();
    }
}
