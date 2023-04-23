package race;

import org.junit.Test;
import util.FileSystemConstants;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatisticsTest {
    @Test
    public void readStatistics() throws FileNotFoundException {
        String file = FileSystemConstants.getCompleteFileName("src/test/res/", "Spielerstatistik");
        Statistics statistics = Statistics.readStatistics(file);
        assertEquals(2, statistics.Spielmodus);
        assertTrue(statistics.Spielerdaten.size() == 2);
    }
}
