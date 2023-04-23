package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * Beinhaltet alle Konstanten, die mit IO-Operationen zu tun haben.
 */
public class FileSystemConstants {
    /**
     * Haupt-Directory des Systems. Wird als Start-Directory fuer alle anderen Dirs verwendet.
     */
    public static String HOME_DIR;
    /**
     * Directory, in dem die Clients gespeichert werden.
     */
    public static String CLIENT_DIR;
    /**
     * Directory, in dem die Konfigurationsdateien der Spielfelder abgespeichert werden.
     */
    public static String MAP_DIR;
    /**
     * Directory, in dem die Server-Binary abgespeichert wird.
     */
    public static String SERVER_DIR;
    public static String TEMP_LOG_DIR;

    public static File LOG_FILE;
    public static File MATCH_HOME;
    public static File CURRENT_MATCH_DIR;
    public static File LOG_DIR;
    public static String REPLAY_DIR;
    public static File matchListInfo;
    public static final Lock matchListInfoLock = new ReentrantLock();

    public final static String CLONE_DIR = "cloneDir";
    public final static String DOCKER_DIR = "dockerDir";
    public static String RACE_HOME;
    public static String LOG_HOME;
    public static String GAME_TEMP_HOME;

    /**
     * Alle verwendeten Directories werden anhand des Home-Dirs aktualisiert
     *
     * @param homeDir Der zu verwendende Hauptordner
     */
    public static void updateDirs(String homeDir) {
        HOME_DIR = homeDir;
        CLIENT_DIR = homeDir + "/clients/";
        MAP_DIR = homeDir + "/maps/";
        SERVER_DIR = homeDir + "/server/";
        TEMP_LOG_DIR = homeDir + "/temp_log/";
        LOG_DIR = Paths.get(homeDir + "/persistent/logs/").toFile();

        MATCH_HOME = Paths.get(homeDir + "/persistent/matches/").toFile();
        if (!MATCH_HOME.exists()) {
            MATCH_HOME.mkdir();
        }

        REPLAY_DIR = MATCH_HOME.getAbsolutePath() + "/replay/";
        RACE_HOME = "racepoint/match/";
        LOG_HOME = RACE_HOME + "logs/";
        GAME_TEMP_HOME = RACE_HOME + "games/";

        matchListInfo = Paths.get(MATCH_HOME.getAbsolutePath() + "/matchListInfo.json").toFile();
    }

    /**
     * Loescht und erstellt ein Datei
     * @param file Neu zu erstellende Datei
     * @throws IOException Datei nicht vorhanden
     */
    public static void deleteAndCreateNew(String file) throws IOException {
        Path excludedMaps = Paths.get(file);
        if (Files.exists(excludedMaps)) {
            excludedMaps.toFile().delete();
        }
        Files.createFile(excludedMaps);
    }

    /**
     * Liefert den kompletten Dateinamen
     * @param dir Pfad zum Directory
     * @param partName Teil eines Dateinamens
     * @return Kompletter Dateiname falls vorhanden
     */
    public static String getCompleteFileName(String dir, String partName) {
        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.getName().contains(partName)) {
                return dir + "/" + file.getName();
            }
        }
        return null;
    }

    /**
     * Loescht die angegebene Datei
     * @param fileName Zu loeschende Datei
     * @return True: Datei geloscht; False: Datei nicht vorhanden
     */
    public static boolean deleteFile(String fileName) {
        return new File(fileName).delete();
    }

    /**
     * Loescht den Directory-Stream
     * @param path Pfad zum Directory-Stream
     * @throws IOException Datei nicht vorhanden
     */
    public static void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Erstellt eine neue Datei und schreibt den content in die Datei
     * @param pFile Pfad zur Datei
     * @param content Inhalt, der geschrieben werden soll
     * @throws IOException Datei nicht vorhanden
     */
    public static void createAndWriteToFile(Path pFile, String content) throws IOException {
        File file = pFile.toFile();
        if (!file.exists()) {
            Files.createFile(pFile);
        }
        FileWriter fw = new FileWriter(file);
        fw.write(content);
        fw.close();
    }

    /**
     * Ermittelt die Extension einer Datei
     * @param fileName Zu pruefende Datei
     * @return Extension der Datei
     */
    public static String getFileExtension(String fileName) {
        String extension = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0) {
            extension = fileName.substring(index + 1);
        }
        return extension;
    }

    /**
     * Loescht den angegebenen Ordern inkl. Sub-Inhalt
     * @param folder Zu loeschender Ordner
     */
    public static void deleteFolderContent(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolderContent(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
