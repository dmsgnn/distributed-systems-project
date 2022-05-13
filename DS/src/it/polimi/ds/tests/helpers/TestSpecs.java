package it.polimi.ds.tests.helpers;

import java.util.ArrayList;
import java.util.List;

public class TestSpecs {
    List<DelayMessageDelivery> delays = new ArrayList<>();
    List<Integer> serverIDs = new ArrayList<>();

    public void addDelay(DelayMessageDelivery delay) {
        this.delays.add(delay);
    }

    public void addDelays(List<DelayMessageDelivery> delays) {
        this.delays.addAll(delays);
    }

    public void addServer(Integer id) {
        this.serverIDs.add(id);
    }

    public List<Integer> getServerIDs() {
        return serverIDs;
    }

    public List<DelayMessageDelivery> getDelays() {
        return delays;
    }
}
