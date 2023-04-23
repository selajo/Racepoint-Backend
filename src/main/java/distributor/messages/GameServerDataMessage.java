package distributor.messages;

import com.google.gson.annotations.Expose;

public class GameServerDataMessage extends Message {

    @Expose
    private final String startCommand;

    @Expose
    private final String args;

    @Expose
    private final byte[] serverBinary;

    public GameServerDataMessage(String startCommand, String args, byte[] serverBinary) {
        this.startCommand = startCommand;
        this.args = args;
        this.serverBinary = serverBinary;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public byte[] getServerBinary() {
        return serverBinary;
    }
}