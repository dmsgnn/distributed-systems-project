package it.polimi.ds.client;

import it.polimi.ds.helpers.PrintHelper;
import it.polimi.ds.messages.ReadRequest;
import it.polimi.ds.messages.WriteRequest;

import java.io.File;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.polimi.ds.model.Server;
import it.polimi.ds.model.Tuple;
import org.w3c.dom.*;

import javax.xml.parsers.*;

public class ClientMain {

    private static final String FILENAME = "DS/src/servers.xml";
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
        List<ClientSocketHandler> connections = new ArrayList<>();
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

    public void selectServer(List<ClientSocketHandler> connections, List<Server> servers) {
        if (servers.size() != 0) {
            printServers(servers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt() - 1;
            sc.nextLine();
            // Try to create a connection with the selected server
            try {
                ClientSocketHandler s = new ClientSocketHandler(servers.get(choice));
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

    public ClientSocketHandler selectConnection(List<ClientSocketHandler> connections) {
        List<Server> servers = new ArrayList<>();
        for (ClientSocketHandler sock : connections) {
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

    public void closeConnection(List<ClientSocketHandler> connections) { // TODO

    }

    private void printServers(List<Server> servers) {
        int i = 1;
        for (Server s: servers) {
            System.out.println(i + ") " + s.getHost() + " : " + s.getPort());
            i++;
        }
    }

    // Get the servers from `servers.xml`
    private List<Server> getServers() {
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

    private void menu(List<ClientSocketHandler> connections, List<Server> servers) {
        System.out.println("Select one of the following operations:");
        String[] options = {
                "Add connection",       //1
                "Detach connection",    //2
                "Begin transaction",     //3
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

    private void doWrite(List<ClientSocketHandler> connections) {
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
        for (ClientSocketHandler s : connections) {
            s.send(new WriteRequest(new Tuple(key, value), ts, serverConnections));
        }
    }

    private void doRead(List<ClientSocketHandler> connections) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        // prepare the timestamp
        Timestamp ts = Timestamp.from(Instant.now());

        // send the request to all servers connected
        for (ClientSocketHandler s : connections) {
            s.send(new ReadRequest(key, ts));
            
        }
    }
}
