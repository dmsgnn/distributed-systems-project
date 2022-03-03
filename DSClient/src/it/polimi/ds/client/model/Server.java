package it.polimi.ds.client.model;

public class Server {
    private String host;
    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Server(String h, int p) {
        this.host = h;
        this.port = p;
    }
}
