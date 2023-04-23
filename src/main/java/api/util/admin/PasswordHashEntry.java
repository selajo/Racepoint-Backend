package api.util.admin;

import com.google.gson.annotations.Expose;

/**
 * Stellt die Struktur der Datei dar, die die Passwoerter der Nutzer verwaltet.
 */
public class PasswordHashEntry {
    @Expose
    private final String username;
    @Expose
    private final String hash;

    PasswordHashEntry(String username, String hash) {
        this.username = username;
        this.hash = hash;
    }

    public String getUsername() {
        return username;
    }

    public String getHash() {
        return hash;
    }
}
