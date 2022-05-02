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
}
