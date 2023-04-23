package util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.LOG_DIR;

/**
 * Verwaltet die erzeugten Log-Zips.
 * Schema: LOG_DIR/matchID.tar.gz
 */
public class LogHandler {
    private static final Logger logger = Logger.getLogger("LogHandler");

    /**
     * Liefert die gewuenschte Log-Zip-Datei via MatchID.
     * @param matchID Gewuenschte Log-Zip-Datei des jeweiligen Matches.
     * @return Pfad zur Datei.
     */
    public static File getLogFile(long matchID) {
        return Paths.get(LOG_DIR.getAbsolutePath() + "/" + matchID + ".tar.gz").toFile();
    }

    /**
     * Loescht eine Log-Zip-Datei via matchID.
     * @param matchID Zu loeschende Log-Zip-Datei.
     * @return Ob Datei erfolgreich geloescht wurde.
     */
    public static boolean deleteLogFile(long matchID) {
        File toDelete = getLogFile(matchID);
        return toDelete.delete();
    }

    /**
     * Liefert alle Dateien, deren Daten aelter als deleteBefore sind.
     * @param deleteBefore Zeit in Sekunden.
     * @return Liste aller Dateien, die dem Schema entsprechen.
     */
    private static File[] listLogFiles(long deleteBefore) {
        File[] logFiles = LOG_DIR.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                filename = filename.strip();
                if(filename.endsWith(".tar.gz")) {
                    String baseName = filename.split("\\.")[0];
                    try {
                        long logTime = Long.parseLong(baseName);
                        if(logTime < deleteBefore) {
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        logger.log(Level.WARNING, "Could not parse timestamp while trying to delete a logfile: " + e.getLocalizedMessage(), e);
                        logger.log(Level.WARNING, "Logfile name was " + filename + ", basename was " + baseName);
                    }
                }
                return false;
            }
        });
        return logFiles;
    }

    /**
     * Loescht alle Log-Zip-Dateien, die aelter als die angegebene Anzahl an Tagen ist.
     * @param numOfDays Anzahl an Tagen, der Logs.
     * @return Ob der Loeschvorgang erfolgreich war.
     */
    public static boolean deleteLogFilesOlderThan(int numOfDays) {
        boolean success = true;
        long currentTime = System.currentTimeMillis();
        final int MSEC_PER_SEC = 1000;
        final int SEC_PER_MIN = 60;
        final int MIN_PER_HOUR = 60;
        final int HOUR_PER_DAY = 24;
        long deleteBefore = currentTime - (long) MSEC_PER_SEC * SEC_PER_MIN * MIN_PER_HOUR * HOUR_PER_DAY * numOfDays;
        File[] logFiles = listLogFiles(deleteBefore);

        if(logFiles != null) {
            for(File file : logFiles) {
                if(!file.delete()) {
                    success = false;
                }
            }
        }
        return success;
    }


}
