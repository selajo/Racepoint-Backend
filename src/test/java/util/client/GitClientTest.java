package util.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import util.FileSystemConstants;
import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitClientTest {
    @Ignore
    @Test
    public void runNormalBuildTest() throws FileNotFoundException, InterruptedException {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        GitClient.USE_DOCKERIZED_BUILD = false;

        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/gitClient_test.json", null ,null);
        GitClient client = clientsAndServerFromJson.readGitClientJson()[0];
        client.start();
        client.join();
        assertTrue(Binaries.clientBinaries.size() == 1);
    }

    @Ignore
    @Test
    public void runDockerBuildTest() throws FileNotFoundException, InterruptedException {
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            return;
        }
        GitClient.USE_DOCKERIZED_BUILD = true;

        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/gitClient_test.json", null ,null);
        GitClient client = clientsAndServerFromJson.readGitClientJson()[0];
        client.start();
        client.join();
        assertTrue(Binaries.clientBinaries.size() == 1);
    }

    @Test
    public void buildDockerfileTest() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/gitClient_test.json", null ,null);
        GitClient client = clientsAndServerFromJson.readGitClientJson()[0];
        String actual = client.buildDockerfile();
        assertEquals("FROM registry.git.rwth-aachen.de/moves/docker-baseimage:latest AS build-stage\n" +
                "\n" +
                "ENV TERM linux\n" +
                "ENV DEBIAN_FRONTEND noninteractive\n" +
                "\n" +
                "WORKDIR /builder\n" +
                "COPY cloneDir/ /builder\n" +
                "RUN mvn clean package\n" +
                "\n" +
                "FROM scratch AS export-stage\n" +
                "COPY --from=build-stage /builder//target/HelloWorld-1.0-SNAPSHOT.jar /" +
                "\n", actual);
    }
}
