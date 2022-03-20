package it.polimi.ds.client;

import it.polimi.ds.helpers.PrintHelper;

import java.util.List;
import java.util.Scanner;

import it.polimi.ds.model.Peer;

public class ClientMain {

    private static final String FILENAME = "DS/src/config.xml";

    public static void main(String[] args) {
        System.out.println("  ____  ____                        _           _   \n" +
                " |  _ \\/ ___|       _ __  _ __ ___ (_) ___  ___| |_ \n" +
                " | | | \\___ \\ _____| '_ \\| '__/ _ \\| |/ _ \\/ __| __|\n" +
                " | |_| |___) |_____| |_) | | | (_) | |  __/ (__| |_ \n" +
                " |____/|____/      | .__/|_|  \\___/, |\\___|\\___|\\__|\n" +
                "                   |_|           |__/               ");
        System.out.println("Welcome to the most efficient distributed key storage, please connect to one of the peers below: ");
        Client client = new Client(FILENAME);

        do {
            menuSelectPeer(client);
        } while (client.getConnectionsSize() != 1);

        while (true) menu(client);
    }

    public static void menuSelectPeer(Client client) {
        List<Peer> peers = client.getAvailablePeers();
        if (peers.size() != 0) {
            printPeers(peers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt();
            sc.nextLine();
            client.connect(choice);
        }
        else {
            PrintHelper.printError("No peer available :(");
        }
    }

    private static void printPeers(List<Peer> peers) {
        for (Peer s: peers) {
            System.out.println(s.getId() + ") " + s.getHost() + ":" + s.getPort());
        }
    }

    private static void menu(Client client) {
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
                    menuSelectPeer(client);
            case 4 -> // write tuple
                    menuWrite(client);
            case 5 -> // read tuple
                    menuRead(client);
            case 8 -> { // exit
                System.out.println("Cya!");
                System.exit(0);
            }
            default -> System.out.println("WORK IN PROGRESS... (the feature has yet to be implemented)");
        }
    }

    private static void menuWrite(Client client) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        // read value
        System.out.print("Value: ");
        String value = sc.nextLine();

        client.write(key, value);
    }

    private static void menuRead(Client client) {
        Scanner sc = new Scanner (System.in);

        // read key
        System.out.print("Key: ");
        int key = sc.nextInt();
        sc.nextLine();

        client.read(key);
    }
}
