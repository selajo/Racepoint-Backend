package util.matches;

import com.google.gson.annotations.Expose;

/**
 * Eine Kurzform der Clients
 */
public class ShortClientInfo {
    @Expose
    private final int ID;
    @Expose
    private final String showName;
    @Expose
    private final String showColor;
    @Expose
    private final int rank;
    @Expose
    private final int points;

    public ShortClientInfo(int ID, String showName, String showColor, int rank, int points) {
        this.ID = ID;
        this.showName = showName;
        this.showColor = showColor;
        this.rank = rank;
        this.points = points;
    }

    public int getPoints() {
        return points;
    }
}
