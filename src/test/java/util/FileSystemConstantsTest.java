package util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileSystemConstantsTest {
    private static boolean unixOs = true;
    @BeforeClass
    public static void setUp() {
        FileSystemConstants.updateDirs("someHomeDir");
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            unixOs = false;
        }
    }

    @AfterClass
    public static void tearDown() {
        FileSystemConstants.updateDirs("");
    }

    @Test
    public void updateDirs_HOME_DIR() {
        assertEquals("someHomeDir", FileSystemConstants.HOME_DIR);
    }

    @Test
    public void updateDirs_CLIENT_DIR() {
        assertEquals("someHomeDir/clients/", FileSystemConstants.CLIENT_DIR);
    }

    @Test
    public void updateDirs_MAP_DIR() {
        assertEquals("someHomeDir/maps/", FileSystemConstants.MAP_DIR);
    }

    @Test
    public void updateDirs_SERVER_DIR() {
        assertEquals("someHomeDir/server/", FileSystemConstants.SERVER_DIR);
    }

    @Test
    public void updateDirs_TEMP_LOG_DIR() {
        assertEquals("someHomeDir/temp_log/", FileSystemConstants.TEMP_LOG_DIR);
    }

    @Test
    public void updateDirs_LOG_DIR() {
        String path = "someHomeDir\\persistent\\logs";
        if(unixOs) {
            path = "someHomeDir/persistent/logs";
        }
        assertEquals(path, FileSystemConstants.LOG_DIR.toString());
    }

    @Test
    public void updateDirs_MATCH_HOME_DIR() {
        String path = "someHomeDir\\persistent\\matches";
        if(unixOs) {
            path = "someHomeDir/persistent/matches";
        }
        assertEquals(path, FileSystemConstants.MATCH_HOME.toString());
    }

    @Test
    public void updateDirs_matchListInfo() {
        String path = FileSystemConstants.MATCH_HOME.getAbsolutePath() + "\\matchListInfo.json";
        if(unixOs) {
            path = FileSystemConstants.MATCH_HOME.getAbsolutePath() + "/matchListInfo.json";
        }
        assertEquals(path, FileSystemConstants.matchListInfo.toString());
    }

}
