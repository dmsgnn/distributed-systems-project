package it.polimi.ds.client;

import it.polimi.ds.messages.ReadRequest;
import it.polimi.ds.messages.WriteRequest;
import it.polimi.ds.middleware.SocketWrapper;

import java.io.File;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;
import org.w3c.dom.*;

import javax.xml.parsers.*;

public class ClientMain {

    private static final String FILENAME = "DS/src/servers.xml";

    public static void main(String[] args) {
        System.out.println("  ____  ____                        _           _   \n" +
                " |  _ \\/ ___|       _ __  _ __ ___ (_) ___  ___| |_ \n" +
                " | | | \\___ \\ _____| '_ \\| '__/ _ \\| |/ _ \\/ __| __|\n" +
                " | |_| |___) |_____| |_) | | | (_) | |  __/ (__| |_ \n" +
                " |____/|____/      | .__/|_|  \\___/, |\\___|\\___|\\__|\n" +
                "                   |_|           |__/               ");
        System.out.println("Welcome to the most efficient distributed key storage, please connect to one of the servers below: ");
        List<SocketWrapper> connections = new ArrayList<>();
        List<Server> servers = getServers();
        // add the first server to the list of connections
        do {
            selectServer(connections, servers);
        } while (connections.size() != 1);

        // choose the operation
        while (true) {
            menu(connections, servers);
        }
    }

    public static void selectServer(List<SocketWrapper> connections, List<Server> servers) {
        if (servers.size() != 0) {
            printServers(servers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt() - 1;
            sc.nextLine();
            // Try to create a connection with the selected server
            try {
                SocketWrapper s = new SocketWrapper(servers.get(choice));
                if (s.isConnected()) {
                    connections.add(s);
                    servers.remove(servers.get(choice));
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.println("[!] Invalid input...");
            }
        }
        else {
            System.out.println("No server available :(");
        }
    }

    public static SocketWrapper selectConnection(List<SocketWrapper> connections) {
        List<Server> servers = new ArrayList<>();
        for (SocketWrapper sock : connections) {
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
            System.out.println("No server available :(");
            return null;
        }
    }

    public static void closeConnection(List<SocketWrapper> connections) {

    }

    private static void printWelcome(List<Server> servers) {
        printServers(servers);
    }

    private static void printServers(List<Server> servers) {
        int i = 1;
        for (Server s: servers) {
            System.out.println(i + ") " + s.getHost() + ":" + s.getPort());
        }
    }

    // Get the servers from `servers.xml`
    private static List<Server> getServers() {
        List<Server> res = new ArrayList<>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document serversDoc = db.parse(new File(FILENAME));
            NodeList sList = serversDoc.getElementsByTagName("server");
            for (int i = 0; i < sList.getLength(); i++) {
                Node n = sList.item(i);
                if(n.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) n;
                    Server s = new Server(
                            e.getElementsByTagName("host").item(0).getTextContent(),
                            Integer.parseInt(e.getElementsByTagName("port").item(0).getTextContent())
                            );
                    res.add(s);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return res;
    }

    private static void menu(List<SocketWrapper> connections, List<Server> servers) {
        System.out.println("Select one of the following operations:");
        String[] options = {
                "Add connection",       //1
                "Detach connection",    //2
                "Begin transacion",     //3
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
            case 1: // add connection
                selectServer(connections, servers);
                break;
            case 4: // write tuple
                doWrite(connections);
                break;
            case 5: // read tuple
                doRead(connections);
                break;
            case 8: // exit
                System.out.println("Cya!");
                System.exit(0);
            default:
                System.out.println("WORK IN PROGRESS... (the feature has yet to be implemented)");
        }
    }

    private static void doWrite(List<SocketWrapper> connections) {
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
        for (SocketWrapper s : connections) {
            s.send(new WriteRequest(new Tuple(key, value), ts));
        }
    }

    private static void doRead(List<SocketWrapper> connections) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all servers connected
        for (SocketWrapper s : connections) {
            s.send(new ReadRequest(key, ts));
        }
    }
}
