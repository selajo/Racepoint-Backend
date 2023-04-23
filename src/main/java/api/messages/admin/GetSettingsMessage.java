package api.messages.admin;

import api.messages.Message;

import static api.Constants.CODE_SETTINGS;
/**
 * Wird verwendet, um die aktuellen Einstellungen des RacePoint-Systems zu bekommen.
 */
public class GetSettingsMessage extends Message {
    /*
    private int matchPeriod;
    private int logfileLifespan;
    private boolean forceIntervalStart;
    private boolean continuousMode;
    private boolean tournamentMode;
    private String versionGit;

    private Map<String, Boolean> mapsActivated;
    private Map<String, Boolean> clientsActivated;

    private String serverArguments;

    //private List<SecurityEntry> securityEntryList;

    public GetSettingsMessage(int matchPeriod, int logfileLifespan, boolean forceIntervalStart, boolean continuousMode,
                              boolean tournamentMode, String versionGit, Map<String, Boolean> mapsActivated,
                              Map<String, Boolean> clientsActivated, String serverArguments, List<SecurityEntry> securityEntryList) {
        super(300);
        this.matchPeriod = matchPeriod;
        this.forceIntervalStart = forceIntervalStart;
        this.continuousMode = continuousMode;
        this.tournamentMode = tournamentMode;
        this.versionGit = versionGit;
        this.mapsActivated = mapsActivated;
        this.clientsActivated = clientsActivated;
        this.serverArguments = serverArguments;
        //this.securityEntryList = securityEntryList;
        this.logfileLifespan = logfileLifespan;
    }

 */
    String Name;
    int Size;

    public GetSettingsMessage(String Name, int Size) {
        super(CODE_SETTINGS);
        this.Name = Name;
        this.Size = Size;
    }
}
