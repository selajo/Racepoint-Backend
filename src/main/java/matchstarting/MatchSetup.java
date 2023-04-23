package matchstarting;

import race.Client;
import race.Race;
import race.RaceMap;
import race.Match;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import util.LogHandler;
import util.client.ClientSetup;
import util.client.ClientsAndServerFromJson;
import util.client.GameServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.LOG_DIR;
import static util.FileSystemConstants.TEMP_LOG_DIR;

/**
 * Plant und startet anschliessend das Match.
 */
public class MatchSetup extends TimerTask {

    private boolean forceStart = false;
    private Thread onMatchStartScript;
    private static final Logger logger = Logger.getLogger("matchstarting.MatchSetup");

    private List<String> allowedMaps = new ArrayList<>();
    private List<Integer> allowedClientIDs = new ArrayList<>();
    private List<String> allowedClientNames = new ArrayList<>();

    public MatchSetup() {
    }

    public MatchSetup(boolean forceStart) {
        this.forceStart = forceStart;
    }

    /**
     * Plant und startet anschliessend das Match
     */
    @Override
    public void run() {
        logger.log(Level.INFO, "Requested match setup");

        //Pruefe, ob Match schon stattfinden sollte
        Properties properties = RacepointProperties.readProperties();
        boolean forceIntervalStart = Boolean.parseBoolean(properties.getProperty(RacepointProperties.FORCE_INTERVAL_START, "false"));
        Match current = ControlManager.getInstance().getCurrentMatch();
        if (current != null && current.isActive()) {
            logger.log(Level.INFO, "Current match already running");
            if (forceIntervalStart || forceStart) {
                logger.log(Level.INFO, "Force end running match");
                MatchManager.getInstance().forceEndMatchManual();
            } else {
                logger.log(Level.INFO, "Checking for timeout");
                if (!MatchManager.getInstance().checkForTimeout()) { // no timeout and no forced intervals, do not start new match
                    logger.log(Level.INFO, "No timeout, continuing with current match");
                    return;
                }
                logger.log(Level.INFO, "Timeout on current match, creating new one");
                MatchManager.getInstance().forceEndMatchManual();
            }
        } else {
            logger.log(Level.INFO, "No match currently running, creating new one");
        }

        //Zu alte Logs loeschen
        int logFileLifespan = Integer.parseInt(properties.getProperty(RacepointProperties.LOGFILE_LIFESPAN, "14"));
        if (LogHandler.deleteLogFilesOlderThan(logFileLifespan)) {
            logger.log(Level.INFO, "Deleted older logs if there were any");
        } else {
            logger.log(Level.WARNING, "Failed to delete old logfiles");
        }

        //Spiel-Setup durchfuehren
        setUpDirs();
        String sharedSecret = readSharedSecret();
        ClientsAndServerFromJson binaries = new ClientsAndServerFromJson(CLI.getGitClientJSON(), CLI.getOtherClientJSON(), CLI.getServerJSON());
        GameServer gameServer = setUpGameServer(binaries, properties);
        ArrayList<Client> clients = ClientSetup.setUpGameClients(binaries);
        clients = excludeClients(MatchManager.getInstance().allowedClientIDs, MatchManager.getInstance().allowedClientNames, clients);
        List<RaceMap> gameMaps = RaceMapSetup.readGameMaps(false);
        gameMaps = excludeMaps(MatchManager.getInstance().allowedMaps, new ArrayList<RaceMap>(gameMaps));

        ControlManager controlManager = ControlManager.getInstance();
        boolean tournamentMode = Boolean.parseBoolean(properties.getProperty(RacepointProperties.TOURNAMENT_MODE, "false"));
        //Fuer Nodes
        /*
        if (CLI.getOnMatchStartScript() != null) {
            logger.log(Level.INFO, "Starting script for match startup");
            onMatchStartScript = new Thread(new OnMatchStartScript(CLI.getOnMatchStartScript()));
            onMatchStartScript.setDaemon(true);
            onMatchStartScript.start();
        }
         */
        controlManager.init(clients, gameServer, gameMaps, tournamentMode, this);
        controlManager.playAll();
    }

