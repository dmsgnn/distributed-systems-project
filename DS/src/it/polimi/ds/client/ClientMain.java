package it.polimi.ds.client;

import it.polimi.ds.helpers.ConfigHelper;
import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.ReadMessage;
import it.polimi.ds.messages.WriteMessage;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;

public class ClientMain {

    private ConfigHelper ch;
    private static final String FILENAME = "DS/src/config.xml";
    private ArrayList<Server> serverConnections = new ArrayList<Server>();

    public static void main(String[] args) {
        System.out.println("  ____  ____                        _           _   \n" +
                " |  _ \\/ ___|       _ __  _ __ ___ (_) ___  ___| |_ \n" +
                " | | | \\___ \\ _____| '_ \\| '__/ _ \\| |/ _ \\/ __| __|\n" +
                " | |_| |___) |_____| |_) | | | (_) | |  __/ (__| |_ \n" +
                " |____/|____/      | .__/|_|  \\___/, |\\___|\\___|\\__|\n" +
                "                   |_|           |__/               ");
        System.out.println("Welcome to the most efficient distributed key storage, please connect to one of the servers below: ");
        ClientMain client = new ClientMain();
        client.startClient();
    }

    private void startClient() {

        try {
            this.ch = new ConfigHelper(FILENAME);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        List<SocketHandler> connections = new ArrayList<>();
        List<Server> servers = ch.getServerList();
        // add the first server to the list of connections
        do {
            selectServer(connections, servers);
        } while (connections.size() != 1);

        // choose the operation
        while (true) {
            menu(connections, servers);
        }
    }

    public void selectServer(List<SocketHandler> connections, List<Server> servers) {
        if (servers.size() != 0) {
            printServers(servers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt() - 1;
            sc.nextLine();
            // Try to create a connection with the selected server
            try {
                SocketHandler s = new SocketHandler(servers.get(choice));
                if (s.isConnected()) {
                    connections.add(s);
                    serverConnections.add(s.getServer());
                    servers.remove(servers.get(choice));
                }
            } catch (IndexOutOfBoundsException e) {
                PrintHelper.printError("Invalid input...");
                //System.out.println("[!] Invalid input...");
            }
        }
        else {
            PrintHelper.printError("No server available :(");
            //System.out.println("No server available :(");
        }
    }

    public SocketHandler selectConnection(List<SocketHandler> connections) {
        List<Server> servers = new ArrayList<>();
        for (SocketHandler sock : connections) {
            servers.add(sock.getServer());
        }
        if (connections.size() != 0) {
            printServers(servers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt() - 1;
            sc.nextLine();
            return connections.get(choice);
        }
        else {
            PrintHelper.printError("No server available :(");
            //System.out.println("No server available :(");
            return null;
        }
    }

    public void closeConnection(List<SocketHandler> connections) { // TODO

    }

    private void printServers(List<Server> servers) {
        int i = 1;
        for (Server s: servers) {
            System.out.println(i + ") " + s.getHost() + ":" + s.getPort());
            i++;
        }
    }

    private void menu(List<SocketHandler> connections, List<Server> servers) {
        System.out.println("Select one of the following operations:");
        String[] options = {
                "Add connection",       //1
                "Detach connection",    //2
                "Begin transaction",    //3
                "Write",                //4
                "Read",                 //5
                "Commit transaction",   //6
                "Abort transaction",    //7
                "Exit",                 //8
        };
        int i = 1;
        for (String s : options) {
            System.out.println(i+") " + s);
            i++;
        }

        Scanner sc = new Scanner (System.in);
        int choice = sc.nextInt();
        sc.nextLine();

        switch (choice) {
            /*
                Yet to implement:
                2. detach
                3. begin transaction
                6. commit transaction
                7. abort transaction
             */
            case 1 -> // add connection
                    selectServer(connections, servers);
            case 4 -> // write tuple
                    doWrite(connections);
            case 5 -> // read tuple
                    doRead(connections);
            case 8 -> { // exit
                System.out.println("Cya!");
                System.exit(0);
            }
            default -> System.out.println("WORK IN PROGRESS... (the feature has yet to be implemented)");
        }
    }

    private void doWrite(List<SocketHandler> connections) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        // read value
        System.out.print("Value: ");
        String value = sc.nextLine();

        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all servers connected
        for (SocketHandler s : connections) {
            s.send(new WriteMessage(new Tuple(key, value), ts, serverConnections));
        }
    }

    private void doRead(List<SocketHandler> connections) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all servers connected
        for (SocketHandler s : connections) {
            s.send(new ReadMessage(key, ts));
            
        }
    }
}
