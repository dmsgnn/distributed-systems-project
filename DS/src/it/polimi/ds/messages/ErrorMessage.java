package it.polimi.ds.messages;

import it.polimi.ds.enums.ErrorCode;

public class ErrorMessage extends Message {
    private ErrorCode code;

    private Object arg;

    public ErrorMessage(ErrorCode code, Object arg) {
        this.code = code;
        this.arg = arg;
    }

    public ErrorCode getCode() {
        return code;
    }

    public Object getArgument() {
        return arg;
    }

    @Override
    public String toString() {
        return "(" + this.getClass() +") " + "code=" + this.code + ", arg=`" + arg + "`";
    }
}
