package util;

/**
 * Signalisiert, dass beim Erstellen eines Logs ein Fehler aufgetreten ist
 */
public class BuildMessageException extends Exception {

    public BuildMessageException(String errorMessage) {
        super(errorMessage);
    }
}
