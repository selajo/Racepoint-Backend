package race;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import distributor.observermessages.ObserverMessage;
import matchstarting.MatchSetup;
import matchstarting.RacepointProperties;
import util.FileSystemConstants;
import util.matches.RaceInfo;
import util.matches.MatchListInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beinhaltet Informationen zum geplanten Match.
 */
public class Match {
    @Expose
    private final int numPlayers;
    @Expose
    private final int numMaps;
    @Expose
    private final long matchID;
    @Expose
    private final transient ArrayList<Client> clients;
    @Expose
    private final transient List<RaceMap> raceMaps;
    @Expose
    private transient Map<Integer, Race> races;
    @Expose
    private final long startTime;
    @Expose
    private long endTime;
    @Expose
    private int numRaces;
    @Expose
    private int numGamesPerPlayer;
    @Expose
    private Integer completedRacesCount;
    @Expose
    private ArrayList<Race> completedRaces;
    @Expose
    private MatchPhase matchPhase;
    @Expose
    private final String serverArgs;
    @Expose
    private String status;

    @Expose
    private Calendar startTimestamp;

    @Expose
    private Calendar endTimestamp;

    @Expose
    private List<Statistics> allStatistics;

    @Expose
    private Map<Integer, Client> ranking;

    /**
     * Stellt die moeglichen Phasen eines Matches dar.
     */
    public enum MatchPhase {
        INITIALIZED,
        RUNNING,
        COMPLETED,
        ERROR,
        TIMEOUT,
        INITIALIZING
    }

    private boolean startedSaving = false;
    private transient boolean isActive;
    private transient long lastUpdate;
    private transient final MatchSetup matchSetup;

    private transient ReadWriteLock completedGamesLock;
    private transient final Lock completedGamesReadLock;
    private transient final Lock completedGamesWriteLock;

