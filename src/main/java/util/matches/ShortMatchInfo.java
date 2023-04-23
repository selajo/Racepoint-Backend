package util.matches;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

/**
 * Beinhaltet alle Kurformen zu den Clients.
 */
public class ShortMatchInfo {
    @Expose
    private final long ID;
    @Expose
    private final ArrayList<ShortClientInfo> matchListClientInfo;

    public ShortMatchInfo(long ID, ArrayList<ShortClientInfo> matchListClientInfo) {
        this.ID = ID;
        this.matchListClientInfo = matchListClientInfo;
    }

    public long getID() {
        return ID;
    }

    public ArrayList<ShortClientInfo> getMatchListClientInfo() {
        return matchListClientInfo;
    }
}
