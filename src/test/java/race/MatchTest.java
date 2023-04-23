package race;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class MatchTest {
    private ArrayList<Client> clients;

    private ArrayList<Integer> ranking;


    public void updateRanks() {
        List<Client> copy = new ArrayList<>(clients);
        Collections.copy(copy, clients);
        Collections.sort(copy, Comparator.comparingInt(Client::getPoints));
        Collections.reverse(copy);

        ranking = new ArrayList<>();
        for(Client client : copy) {
            ranking.add(client.getClientID());
        }
    }

    public void addClients() {
        clients = new ArrayList<>();

        Client client1 = new Client("test1", "red", "start", "rev", 0, "file", "args", "test");
        client1.updateRank(2);
        clients.add(client1);

        Client client2 = new Client("test3", "red", "start", "rev", 50, "file", "args", "test");
        client2.updateRank(5);
        clients.add(client2);

        Client client3 = new Client("test2", "red", "start", "rev", 10, "file", "args", "test");
        client3.updateRank(1);
        clients.add(client3);
    }

    @Test
    public void updateRanks_order() {
        addClients();
        updateRanks();

        assertEquals((Integer)10, ranking.get(0));
        assertEquals((Integer)0, ranking.get(1));
        assertEquals((Integer)50, ranking.get(2));

        assertEquals(0, clients.get(0).getClientID());
        assertEquals(50, clients.get(1).getClientID());
        assertEquals(10, clients.get(2).getClientID());
    }


}