    private final transient Logger logger = Logger.getLogger("Match");
    //private transient final MatchSetup matchSetup;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Initialisiert das Match anhand des geplanten Match-Setups.
     * @param clients Teilnehmende Clients
     * @param gameMaps Zu spielende Spielfelder
     * @param matchSetup Geplantes Match-Setup
     */
    public Match(ArrayList<Client> clients, List<RaceMap> gameMaps, MatchSetup matchSetup) {
        completedGamesLock = new ReentrantReadWriteLock();
        completedGamesReadLock = completedGamesLock.readLock();
        completedGamesWriteLock = completedGamesLock.writeLock();

        updateStatus(MatchPhase.INITIALIZING);
        logger.log(Level.INFO, "Creating new match");
        matchID = System.currentTimeMillis();
        lastUpdate = matchID;
        startTime = matchID;
        this.completedRaces = new ArrayList<>();
        this.clients = clients;
        this.raceMaps = gameMaps;
        this.numPlayers = clients.size();
        this.numMaps = gameMaps.size();
        this.isActive = true;
        this.completedRacesCount = 0;
        this.matchSetup = matchSetup;
        this.serverArgs = RacepointProperties.prettyServerArgs();
        this.allStatistics = new ArrayList<>();
        FileSystemConstants.CURRENT_MATCH_DIR = Paths.get(FileSystemConstants.MATCH_HOME.getAbsolutePath() + "/" + matchID).toFile();
        if (!FileSystemConstants.CURRENT_MATCH_DIR.mkdir()) {
            logger.log(Level.WARNING, "Could not create directory for new match!");
        }
        FileSystemConstants.LOG_FILE = Paths.get(FileSystemConstants.LOG_DIR.getAbsolutePath() + "/" + matchID + ".tar.gz").toFile();
        File gameDir = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsoluteFile() + "/games/").toFile();
        if (!gameDir.mkdir()) {
            logger.log(Level.WARNING, "Could not create game directory for new match!");
        }
        sortMaps();
        logger.log(Level.INFO, "Sorted provided maps");
        printMapsInOrder();
        updateStatus(MatchPhase.INITIALIZED);
    }

    /**
     * Gibt alle Maps in der Reihenfolge aus, in der diese gespielt werden.
     */
    private void printMapsInOrder() {
        logger.log(Level.INFO, "The maps will be played in the following order:");
        for (RaceMap raceMap : raceMaps) {
            logger.log(Level.INFO, raceMap.getMapName());
        }
    }

    public long getMatchID() {
        return matchID;
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Sortiert alle Maps anhand der Laenge und Breite.
     */
    private void sortMaps() {
        raceMaps.sort(Comparator.comparing(RaceMap::getMapName));
        raceMaps.sort((first, second) -> Integer.compare(second.getLength() * second.getHeight(), first.getLength() * first.getHeight()));
    }

    /**
     * Setzt den ERROR-Status
     */
    public void setError() {
        this.matchPhase = MatchPhase.ERROR;
    }

    /**
     * Sezte den TIMEOUT-Status
     */
    public void setTimeOut() {
        this.matchPhase = MatchPhase.TIMEOUT;
    }

    /**
     * Prueft, ob der Timeout des Matches erreicht wurde und beendet ggf. das Match.
     * @return True: Timeout erreicht. False: Sonst
     */
    public boolean checkForTimeOut() {
        long currentTime = System.currentTimeMillis();
        long INTERVAL_60_MIN = 1000 * 60 * 60;
        if (currentTime > lastUpdate + INTERVAL_60_MIN) {
            logger.log(Level.WARNING, "Setting time out -> end match");
            setTimeOut();
            endMatch();
            return true;
        }
        return false;
    }

    /**
     * Liefert die Anzahl an abgeschlossenen Races des Matches.
     * @return Anzahl an abgeschlossenen Races des Matches.
     */
    public int getCompletedRacesCount() {
        try {
            completedGamesReadLock.lock();
            return completedRacesCount;
        } finally {
            completedGamesReadLock.unlock();
        }
    }

    /**
     * Fuegt dem angegebenen Race einen Timeout hinzu.
     * @param gameID Race-ID
     */
    public void addTimeoutToGame(int gameID) {
        races.get(gameID).addTimeout();
        completedRaces.add(races.get(gameID));
    }

    /**
     * Fuegt das angegebene Race als abgeschlossen hinzu, falls das Race ordnungsgemaess beendet wurde.
     * Ordnungsgemaess: Statistiken und Replay-Dateien wurden vom Server erstellt.
     * @param gameID Race-ID
     * @return True: Race ordnungsgemaess beendet. False: sonst
     */
    public boolean addCompletedGame(int gameID) {
        boolean retVal = false;
        try {
            completedGamesWriteLock.lock();
            logger.log(Level.INFO, "Adding completed game " + gameID);
            retVal = addFinishedGame(gameID);
            if(retVal) {
                completedRacesCount++;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred while retrieving and saving statistics. " + e.getLocalizedMessage());
        } finally {
            completedGamesWriteLock.unlock();
            return retVal;
        }
    }

    /**
     * Fuegt das angegebene Race als abgeschlossen hinzu, falls das Race ordnungsgemaess beendet wurde.
     * Aktualisiert die Punktestaende der Clients.
     * @param gameID Race-ID
     * @return False: Statistiken wurden nicht erstellt. True: sonst
     * @throws FileNotFoundException
     */
    boolean addFinishedGame(int gameID) throws FileNotFoundException {
        String file = FileSystemConstants.getCompleteFileName("./", RacepointProperties.STATISTICFILE_PARTNAME);
        logger.log(Level.INFO, "Statistics " + file);
        Statistics statistics = Statistics.readStatistics(file);
        if (statistics == null) {
            return false;
        }
        logger.log(Level.INFO, statistics.toString());
        FileSystemConstants.deleteFile(file);

        races.get(gameID).addStatistics(statistics);

        addFinishedGamePerClient(gameID);

        updateClientPoints(statistics);
        updateRanks(gameID);
        allStatistics.add(statistics);
        completedRaces.add(races.get(gameID));
        return true;
    }

    /**
     * Fuegt bei jedem Client, der an dem angegebenem Race teilgenommen hat, ein abgeschlossenes Rennen hinzu.
     * @param gameID Race-ID
     */
    void addFinishedGamePerClient(int gameID) {
        Race race = races.get(gameID);
        for(Player player : race.getPlayers()) {
            for(Client client : this.getClients()) {
                if(player.getClientID() == client.getClientID()) {
                    client.addFinishedGame();
                }
            }
        }
    }

    /**
     * Aktualisiert die Punktestaende aller Clients, die an dem angegebenem Race teilgenommen haben.
     * @param gameID Race-ID
     */
    public void updateRanks(int gameID) {
        List<Client> copy = new ArrayList<>(clients);
        Collections.copy(copy, clients);
        Collections.sort(copy, Comparator.comparingInt(Client::getPoints));
        Collections.reverse(copy);

        int ranking_size = 1;
        ranking = new HashMap<>();
        for (Client client : copy) {
            ranking.put(ranking_size, client);
            ranking_size++;
        }

        if(gameID + 1 < races.size()) {
            for (Player player : races.get(gameID + 1).getPlayers()) {
                for (Client client : clients) {
                    if (player.getClientID() == client.getClientID()) {
                        player.update(client.getLastRank());
                    }
                }
            }
        }
    }

    /**
     * Aktualisiert die Raenge der Clients anhand der angegebenen Statistiken
     * @param statistics Statistiken eines Races.
     */
    public void updateClientPoints(Statistics statistics) {
        List<Player> players = races.get(statistics.gameID).getPlayers();
        for (int i = 0; i < players.size(); i++) {
            for (Client client : clients) {
                if (client.getClientID() == players.get(i).getClientID()) {
                    players.get(i).getClient().updateRank(statistics.Spielerdaten.get(i).Endplatzierung);
                    logger.log(Level.INFO, "" + client.getClientID() + " mit Platzierung: " + statistics.Spielerdaten.get(i).Endplatzierung);
                }
            }
        }
    }

    /**
     * Beendet das Match. Alle Informationen des Matches und der Races werden auf dem System gespeichert.
     */
    public void endMatch() {

        this.endTime = System.currentTimeMillis();
        logger.log(Level.INFO, "Match ended");
        this.matchSetup.stopOnMatchStartScript();

        logger.log(Level.INFO, "Saving game info to file system");

        //Client-Statistiken speichern
        logger.log(Level.INFO, "Saving client statistics");
        saveClientStatistics();

        //Maps speichern
        logger.log(Level.INFO, "Saving maps");
        saveMaps();

        //Game-Info speichern
        logger.log(Level.INFO, "Saving game info");
        saveGameInfo();

        //Logs speichern
        logger.log(Level.INFO, "Saving game logs");
        saveLogs();

        if (this.matchPhase == MatchPhase.RUNNING) {
            this.matchPhase = MatchPhase.COMPLETED;
        }

        //Match-Info speichern
        logger.log(Level.INFO, "Saving general match info");
        saveMatchInfo();

        //Beende Spiel, die noch nicht beendet sind
        for (Race game : races.values()) {
            game.finishAndSave();
        }

        //Logs und sonstige Infos zippen
        try {
            util.misc.Zipper.combineZips();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed zipping data. There won't be a downloadable ZIP file: " + e.getMessage());
        }

        this.matchPhase = MatchPhase.COMPLETED;
        MatchListInfo.addMatchInfo(this);
        isActive = false;
        updateStatus(MatchPhase.COMPLETED);
    }

    /**
     * Speichert alle Statistiken, die die Clients erzielt haben, auf dem System
     */
    public void saveClientStatistics() {
        File clientDir = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsolutePath() + "/clients/").toFile();
        if (clientDir.mkdir() || clientDir.exists()) {
            for (Client client : clients) {
                String json = gson.toJson(client);
                try {
                    Files.writeString(Paths.get(clientDir.getAbsolutePath() + "/" + client.getClientID() + ".json"), json);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save client JSON for client " + client.getClientID() + ", " + client.getShowName() + ": " + e.getLocalizedMessage(), e);
                }
            }
        } else {
            logger.log(Level.WARNING, "Failed to create directory for client statistics! " + clientDir);
        }
    }

    /**
     * Speichert alle gespielten Maps (im JSON-Format) auf dem System ab.
     */
    public void saveMaps() {
        File mapDir = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsoluteFile() + "/maps/").toFile();
        if (mapDir.mkdir() || mapDir.exists()) {
            for (RaceMap gameMap : raceMaps) {
                String json = gson.toJson(gameMap);
                try {
                    Files.writeString(Paths.get(mapDir.getAbsolutePath() + "/" + gameMap.getID() + ".json"), json);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save client JSON for map " + gameMap.getID() + ": " + e.getLocalizedMessage(), e);
                }
                // copy actual map config
                try {
                    Files.write(Paths.get(mapDir.getAbsolutePath() + "/" + gameMap.getID()), Files.readAllBytes(gameMap.toPath()));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save map " + gameMap.getID() + ": " + e.getLocalizedMessage(), e);
                }
                // copy actual map
                try {
                    if (!Paths.get(mapDir.getAbsolutePath() + "/" + gameMap.getMapTxtName()).toFile().exists()) {
                        Files.copy(Paths.get(FileSystemConstants.MAP_DIR + "/" + gameMap.getMapTxtName()),
                                Paths.get(mapDir.getAbsolutePath() + "/" + gameMap.getMapTxtName()));
                    }
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save map txt file " + gameMap.getMapTxtName());
                }
            }
        } else {
            logger.log(Level.WARNING, "Failed to create directory for game maps! " + mapDir);
        }
    }

    /**
     * Verschiebt alle Logs der Spiele in den persistent-Ordner.
     */
    public void saveLogs() {
        File mapDir = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsoluteFile() + "/race/").toFile();
        if (mapDir.mkdir() || mapDir.exists()) {
            File[] logs = Paths.get(FileSystemConstants.LOG_HOME).toFile().listFiles();
            for (File log : logs) {
                try {
                    File LOGHOME = Paths.get(FileSystemConstants.MATCH_HOME + "/" + matchID + "/logs/").toFile();
                    if(!LOGHOME.exists()) {
                        LOGHOME.mkdir();
                    }
                    Files.move(Paths.get(FileSystemConstants.LOG_HOME + "/" + log.getName()),
                            Paths.get(LOGHOME.getAbsolutePath() + "/" + log.getName()));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save log " + log.getName());
                }
            }
        }
    }

    /**
     * Speichert alle Informationen zu den Races.
     */
    public void saveGameInfo() {
        File gameInfo = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsolutePath() + "/gameInfo.json").toFile();
        try {
            Files.writeString(gameInfo.toPath(), gson.toJson(new RaceInfo(this, -1, false)));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save game info: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Speichert alle Informationen ueber das aktuellen Match.
     */
    public void saveMatchInfo() {
        File matchInfo = Paths.get(FileSystemConstants.CURRENT_MATCH_DIR.getAbsolutePath() + "/matchInfo.json").toFile();
        try {
            Files.writeString(matchInfo.toPath(), gson.toJson(this));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save general match info: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Generiert alle Rennen. Das Match wird einmalig gespielt.
     */
    public void generateGamesNormal() {
        races = new HashMap<>();
        numGamesPerPlayer = 0;

        for (RaceMap gameMap : raceMaps) {
            //zu wenig Spieler fuer die Map
            if (gameMap.getNumPlayers() > clients.size()) {
                continue;
            }
            numGamesPerPlayer += gameMap.getNumPlayers();

            //Zufaellig eine neue Reihenfolge fuer jede GameMap festlegen
            ArrayList<Client> shufflePlayers = new ArrayList<>(clients);
            Collections.shuffle(shufflePlayers);

            //Spiele durch Rotieren der Spieler erzeugem
            for (int c = 0; c < clients.size(); c++) {
                ArrayList<Client> rotationClients = new ArrayList<>(gameMap.getNumPlayers());

                for (int play = 1; play < gameMap.getNumPlayers(); play++) {
                    rotationClients.add(shufflePlayers.get((c + play) % shufflePlayers.size()));
                }
                Collections.shuffle(rotationClients);

                rotationClients.add(0, shufflePlayers.get(c));
                ArrayList<Player> players = new ArrayList<>();
                for (int i = 0; i < rotationClients.size(); i++) {
                    logger.log(Level.INFO, "Player " + rotationClients.get(i).getShowName() + " joins game " + races.size() + " on map " + gameMap.getMapName());
                    players.add(new Player(i, rotationClients.get(i), true));
                }

                races.put(races.size(), new Race(gameMap, players, races.size(), serverArgs));
            }
        }
        numRaces = races.size();
    }

    /**
     * Generiert alle Rennen des Matches im Turnier-Modus.
     */
    public void generateGamesTournament() {
        races = new HashMap<>();
        numGamesPerPlayer = 0;

        //Spiele fuer alle Maps generieren
        for (RaceMap gameMap : raceMaps) {
            List<Race> gamesPerGameMap = new ArrayList<>();

            //Weniger Spieler als Anzahl an Spielfeld-Spieler
            if (clients.size() < gameMap.getNumPlayers()) {
                continue;
            }

            List<Client> opponents = new ArrayList<>(clients);
            Collections.shuffle(opponents);
            if (opponents.size() > 7) {
                opponents = opponents.subList(0, 7);
            }
            int numberOpponents = opponents.size();

            //Erzeugt die Spiele pro Spieler, die teilnehmen
            for (Client client : clients) {
                // Jeder Spieler hat genau 8 Spiele

                for (int i = 0; i < 8; i++) {
                    ArrayList<Client> gameParticipants = new ArrayList<>();

                    for (int j = 0; j < gameMap.getNumPlayers() - 1; j++) {
                        gameParticipants.add(opponents.get((i + j) % numberOpponents));
                    }

                    int indexOfClientToScore = i % gameMap.getNumPlayers();
                    gameParticipants.add(indexOfClientToScore, client);

                    ArrayList<Player> players = new ArrayList<>();
                    for (int j = 0; j < gameParticipants.size(); j++) {
                        players.add(new Player(j, gameParticipants.get(j), j == indexOfClientToScore)); // only one scoring player
                    }

                    gamesPerGameMap.add(new Race(gameMap, players, races.size() + gamesPerGameMap.size(), serverArgs));
                }
            }
            Collections.shuffle(gamesPerGameMap);
            for (Race game : gamesPerGameMap) {
                races.put(races.size(), new Race(game.getRaceMap(), game.getPlayers(), races.size(), serverArgs));
            }
        }
        numRaces = races.size();
    }

    public List<RaceMap> getRaceMaps() {
        return raceMaps;
    }

    public Map<Integer, Race> getRaces() {
        return races;
    }

    /**
     * Aktualisiert den Status des Matches.
     * @param phase Zu aktualisierende Match-Phase.
     */
    public void updateStatus(MatchPhase phase) {
        this.status = String.valueOf(phase);
        switch (phase) {
            case RUNNING:
                this.startTimestamp = Calendar.getInstance();
                this.matchPhase = MatchPhase.RUNNING;
                break;
            case COMPLETED:
                this.endTimestamp = Calendar.getInstance();
                this.matchPhase = MatchPhase.COMPLETED;
                break;
            default:
                this.matchPhase = phase;
                break;
        }
    }

    public synchronized void apply(ObserverMessage message) {
        lastUpdate = System.currentTimeMillis();
        if (matchPhase == MatchPhase.INITIALIZING) {
            matchPhase = MatchPhase.RUNNING;
        }
        // TODO: Update Ranks mit Message-Data
    }
    public ArrayList<Race> getCompletedRaces() {
        return completedRaces;
    }

    public MatchPhase getMatchPhase() {
        return matchPhase;
    }
}
