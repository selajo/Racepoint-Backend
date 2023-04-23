package race;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RacegameConstantsTest {
    @Test
    public void convertToDirection_up() {
        assertEquals("up", RacegameConstants.convertToDirection(1));
    }

    @Test
    public void convertToDirection_right() {
        assertEquals("right", RacegameConstants.convertToDirection(4));
    }

    @Test
    public void convertToDirection_down() {
        assertEquals("down", RacegameConstants.convertToDirection(2));
    }

    @Test
    public void convertToDirection_left() {
        assertEquals("left", RacegameConstants.convertToDirection(3));
    }
}
