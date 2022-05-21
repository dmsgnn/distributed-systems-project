package it.polimi.ds.helpers;

import it.polimi.ds.enums.ErrorCode;
import it.polimi.ds.messages.ErrorMessage;

public class PrintHelper {

    /**
     * Prints an error message in red
     * @param msg
     */
    public static void printError(String msg) {
        System.out.println("\u001B[31m" + msg + "\u001B[0m");
    }

    public static void printError(ErrorMessage errorMessage, String source) {
        ErrorCode code = errorMessage.getCode();
        String msg = "";
        switch (code) {
            case INVALID_KEY:
                msg = "[" + source + "] Unable to retrieve key " + errorMessage.getArgument();
                break;
            default:
                msg = "Unknown error";
                break;
        }
        printError(msg);
    }

    public static void printConfirm(String msg) { System.out.println("\u001B[32m" + msg + "\u001B[0m");}
}
