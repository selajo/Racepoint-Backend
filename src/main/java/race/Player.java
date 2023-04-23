package race;

import com.google.gson.annotations.Expose;

/**
 * Teilnehmender Client an einem Race. Beinhaltet weniger Informationen fuer das Frontend als ein Client.
 */
public class Player {
    @Expose
    private final int ID;
    private transient final Client client;
    @Expose
    private final int clientID;
    @Expose
    private final String clientShowName;
    @Expose
    private final String clientShowColor;
    @Expose
    private int lastRank;
    @Expose
    private final boolean scoring;

    Player(int ID, Client client, boolean scoring) {
        this.ID = ID;
        this.client = client;
        this.clientID = client.getClientID();
        this.clientShowName = client.getShowName();
        this.clientShowColor = client.getShowColor();
        this.scoring = scoring;
        this.lastRank = client.getLastRank();
    }


    /**
     * Aktualisiert den Rang des Spielers.
     * @param newRank
     */
    void update(int newRank) {
        this.lastRank = newRank;
    }


    public boolean getScoring() {
        return scoring;
    }

    public Client getClient() {
        return client;
    }

    public int getLastRank() {
        return lastRank;
    }

    public int getClientID() {
        return client.getClientID();
    }

    public String getShowName() {
        return client.getShowName();
    }

    public String getStartCommand() {
        return client.getStartCommand();
    }

    public String getArgs() {
        return client.getArgs();
    }

}

