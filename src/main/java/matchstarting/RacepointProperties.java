package matchstarting;

import race.RaceMap;
import util.FileSystemConstants;
import util.client.ClientSetup;
import util.client.ClientsAndServerFromJson;
import util.client.GitClient;
import util.client.OtherClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.HOME_DIR;

/**
 * Beinhaltet die wichtigen Einstellungen fuer RacePoint.
 */
public class RacepointProperties {
    public static final String MATCH_PERIOD_IN_MIN = "Match Period in Minutes";
    public static final String FORCE_INTERVAL_START = "Force Interval Start";
    public static final String CONTINUOUS_MODE = "Continuous Mode";
    public static final String TOURNAMENT_MODE = "Tournament Mode";
    public static final String ALL_V_ALL_MODE = "AllvAll Mode";
    public static final String SERVER_ARGUMENTS = "Server Arguments";
    public static final String LOGFILE_LIFESPAN = "Logfile Lifespan";
    public static final String STATISTICFILE_PARTNAME = "Spielerstatistik";
    public static List<RaceMap> ALLOWED_MAPS = new ArrayList<>();

    private static final Logger logger = Logger.getLogger("MatchpointProperties");

    /**
     * Speichert die angegebenen RacePoint-Properties in einer Datei.
     * @param properties Die Properties, die gesetzt werden sollen.
     */
    private static void storeProperties(Properties properties) {
        logger.log(Level.INFO, "Storing matchpoint properties:");
        logger.log(Level.INFO, properties.toString());
        File propertyFile = Paths.get(HOME_DIR + "/persistent/config.properties").toFile();
        try {
            if(!propertyFile.exists()) {
                propertyFile.createNewFile();
            }
            properties.store(new FileOutputStream(propertyFile), null);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save matchpoint property file: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Liest die Datei mit den Properties des RacePoints ein.
     * @return Die aktuelles Properties des Systems.
     */
    public static Properties readProperties() {
        logger.log(Level.INFO, "Reading matchpoint properties");
        File propertyFile = Paths.get(HOME_DIR + "/persistent/config.properties").toFile();
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read in property file: " + e.getLocalizedMessage(), e);
        }
        return prop;
    }

    /**
     * Liefert die Namen der Spielfelder, auf denen gespielt wird.
     * Es wird zusätzlich angegeben, ob das jeweilige Spielfeld deaktiviert ist oder nicht.
     * @return Sammlung an Spielfeldern mit Angabe, ob deaktiviert oder nicht.
     */
    public static Map<String, Boolean> getPlayedMapNames() {
        try {
            Map<String, Boolean> names = new HashMap<>();
            Map<File, Boolean> maps = RaceMapSetup.getPlayedMaps();
            for(File file : maps.keySet()) {
                names.put(file.getName(), maps.get(file));
            }
            return names;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to get played map names: " + e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Liefert die Namen aller Clients und ob diese aktiviert sind.
     * @return Name und Indikator für Aktivierung der Clients.
     */
    public static Map<String, Boolean> getActiveClientNames() {
        ClientsAndServerFromJson clients = new ClientsAndServerFromJson(CLI.getGitClientJSON(), CLI.getOtherClientJSON(), CLI.getServerJSON());
        Map<String, Boolean> activeClientNames = new HashMap<>();
        try {
            if(CLI.getOtherClientJSON() != null) {
                for(OtherClient otherClient : clients.readOtherClientJson()) {
                    activeClientNames.put(otherClient.getShowName(), ClientSetup.isEnabled(otherClient));
                }
            }
            if(CLI.getGitClientJSON() != null) {
                for(GitClient gitClient : clients.readGitClientJson()) {
                    activeClientNames.put(gitClient.getShowName(), ClientSetup.isEnabled(gitClient));
                }
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Failed to read in client jsons while trying to read active clients for admin panel: " + e.getLocalizedMessage(), e);
            return null;
        }
        return activeClientNames;
    }

    /**
     * Setzt die Racepoint-Properties mithilfe einer Backup-Datei zurueck.
     * Zusaetzlich werden alle ausgeschlossenen Spieler und Maps wieder aktiviert.
     * @return Ob die Properties zurueckgesetzt werden konnten.
     */
    public static boolean resetProperties() {
        Properties oldProperties = readProperties();
        boolean continuousOld = Boolean.parseBoolean(oldProperties.getProperty(RacepointProperties.CONTINUOUS_MODE, "false"));
        int matchPeriodOld = Integer.parseInt(oldProperties.getProperty(RacepointProperties.MATCH_PERIOD_IN_MIN));

        //Reset properties from backup
        Path backupProperties = Paths.get(HOME_DIR + "/config.properties.backup");
        if(!backupProperties.toFile().exists()) {
            return false;
        }
        Path properties = Paths.get(HOME_DIR + "/persistent/config.properties");
        try {
            if(!properties.toFile().delete()) {
                logger.log(Level.INFO, "Error while trying to remove old properties");
                return false;
            }
            Files.copy(backupProperties, properties);
        } catch (IOException e) {
            logger.log(Level.INFO, "Error while trying to copy backup properties: " + e.getLocalizedMessage(), e);
            return false;
        }

        //Reset scheduling timer, if needed
        if (timerResetNeeded(continuousOld, matchPeriodOld)) {
            //TODO: MatchManager.getInstance().setTimer();
        }

        //Reset excluded clients
        try {
            FileSystemConstants.deleteAndCreateNew(HOME_DIR + "persistent/excludedClients.txt");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed while resetting client enabled / disabled: " + e.getLocalizedMessage(), e);
            return false;
        }

        //Reset excluded maps
        try {
            FileSystemConstants.deleteAndCreateNew(HOME_DIR + "persistent/excludedMaps.txt");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed while resetting maps enabled / disabled: " + e.getLocalizedMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * Setzt die geplanten Timer zurueck, falls dieser geaendert wurde.
     * @param continuous Neuer Wert fuer den CONTINOUS_MODE
     * @param matchPeriod Neuer Wert fuer die Match-Dauer
     * @return Ob der Timer geaendert werden muss.
     */
    private static boolean timerResetNeeded(boolean continuous, int matchPeriod) {
        Properties properties = readProperties();
        if (matchPeriod == Long.parseLong(properties.getProperty(MATCH_PERIOD_IN_MIN))) {
            return continuous != Boolean.parseBoolean(properties.getProperty(CONTINUOUS_MODE));
        }
        return true;
    }


    public static void applySettings(int matchPeriod, int logfileLifespan, boolean forceIntervalStart, boolean continuousMode,
                                     boolean tournamentMode, boolean allvallMode, List<String> mapsDeactivated,
                                     List<String> clientsDeactivated, String serverArguments) {
        logger.log(Level.INFO, "Updating properties for racepoint");
        boolean timerResetNeeded = timerResetNeeded(continuousMode, matchPeriod);

        Properties properties = new Properties();
        properties.setProperty(CONTINUOUS_MODE, String.valueOf(continuousMode));
        properties.setProperty(ALL_V_ALL_MODE, String.valueOf(allvallMode));
        properties.setProperty(MATCH_PERIOD_IN_MIN, String.valueOf(matchPeriod));
        properties.setProperty(FORCE_INTERVAL_START, String.valueOf(forceIntervalStart));
        properties.setProperty(TOURNAMENT_MODE, String.valueOf(tournamentMode));
        properties.setProperty(SERVER_ARGUMENTS, serverArguments);
        properties.setProperty(LOGFILE_LIFESPAN, String.valueOf(logfileLifespan));
        storeProperties(properties);

        // Aenderungen im Timer anwenden
        if(timerResetNeeded) {
            //TODO: Set Timer
        }

        // Maps ausschliessen
        logger.log(Level.INFO, "Deactivated maps:");
        logger.log(Level.INFO, mapsDeactivated.toString());
        try {
            FileSystemConstants.deleteAndCreateNew(HOME_DIR + "/persistent/excludedMaps.txt");
            StringBuilder stringBuilder = new StringBuilder();
            for(String mapName : mapsDeactivated) {
                stringBuilder.append(mapName).append("\n");
            }
            Path excludedMaps = Paths.get(HOME_DIR + "/persistent/excludedMaps.txt");
            Files.writeString(excludedMaps, stringBuilder.toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed while applying maps enabled / disabled: " + e.getLocalizedMessage(), e);
        }

        // Clients ausschliessen
        logger.log(Level.INFO, "Deactivated clients:");
        logger.log(Level.INFO, clientsDeactivated.toString());
        try {
            FileSystemConstants.deleteAndCreateNew(HOME_DIR + "/persistent/excludedClients.txt");
            StringBuilder stringBuilder = new StringBuilder();
            for(String clientShowName : clientsDeactivated) {
                stringBuilder.append(clientShowName).append("\n");
            }
            Path excludedClients = Paths.get(HOME_DIR + "/persistent/excludedClients.txt");
            Files.writeString(excludedClients, stringBuilder.toString());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed while applying clients enabled / disabled: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Nutzbare Ports aus der open_ports.txt einlesen.
     * @return Eine Liste der verfuegbaren aus der open_ports.txt.
     */
    public static List<Integer> getOpenDataPorts() {
        try {
            String openPortsFile = HOME_DIR + "/open_ports.txt";
            String openPortsString = Files.readString(Paths.get(openPortsFile)).trim();
            String[] portsMinMax = openPortsString.split("-");
            if(portsMinMax.length == 2) {
                List<Integer> openPorts = new LinkedList<>();
                int minPort = Integer.parseInt(portsMinMax[0]);
                int maxPort = Integer.parseInt(portsMinMax[1]);
                for(int port = minPort; port <= maxPort; port++) {
                    openPorts.add(port);
                }
                return openPorts;
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read open ports file! Matchpoint does not know which ports" +
                    " are available for data connections of the nodes: " + e.getLocalizedMessage(), e);
        }
        logger.log(Level.SEVERE, "Could not read in which ports to use for the data connections!");
        logger.log(Level.SEVERE, "Ending program");
        System.exit(-1); // severe error
        return null;
    }

    public static String prettyServerArgs() {
        return "something";
    }

    /**
     * Schliesst ein Spielfeld via ID aus.
     * @param mapID Map-ID des Spielfeldes.
     */
    public static void excludeMapViaID(int mapID) {
        for (Iterator<RaceMap> iter = ALLOWED_MAPS.listIterator(); iter.hasNext(); ) {
            RaceMap a = iter.next();
            if (a.getID() == mapID) {
                iter.remove();
            }
        }
    }

    /**
     * Schliesst Spielfelder via IDs ein.
     * @param ids Map-IDs der Spielfelder.
     */
    public static void includeMapsViaIDs(List<Integer> ids) {
        List<RaceMap> result = new ArrayList<>();
        for(RaceMap map : RaceMapSetup.readGameMaps(true)) {
            if(ids.contains(map.getID())) {
                result.add(map);
            }
        }
        ALLOWED_MAPS = result;
    }
}
