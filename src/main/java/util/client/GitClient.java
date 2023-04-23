package util.client;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import util.BashExec;
import util.FileSystemConstants;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.FileSystemConstants.CLONE_DIR;
import static util.FileSystemConstants.DOCKER_DIR;

/**
 * Verwaltet das Klonen und Builden von Git-Clients.
 */
public class GitClient extends Thread {
    private final static Logger logger = Logger.getLogger("GitClient");


    private final static long MAX_BUILD_DURATION_SECONDS = 120;
    private final static long MAX_DOCKER_BUILD_DURATION_SECONDS = 4 * 60;

    private final static long SLEEP_AFTER_BUILD_MILLISECONDS = 2000;
    private final static long SLEEP_AFTER_CLONE_MILLISECONDS = 2000;

    private final static long MAX_SLEEP_BEFORE_BUILD_MILLISECONDS = 5000;
    private final static String BASE_IMAGE = "registry.git.rwth-aachen.de/moves/docker-baseimage";

    @Expose
    private String home;

    @Expose
    private String repoUrl;

    @Expose
    private String checkout;

    @Expose
    private String[] clean;

    @Expose
    private String[] build;

    @Expose
    private String output;

    @Expose
    private String showName;

    @Expose
    private String showColor;

    @Expose
    private String startCommand;

    @Expose
    private String fileName;

    @Expose
    private String args;

    @Expose
    private boolean log;

    @Expose
    private int clientID;

    @Expose
    private boolean buildSuccess = false;

    public static boolean USE_DOCKERIZED_BUILD = false;

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public String toString() {
        return "GitClient{" +
                "home='" + home + '\'' +
                ", repoUrl='" + repoUrl + '\'' +
                ", checkout='" + checkout + '\'' +
                ", clean=" + Arrays.toString(clean) +
                ", buildClient=" + Arrays.toString(build) +
                ", output='" + output + '\'' +
                ", showName='" + showName + '\'' +
                ", showColour='" + showColor + '\'' +
                ", startCommand='" + startCommand + '\'' +
                ", fileName = '" + fileName + '\'' +
                ", args = '" + args + '\'' +
                ", log = '" + log + '\'' +
                ", clientID = '" + clientID + '\'' +
                '}';
    }

