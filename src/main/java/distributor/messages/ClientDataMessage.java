package distributor.messages;

import com.google.gson.annotations.Expose;

public class ClientDataMessage extends Message {

    @Expose
    private final int clientID;

    @Expose
    private final String startCommand;

    @Expose
    private final String args;

    @Expose
    private final byte[] binary;

    @Expose
    private final String fileName;

    @Expose
    private final String showName;

    public ClientDataMessage(int clientID, String startCommand, String args, byte[] binary, String fileName, String showName) {
        this.clientID = clientID;
        this.startCommand = startCommand;
        this.args = args;
        this.binary = binary;
        this.fileName = fileName;
        this.showName = showName;
    }

    public int getClientID() {
        return clientID;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public byte[] getBinary() {
        return binary;
    }

    public String getFileName() {
        return fileName;
    }

    public String getShowName() {
        return showName;
    }
}
