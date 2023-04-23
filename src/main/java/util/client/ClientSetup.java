package util.client;

import race.Client;
import matchstarting.CLI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.HOME_DIR;

/**
 * Verwaltet die Clients. OtherClients: Binaries werden eingelesen.
 * GitClients: Werden geklont und gebuildet.
 */
public class ClientSetup {
    private static Logger logger = Logger.getLogger("ClientSetup");

    /**
     * Liefert die Namen der Clients, die für das Match deaktiviert sind.
     * @return Alle deaktivierten Clients.
     */
    private static List<String> getExcludedClients() {
        String excludedClientsFile = HOME_DIR + "/persistent/excludedClients.txt";
        Path path = Paths.get(excludedClientsFile);
        if(path.toFile().exists()) {
            try {
                List<String> clientNamesRaw = Files.readAllLines(path);
                List<String> clientNames = new LinkedList<>();
                for(String rawClientNames : clientNamesRaw) {
                    clientNames.add(rawClientNames.trim());
                }
                return clientNames;
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to read in excluded clients file: " + e.getLocalizedMessage(), e);
            }
        }
        return new LinkedList<>();
    }

    /**
     * Prueft, ob der Other-Client fuer das Spiel aktiviert ist.
     * @param otherClient Der zu ueberpruefende Other-Client.
     * @return False: Client ist nicht aktiviert. True: Client ist aktiviert.
     */
    public static boolean isEnabled(OtherClient otherClient) {
        String showName = otherClient.getShowName();
        for(String excludedClientName : getExcludedClients()) {
            if(showName.contentEquals(excludedClientName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prueft, ob der Git-Client fuer das Spiel aktiviert ist.
     * @param gitClient Der zu ueberpruefende Git-Client.
     * @return False: Client ist nicht aktiviert. True: Client ist aktiviert.
     */
    public static boolean isEnabled(GitClient gitClient) {
        String showName = gitClient.getShowName();
        for(String excludedClientName : getExcludedClients()) {
            if(showName.contentEquals(excludedClientName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Alle Clients werden fuer den Start vorbereitet. OtherClients werden eingelesen.
     * GitClients werden - jeweils als eigener Thread - geklont und gebuildet.
     * Alle teilnehmenden Clients, die erfolgreich gebuildet wurden, werden gespeichert.
     * @param binaries Informationen zu den Clients.
     * @return Alle erfolgreich eingelesenen Clients.
     */
    public static ArrayList<Client> setUpGameClients(ClientsAndServerFromJson binaries) {
        if (CLI.getGitClientJSON() == null && CLI.getOtherClientJSON() == null) {
            logger.log(Level.SEVERE, "Specify at least one from otherClient.json and gitClient.json !");
            System.exit(-1);
        }
        Binaries.clientBinaries = new ConcurrentHashMap<>();
        ArrayList<Client> clients = new ArrayList<>();

        //Other Clients
        if(CLI.getOtherClientJSON() != null) {
            try {
                OtherClient[] otherClients = binaries.readOtherClientJson();
                if(otherClients != null) {
                    for(OtherClient otherClient : otherClients) {
                        if(isEnabled(otherClient)) {
                            clients.add(new Client(otherClient));
                            Binaries.clientBinaries.put(otherClient.getClientID(), otherClient.getBytes());
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                logger.log(Level.SEVERE, "Could not find other client JSON: " + e.getLocalizedMessage(), e);
                logger.log(Level.SEVERE, "Exiting...");
                System.exit(-1);
            }
        }

        //Git Clients
        if(CLI.getGitClientJSON() != null) {
            logger.log(Level.INFO, "Starting to pull and compile gitClients");
            try {
                GitClient[] gitClients = binaries.readGitClientJson();
                if(gitClients != null) {
                    boolean startedPull = false;
                    for(GitClient gitClient : gitClients) {
                        if(isEnabled(gitClient)) {
                            gitClient.start();
                            startedPull = true;
                        }
                    }
                    if(startedPull) {
                        for(int i = 0; i < 240; i++) {
                            Thread.sleep(1000);
                            boolean done = true;
                            for(GitClient gitClient : gitClients) {
                                if(isEnabled(gitClient) && gitClient.isAlive()) {
                                    done = false;
                                    break;
                                }
                            }
                            if(done) {
                                break;
                            }
                        }
                        Thread.sleep(5000);
                        for(GitClient gitClient : gitClients) {
                            if(isEnabled(gitClient)) {
                                if(Binaries.clientBinaries.containsKey(gitClient.getClientID())) {
                                    clients.add(new Client(gitClient));
                                    logger.log(Level.INFO, "Added gitClient " + gitClient.getShowName());
                                } else {
                                    logger.log(Level.INFO, "Did not include gitClient " + gitClient.getShowName()
                                            + " either due to timeout or failing to pull or compile. See previous logs");
                                }
                            }
                        }
                        logger.log(Level.INFO, "Done with gitClients");
                    }
                }
            } catch (FileNotFoundException | InterruptedException e) {
                logger.log(Level.SEVERE, "Error while trying to read in / compile git clients: " + e.getLocalizedMessage(), e);
                logger.log(Level.SEVERE, "Exiting...");
                System.exit(-1);
            }
        }
        return clients;
    }




}
