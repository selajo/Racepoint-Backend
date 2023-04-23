package race;

/**
 * Beinhaltet Konstanten, die mit dem Rennspiel zu tun haben.
 */
public class RacegameConstants {
    public static String MAP_IDENTIFIER = "MAP";
    public static String CHECKPOINT_IDENTIFIER = "CHECKPOINT";
    public static String STARTPOINT_IDENTIFIER = "STARTPOINT";

    public static final String REPLAY_FILE_SUB = "player";


    public static String convertToDirection(int direction) {
        if (direction == 1) {
            return "up";
        }
        else if (direction == 2) {
            return "down";
        }
        else if (direction == 3) {
            return "left";
        }
        else if (direction == 4) {
            return "right";
        }
        return "";
    }
}
