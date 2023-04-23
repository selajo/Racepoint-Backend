package util.matches;

import com.google.gson.annotations.Expose;

/**
 * Beinhaltet eine Kurzform eines Clients.
 */
public class ClientInfo {
    @Expose
    private final int clientID;
    @Expose
    private final String clientName;
    @Expose
    private final String clientColor;

    public ClientInfo(int clientID, String clientName, String clientColor) {
        this.clientID = clientID;
        this.clientName = clientName;
        this.clientColor = clientColor;
    }

    public int getClientID() {
        return clientID;
    }
}
