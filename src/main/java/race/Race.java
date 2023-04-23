package race;

import com.google.gson.annotations.Expose;
import matchstarting.ControlManager;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.CURRENT_MATCH_DIR;

/**
 * Beinhaltet alls notwendigen Informationen, die ein Spiel besitzt.
 */
public class Race {
    private final transient RaceMap raceMap;
    @Expose
    private final int raceMapID;
    private final String mapName;
    @Expose
    private final ArrayList<Player> players;
    @Expose
    private final int raceID;
    private final transient Logger logger;
    @Expose
    private final int mapLength;
    @Expose
    private final int mapWidth;
    @Expose
    private final String serverArgs;
    @Expose
    private RacePhase racePhase;

    @Expose
    private Statistics statistics;

    //private transient GameState lastGameState;

    private transient boolean saved = false;
    private transient Boolean startedSaving = false;
    private final transient Lock startedSavingLock = new ReentrantLock();
    private transient File gameDir;

    /**
     * Stellt alle Phasen, die ein Race haben kann, dar.
     */
    public enum RacePhase {
        INIT,
        COMPLETED,
        ERROR,
        TIMEOUT
    }

    public Race(RaceMap gameMap, ArrayList<Player> players, int gameID, String serverArgs) {
        this.raceMap = gameMap;
        this.raceMapID = gameMap.getID();
        this.players = players;
        this.raceID = gameID;
        this.mapName = gameMap.getName();
        this.mapLength = gameMap.getLength();
        this.mapWidth = gameMap.getHeight();
        this.serverArgs = serverArgs;
        this.racePhase = RacePhase.INIT;
        logger = Logger.getLogger("Game " + gameID);
    }

    /**
     * Race wird als TIMEOUT markiert.
     */
    public void addTimeout() {
        this.racePhase = RacePhase.TIMEOUT;
    }

    public RaceMap getRaceMap() {
        return raceMap;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    /**
     * Setzt das Rennen auf den Ausgangszustand zurueck.
     */
    public void resetGame() {
        logger.log(Level.INFO, "Resetting game " + raceID);
        this.racePhase = RacePhase.INIT;

    }

    public RacePhase getRacePhase() {
        return racePhase;
    }

    public int getRaceID() {
        return raceID;
    }

    /**
     * Beendet das aktuelle Rennen und speichert die erzielten Informationen ab.
     */
    public void finishAndSave() {
        try {
            this.racePhase = RacePhase.COMPLETED;
            startedSavingLock.lock();
            if(startedSaving) {
                return;
            } else {
                startedSaving = true;
            }
        } finally {
            startedSavingLock.unlock();
        }
        synchronized (this) {
            if(!saved) {
                logger.log(Level.INFO, "Saving game " + raceID + " was requested");
                /*
                for(Player player : players) {
                    if(player.getScoring()) {
                        player.getClient().addFinishedGame();
                    }
                }
                */

                logger.log(Level.INFO, "Added finished game to the participating players of game " + raceID);
                gameDir = Paths.get(CURRENT_MATCH_DIR + "/games/" + raceID).toFile();

                /*
                String json = gameGsonBuilder().create().toJson(this);
                try {
                    File generalJson = Paths.get(gameDir.getAbsolutePath() + "/general.json").toFile();
                    File generalJsonZip = Paths.get(gameDir.getAbsolutePath() + "/general.json.7z").toFile();
                    Files.writeString(generalJson.toPath(), json);
                    Zipper.zipAndDelete(generalJson, generalJsonZip);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to save JSON for game " + gameID + ": " + e.getLocalizedMessage(), e);
                }

                 */
                //ControlManager.getInstance().getCurrentMatch().addCompletedGame(this.gameID);
            }
        }
    }

    /**
     * Beendet das Rennen, falls ein Error aufgetreten ist.
     */
    public void onError() {
        racePhase = RacePhase.ERROR;
        ControlManager.getInstance().getCurrentMatch().setError();
        finishAndSave();
    }

    /**
     * Liefert alle Client-IDs, die am Rennen teilnehmen.
     * @return Alle Client-IDs, die am Rennen teilnehmen.
     */
    public int[] getClientIDs() {
        int[] clientIDs = new int[players.size()];
        for(int i = 0; i < clientIDs.length; i++) {
            clientIDs[i] = players.get(i).getClient().getClientID();
        }
        return clientIDs;
    }

    /**
     * Fuegt dem Race eine Statistik hinzu.
     * @param statistics Hinzuzufuegende Statistik.
     */
    public void addStatistics(Statistics statistics) {
        statistics.gameID = this.raceID;
        statistics.mapID = this.raceMapID;
        this.statistics = statistics;
        this.racePhase = RacePhase.COMPLETED;
    }

}
