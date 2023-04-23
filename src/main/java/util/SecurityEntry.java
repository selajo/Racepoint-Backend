package util;

import com.google.gson.annotations.Expose;

/**
 * Der Eintrag einer Security-Datei fuer die Login-Incidents
 */
public class SecurityEntry {
    @Expose
    private final String message;

    @Expose
    private final long timeStamp;

    @Expose
    private final boolean probablyCritical;

    SecurityEntry(String message, long timeStamp, boolean probablyCritical) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.probablyCritical = probablyCritical;
    }
}
