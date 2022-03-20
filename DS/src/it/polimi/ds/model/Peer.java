package it.polimi.ds.model;

import java.io.Serializable;

public class Peer implements Serializable {
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

    public Peer(String h, int p, int id) {
        this.host = h;
        this.port = p;
        this.id = id;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
            return true;

        if (obj == null || !(obj instanceof Peer))
            return false;

        Peer otherPeer = (Peer) obj;

        if (! otherPeer.getHost().equals(this.getHost()))
            return false;
        if (otherPeer.getPort() != this.getPort())
            return false;

        return true;
    }
}
