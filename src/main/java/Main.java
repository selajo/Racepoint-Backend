//import matchstarting.MatchManager;
//import org.apache.commons.cli.ParseException;

import api.RacepointApiApplication;
import matchstarting.CLI;
import org.apache.commons.cli.ParseException;
import util.FileSystemConstants;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Startet das Match-Verwaltungssystem
 */
public class Main {

    private static final Logger logger = Logger.getLogger("Main");

    /**
     * Startet das Match-Verwaltungssystem
     * @param args Uebergabeparameter
     */
    public static void main(String[] args) {
        logger.log(Level.INFO, "Start spring api");
        RacepointApiApplication.main(args);

        try {
            CLI.initOptions(args);
        } catch (ParseException e) {
            logger.log(Level.SEVERE, "Failed to parse cli arguments: " + e.getLocalizedMessage());
            CLI.printHelp();
            System.exit(-1);
        }
        FileSystemConstants.updateDirs(CLI.getHomeDir()); // update directory structure


    }
}
