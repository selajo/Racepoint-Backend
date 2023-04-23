package matchstarting;

import race.RaceMap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static matchstarting.RacepointProperties.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.FileSystemConstants.MAP_DIR;

public class RacepointPropertiesTest {
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
    public void excludeMapViaID_exclude() throws Exception {
        List<RaceMap> raceMaps = new ArrayList<>();
        raceMaps.add(new RaceMap(1, "src/test/res/map_test.json"));

        ALLOWED_MAPS = raceMaps;

        excludeMapViaID(1);
        assertEquals(0, ALLOWED_MAPS.size());
    }

    @Test
    public void excludeMapViaID_noID() throws Exception {
        List<RaceMap> raceMaps = new ArrayList<>();
        raceMaps.add(new RaceMap(1, "src/test/res/map_test.json"));

        ALLOWED_MAPS = raceMaps;

        excludeMapViaID(2);
        assertEquals(1, ALLOWED_MAPS.size());
    }

    @Test
    public void includeMapsViaIDs_include() {
        try (MockedStatic<RaceMapSetup> utilities = Mockito.mockStatic(RaceMapSetup.class)) {
            utilities.when(() -> RaceMapSetup.readGameMaps(true))
                    .thenReturn(Arrays.asList(new RaceMap(1, "src/test/res/map_test.json")));

            List<Integer> numbers = Arrays.asList(1, 2);
            includeMapsViaIDs(numbers);
            assertEquals(1, ALLOWED_MAPS.size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
