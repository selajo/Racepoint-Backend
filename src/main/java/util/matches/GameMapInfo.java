package util.matches;

import com.google.gson.annotations.Expose;

import java.util.Map;

/**
 * Beinhaltet die Kurzform eines Spielfeldes
 */
public class GameMapInfo {
    @Expose
    private final int gameID;
    @Expose
    private final String gamePhase;
    @Expose
    private final int mapID;
    @Expose
    private final String mapName;
    @Expose
    private final Map<Integer, Integer> clientPointMapping;

    public GameMapInfo(int gameID, String gamePhase, int mapID, String mapName, Map<Integer, Integer> clientPointMapping) {
        this.gameID = gameID;
        this.gamePhase = gamePhase;
        this.mapID = mapID;
        this.mapName = mapName;
        this.clientPointMapping = clientPointMapping;
    }

    public int getGameID() {
        return gameID;
    }

    public Map<Integer, Integer> getClientPointMapping() {
        return clientPointMapping;
    }
}
