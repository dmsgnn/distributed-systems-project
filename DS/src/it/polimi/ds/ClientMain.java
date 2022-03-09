package it.polimi.ds;

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
        List<SocketWrapper> connections = new ArrayList<>();
        List<Server> servers = getServers();
        Scanner sc = new Scanner (System.in);
        // TODO: this loop apparently doesn't work
        //do {
            printWelcome(servers);
            int choice = sc.nextInt() - 1;
            SocketWrapper s = new SocketWrapper(servers.get(choice));
            if (s.isConnected()) {
                connections.add(s);
            }
            // some send to test the exchange of requests and replies
        Timestamp instant;
        s.send(new ReadRequest((25), instant= Timestamp.from(Instant.now())));
            s.send(new WriteRequest(new Tuple(10, "testValue"), instant= Timestamp.from(Instant.now())));
        //} while (connections.size() != 1);
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
