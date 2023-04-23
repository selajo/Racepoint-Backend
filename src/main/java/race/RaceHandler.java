package race;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import util.BashExec;
import util.client.GameServer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.*;

/**
 * Startet und unterbricht einzelne Races.
 */
public class RaceHandler implements Runnable {

    private int MAX_GAME_DURATION_IN_SEC = 10 * 60;
    private final int SLEEP_AFTER_CLEANUP_IN_SEC = 5;

    private final int gameID;
    private final GameServer gameServer;
    private List<Player> clients;
    private List<Process> clientProcesses;
    private Process serverProcess;
    private final Logger logger;
    private String gameDir;
    private String serverLogFile;
    private String configMapPath;
    private boolean timeout;

    /**
     * Auszufuehrendes Kommando zum Starten und Speichern der Ausgaben eines Kommandos.
     */
    private final String fileCommand = "LOG_PID=$!\n" +
            "JAVA_PID=$(pgrep -f \"$JAVA_COMMAND$\")\n" +
            "\n" +
            "function cleanup {\n" +
            "  echo \"Cleaning up...\"\n" +
            "  echo \"$JAVA_PID\"\n" +
            "  echo \"$LOG_PID\"\n" +
            "  kill $LOG_PID\n" +
            "  kill $JAVA_PID\n" +
            "  exit\n" +
            "}\n" +
            "\n" +
            "function sigkill_handler {\n" +
            "  echo \"Caught SIGKILL signal\"\n" +
            "  cleanup\n" +
            "}\n" +
            "\n" +
            "trap cleanup SIGINT SIGTERM\n" +
            "trap sigkill_handler SIGKILL\n" +
            "\n" +
            "wait";

    public RaceHandler(int gameID, GameServer gameServer, List<Player> clients, RaceMap raceMap) {
        this.gameID = gameID;
        logger = Logger.getLogger("GameHandler " + gameID);
        logger.log(Level.INFO, "Setting up arguments for game server and clients");
        this.gameServer = gameServer;
        this.clients = clients;
        this.configMapPath = raceMap.getAbsolutePath();
        for(Player client : clients) {
            logger.log(Level.INFO, client.getShowName() + " takes part in this match");
        }
        calcMaxTimeout(raceMap);
    }

    /**
     * Berechnet den maximalen Timeout anhand der angegebenen Map.
     * Maximale Spielzeit der Map + zwei Minuten.
     * @param raceMap Spielfeld
     */
    private void calcMaxTimeout(RaceMap raceMap) {
        this.MAX_GAME_DURATION_IN_SEC = raceMap.getGameDurationInSec() + 2 * 60;
    }

    /**
     * Stoppt einen Client bei einer Disqualifikation.
     * @param playerID Player-ID
     */
    public void stopClientAfterDisq(byte playerID) {
        final int clientID = clients.get(playerID).getClientID();
        final Process clientProcess = clientProcesses.get(playerID);
        terminateProcess(clientProcess, clientID, "disqualification");
    }

    /**
     * Beendet den Client-Prozess anhand er Client-ID.
     * @param clientProcess Zu beendender Prozess.
     * @param clientID Zu beendender Client
     * @param reason Grund der Terminierung.
     */
    private void terminateProcess(Process clientProcess, int clientID, String reason) {
        final long clientPid = clientProcess.pid();
        if (clientProcess.isAlive()) {
            logger.log(Level.INFO, "Stopping client " + clientID + " (PID: " + clientPid + ") after "+ reason + ".");
            /*
                Warning: using .destroy is _not_ enough, as the bash script starts a new sub process that is not killed by this operation.
                The negated PID sends the signal to the entire process group, which the shell should have created, and the forked process inherited.
             */
            BashExec.executeBashCommandWithLogging("kill " + clientPid, "Softkill for Client " + clientID + " (PID: " + clientPid + ")", null, 1);

            try {
                Thread.sleep(250);
            } catch (InterruptedException ex) {
                //
            }

            if (clientProcess.isAlive()) {
                logger.log(Level.INFO, "Client " + clientID + " (PID: " + clientPid + ") failed to shut down using SIGINT after "+reason+".");
                BashExec.executeBashCommandWithLogging("kill -s SIGKILL -" + clientPid, "Hardkill for Client " + clientID + " (PID: " + clientPid + ")", null, 1);
            } else {
                logger.log(Level.INFO, "Client " + clientID + " (PID: " + clientPid + ") stopped using SIGINT after "+reason+".");
            }
        }
    }

