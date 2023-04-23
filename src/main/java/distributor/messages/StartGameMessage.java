package distributor.messages;

import com.google.gson.annotations.Expose;

public class StartGameMessage extends Message {

    @Expose
    private final int gameID;

    public StartGameMessage(int gameID) {
        this.gameID = gameID;
    }

    public int getGameID() {
        return gameID;
    }
}
