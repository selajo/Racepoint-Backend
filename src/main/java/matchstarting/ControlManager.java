package matchstarting;

import race.*;
import util.FileSystemConstants;
import util.client.Binaries;
import util.client.GameServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.*;

/**
 * Beinhaltet alle Informationen ueber das aktuelle Match und spielt nacheinander die geplanten Races.
 * Ist ein Singleton.
 */
public class ControlManager {
    private static final ControlManager instance = new ControlManager();
    private final Logger logger = Logger.getLogger("ControlManager");
    private Match currentMatch;
    private int currentGameID;
    private Race currentGame;
    private LinkedBlockingDeque<Race> remainingGames; // the games that can still be distributed between the nodes
    private RaceHandler gameHandler;
    private boolean interrupting;


    public static ControlManager getInstance() {
        return instance;
    }

    /**
     * Initialisiert alle notwendigen Daten, um ein Match auszufuehren
     * @param clients Alle teilnehmenden Clients
     * @param gameServer Zu startender Server
     * @param gameMaps Alle zu spielenden Maps
     * @param tournamentMode Ob ein Turnier stattfinden soll oder nicht
     * @param matchSetup Das geplante Match als Setup
     */
    public void init(ArrayList<Client> clients, GameServer gameServer, List<RaceMap> gameMaps, boolean tournamentMode,
                     MatchSetup matchSetup) {
        logger.log(Level.INFO, "Init ControlManager");
        currentGameID = 0;
        currentMatch = new Match(clients, gameMaps, matchSetup);
        Binaries.gameServer = gameServer;
        if (tournamentMode) {
            logger.log(Level.INFO, "Generating tournament");
            currentMatch.generateGamesTournament();
        } else {
            logger.log(Level.INFO, "Generating normal games");
            currentMatch.generateGamesNormal();
        }
        logger.log(Level.INFO, "Generated games");
        remainingGames = new LinkedBlockingDeque<>();
        remainingGames.addAll(currentMatch.getRaces().values());

        logger.log(Level.INFO, "We need to play " + remainingGames.size() + " games in total");
        //Fuer Nodes: DataManager fuer ObserverMessages
        //playAll();
        interrupting = false;
    }

    /**
     * Alle verbleibenden Spiele ausfuehren
     */
    public void playAll() {
        interrupting = false;
        currentMatch.updateStatus(Match.MatchPhase.RUNNING);
        while (!remainingGames.isEmpty()) {
            playNextGame();
            if(interrupting) {
                break;
            }
        }
        logger.log(Level.INFO, "Finished match");
        currentMatch.endMatch();
    }

    /**
     * Startet das naechste Spiel und startet dieses bei aufgetretenen Fehlern neu
     */
    private void playNextGame() {
        Race game = remainingGames.poll();
        this.currentGame = game;
        gameHandler = new RaceHandler(game.getRaceID(), Binaries.gameServer, game.getPlayers(), game.getRaceMap());
        gameHandler.run();

        if(interrupting) {
            return;
        }

        //Hat keine Statistiken produziert (aufgrund von Absturz, etc.) -> Neustart
        boolean completed = currentMatch.addCompletedGame(currentGameID);
        while (completed == false) {
            //Timeout erreicht -> Vermutung: Clients konnten sich nicht verbinden
            if(gameHandler.getTimeout()) {
                logger.log(Level.SEVERE, "The last game did not end normally, because the timeout was reached -> stop game");
                gameReachedTimeout();
                break;
            }
            logger.log(Level.SEVERE, "The last game did not end normally -> restart game");
            gameHandler.run();
            completed = currentMatch.addCompletedGame(currentGameID);
        }

        //Sichere Replay Dateien
        saveReplayFiles();;

        currentGameID++;
    }

    /**
     * Server hat den Timeout erreicht. Das Race wird als Timeout markiert.
     */
    public void gameReachedTimeout() {
        currentMatch.addTimeoutToGame(currentGameID);
        currentGameID++;
    }

    /**
     * Speichert alle erzeugten Replay-Config-Dateien
     */
    public void saveReplayFiles()  {
        try {
            REPLAY_DIR = CURRENT_MATCH_DIR.getAbsolutePath() + "/replay/";
            if (!Files.exists(Paths.get(FileSystemConstants.REPLAY_DIR + "/race" + currentGameID))) {
                Files.createDirectories(Paths.get(FileSystemConstants.REPLAY_DIR + "/race" + currentGameID));
            }
            File[] files = Paths.get(FileSystemConstants.HOME_DIR).toFile().listFiles();
            for(File file : files) {
                if(!getFileExtension(file.getName()).contentEquals("json")) {
                    continue;
                }
                String fileName = file.getName();
                if(!fileName.contains(RacegameConstants.REPLAY_FILE_SUB)) {
                    continue;
                }
                Files.move(file.toPath(), Paths.get(REPLAY_DIR + "/race" + currentGameID + "/" + file.getName()));
                logger.log(Level.INFO, "Moved " + file.getName());
            }
            logger.log(Level.INFO, "Successfully saved all replay data files");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save replay files: " + e.getLocalizedMessage());
        }


    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    public int getCurrentGameID() {
        return currentGameID;
    }

    /**
     * Prueft, ob sich ein Client mit der angegebenen ID im Match befindet
     *
     * @param ID Client-ID
     * @return Client, falls vorhanden. Sonst: null
     */
    public Client getClientWithID(Integer ID) {
        for (var client : getCurrentMatch().getClients()) {
            if (client.getClientID() == ID) {
                return client;
            }
        }
        return null;
    }

    /**
     * Prueft und gibt ggf. die gewuenschte Map anhand einer ID zurueck
     * @param ID Map-ID
     * @return Map, falls vorhanden. Sonst: null
     */
    public RaceMap getMapWithID(Integer ID) {
        for (var map : getCurrentMatch().getRaceMaps()) {
            if (map.getID() == ID) {
                return map;
            }
        }
        return null;
    }

    public Race getCurrentGame() {
        return currentGame;
    }


    public LinkedBlockingDeque<Race> getRemainingGames() {
        return remainingGames;
    }

    /**
     * Bricht das aktuelle Race ab
     */
    public void interruptGame() {
        interrupting = true;
        logger.log(Level.INFO, "Stopping the current game");
        gameHandler.interruptGame();
    }

}
