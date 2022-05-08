package it.polimi.ds.messages;

public class OutcomeMessage extends Message{

    private boolean outcome;

    public OutcomeMessage(boolean outcome) {
        this.outcome = outcome;
    }

    public boolean isOutcome() {
        return outcome;
    }
}
