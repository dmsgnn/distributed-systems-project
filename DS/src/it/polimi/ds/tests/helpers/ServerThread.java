package it.polimi.ds.tests.helpers;

import it.polimi.ds.server.Server;
import it.polimi.ds.tests.helpers.TestSpecs;

import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;

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