    public String toJson() {
        return Base64.getEncoder().encodeToString(gson.toJson(this).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Hält den aktuellen Thread fuer die angegebenen Millisekunden an.
     *
     * @param millis Zu wartende Millisekunden.
     */
    private void doSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Sleep of " + millis + " ms was interrupted: " + e.getLocalizedMessage(),
                    e.getCause());
        }
    }

    /**
     * Checkout in HOME_DIR/$CLONE_DIR
     *
     * @param base
     * @return
     */
    boolean makeCheckout(Path base) {
        final Path to = base.resolve(CLONE_DIR);
        boolean cloneDirExists = Files.exists(to) && Files.exists(to.resolve(".git"));
        this.fileName = to.toString();

        //Nur fetch und reset
        if (cloneDirExists) {
            final File cloneDirFile = to.toFile();
            if (BashExec.executeBashCommandWithLogging("git fetch --all", showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS)) {
                logger.log(Level.WARNING, "Failed to fetch repo for " + showName + ".");
                return true;
            } else if (BashExec.executeBashCommandWithLogging("git reset --hard origin/" + checkout, showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS)) {
                logger.log(Level.WARNING, "Failed to reset repo for " + showName + ".");
                return true;
            } else if (BashExec.executeBashCommandWithLogging("git submodule update --init", showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS)) {
                logger.log(Level.WARNING, "Failed to update submodules for " + showName + ".");
                return true;
            }
        }
        //Neu klonen
        else {
            final File baseFile = base.toFile();
            if (BashExec.executeBashCommandWithLogging("git clone " + repoUrl + " --recurse-submodules --branch " + checkout + " " + CLONE_DIR, showName, baseFile, MAX_BUILD_DURATION_SECONDS)) {
                logger.log(Level.WARNING, "Failed to git clone repo for " + showName + ".");
                return true;
            }
        }
        return false;
    }

    /**
     * Erzeugt die Clients.
     * Zuerst wird Checkout ausgefuehrt, anschliessend das Build-Kommando.
     *
     * @return true: erfolgreich erstellt. false: entweder checkout oder build nicht erfolgreich.
     */
    public boolean buildClient() {
        logger.log(Level.INFO, showName + " - Started buildClient for new GitClient '" + showName + "', config: " + toJson());
        this.fileName = Paths.get(home).toString();
        //Check if Directories for clients already exist
        if (!Files.exists(Paths.get(home))) {
            logger.log(Level.INFO, showName + " - GitClient directory not existing yet, creating...");
            try {
                Files.createDirectory(Paths.get(home));
            } catch (IOException e) {
                logger.log(Level.WARNING, showName + " - Could not create directory for GitClient: " + e.getLocalizedMessage(), e.getCause());
                return false;
            }
        }

        if (USE_DOCKERIZED_BUILD) {
            return buildDockerized();
        } else {

            //Make checkout. If unsuccessful -> end
            if (makeCheckout(Paths.get(home))) {
                return false;
            }
            final File cloneDirFile = Paths.get(home, CLONE_DIR).toFile();

            //Execute clean command
            for (String c : clean) {
                if (BashExec.executeBashCommandWithLogging(c, showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS)) {
                    logger.log(Level.WARNING, "Failed to execute clean command '" + c + "' for " + showName + ".");
                    return false;
                }
            }

            this.fileName += this.output;
            //Execute build command
            for (String b : build) {
                if (BashExec.executeBashCommandWithLogging(b, showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS)) {
                    logger.log(Level.WARNING, "Failed to execute build command '" + b + "' for " + showName + ".");
                    return false;
                }
            }

            //Check if output file is produced
            if (Files.exists(Paths.get(home, CLONE_DIR, output))) {
                try {
                    byte[] binary = Files.readAllBytes(Paths.get(home, CLONE_DIR, output));
                    if (Binaries.clientBinaries == null) {
                        Binaries.clientBinaries = new ConcurrentHashMap<>();
                    }
                    Binaries.clientBinaries.put(clientID, binary);
                    return true;
                } catch (IOException e) {
                    logger.log(Level.WARNING, showName + " - Failed to read produced binary: "
                            + e.getLocalizedMessage(), e.getCause());
                    return false;
                }
            } else {
                logger.log(Level.WARNING, showName + " - GitClient with ID " + clientID +
                        " did not time out, but failed to produce a binary output file!");
                return false;
            }
        }
    }

    @Override
    public void run() {
        buildSuccess = buildClient();
    }

    public String getShowName() {
        return showName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getStartCommand() {
        return startCommand;
    }

    public String getArgs() {
        return args;
    }

    public int getClientID() {
        return clientID;
    }

    public String getShowColor() {
        return showColor;
    }

    public boolean getBuildSuccess() {
        return buildSuccess;
    }

    /**
     * Ermittelt die Revision, die sich auf dem System befindet.
     *
     * @return Die Revision als String. Falls nicht erfolgreich, wird null zurueckgegeben.
     */
    public String getRevision() {
        File cloneDirFile = Paths.get(home, CLONE_DIR).toFile();
        final BashExec.Result result = BashExec.executeBashCommand("git log --pretty=format:'%ad %h %d' --abbrev-commit --date=short -1", showName, cloneDirFile, MAX_BUILD_DURATION_SECONDS);
        if (!result.hadError) {
            return String.join("", result.out);
        }

        logger.log(Level.WARNING, "Failed to get revision from checkout for " + showName + "!");
        BashExec.logLines(showName, result);
        return null;
    }

    /**
     * Erstellt den Inhalt der Docker-File fuer den Git-Client.
     * @return Dateiinhalt der Docker-File
     */
    public String buildDockerfile() {
        List<String> lines = new ArrayList<>();
        lines.add("FROM " + BASE_IMAGE + ":latest AS build-stage");
        lines.add("");
        lines.add("ENV TERM linux");
        lines.add("ENV DEBIAN_FRONTEND noninteractive");
        lines.add("");

        // Set the working directory
        lines.add("WORKDIR /builder");
        // Copy the entire git to the container
        lines.add("COPY " + CLONE_DIR + "/ /builder");
        if (clean.length > 0) {
            lines.add("RUN " + String.join(" && ", clean));
        }
        if (build.length > 0) {
            lines.add("RUN " + String.join(" && ", build));
        }

        lines.add("");
        lines.add("FROM scratch AS export-stage");
        lines.add("COPY --from=build-stage /builder/" + output + " /");
        lines.add("");

        return String.join("\n", lines);
    }

    /**
     * Erstellt den angegebenen Pfad als Directory.
     *
     * @param path
     * @param docker
     * @throws IOException
     */
    private void createBasePath(Path path, boolean docker) throws IOException {
        String type = docker ? "Docker" : "GitClient";
        if (!Files.exists(path)) {
            logger.log(Level.INFO, showName + " - " + type + " directory not existing yet, creating...");
            Files.createDirectory(path);
        }
    }

    /**
     * Fueht den Build-Prozess mit Docker aus. DEAKTIVIERT!
     * @return True: Build erfolgreich; False: sonst
     */
    private boolean buildDockerized() {
        final Path dockerPath = Paths.get(home, DOCKER_DIR);
        try {
            createBasePath(dockerPath, true);
        } catch (IOException e) {
            logger.log(Level.WARNING, showName + " - Could not create directory for Docker: " + e.getLocalizedMessage(), e.getCause());
            return false;
        }

        // Checkout the code next to the Dockerfile
        if (makeCheckout(dockerPath)) {
            return false;
        }

        // Write Dockerfile
        final String dockerFileContents = buildDockerfile();
        final Path dockerFilePath = dockerPath.resolve("Dockerfile");
        try {
            FileSystemConstants.createAndWriteToFile(dockerFilePath, dockerFileContents);
        } catch (IOException ioException) {
            logger.log(Level.WARNING, showName + " - Failed to create/write Dockerfile: " + ioException.getLocalizedMessage(), ioException.getCause());
            return false;
        }

        // Create/clean the output directory
        final Path pathOut = dockerPath.resolve("out");
        if (Files.exists(pathOut)) {
            try {
                FileSystemConstants.deleteDirectoryStream(pathOut);
            } catch (IOException e) {
                logger.log(Level.WARNING, showName + " - Could not delete output directory for Docker: " + e.getLocalizedMessage(), e.getCause());
                return false;
            }
        }
        try {
            Files.createDirectory(pathOut);
        } catch (IOException e) {
            logger.log(Level.WARNING, showName + " - Could not create output directory for Docker: " + e.getLocalizedMessage(), e.getCause());
            return false;
        }

        // Start build with timeout
        final long delayBeforeBuild = ThreadLocalRandom.current().nextLong(MAX_SLEEP_BEFORE_BUILD_MILLISECONDS + 1);
        logger.log(Level.INFO, showName + " - Will now sleep for " + delayBeforeBuild + "ms before starting Docker build to space out the load.");
        //doSleep(delayBeforeBuild);

        //TODO: dieser Kommando kehrt nicht mehr zurueck, obwohl Tests das Erzeugen der Zieldatei bestaetigen ... Was tun?

        if (BashExec.executeBashCommandWithLogging("DOCKER_BUILDKIT=1 docker build --pull --no-cache --progress plain -o out .",
                showName, dockerPath.toFile(), MAX_DOCKER_BUILD_DURATION_SECONDS)) {
            logger.log(Level.WARNING, "Failed to build client " + showName + ".");
            return false;
        }




        /*
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

        // Erstellen Sie das Build-Verzeichnis und geben Sie das Dockerfile an
        File dockerDir = dockerPath.toFile();
        String dockerfile = "Dockerfile";

        // Erstellen Sie das Docker-Image
        BuildImageResultCallback callback = new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                System.out.println(item.getStream());
                super.onNext(item);
            }
        };
        dockerClient.buildImageCmd(dockerDir)
                .withDockerfile(new File(dockerDir, dockerfile))
                .withTags(new HashSet<>(Arrays.asList("mein-image")))
                .exec(callback)
                .awaitImageId();

         */

        /*
        if (0 != p.getErrorStream().toString().length()) {
            logger.log(Level.WARNING, "Failed to build client " + showName + ".");
            return false;
        }
         */

        //doSleep(SLEEP_AFTER_BUILD_MILLISECONDS);

        // Read in executable
        final Path outputFilePath = pathOut.resolve(fileName);
        if (Files.exists(outputFilePath)) {
            try {
                byte[] binary = Files.readAllBytes(outputFilePath);
                Binaries.clientBinaries.put(clientID, binary);
                return true;
            } catch (IOException ioException) {
                logger.log(Level.WARNING, showName + " - Failed to read binary produced in Docker: " + ioException.getLocalizedMessage(), ioException.getCause());
                return false;
            }
        } else {
            logger.log(Level.WARNING, showName + " - Docker-based build of GitClient with ID " + clientID + " did not time out, but failed to produce a binary output file '" + outputFilePath + "'!");
            return false;
        }
    }
}
