package util.client;

import race.Client;
import org.junit.Test;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class ClientsAndServerFromJsonTest {
    @Test
    public void ReadGitClientJson() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/gitClient_test.json", null, null);
        GitClient[] clients = clientsAndServerFromJson.readGitClientJson();
        assertEquals(13, clients[0].getClientID());
        assertEquals("HelloWorld-1.0-SNAPSHOT.jar", clients[0].getFileName());
    }

    @Test
    public void ReadGitClientJson_null() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, null, null);
        GitClient[] clients = clientsAndServerFromJson.readGitClientJson();
        assertNull(clients);
    }

    @Test
    public void ReadGitClientJson_FileNotFound() {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/something.json", null, null);
        try {
            clientsAndServerFromJson.readGitClientJson();
        } catch(Exception e) {
            assertTrue(e.getClass() == FileNotFoundException.class);
        }
    }

    @Test
    public void ReadOtherClientJson() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, "src/test/res/otherClient_test.json", null);
        OtherClient[] clients = clientsAndServerFromJson.readOtherClientJson();
        assertEquals(1000, clients[0].getClientID());
        assertEquals("AI Trivial", clients[0].getShowName());
    }

    @Test
    public void ReadOtherClientJson_FileNotFound() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, "something.json", null);
        try {
            clientsAndServerFromJson.readOtherClientJson();
        } catch(Exception e) {
            assertTrue(e.getClass() == FileNotFoundException.class);
        }
    }

    @Test
    public void ReadOtherClientJson_null() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, null, null);
        OtherClient[] clients = clientsAndServerFromJson.readOtherClientJson();
        assertNull(clients);
    }

    @Test
    public void ReadOtherClientJson_getCompletePath() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, "src/test/res/otherClient_test.json", null);
        OtherClient[] clients = clientsAndServerFromJson.readOtherClientJson();
        assertEquals("./clients/aitrivial/ai_trivial", clients[0].getCompletePath());
    }

    @Test
    public void ReadServerJson() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, null, "src/test/res/server_test.json");
        GameServer server = clientsAndServerFromJson.readServerJson();
        assertEquals("FILE_VAR", server.getStartCommand());
        assertEquals("server \"/Res/Config/ConfigWorld00.json\"", server.getArgs());
    }

    @Test
    public void ReadServerJson_FileNotFound() {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(null, null, "src/test/res/something.json");
        try {
            clientsAndServerFromJson.readServerJson();
        } catch (Exception e) {
            assertTrue(e.getClass() == FileNotFoundException.class);
        }
    }

    @Test
    public void ReadAllClients() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(
                "src/test/res/gitClient_test.json", "src/test/res/otherClient_test.json",
                null);
        ArrayList<Client> clients = clientsAndServerFromJson.readAllClients();
        assertEquals(4, clients.size());
    }

    @Test
    public void ReadAllClients_homeTest() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(
                "src/test/res/gitClient_test.json", "src/test/res/otherClient_test.json",
                null);
        ArrayList<Client> clients = clientsAndServerFromJson.readAllClients();
        assertEquals("GIT", clients.get(0).getHome());
        assertEquals("GIT", clients.get(1).getHome());
        assertEquals("./clients/aitrivial", clients.get(2).getHome());
        assertEquals("./clients/aitrivial", clients.get(3).getHome());
    }
}
