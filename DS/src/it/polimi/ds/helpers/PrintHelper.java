package it.polimi.ds.helpers;

public class PrintHelper {

    /**
     * Prints an error message in red
     * @param msg
     */
    public static void printError(String msg) {
        System.out.println("\u001B[31m" + msg + "\u001B[0m");
    }
}
