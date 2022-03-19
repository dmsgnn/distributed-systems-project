package it.polimi.ds.model;

import java.io.Serializable;

public class Server implements Serializable {
    private String host;
    private int port;
    private int id;

    public int getId() {
        return id;
    }

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

    public Server(String h, int p, int id) {
        this.host = h;
        this.port = p;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof Server))
            return false;

        Server otherServer = (Server) obj;

        if (! otherServer.getHost().equals(this.getHost()))
            return false;
        if (otherServer.getPort() != this.getPort())
            return false;

        return true;
    }
}
