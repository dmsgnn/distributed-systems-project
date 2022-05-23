package it.polimi.ds.server;

import it.polimi.ds.middleware.SocketHandler;
import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.messages.HandshakeMessage;
import it.polimi.ds.messages.Message;
import it.polimi.ds.messages.WriteMessage;
import it.polimi.ds.model.Peer;

import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.*;

/**
 * Main class of the server
 */
public class ServerMain {

    private static final String FILENAME = "DS/src/config.xml";

    public static void main(String[] args) {
        int id;
        if(args.length >= 1)
            id = Integer.parseInt(args[0]);
        else
            id = 0;


        new Server(id, FILENAME);
        //server.startServer();
    }
}
