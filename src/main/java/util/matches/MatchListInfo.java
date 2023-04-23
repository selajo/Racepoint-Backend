package util.matches;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.MalformedJsonException;
import race.Client;
import race.Match;
import util.FileSystemConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beinhaltet die Informationen zu allen Matches.
 */
public class MatchListInfo {
    @Expose
    private final LinkedList<ShortMatchInfo> shortMatchInfoList;

    private static final Logger logger = Logger.getLogger("MatchListInfo");
    private static boolean hasChanged = true;
    private static MatchListInfo cached = null;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    private MatchListInfo(LinkedList<ShortMatchInfo> shortMatchInfoList) {
        this.shortMatchInfoList = shortMatchInfoList;
    }

    public LinkedList<ShortMatchInfo> getShortMatchInfoList() {
        return shortMatchInfoList;
    }

    /**
     * Fuegt eine neue MatchInfo den bereits gespeicherten MatchInfos der alten Matches hinzu.
     * @param match Zu speicherndes Match
     */
    public static void addMatchInfo(Match match) {
        try {
            FileSystemConstants.matchListInfoLock.lock();
            try {
                //Alle bereits gespielten Spiele einlesen
                MatchListInfo matchListInfo = getMatchListInfoUnsynchronized();
                LinkedList<ShortMatchInfo> shortMatchInfoList;
                if (matchListInfo == null) {
                    shortMatchInfoList = new LinkedList<>();
                } else {
                    shortMatchInfoList = matchListInfo.shortMatchInfoList;
                }

                //Aktuelle Match-Daten hinzufuegen
                ArrayList<ShortClientInfo> matchListClientInfoList = new ArrayList<>();
                for (Client client : match.getClients()) {
                    matchListClientInfoList.add(new ShortClientInfo(client.getClientID(), client.getShowName(),
                            client.getShowColor(), client.getLastRank(), client.getPoints()));
                }

                //Clients nach Ranking sortieren
                matchListClientInfoList.sort(Comparator.comparing(ShortClientInfo::getPoints).reversed());
                shortMatchInfoList.addFirst(new ShortMatchInfo(match.getMatchID(), matchListClientInfoList));
                Files.writeString(FileSystemConstants.matchListInfo.toPath(),
                        gson.toJson(new MatchListInfo(shortMatchInfoList)));

            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not add match to matchListInfo: " + e.getLocalizedMessage(), e);
            }
            hasChanged = true;
        } finally {
            FileSystemConstants.matchListInfoLock.unlock();
        }
    }

    /**
     * Loescht ein Match aus der MatchListInfo.
     * @param matchID Zu loeschendes Match
     * @return True: erfolgreich, False: sonst
     * @throws IOException Falls Datei nicht gefunden wurde
     */
    public static boolean deleteMatchFromList(long matchID) throws IOException {
        try {
            FileSystemConstants.matchListInfoLock.lock();
            final MatchListInfo matchListInfo = getMatchListInfoUnsynchronized();
            if (matchListInfo != null) {
                LinkedList<ShortMatchInfo> shortMatchInfoList = matchListInfo.shortMatchInfoList;
                for (int i = 0; i < shortMatchInfoList.size(); i++) {
                    if (shortMatchInfoList.get(i).getID() == matchID) {
                        shortMatchInfoList.remove(i);
                        // save it
                        Files.writeString(FileSystemConstants.matchListInfo.toPath(),
                                gson.toJson(new MatchListInfo(shortMatchInfoList)));
                        hasChanged = true;
                        return true;
                    }
                }
            }
        } finally {
            FileSystemConstants.matchListInfoLock.unlock();
        }
        return false;
    }

    public static MatchListInfo getMatchListInfo() throws IOException {
        try {
            FileSystemConstants.matchListInfoLock.lock();
            return getMatchListInfoUnsynchronized();
        } finally {
            FileSystemConstants.matchListInfoLock.unlock();
        }
    }

    private static MatchListInfo getMatchListInfoUnsynchronized() throws IOException {
        if(hasChanged || cached == null) {
            if(Files.exists(FileSystemConstants.matchListInfo.toPath())) {
                try {
                    cached = gson.fromJson(Files.readString(FileSystemConstants.matchListInfo.toPath()), MatchListInfo.class);
                } catch (MalformedJsonException malformedJsonException) {
                    logger.log(Level.SEVERE, "Failed to load matchListInfo from JSON, path was '" + FileSystemConstants.matchListInfo.toPath() + "'!", malformedJsonException.getCause());
                    throw new IOException("Failed to load matchListInfo from JSON, path was '" + FileSystemConstants.matchListInfo.toPath() + "'!", malformedJsonException.getCause());
                }

                hasChanged = false;
                return cached;
            } else {
                Files.createFile(FileSystemConstants.matchListInfo.toPath());
                return null;
            }
        } else {
            return cached;
        }
    }

}
