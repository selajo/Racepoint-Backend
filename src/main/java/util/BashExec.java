package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uebernimmt das Starten von Bash-Befehlen.
 */
public class BashExec {

    private final static Logger logger = Logger.getLogger("BachExecution");

    /**
     * Beinhaltet den Rueckgabewert und die Ausgabe des Bash-Aufrufes.
     * Zusaetzlich wird gekennzeichnet, ob ein Fehler aufgetreten ist.
     */
    public static class Result {
        public boolean hadError;
        public final List<String> out;
        public final List<String> err;
        public final int exitCode;

        public Result(boolean hadError, List<String> out, List<String> err, int exitCode) {
            this.hadError = hadError;
            this.out = out;
            this.err = err;
            this.exitCode = exitCode;
        }
    }

    /**
     * Liest den Stream aus einer CMD-Operation.
     * @param inputStream Zu lesender Stream
     * @return Gelesener Stream als String
     */
    private static List<String> readStream(InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader b = new BufferedReader(inputStreamReader);

        ArrayList<String> result = new ArrayList<>();
        String line = "";
        try {
            while ((line = b.readLine()) != null) {
                result.add(line);
            }
            b.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to read data from input stream due to IOException: " + e.getLocalizedMessage(), e.getCause());
        }
        return result;
    }

    /**
     * Gibt Prefix und Result als Log aus
     * @param prefix Prefix
     * @param result Log
     */
    public static void logLines(String prefix, Result result) {
        if (result.out.size() > 0) {
            for (String line : result.out) {
                logger.log(Level.INFO, prefix + " - OUT - " + line);
            }
        }
        if (result.err.size() > 0) {
            for (String line : result.err) {
                logger.log(Level.INFO, prefix + " - ERR - " + line);
            }
        }
    }

    /**
     * Startet einen Bash-Befehl im aktuellen Terminal.
     * @param command Auszufuehrendes Kommando.
     * @param logPrefix Log-Prefix
     * @param workingDir Arbeits-Directory
     * @param timeout Timeout, nach dem der Befehl abgebrochen werden soll.
     * @return Die Ergebnis-Ausgabe des Befehls.
     */
    public static Result executeBashCommand(String command, String logPrefix, File workingDir, long timeout) {
        boolean hadError = true;
        logger.log(Level.FINE, logPrefix + " - Executing command in bash: " + command);

        String[] commands = {"bash", "-c", command};
        Process p = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.directory(workingDir);
            p = processBuilder.start();

            p.waitFor(timeout, TimeUnit.SECONDS);
            if (p.isAlive()) {
                logger.log(Level.WARNING, logPrefix + " - Timeout while waiting for execution of command '" + command + "'!");

                List<String> output = readStream(p.getInputStream());
                List<String> errors = readStream(p.getErrorStream());
                if (output.size() == 0 && errors.size() == 0) {
                    logger.log(Level.WARNING, logPrefix + " - No log output was generated for command '" + command + "'!");
                }
                p.destroyForcibly();
                return new Result(true, output, errors, -1);
            }

            hadError = (p.exitValue() != 0);
        } catch (Exception e) {
            logger.log(Level.WARNING, logPrefix + " - Failed to execute bash with command '" + command + "', error: " + e.getLocalizedMessage(), e.getCause());
        }

        assert p != null;
        List<String> output = readStream(p.getInputStream());
        List<String> errors = readStream(p.getErrorStream());
        return new Result(hadError, output, errors, p.exitValue());
    }

    /**
     * Fuehrt einen Bash-Befehl mit Logging aus.
     * @param command Auszufuehrendes Kommando.
     * @param logPrefix Log-Prefix
     * @param workingDir Arbeits-Directory
     * @param timeout Timeout, nach dem der Befehl unterbrochen werden soll.
     * @return Ob ein Fehler aufgetreten ist.
     */
    public static boolean executeBashCommandWithLogging(String command, String logPrefix, File workingDir, long timeout) {
        final Result result = executeBashCommand(command, logPrefix, workingDir, timeout);
        logLines(logPrefix, result);
        return result.hadError;

    }

}
