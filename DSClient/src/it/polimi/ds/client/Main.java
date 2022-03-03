package it.polimi.ds.client;

import it.polimi.ds.client.middleware.SocketWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import it.polimi.ds.client.model.Server;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;

public class Main {

    static String FILENAME = "src/servers.xml";

    public static void main(String[] args) {
        List<SocketWrapper> connections = new ArrayList<>();
        List<Server> servers = getServers();
        Scanner sc = new Scanner (System.in);
        do {
            printWelcome(servers);
            int choice = sc.nextInt() - 1;
            SocketWrapper s = new SocketWrapper(servers.get(choice));
            if (s.isConnected()) {
                connections.add(s);
            }
        } while (connections.size() != 1);
    }

    private static void printWelcome(List<Server> servers) {
        System.out.println("Welcome to the most efficient distributed key storage, please connect to one of the servers below: ");
        int i = 1;
        for (Server s: servers) {
            System.out.println(i + ") " + s.getHost() + ":" + s.getPort());
        }
    }

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
}
