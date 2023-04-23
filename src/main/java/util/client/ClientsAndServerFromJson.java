package util.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import race.Client;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

/**
 * Verwaltet das Einlesen und Konvertieren der Konfigurationsdateien der Clients und des Servers.
 */
public class ClientsAndServerFromJson {
    private final String gitClientJSON;
    private final String otherClientJSON;
    private final String serverJSON;

    private final static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Erzeugt ClientsAndServerFromJson-Instanz.
     * @param gitClientJSON Pfad zur Git-Client-Konfig.
     * @param otherClientJSON Pfad zu Other-Client-Binary.
     * @param serverJSON Pfad zu Server-Binary.
     */
    public ClientsAndServerFromJson(String gitClientJSON, String otherClientJSON, String serverJSON) {
        this.gitClientJSON = gitClientJSON;
        this.otherClientJSON = otherClientJSON;
        this.serverJSON = serverJSON;
    }

    /**
     * Liest die Konfigurationsdatei eines GitClients als JSON ein.
     * @return Array des GitClients.
     * @throws FileNotFoundException Falls die Datei nicht existiert.
     */
    public GitClient[] readGitClientJson() throws FileNotFoundException {
        if(gitClientJSON != null) {
            Reader reader = new FileReader(gitClientJSON);
            return gson.fromJson(reader, GitClient[].class);
        }
        return null;
    }

    /**
     * Liest die Konfigurationsdatei eines OtherClients als JSON ein.
     * @return Array des OtherClients.
     * @throws FileNotFoundException Falls die Datei nicht existiert.
     */
    public OtherClient[] readOtherClientJson() throws FileNotFoundException {
        if(otherClientJSON != null) {
            Reader reader = new FileReader(otherClientJSON);
            return gson.fromJson(reader, OtherClient[].class);
        }
        return null;
    }

    /**
     * Liest die Konfigurationsdatei des Servers als JSON ein.
     * @return Array des Servers.
     * @throws FileNotFoundException Falls die Datei nicht existiert.
     */
    public GameServer readServerJson() throws FileNotFoundException {
        Reader reader = new FileReader(serverJSON);
        return gson.fromJson(reader, GameServer.class);
    }

    /**
     * Liest alle Other-Clients ein und konvertiert diese zu Clients.
     * @return Alle Other-Clients als Clients.
     * @throws FileNotFoundException Datei nicht gefunden
     */
    public ArrayList<Client> readAllOtherClients() throws FileNotFoundException {
        OtherClient[] otherClients = readOtherClientJson();
        ArrayList<Client> clients = new ArrayList<>();
        for(OtherClient otherClient : otherClients) {
            Client client = new Client(otherClient);
            clients.add(client);
        }
        return clients;
    }

    /**
     * Liest alle Git-Clients ein und konvertiert diese zu Clients.
     * @return Alle Git-Clients als Clients.
     * @throws FileNotFoundException Datei nicht gefunden
     */
    public ArrayList<Client> readAllGitClients() throws FileNotFoundException {
        GitClient[] otherClients = readGitClientJson();
        ArrayList<Client> clients = new ArrayList<>();
        for(GitClient otherClient : otherClients) {
            Client client = new Client(otherClient, false);
            clients.add(client);
        }
        return clients;
    }

    /**
     * Liest und konvertiert alle Other- und Git-Clients ein und konvertiert diese zu Clients.
     * @return Alle Other- und Git-Clients als Clients
     * @throws FileNotFoundException Datei nicht gefunden
     */
    public ArrayList<Client> readAllClients() throws FileNotFoundException {
        ArrayList<Client> clients = new ArrayList<>();
        clients.addAll(readAllGitClients());
        clients.addAll(readAllOtherClients());
        return clients;
    }
}
