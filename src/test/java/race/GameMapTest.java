package race;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.FileSystemConstants.MAP_DIR;

public class GameMapTest {
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
    public void readCompleteMap() throws Exception {
        RaceMap gameMap = new RaceMap(1, "src/test/res/map_test.json");
        assertEquals(30, gameMap.getHeight());
        assertEquals(9, gameMap.getNum_checkpoints());
    }
}
