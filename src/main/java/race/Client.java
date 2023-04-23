package race;

import com.google.gson.annotations.Expose;
import util.client.GitClient;
import util.client.OtherClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static race.Points.pointsFromRank;

/**
 * Beinhaltet alle notwendigen Informationen eines Clients.
 */
public class Client {
    @Expose
    private final String showName;
    @Expose
    private final String showColor;

    private transient final String startCommand;
    private transient final String args;
    private transient final String fileName;

    @Expose
    private final String home;
    @Expose
    private final int clientID;
    @Expose
    private final String revision;
    @Expose
    private RankData rankData;
    @Expose
    private int finishedRaces;

    // Ranking Data
    @Expose
    private int lastRank;
    @Expose
    private Integer points;
    @Expose
    private Status buildStatus;
    @Expose
    private final Map<String, LinkedList<Integer>> disqualifications;

    private final transient Logger logger;

    enum Status {
        OTHER_CLIENT,
        BUILD_SUCCESS,
        BUILD_FAILED,
        NOT_BUILT_YET,
        UNKNOWN
    }

    /**
     * Initialisiert den Client als GIT-Client
     * @param gitClient Informationen des GIT-Clients
     */
    public Client(GitClient gitClient) {
        this(gitClient.getShowName(), gitClient.getShowColor(), gitClient.getStartCommand(),
                gitClient.getRevision(), gitClient.getClientID(), gitClient.getFileName(),
                gitClient.getArgs(), "GIT");
        this.buildStatus = getGitBuildStatus(gitClient.getBuildSuccess());
    }

    /**
     * Initialisiert den Client als GIT-Client. Build-Prozess noch nicht gestartet.
     * @param gitClient Informationen des GIT-Clients
     * @param earlyRead Signalisiert, dass der Build-Prozess noch nicht gestartet wurde
     */
    public Client(GitClient gitClient, boolean earlyRead) {
        this(gitClient.getShowName(), gitClient.getShowColor(), gitClient.getStartCommand(),
                "NaN", gitClient.getClientID(), gitClient.getFileName(),
                gitClient.getArgs(), "GIT");
        this.buildStatus = Status.NOT_BUILT_YET;
    }

    /**
     * Initialisiert den Client als Other-Client.
     * @param otherClient Informationen des Other-Clients.
     */
    public Client(OtherClient otherClient) {
        this(otherClient.getShowName(), otherClient.getShowColor(), otherClient.getStartCommand(),
                null, otherClient.getClientID(), otherClient.getCompletePath(), otherClient.getArgs(),
                otherClient.getHome());
        this.buildStatus = Status.OTHER_CLIENT;
    }

    public Client(String showName, String showColor, String startCommand, String revision, int clientID, String fileName, String args, String home) {
        this.showName = showName;
        this.showColor = showColor;
        this.startCommand = startCommand;
        this.revision = revision;
        this.clientID = clientID;
        this.fileName = fileName;
        this.args = args;
        this.home = home;
        this.buildStatus = Status.UNKNOWN;

        this.disqualifications = new HashMap<>();

        this.lastRank = 0;
        this.finishedRaces = 0;
        this.points = 0;
        rankData = null;
        logger = Logger.getLogger("Client " + showName);
        logger.setLevel(Level.FINEST);
    }

    /**
     * Liefert den Build-Status anhand des angegebenen Parameters buildSuccess.
     * @param buildSuccess True: Build erfolgreich, False: sonst
     * @return BUILD_SUCCESS: buildSuccess==True; BUILD_FAILED: sonst
     */
    public Status getGitBuildStatus(boolean buildSuccess) {
        if(buildSuccess) {
            return Status.BUILD_SUCCESS;
        }
        return Status.BUILD_FAILED;
    }

    public int getClientID() {
        return clientID;
    }


    public String getShowColor() {
        return showColor;
    }

    public String getShowName() {
        return showName;
    }

    public Integer getPoints() {
        return points;
    }

    void updateRank(int rank) {
        updatePoints(rank);
        this.lastRank = rank;
    }

    /**
     * Anzahl beendeter Races des Clients wird um eins erhoeht.
     */
    void addFinishedGame() {
        synchronized (this) {
            finishedRaces++;
        }
    }

    public int getLastRank() {
        return lastRank;
    }

    /**
     * Verrechnet die Points, die der Client beim letzten Race erzielt hat zu den Gesamt-Points des Clients
     * @param newRank Platzierung, den der Client erreicht hat
     */
    void updatePoints(int newRank) {
        synchronized (this) {
            logger.log(Level.FINEST, "Changed points for client " + showName);
            points += pointsFromRank(newRank);
        }
    }

    void addAfterGameData(Race game) {
        if (rankData == null) {
            rankData = new RankData(clientID);
        }
        synchronized (this) {
            rankData.addRankDataFromFinishedGame(game, null);
        }
    }

    void addDisqualification(int lastRank, String reason, int gameID, boolean isScoring) {
        if (reason.contains("invalid move")) {
            String[] reasonParts = reason.split(":");
            reason = "invalid move:".concat(reasonParts[reasonParts.length - 1]);
        }
        synchronized (disqualifications) {
            int lastPoints = Points.pointsFromRank(lastRank);
            logger.log(Level.FINEST, "Disqualified client " + showName);
            logger.log(Level.FINEST, "Adding penalty " + Points.PENALTY_DISQUALIFICATION);
            logger.log(Level.FINEST, "Subtracting points from last rank: " + lastPoints);
            if (isScoring) {
                synchronized (this) {
                    points += Points.PENALTY_DISQUALIFICATION;
                    points -= lastPoints;
                }
            } else {
                reason = "Disqualification without score penalty: " + reason;
            }
            LinkedList<Integer> disqsWithSameReason;
            if (disqualifications.containsKey(reason)) {
                disqsWithSameReason = disqualifications.get(reason);
            } else {
                disqsWithSameReason = new LinkedList<>();
            }
            disqsWithSameReason.add(gameID);
            disqualifications.put(reason, disqsWithSameReason);
        }
    }

    public String getHome() {
        return home;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public String getFileName() {
        return fileName;
    }
}
