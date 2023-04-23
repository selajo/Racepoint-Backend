package util.matches;

import com.google.gson.annotations.Expose;
import race.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Beinhaltet die Information zu einem Race mit ClientInfos und MapInfos
 */
public class RaceInfo {
    @Expose
    private final ArrayList<ClientInfo> clientInfos;

    @Expose
    private final ArrayList<GameMapInfo> gameMapInfos;

    /**
     * Fuegt eine neue RaceInfo den vergangenen RaceInfos hinzu
     * @param currentMatch Aktuelles Match
     * @param groupID ID der Match-Gruppe
     * @param isRunningFlag Ob das Race ausgefuehrt wird.
     */
    public RaceInfo(Match currentMatch, int groupID, boolean isRunningFlag) {
        Map<Integer, Race> games = currentMatch.getRaces();

        ArrayList<GameMapInfo> maps = new ArrayList<>();
        ArrayList<GameMapInfo> unstarted = new ArrayList<>();
        ArrayList<GameMapInfo> finished = new ArrayList<>();
        ArrayList<ClientInfo> clientInfos = new ArrayList<>();

        //Fuer jedes Game wird eine GameMapInfo erstellt
        for(Race game : games.values()) {
            boolean groupParticipates = false;
            Map<Integer, Integer> points = new HashMap<>();
            ArrayList<Player> players = game.getPlayers();

            for(Player player : players) {
                if(player.getScoring()) {
                    points.put(player.getClient().getClientID(), Points.pointsFromRank(player.getLastRank()));
                    if(player.getClient().getClientID() == groupID) {
                        groupParticipates = true;
                    }
                }
            }

            RaceMap map = game.getRaceMap();
            if(groupID == -1 || groupParticipates) {
                if(isRunningFlag) {
                    if(game.getRacePhase() == Race.RacePhase.INIT) {
                        unstarted.add(new GameMapInfo(game.getRaceID(), game.getRacePhase().toString(), map.getID(), map.getName(), points));
                    } else if (game.getRacePhase() == Race.RacePhase.ERROR ||
                            game.getRacePhase() == Race.RacePhase.TIMEOUT ||
                            game.getRacePhase() == Race.RacePhase.COMPLETED) {
                        finished.add(new GameMapInfo(game.getRaceID(), game.getRacePhase().toString(), map.getID(), map.getName(), points));
                    } else {
                        maps.add(new GameMapInfo(game.getRaceID(), game.getRacePhase().toString(), map.getID(), map.getName(), points));
                    }
                } else {
                    maps.add(new GameMapInfo(game.getRaceID(), game.getRacePhase().toString(), map.getID(), map.getName(), points));
                }
            }
        }

        //Anzeigenamen bekommen
        for(Client client : currentMatch.getClients()) {
            clientInfos.add(new ClientInfo(client.getClientID(), client.getShowName(), client.getShowColor()));
        }
        this.clientInfos = clientInfos;
        if(isRunningFlag) {
            maps.addAll(finished);
            maps.addAll(unstarted);
        }
        this.gameMapInfos = maps;
    }
}
