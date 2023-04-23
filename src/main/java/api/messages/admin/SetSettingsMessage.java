package api.messages.admin;

import java.util.List;

/**
 * Wird verwendet, um die Einstellungen des RacePoint-Systems zu speichern/aendern.
 */
public class SetSettingsMessage extends TokenizedMessage {
    private int matchPeriod;
    private int logfileLifespan;
    private boolean forceIntervalStart;
    private boolean continuousMode;
    private boolean tournamentMode;

    private String serverArguments;

    private List<String> mapsDeactivated;
    private List<String> clientsDeactivated;

    public SetSettingsMessage(String userName, String token) {
        super(userName, token);
    }

    public int getMatchPeriod() {
        return matchPeriod;
    }

    public boolean isForceIntervalStart() {
        return forceIntervalStart;
    }

    public boolean isContinuousMode() {
        return continuousMode;
    }

    public boolean isTournamentMode() {
        return tournamentMode;
    }

    public String getServerArguments() {
        return serverArguments;
    }

    public List<String> getDeactivatedMaps() {
        return mapsDeactivated;
    }

    public List<String> getDeactivatedClients() {
        return clientsDeactivated;
    }

    public int getLogfileLifespan() {
        return logfileLifespan;
    }
}
