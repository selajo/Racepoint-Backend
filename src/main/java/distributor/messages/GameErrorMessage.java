package distributor.messages;

import com.google.gson.annotations.Expose;

public class GameErrorMessage extends Message {

    @Expose
    private final int gameID;

    public GameErrorMessage(int gameID) {
        this.gameID = gameID;
    }

    public int getGameID() {
        return gameID;
    }
}
