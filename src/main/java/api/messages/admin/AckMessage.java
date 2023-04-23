package api.messages.admin;

import api.messages.Message;

import static api.Constants.CODE_ACK;

/**
 * Wird verwendet, um Ack-Nachrichten (akzeptierte Anfragen) zu schicken.
 */
public class AckMessage extends Message {
    public AckMessage() {
        super(CODE_ACK);
    }
}
