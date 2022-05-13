package it.polimi.ds.tests.helpers;

import it.polimi.ds.messages.ErrorMessage;
import it.polimi.ds.messages.Message;

public class DelayMessageDelivery {
    private Class<?> messageClass;
    private Boolean fromClient;

    public int delayMillis;

    public DelayMessageDelivery(Class messageClass, Boolean fromClient, int delayMillis) {
        this.messageClass = messageClass;
        this.fromClient = fromClient;
        this.delayMillis = delayMillis;
    }

    public Class<?> getMessageClass() {
        return messageClass;
    }

    public Boolean isFromClient() {
        return fromClient;
    }

    public int getDelayMillis() {
        return delayMillis;
    }
}
