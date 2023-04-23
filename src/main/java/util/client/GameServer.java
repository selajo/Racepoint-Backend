package util.client;

import com.google.gson.annotations.Expose;

/**
 * Beinhaltet Kommandos und Informationen zum Starten des Servers.
 */
public class GameServer {
    @Expose
    private String startCommand;

    @Expose
    private String args;

    @Expose
    private String path;

    private boolean inTournamentMode() {
        return args.contains(" --racepoint");
    }

    /**
     * Setzt den Tournament-Modus, falls setting true ist.
     * Entfernt den Tournament-Modus, falls setting false ist.
     *
     * @param setting True: Turning-Modus; False: Normaler Modus
     */
    public void setTournamentMode(boolean setting) {
        if (inTournamentMode()) {
            //Unset tournament args
            if (!setting) {
                args = args.replace(" --racepoint", "");
            }
        } else {
            if (setting) {
                args += " --matchpoint";
            }
        }
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public String getPath() {
        return path;
    }

    public void setConfigFile(String config) {
        args = " -in server " + args + " -c " + config + " ";
    }
}
