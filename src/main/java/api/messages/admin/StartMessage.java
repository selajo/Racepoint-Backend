package api.messages.admin;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Wird fuer die Start-Anfrage eines Matches verwendet, um erlaubte Clients und Maps herauszufiltern
 */
public class StartMessage {
    private String message;
    private String userName;
    private String token;
    private int gameMode;
    private ArrayList<String> clientNames;
    private ArrayList<String> clientID;
    private ArrayList<String> mapNames;

    public String getToken() {
        return token;
    }

    public String getUserName() {
        return userName;
    }

    public int getGameMode() {
        return gameMode;
    }

    /**
     * Liefert eine Liste der Cients IDs als ArrayList
     * @return eine Liste der Cients IDs als ArrayList
     */
    public ArrayList<Integer> getClientID() {
        ArrayList<Integer> integerList = new ArrayList<Integer>();
        for (String i : clientID) {
            integerList.add(Integer.valueOf(i));
        }
        return integerList;
    }

    public ArrayList<String> getClientNames() {
        return clientNames;
    }

    public ArrayList<String> getMapNames() {
        return mapNames;
    }
}
