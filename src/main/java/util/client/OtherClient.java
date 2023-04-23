package util.client;

import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Beinhaltet Informationen über einen Client, der kein Git-Client ist.
 */
public class OtherClient {
    @Expose
    private String home;

    @Expose
    private String fileName;

    @Expose
    private String showName;

    @Expose
    private String showColor;

    @Expose
    private String startCommand;

    @Expose
    private String args;

    @Expose
    private int clientID;

    /**
     * Liest die Binaries des Other-Clients ein.
     * @return Binaries des Other-Clients
     */
    public byte[] getBytes() {
        try {
            return Files.readAllBytes(Paths.get(home + "/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getShowName() {
        return showName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public int getClientID() {
        return clientID;
    }

    public String getShowColor() {
        return showColor;
    }

    public String getHome() {
        return home;
    }

    @Override
    public String toString() {
        return "OtherClient{" +
                "home='" + home + '\'' +
                ", fileName='" + fileName + '\'' +
                ", showName='" + showName + '\'' +
                ", startCommand='" + startCommand + '\'' +
                ", args='" + args + '\'' +
                '}';
    }

    public String getCompletePath() {
        return home + "/" + fileName;
    }
}
