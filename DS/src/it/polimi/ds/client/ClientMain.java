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
            int peerId = menuSelectPeer(client.getAvailablePeers());
            client.connect(peerId);
        } while (client.getConnectionsSize() != 1);

        while (true) {
            menuConnections(client);
            if (client.getConnectionsSize() == 0) {
                do {
                    System.out.println("Please connect to one of the peers below: ");
                    int peerId = menuSelectPeer(client.getAvailablePeers());
                    client.connect(peerId);
                } while (client.getConnectionsSize() != 1);
            }
        }
    }

    public static int menuSelectPeer(List<Peer> peers) {
        if (peers.size() != 0) {
            printPeers(peers);
            Scanner sc = new Scanner (System.in);
            int choice = sc.nextInt();
            sc.nextLine();
            return choice;
        }
        else {
            PrintHelper.printError("No peer available :(");
            return -1;
        }
    }

    private static void printPeers(List<Peer> peers) {
        for (Peer s: peers) {
            System.out.println(s.getId() + ") " + s.getHost() + ":" + s.getPort());
        }
    }

    private static void menuConnections(Client client) {
        System.out.println("Select one of the following operations:");
        String[] options = {
                "Add connection",       //1
                "Detach connection",    //2
                "Begin transaction",    //3
                "Exit",                 //4
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
            case 1 -> { // add connection
                int peerId = menuSelectPeer(client.getAvailablePeers());
                if (peerId != -1)
                    client.connect(peerId);
            }
            case 2 -> { // detach connection
                int peerId = menuSelectPeer(client.getPeersConnected());
                if (peerId != -1)
                    client.detach(peerId);
            }
            case 3 -> { // begin transaction
                client.begin();
                boolean isTerminated = false;
                int count = 0;
                do {
                    isTerminated = menuTransaction(client, count);
                    count++;
                } while (!isTerminated);
            }
            case 4 -> { // exit
                System.out.println("Cya!");
                System.exit(0);
            }
            default -> System.out.println("Invalid selection");
        }
    }

    /**
     *
     * @param client The instance of client that performs the transaction
     * @return True if the transaction is terminated, False otherwise
     */
    private static boolean menuTransaction(Client client, int count) {
        System.out.println("Select one of the following operations:");
        String[] options = {
                "Write",                //1
                "Read",                 //2
                "Commit transaction",   //3
                "Abort transaction",    //4
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
            case 1 -> // write tuple
                menuWrite(client);
            case 2 -> // read tuple
                menuRead(client);
            case 3 -> { // commit
                if (count != 0) {
                    client.commit();
                }
                else {
                    client.abort();
                }
                return true;
            }
            case 4 -> { // abort
                client.abort();
                return true;
            }
            default -> System.out.println("Invalid selection");
        }
        return false;
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
