package distributor;

import race.Client;
import race.Match;
import race.Race;
import race.RaceMap;
import matchstarting.MatchSetup;
import org.springframework.lang.Nullable;
import util.client.Binaries;
import util.client.GameServer;

import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Distributes games between nodes, keeps track of available server capacities
 * Singleton
 */
public class ControlManager {

    private static final ControlManager instance = new ControlManager();
    private final Logger logger = Logger.getLogger("ControlManager");

    private Match currentMatch;
    private LinkedBlockingDeque<Race> remainingGames; // the games that can still be distributed between the nodes

    private Map<SocketAddress, HashSet<Integer>> currentGamesPerNode; // saving which games run on which node

    private Map<SocketAddress, Boolean> logsReceived; // saving from which nodes the logs where already received

    public static ControlManager getInstance() {
        return instance;
    }

    /**
     * Initialising the control manager to distribute games
     * @param sharedKey the key for the hmac of the control connection
     * @param aesKey the encryption key for the control connection
     * @param port the port on which to listen for control connections
     * @param clients the clients for the upcoming match
     * @param gameServer the game server to be used in the upcoming match
     * @param gameMaps the game maps for the next match
     * @param tournamentMode whether the next match should be played in tournament mode
     * @param matchSetup the match setup used for creating the current match
     */
    public void init(String sharedKey, String aesKey, int port, ArrayList<Client> clients, GameServer gameServer,
                     List<RaceMap> gameMaps, boolean tournamentMode, MatchSetup matchSetup) {
        logger.log(Level.INFO, "Init ControlManager");
        currentMatch = new Match(clients, gameMaps, matchSetup); // create match
        Binaries.gameServer = gameServer;
        if (tournamentMode) {
            currentMatch.generateGamesTournament();
        } else {
            currentMatch.generateGamesNormal();
        }
        remainingGames = new LinkedBlockingDeque<>();
        remainingGames.addAll(currentMatch.getRaces().values());
        currentGamesPerNode = new HashMap<>();
        logsReceived = new HashMap<>();
        DataManager.getInstance().init();
        DataManager.getInstance().start();
        //NodeControlNetwork.getInstance().init(port, sharedKey, aesKey);
    }

    public Match getCurrentMatch() {
        return currentMatch;
    }

    @Nullable
    Race takeRemainingGame() {
        return remainingGames.poll();
    }

    /**
     * Requeue a game that failed on a node so that it can be played on another node
     * @param gameID the id of the failed game
     */
    void requeueGame(int gameID) {
        logger.log(Level.INFO, "Requested requeue of game " + gameID);
        try {
            Race game = currentMatch.getRaces().get(gameID);
            game.resetGame(); // reset metadata of the game played so far
            remainingGames.putFirst(game);
            logger.log(Level.INFO, "Re-queued game " + gameID);
            currentMatch.updateRanks(gameID);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "ControlManager interrupted: " + e.getLocalizedMessage(), e);
        }
    }

    Map<SocketAddress, HashSet<Integer>> getCurrentGamesPerNode() {
        return currentGamesPerNode;
    }

    Map<SocketAddress, Boolean> getLogsReceived() {
        return logsReceived;
    }
}