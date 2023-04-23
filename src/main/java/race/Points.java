package race;

/**
 * Ist fuer die Berechung der Punkte des jeweiligen Ranges zustaendig.
 */
public class Points {
    /**
     * Punkte, die bei Disqualifikation verrechnet werden.
     */
    static final int PENALTY_DISQUALIFICATION = -50;

    /**
     * Der definierte Rang fuer die Disqualifikation.
     */
    static final int DISQUALIFICATION_RANK = 20;

    /**
     * Ermittelt den jeweiligen Punktestand, der mit dem angegebenen Rang angegeben wird.
     * @param rank Erzielter Rang des Spielers.
     * @return Dazugehoerige Punktanzahl.
     */
    public static int pointsFromRank(int rank) {
        switch(rank) {
            case 1:
                return 25;
            case 2:
                return 12;
            case 3:
                return 6;
            case 4:
                return 1;
            case DISQUALIFICATION_RANK:
                return PENALTY_DISQUALIFICATION;
        }
        return 0;
    }
}
