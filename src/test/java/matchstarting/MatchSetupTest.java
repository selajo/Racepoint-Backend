package matchstarting;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import race.Client;
import race.RaceMap;
import util.client.ClientsAndServerFromJson;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.FileSystemConstants.MAP_DIR;

public class MatchSetupTest {
    static String map_save;
    @BeforeClass
    public static void beforeClass() {
        map_save = MAP_DIR;
        MAP_DIR = "./";
    }

    @AfterClass
    public static void afterClass() {
        MAP_DIR = map_save;
    }

    @Test
    public void excludeRaceMaps_not() throws Exception {
        ArrayList<RaceMap> raceMaps = new ArrayList<>();
        raceMaps.add(new RaceMap(1, "src/test/res/map_test.json"));
        ArrayList<String> include = new ArrayList<>();
        include.add("somethingelse.json");

        List<RaceMap> actual = MatchSetup.excludeMaps(include, raceMaps);
        assertEquals(0, actual.size());
    }

    @Test
    public void excludeRaceMaps() throws Exception {
        ArrayList<RaceMap> raceMaps = new ArrayList<>();
        raceMaps.add(new RaceMap(1, "src/test/res/map_test.json"));
        ArrayList<String> include = new ArrayList<>();
        include.add("map_test.json");

        List<RaceMap> actual = MatchSetup.excludeMaps(include, raceMaps);
        assertEquals("map_test.json", actual.get(0).getMapName());
    }

    @Test
    public void excludeClients_not() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(
                "src/test/res/gitClient_test.json", "src/test/res/otherClient_test.json",
                null);
        ArrayList<Client> clients = clientsAndServerFromJson.readAllClients();
        ArrayList<String> includeName = new ArrayList<>();
        includeName.add("somethingelse");
        ArrayList<Integer> includeId = new ArrayList<>();
        includeId.add(1000);

        List<Client> actual = MatchSetup.excludeClients(includeId, includeName, clients);
        assertEquals(0, actual.size());
    }

    @Test
    public void excludeClients() throws FileNotFoundException {
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson(
                "src/test/res/gitClient_test.json", "src/test/res/otherClient_test.json",
                null);
        ArrayList<Client> clients = clientsAndServerFromJson.readAllClients();
        ArrayList<String> includeName = new ArrayList<>();
        includeName.add("AI Trivial");
        ArrayList<Integer> includeId = new ArrayList<>();
        includeId.add(1000);

        List<Client> actual = MatchSetup.excludeClients(includeId, includeName, clients);
        assertEquals(1, actual.size());
    }
}
