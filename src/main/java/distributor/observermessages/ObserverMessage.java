package distributor.observermessages;

public class ObserverMessage {
    private int gameID;
    private byte type;
    private int length;
    private byte[] body;

    public ObserverMessage(int gameID, byte type, int length, byte[] body) {
        this.gameID = gameID;
        this.type = type;
        this.length = length;
        this.body = body;
    }

    public int getGameID() {
        return gameID;
    }

    public byte getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public byte[] getBody() {
        return body;
    }
}
