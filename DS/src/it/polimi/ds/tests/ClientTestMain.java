package it.polimi.ds.tests;

import it.polimi.ds.client.Client;

public class ClientTestMain {
    private static final String FILENAME = "DS/src/config.xml";

    public static void main(String[] args) {
        test1();
    }

    private static void test1() {
        Client c1 = new Client(FILENAME);
        Client c2 = new Client(FILENAME);

        boolean b1 = c1.connect(1);
        boolean b2 = c2.connect(0);
        while (!b1 || !b2) {
            b1 = c1.connect(1);
            b2 = c2.connect(0);
        }

        c1.begin();
        c1.write(9, "primo");

        c2.begin();
        c2.write(10, "secondo");

        c1.commit();
        c2.commit();
        // t1 < t2
    }
}
