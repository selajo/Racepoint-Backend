package matchstarting;

import race.RaceMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.*;

/**
 * Plant und liest alle Spielfelder ein, die fuer das Match verwendet werden.
 */
public class RaceMapSetup {
    private static Logger logger = Logger.getLogger("RaceMapSetup");

    /**
     * Liest alle Spielfelder ein, die sich im Maps-Ordner.
     * Prueft zusaetzlich, ob das jeweilige Spielfeld deaktiviert ist oder nicht.
     *
     * @return Eine Collection an Spielfelder, die gekennzeichnet sind, ob diese gespielt werden sollen.
     * @throws IOException Falls die Spielfelder nicht gelesen werden konnten.
     */
    static Map<File, Boolean> getPlayedMaps() throws IOException {
        List<String> excludedMaps = getExcludedMaps();
        Map<File, Boolean> playedMaps = new HashMap<>();

        File folder = new File(Paths.get(MAP_DIR).toUri());
        File[] files = folder.listFiles();

        for (File file : files) {
            if (!getFileExtension(file.getName()).contentEquals("json")) {
                continue;
            }
            String fileName = file.getName();
            if (!fileName.contentEquals("excludedMaps.txt")) {
                boolean playMap = true;
                for (String exclMapName : excludedMaps) {
                    if (exclMapName.contentEquals(fileName)) {
                        playMap = false;
                        break;
                    }
                }
                playedMaps.put(file, playMap);
            }
        }

        return playedMaps;
    }

    /**
     * Liest die Namen der Spielfelder ein, die fuer die Matches deaktiviert sind.
     *
     * @return Liste der Spielfeldnamen, die deaktiviert sind.
     */
    private static List<String> getExcludedMaps() {
        String excludedMaps = HOME_DIR + "/persistent/excludedMaps.txt";
        if (Paths.get(excludedMaps).toFile().exists()) {
            try {
                return Files.readAllLines(Paths.get(excludedMaps));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to read excluded maps: " + e.getLocalizedMessage(), e);
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Liest alle Spielfelder, die nicht deaktiviert sind, ein.
     * @param all Ob alle Maps eingelesen werden sollen
     * @return Die Liste der aktuell aktivierten Spielfelder.
    */
    public static List<RaceMap> readGameMaps(Boolean all) {
        List<RaceMap> gameMaps = new LinkedList<>();
        try {
            int mapID = 0;
            Map<File, Boolean> mapsToPlayMap = new LinkedHashMap<>();
            if (all) {
                mapsToPlayMap = getAllPlayedMaps();
            } else {
                mapsToPlayMap = getPlayedMaps();
            }
            List<File> mapsToPlay = new LinkedList<>();
            for (File file : mapsToPlayMap.keySet()) {
                if (mapsToPlayMap.get(file)) {
                    mapsToPlay.add(file);
                }
            }
            for (File file : mapsToPlay) {
                RaceMap map = new RaceMap(mapID, file.getPath());
                if (!map.broken) {
                    gameMaps.add(map);
                    mapID++;
                } else {
                    logger.log(Level.WARNING, "Skipped broken map " + file.getName());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to read in map files: " + e.getLocalizedMessage(), e);
            logger.log(Level.SEVERE, "Exiting...");
            System.exit(-1);
        }
        return gameMaps;
    }

    /**
     * Liefert alle Spielfelder zurueck, die auf dem System vorhanden sind.
     * @return Alle Spielfelder, die auf dem System vorhanden sind.
     * @throws IOException Datei nicht vorhanden
     */
    public static Map<File, Boolean> getAllPlayedMaps() throws IOException {
        Map<File, Boolean> playedMaps = new HashMap<>();

        File folder = new File(Paths.get(MAP_DIR).toUri());
        File[] files = folder.listFiles();

        for (File file : files) {
            if (!getFileExtension(file.getName()).contentEquals("json")) {
                continue;
            }
            String fileName = file.getName();
            boolean playMap = true;
            playedMaps.put(file, playMap);

        }

        return playedMaps;
    }
}
