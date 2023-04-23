package race;

import matchstarting.ControlManager;
import matchstarting.MatchSetup;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import util.client.ClientsAndServerFromJson;
import util.client.GameServer;
import util.client.GitClient;

import java.util.ArrayList;

public class RankDataTest {
    static Race game;
    static Client client;
    static GameServer gameServer;
    static RaceMap gameMap;

    @BeforeClass
    public static void setUp() throws Exception {
        gameMap = new RaceMap(1, "src/test/res/map_test.txt");
        ClientsAndServerFromJson clientsAndServerFromJson = new ClientsAndServerFromJson("src/test/res/gitClient_test.json", null ,null);
        GitClient gclient = clientsAndServerFromJson.readGitClientJson()[0];
        client = new Client(gclient);
        Player player = new Player(1, client, true);
        ArrayList<Player> playerList = new ArrayList<>();
        playerList.add(player);
        game = new Race(gameMap, playerList, 1, "something");
        gameServer = new GameServer();
    }

    public void initControlManager() {
        ArrayList<Client> client_list = new ArrayList<>();
        client_list.add(client);
        ArrayList<RaceMap> gameMaps = new ArrayList<>();
        gameMaps.add(gameMap);
        ControlManager.getInstance().init(client_list, gameServer, gameMaps, true, new MatchSetup());
    }

    @Ignore
    @Test
    public void RankData() {
        initControlManager();
        RankData rankData = new RankData(0);
        rankData.addRankDataFromFinishedGame(game, null);
    }
}
