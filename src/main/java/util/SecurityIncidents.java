package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.HOME_DIR;

/**
 * Verwaltet Sicherheits-zusammenhaengende Ereignisse.
 */
public class SecurityIncidents {
    private static final Path eventListPath = Paths.get(HOME_DIR + "/persistent/security_events.json");
    private static final Logger logger = Logger.getLogger("SecurityIncidents");
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    /**
     * Liefert den Inhalt der bisherigen Security-Events.
     * @return Alle bisherigen Security-Events
     */
    public static List<SecurityEntry> getSecurityEvents() {
        try {
            if(!eventListPath.toFile().exists()) {
                return new ArrayList<>();
            }
            String json = Files.readString(eventListPath);
            Type securityEntryListType = new TypeToken<ArrayList<SecurityEntry>>() {
            }.getType();
            return gson.fromJson(json, securityEntryListType);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read in security entries: " + e.getLocalizedMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Speichert die Security-Eintraege
     * @param list Zu speichernde Security-Eintraege
     */
    private static void saveSecurityEventList(List<SecurityEntry> list) {
        try {
            if(!eventListPath.toFile().exists()) {
                eventListPath.toFile().createNewFile();
            }
            Files.writeString(eventListPath, gson.toJson(list));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not save security event list: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Fuegt einen neuen Security-Eintrag hinzu
     * @param message Zu speichernde Nachricht
     * @param probablyCritical True: ist critical; False: sonst
     */
    public static void addSecurityEvent(String message, boolean probablyCritical) {
        List<SecurityEntry> events = getSecurityEvents();
        events.add(new SecurityEntry(message, System.currentTimeMillis(), probablyCritical));
        saveSecurityEventList(events);
    }
}