    /**
     * Erzeugt die Shell-Datei mit den Startkommandos fuer den Server.
     * @return True: erfolgreich; False: ein Fehler ist aufgetreten.
     */
    private boolean setupDirServer() {
        logger.log(Level.INFO, "Setting up dir for new game");
        gameDir = GAME_TEMP_HOME + "race" + gameID + "/";
        try {
            if(!Files.exists(Path.of(LOG_HOME))) {
                Files.createDirectory(Path.of(LOG_HOME));
            }
            new File(gameDir).mkdirs();
            File serverFile = new File(gameDir + "server");
            serverLogFile = LOG_HOME + "race" + gameID + "server.txt";
            serverFile.createNewFile();
            serverFile.setExecutable(true);
            FileWriter fw = new FileWriter(serverFile);
            String serverCommand = gameServer.getStartCommand() + " " +
                    gameServer.getArgs().replace("$MAP$", this.configMapPath);
            String firstCommand = "#!/bin/bash\n" + "(( cd " + gameServer.getPath() + " && " +
                    serverCommand + " ) &> " + serverLogFile + ") &\n";
            String completeCommand = firstCommand + this.fileCommand.replace("$JAVA_COMMAND$", serverCommand.replace("'", ""));
            fw.write(completeCommand);
            fw.flush();
            fw.close();
            logger.log(Level.INFO, "Setup server start command");


        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not create directory for new game!");
            logger.log(Level.WARNING, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Erzeugt die Shell-Datei mit den Startkommandos fuer alle Clients.
     * @return True: erfolgreich; False: ein Fehler ist aufgetreten.
     */
    private boolean setUpDirClients() {
        try {
            for (Player client : clients) {
                logger.log(Level.INFO, "Client " + client.getShowName());
                String clientLogFile = LOG_HOME + "race" + gameID + client.getShowName().replace(" ", "") + ".txt";
                File clientFile = new File(gameDir + "client" + client.getClientID());
                clientFile.createNewFile();
                clientFile.setExecutable(true);
                FileWriter fw = new FileWriter(clientFile);
                String clientStartCommand = client.getStartCommand() + " " + client.getClient().getFileName();
                // Why hard-limit the files using head? Because they only get truncated when it is too late, we had 500G+ logfiles...
                // Why size + 16? When size is exceeded, we automatically truncate and attach a message, so we need to trigger that.
                String clientSoloStartCommand = clientStartCommand + " " + client.getArgs();
                clientStartCommand = clientSoloStartCommand + " &> " + clientLogFile;
                clientStartCommand = "#!/bin/bash\n(" + clientStartCommand + ")&";
                clientStartCommand = clientStartCommand + this.fileCommand.replace("$JAVA_COMMAND$", clientSoloStartCommand);
                fw.write(clientStartCommand);
                fw.flush();
                fw.close();
                logger.log(Level.INFO, "Setup client start command for client " + client.getClientID() + "'.");
            }
            return true;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to setup client commands: " + e.getLocalizedMessage(), e);
            return false;
        }
    }

    /**
     * Beendet alle Client-Prozesse.
     */
    private void cleanUpClients() {
        logger.log(Level.INFO, "Cleaning up client processes for game " + gameID + "...");
        for (int i = 0; i < clientProcesses.size(); i++) {
            Process clientProcess = clientProcesses.get(i);
            terminateProcess(clientProcess, clients.get(i).getClientID(), "game end");
        }
    }

    /**
     * Beendet den Server-Prozess.
     */
    private void cleanUpServer() {
        try {
            if (serverProcess.isAlive()) {
                BashExec.executeBashCommandWithLogging("kill " + serverProcess.pid(), "Softkill for server", null, 1);
            }
            FileUtils.deleteDirectory(new File(gameDir));
            logger.log(Level.INFO, "Cleaning up temporary directory");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not delete temporary directory for the start scripts: " + e.getLocalizedMessage(), e.getCause());
        }
    }

    public boolean getTimeout() {
        return timeout;
    }


    /**
     * Startet das Race. Zuerst wird der Server und anschliessen alle Spieler gestartet. Wartet darauf, dass der Server beendet wird.
     */
    @Override
    public void run() {
        logger.log(Level.INFO, "Started GameHandler for game " + gameID);
        if (!setupDirServer()) {
            logger.log(Level.WARNING, "Could not create game dir!");
            logger.log(Level.WARNING, "Exiting...");
            return;
        }

        serverProcess = null;
        try {
            logger.log(Level.INFO, "Game " + gameID + ": Starting server process");
            serverProcess = new ProcessBuilder(gameDir + "server").start();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Game " + gameID + ": Could not create server process!");
            logger.log(Level.WARNING, e.getMessage());
            return;
        }
        try {
            Thread.sleep(10000); // some time for the server to start and output to be written onto the file system
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Game " + gameID + ": Interrupted game handler upon starting server: " + e.getLocalizedMessage(), e);
            return;
        }

        if (!setUpDirClients()) { // set up client commands
            logger.log(Level.WARNING, "Game " + gameID + ": Failed to setup clients properly");
            //Controller.getInstance().gameStartFail(gameID);
            return;
        }

        // start clients
        logger.log(Level.INFO, "Starting clients for game " + gameID);
        clientProcesses = new LinkedList<>();
        for (Player client : this.clients) {
            try {
                logger.log(Level.INFO, "Game " + gameID + ": Starting client " + client.getClientID() + " with playerID " + client.getClientID());
                // setsid: Creates a new process group that we can later easily kill
                clientProcesses.add(new ProcessBuilder(gameDir + "client" + client.getClientID()).start());
                Thread.sleep(5*1000);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Game " + gameID + ": Could not create process for client " + client.getClientID() + ": " + e.getLocalizedMessage(), e);
            }
        }

        logger.log(Level.INFO, "Game " + gameID + " is now running...");

        try {
            assert serverProcess != null;
            logger.log(Level.INFO, "Max timeout in seconds: " + this.MAX_GAME_DURATION_IN_SEC);
            timeout = !serverProcess.waitFor(MAX_GAME_DURATION_IN_SEC, TimeUnit.SECONDS);
            Thread.sleep(1000); // some time for the clients to digest the end of the game
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Received interrupt while playing game " + gameID + ": " + e.getLocalizedMessage(), e);
            logger.log(Level.WARNING, "Trying to end client and server processes, cleaning up dir");
            serverProcess.destroyForcibly();
            for (Process clientProcess : clientProcesses) {
                clientProcess.destroyForcibly();
            }
            cleanUpServer();
        }
        logger.log(Level.INFO, "Game " + gameID + " has finished!");

        // clean up client processes
        cleanUpClients();
        cleanUpServer();

        try { // security time offset
            Thread.sleep(SLEEP_AFTER_CLEANUP_IN_SEC * 1000);
        } catch (InterruptedException e) {
            //
        }
    }

    /**
     * Unterbricht ein Race, indem alle Clients und der Server beendet werden.
     */
    public void interruptGame() {
        serverProcess.destroyForcibly();
        for (Process clientProcess : clientProcesses) {
            clientProcess.destroyForcibly();
        }
    }
}
