package api.messages;

import org.apache.tomcat.jni.Error;

import static api.Constants.CODE_ERROR;

/**
 * Stellen Nachrichten dar, die einen Fehler kennzeichnen sollen.
 */
public class ErrorMessage extends Message {
    public ErrorMessage(String message) {
        super(CODE_ERROR, message);
    }

    public ErrorMessage(String message, int errorCode) {
        super(errorCode, message);
    }
}
