package api.messages.matches;
import api.messages.Message;
import com.google.gson.annotations.Expose;
import race.Client;

import java.util.ArrayList;

import static api.Constants.CODE_GROUP;

/**
 * Beinhaltet Informationen ueber eine Gruppe und die Spiele, die diese Gruppe gespielt hat.
 * Wird verwendet, um diese Informationen an das Frontend zu senden.
 */
public class MultipleClientsMessage extends Message {
    @Expose
    private final ArrayList<Client> clients;

    public MultipleClientsMessage(ArrayList<Client> clients) {
        super(CODE_GROUP);
        this.clients = clients;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }
}
