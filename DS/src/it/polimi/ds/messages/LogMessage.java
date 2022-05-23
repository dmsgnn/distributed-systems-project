package it.polimi.ds.messages;

public class LogMessage extends Message {
    private boolean isOutwards = false;

    private Message message;

    public LogMessage (Message m, boolean isOutwards) {
        this.message = m;
        this.isOutwards = isOutwards;
    }
    @Override
    public String toString() {
        String res;
        if(isOutwards) {
            res = "> ";
        }
        else {
            res = "< ";
        }
        return res + message.toString();
    }
}
