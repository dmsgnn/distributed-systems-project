package it.polimi.ds.tests;

import it.polimi.ds.server.Server;
import it.polimi.ds.tests.helpers.TestSpecs;

import java.awt.*;

public class ServerThread implements Runnable{

    private final int id;
    private final String configPath;
    private final TestSpecs testSpecs;

    public ServerThread(int id, String configPath, TestSpecs ts) {
        this.id = id;
        this.configPath = configPath;
        this.testSpecs = ts;
    }

    @Override
    public void run() {
        new Server(id, configPath, testSpecs);
    }
}
