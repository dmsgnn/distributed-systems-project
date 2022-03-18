package it.polimi.ds.helpers;

import it.polimi.ds.model.Server;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class that provides primitives to interact with the config.xml configuration file.
 */
public class ConfigHelper {
    private Document doc;

    /**
     * Create a ConfigHelper object to interact with the config.xml configuration file.
     * @param filename the path to the config.xml file.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public ConfigHelper(String filename) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        doc = db.parse(new File(filename));
    }

    /**
     * @return The list of server saved in the config.xml file.
     */
    public List<Server> getServerList() {
        ArrayList<Server> res = new ArrayList<>();
        NodeList sList = doc.getElementsByTagName("server");
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
        return res;
    }

    /**
     * @return The R parameter, i.e. a configuration parameter that defines how many servers own a copy of the data.
     */
    public int getParamR() {
        int res = Integer.parseInt(doc.getElementsByTagName("R").item(0).getTextContent());
        return res;
    }
}
