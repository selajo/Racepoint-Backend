package race;

import com.google.gson.annotations.Expose;
import matchstarting.ControlManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RankData {
    @Expose
    private final Map<Integer, DuelData> duelData;
    @Expose
    private final Map<Integer, MapData> ranksPerMap;
    @Expose
    private final int clientID;

    private static Logger logger = Logger.getLogger("RankData");


    RankData(int currentClientID) {
        clientID = currentClientID;
        duelData = new HashMap<>();
        for(Client client : ControlManager.getInstance().getCurrentMatch().getClients()) {
            if(client.getClientID() != currentClientID) {
                duelData.put(client.getClientID(), new DuelData());
            }
        }
        ranksPerMap = new HashMap<>();
        for(RaceMap map : ControlManager.getInstance().getCurrentMatch().getRaceMaps()) {
            ranksPerMap.put(map.getID(), new MapData(map.getNumPlayers()));
        }
    }

    void addRankDataFromFinishedGame(Race game, Statistics statistics) {
        game.addStatistics(statistics);
        int currentPlayerID = 0;
        for(int i = 0; i < game.getPlayers().size(); i++) {
            if(game.getPlayers().get(i).getScoring() && game.getPlayers().get(i).getClient().getClientID() == clientID) {
                currentPlayerID = i;
                break;
            }
        }
        int currentPlayerRank = game.getPlayers().get(currentPlayerID).getLastRank();
        ranksPerMap.get(game.getRaceMap().getID()).addRank(currentPlayerRank);

        for(int i = 0; i < game.getPlayers().size(); i++) {
            if(i != currentPlayerID) {
                int enemyClientID = game.getPlayers().get(i).getClient().getClientID();
                if(game.getPlayers().get(currentPlayerID).getClientID() != enemyClientID) {
                    if(game.getPlayers().get(i).getLastRank() > currentPlayerRank) {
                        duelData.get(enemyClientID).win();
                    } else if (game.getPlayers().get(i).getLastRank() < currentPlayerRank) {
                        duelData.get(enemyClientID).lose();
                    }
                }
            }
        }
    }




}

class DuelData{
    @Expose
    private int numWon;
    @Expose
    private int numLost;

    DuelData() {
        this.numWon = 0;
        this.numLost = 0;
    }

    void win() {
        numWon++;
    }

    void lose() {
        numLost++;
    }

    public int getNumLost() {
        return numLost;
    }

    public int getNumWon() {
        return numWon;
    }
}

class MapData {
    @Expose
    public final int numPlayers;
    @Expose
    private final List<Integer> ranks;

    MapData(int numPlayers) {
        this.numPlayers = numPlayers;
        this.ranks = new LinkedList<>();
    }

    void addRank(int rank) {
        ranks.add(rank);
    }

    public int getNumPlayers() {
        return numPlayers;
    }

    public List<Integer> getRanks() {
        return ranks;
    }
}
