package race;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RaceHandlerTest {
    @Test
    public void replace_Map() {
        String toReplace = "( cd ./ && java -jar server.jar -in server -c '$MAP$' ) &> racepoint/match/logs/game0server.txt";
        String actual = toReplace.replace("$MAP$", "TestMap");
        assertEquals("( cd ./ && java -jar server.jar -in server -c 'TestMap' ) &> racepoint/match/logs/game0server.txt", actual);
    }
}
