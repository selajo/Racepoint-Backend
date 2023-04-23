package api.messages.matches;

import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.Client;

import static api.Constants.CODE_GROUP;

/**
 * Beinhaltet Informationen ueber eine Gruppe und die Spiele, die diese Gruppe gespielt hat.
 * Wird verwendet, um diese Informationen an das Frontend zu senden.
 */
public class ClientMessage extends Message {
    @Expose
    private final Client client;

    public ClientMessage(Client client) {
        super(CODE_GROUP);
        this.client = client;
    }

    public Client getClient() {
        return client;
    }
}
