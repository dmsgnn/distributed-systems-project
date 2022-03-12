package it.polimi.ds.server;

import it.polimi.ds.messages.ServerReply;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.*;

/**
 * Main class of the server
 */
public class ServerMain {
    //needed
    private final int portNumber;
    private final ExecutorService executor;

    // store
    private Map<Integer, String> store = new HashMap<>();
    private Map<Integer, Timestamp> storeChronology = new HashMap<>();

    //constructor
    public ServerMain(int port){
        this.portNumber = port;
        executor = Executors.newCachedThreadPool();
    }

    /**
     * Starts the server to listen to clients and to accept their connection
     * creates a ServerSocketHandler for each client
     */
    public void startServer() {
        System.out.println("Port number: " + portNumber);
        ServerSocket serverSocket = null;
        System.out.println("Server started!");
        try {
            try{
                serverSocket = new ServerSocket(portNumber);
            } catch (IOException e) {
                System.out.println("\nPort already in use!\n");
                exit(0);
                //e.printStackTrace();
            }
            while(true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Accepted!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert clientSocket != null;
                ServerSocketHandler connection = new ServerSocketHandler(clientSocket, this);
                executor.submit(connection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // store access

    public String getValue(int key) {
        return store.get(key);
    }

    public void setValue(int key, String value){
        store.put(key, value);
    }

    public boolean isContained (int key) {
        return store.containsKey(key);
    }

    public void showStore() {
        out.println("Store status");
        out.println("Keys      Values");
        for(Map.Entry<Integer, String> entry : store.entrySet()){
            int space = 10 - String.valueOf(entry.getKey()).length();
            String s = String.format("%"+space+"s", "");
            out.println(entry.getKey() + s + entry.getValue());
        }
    }

    public static void main(String[] args) throws IOException {
        int portNumber;
        InetAddress inetAddress = null;
        /*
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        */

        if(inetAddress != null)
            System.out.println("Hello, I'm on: " + inetAddress.getHostAddress());
        else
            System.out.println("Hello, I'm on: 127.0.0.1");

        if(args.length >= 1 && Integer.parseInt(args[0])>1024)
            portNumber = Integer.parseInt(args[0]);
        else
            portNumber = 4000;

        ServerMain server = new ServerMain(portNumber);
        server.startServer();
    }
}
