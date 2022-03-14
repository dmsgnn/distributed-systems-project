package it.polimi.ds.messages;

import java.io.Serializable;

public class ConnectionRequest extends ClientRequest {
    private String ip;
    private int port;

    public ConnectionRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }
}
