package api.controller;

import api.messages.ErrorMessage;
import api.messages.Message;
import api.messages.matches.MultipleClientsMessage;
import api.messages.matches.ClientMessage;
import race.Client;
import matchstarting.CLI;
import matchstarting.ControlManager;
import org.springframework.web.bind.annotation.*;
import util.client.ClientsAndServerFromJson;
import util.client.GitClient;
import util.client.OtherClient;

import java.util.ArrayList;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Verwaltet Anfragen, die mit Clients zu tun haben.
 */
@RestController
public class ClientController {
    /**
     * Cache fuer die Clients, falls noch keine Matches gelaufen sind.
     */
    ArrayList<Client> cacheClients = new ArrayList<>();

    /**
     * Versendet Daten ueber einen Client via ID.
     * @param id Client-ID
     * @return Daten zu den Clients.
     * @throws Exception Falls ein Client nicht eingelesen wurde
     */
    @RequestMapping(value = "/client/{id}", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public Message getClient(@PathVariable long id) throws Exception {
        ControlManager controlManager = ControlManager.getInstance();
        // Existiert ein Match mit eingelesenen Clients?
        if(controlManager.getCurrentMatch() != null) {
            Client client = controlManager.getClientWithID((int) id);
            if (client != null) {
                return new ClientMessage(client);
            }
        } else {
            // Clients versuchen von Konfig einzulesen
            if (cacheClients.size() == 0) {
                ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(CLI.getGitClientJSON(), CLI.getOtherClientJSON(), CLI.getServerJSON());
                GitClient[] gitClients = clientsAndServerFromJson.readGitClientJson();
                OtherClient[] otherClients = clientsAndServerFromJson.readOtherClientJson();
                fillCacheClients(gitClients, otherClients);
            }
            Client client = getClientFromCacheWithID((int) id);
            if (client != null) {
                return new ClientMessage(client);
            }
            //Client trotzdem nicht gefunden
        }
        return new ErrorMessage("No such client with ID " + id + " found");
    }

    /**
     * Versendet Informationen zu allen Clients.
     * @return Informationen zu alllen Clients.
     * @throws Exception Falls ein Client nicht eingelesen wurde
     */
    @RequestMapping(value = "/client/all", produces = APPLICATION_JSON_VALUE)
    public Message getClients() throws Exception {
        return new MultipleClientsMessage(new ClientsAndServerFromJson(CLI.getGitClientJSON(), CLI.getOtherClientJSON(),
                CLI.getServerJSON()).readAllClients());
    }

    /**
     * Fuellt den Client-Cache mit allen Clients, die in den Konfigs vorhanden sind.
     * @param gitClients Alle Git-Clients
     * @param otherClients Alle Other-Clients
     */
    void fillCacheClients(GitClient[] gitClients, OtherClient[] otherClients) {
        for (GitClient gitClient : gitClients) {
            cacheClients.add(new Client(gitClient));
        }
        for (OtherClient otherClient : otherClients) {
            cacheClients.add(new Client(otherClient));
        }
    }

    /**
     * Holt die Clients-IDs zu den Clients, die sich im Cache befinden.
     * @param id Zu holende Client-ID
     * @return Zugehoeriger Client oder null
     */
    Client getClientFromCacheWithID(int id) {
        for (Client client : cacheClients) {
            if (client.getClientID() == id) {
                return client;
            }
        }
        return null;
    }
}