    /**
     * Schliesst bestimmte Clients aus.
     * @param allowedClientIDs Erlaubte Clients via ID
     * @param allowedClientNames Erlaubte Clients via Namen
     * @param clients Alle Clients, die vorhanden sind.
     * @return Alle Clients, die fuer das Match zugelassen sind.
     */
    static ArrayList<Client> excludeClients(ArrayList<Integer> allowedClientIDs, ArrayList<String> allowedClientNames, ArrayList<Client> clients) {
        ArrayList<Client> allAllowedClients = new ArrayList<>();
        for(Client client : clients) {
            if(allowedClientNames.contains(client.getShowName())) {
                allAllowedClients.add(client);
                logger.log(Level.INFO, "Added: " + client.getClientID() + " " + client.getShowName());
            }
        }
        return allAllowedClients;
    }

    /**
     * Schliesst bestimmte Spielfelder aus.
     * @param allowedMaps Erlaubte Spielfelder via Namen.
     * @param maps Alle Spielfelder, die vorhanden sind.
     * @return Alle Spielfelder, die fuer das Match zugelassen sind.
     */
    static ArrayList<RaceMap> excludeMaps(ArrayList<String> allowedMaps, ArrayList<RaceMap> maps) {
        ArrayList<RaceMap> allAllowedMaps = new ArrayList<>();
        for(RaceMap raceMap : maps) {
            if(allowedMaps.contains(raceMap.getMapName())) {
                allAllowedMaps.add(raceMap);
            }
        }
        return allAllowedMaps;
    }

    /**
     * Stellt alle Verzeichnisse her, die fuer das Match notwendig sind.
     */
    private static void setUpDirs() {
        if (CLI.getGitClientJSON() == null && CLI.getOtherClientJSON() == null) {
            logger.log(Level.SEVERE, "Specify at least one from gitClient / otherClient JSON!");
            logger.log(Level.SEVERE, "Exiting...");
            System.exit(-1);
        }

        //Setup Log-Home
        if (Paths.get(TEMP_LOG_DIR).toFile().exists()) {
            try {
                //Alte Daten loeschen
                FileUtils.deleteDirectory(Paths.get(TEMP_LOG_DIR).toFile());
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed while removing old log directory: " + e.getLocalizedMessage(), e);
            }
        }
        Paths.get(TEMP_LOG_DIR).toFile().mkdir();
        if (!LOG_DIR.exists()) {
            LOG_DIR.mkdir();
        }
    }

    /**
     * Stellt die Ordnerstruktur fuer den Server her.
     * @param binaries Binaries des Servers aus der Konfigurationsdatei.
     * @param properties Ob das Match im Endlosmodus gestartet werden soll
     * @return Informationen zum Server.
     */
    private GameServer setUpGameServer(ClientsAndServerFromJson binaries, Properties properties) {
        GameServer gameServer = null;
        try {
            gameServer = binaries.readServerJson();
            boolean tournamentMode = Boolean.parseBoolean(properties.getProperty(RacepointProperties.TOURNAMENT_MODE, "false"));
            //gameServer.setTournamentMode(tournamentMode);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Could not find server JSON: " + e.getLocalizedMessage(), e);
            logger.log(Level.SEVERE, "Exiting...");
            System.exit(-1);        }
        return gameServer;
    }

    /**
     * Bricht das aktuelle Match ab.
     */
    public void stopOnMatchStartScript() {
        if (onMatchStartScript != null) {
            logger.log(Level.INFO, "Stopping onMatchStartScript");
            onMatchStartScript.interrupt();
        }
    }

    /**
     * Liest den HMAC-Schluessel ein, der fuer die Nutzerpasswoerter verwendet wird.
     * Das Programm wird beendet, falls kein Schluessel gefunden bzw. angegeben wurde.
     * @return Den HMAC-Schluessel aus der Konfigurationsdatei.
     */
    private String readSharedSecret() {
        String sharedSecret = null;
        try {
            sharedSecret = Files.readAllLines(Paths.get(CLI.getSharedKey())).get(0);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read in shared secret: " + e.getLocalizedMessage(), e);
            logger.log(Level.SEVERE, "Exiting...");
            System.exit(-1);
        }
        return sharedSecret;
    }
}