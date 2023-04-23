package race;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PointsTest {
    @Test
    public void pointsFromRank_1() {
        assertEquals(25, Points.pointsFromRank(1));
    }

    @Test
    public void pointsFromRank_2() {
        assertEquals(12, Points.pointsFromRank(2));
    }

    @Test
    public void pointsFromRank_3() {
        assertEquals(6, Points.pointsFromRank(3));
    }

    @Test
    public void pointsFromRank_4() {
        assertEquals(1, Points.pointsFromRank(4));
    }

    @Test
    public void pointsFromRank_Disqualify() {
        assertEquals(-50, Points.pointsFromRank(Points.DISQUALIFICATION_RANK));
    }

    @Test
    public void pointsFromRank_Default() {
        assertEquals(0, Points.pointsFromRank(10));
    }
}
