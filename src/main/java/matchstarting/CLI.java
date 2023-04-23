package matchstarting;

import org.apache.commons.cli.*;

/**
 * Stellt die Schnittstelle zu den Kommandozeilen-Parameter dar.
 * Verwaltet alle moeglichen Eingaben der Nutzer.
 */
public class CLI {
    private static CommandLine cli;
    static Options options;

    /**
     * Beinhaltet alle moeglichen CLI-Parameter inkl. Beschreibung und Abkuerzung
     * @param args Uebergebende Parameter bei Programmstart
     * @throws ParseException Falls ein nicht optionaler Key fehlt
     */
    public static void initOptions(String[] args) throws ParseException {
        options = new Options();
        options.addRequiredOption("k", "sharedkey", true, "path to the shrared secret used for HMAC authentication");
        options.addOption("d", "dir", true, "absolute path to the home dir for matchpoint");
        options.addRequiredOption("s", "serverJSON", true, "a JSON file for the server binary and args");
        options.addOption("g", "gitClientJSON", true, "a JSON file for the clients built from git");
        options.addOption("o", "otherClientJSON", true, "a JSON file other clients (i.e. not from git)");

        CommandLineParser parser = new DefaultParser();
        cli = parser.parse(options, args);
    }

    public static String getHomeDir() {
        if(cli.hasOption("d")) {
            return cli.getOptionValue("d") + "/";
        }
        else {
            return System.getProperty("user.dir") + "/";
        }
    }

    public static String getServerJSON() {
        return cli.getOptionValue("s");
    }

    public static String getGitClientJSON() {
        if(cli.hasOption("g")) {
            return cli.getOptionValue("g");
        }
        else {
            return null;
        }
    }

    public static String getOtherClientJSON() {
        if(cli.hasOption("o")) {
            return cli.getOptionValue("o");
        }
        else {
            return null;
        }
    }

    static String getSharedKey() {
        return cli.getOptionValue("k");
    }

    /**
     * Gibt eine Information ueber alle moeglichen Eingabekommandos aus.
     */
    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("RacepPoint-API", options);
    }
}
