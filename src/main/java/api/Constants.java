package api;

/**
 * Beinhaltet Message-ID-Konstanten.
 */
public class Constants {
    /**
     * Stellt einen Fehler dar
     */
    public final static int CODE_ERROR = -1;
    /**
     * Stellt ein Race dar
     */
    public final static int CODE_RACE = 1;
    /**
     * Stellt mehrere Races dar
     */
    public final static int CODE_GROUP = 2;
    /**
     * Stellt eine Match-List dar
     */
    public final static int CODE_MATCH_LIST = 3;
    /**
     * Stellt ein Match dar
     */
    public final static int CODE_MATCH = 4;
    /**
     * Stellt ein Spielfeld dar
     */
    public final static int CODE_MAP = 5;

    // admin
    /**
     * Stellt einen Token dar
     */
    public final static int CODE_TOKEN = 11;
    /**
     * Stellt Einstellungen dar
     */
    public final static int CODE_SETTINGS = 12;
    /**
     * Stellt akzeptierte Nachricht dar
     */
    public final static int CODE_ACK = 13;
    /**
     * Stellt validen Token dar
     */
    public final static int CODE_TOKEN_INVALID = 14;

    /*
        Some strings
     */

    public final static String NO_RUNNING_MATCH = "Sorry! There is currently no running match.";
    public final static String INVALID_CLIENT_ID = "The given clientID is invalid.";
    public final static String INVALID_GAME_ID = "The given gameID is invalid.";
}
