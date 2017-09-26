package mini.math;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Verifies that algorithms in {@link FastMath} are working correctly.
 */
public class FastMathTest {

    private int nearestPowerOfTwoSlow(int number) {
        return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
    }

    @Test
    public void testNearestPowerOfTwo() {
        for (int i = -100; i < 1; i++) {
            assert FastMath.nearestPowerOfTwo(i) == 1;
        }
        for (int i = 1; i < 10000; i++) {
            int nextPowerOf2 = FastMath.nearestPowerOfTwo(i);
            assert i <= nextPowerOf2;
            assert FastMath.isPowerOfTwo(nextPowerOf2);
            assert nextPowerOf2 == nearestPowerOfTwoSlow(i);
        }
    }

    private static int fastCounterClockwise(Vector2f p0, Vector2f p1, Vector2f p2) {
        float result = (p1.x - p0.x) * (p2.y - p1.y) - (p1.y - p0.y) * (p2.x - p1.x);
        return (int) Math.signum(result);
    }

    private static Vector2f randomVector() {
        return new Vector2f(FastMath.nextRandomFloat(),
                            FastMath.nextRandomFloat());
    }

    @Ignore
    @Test
    public void testCounterClockwise() {
        for (int i = 0; i < 100; i++) {
            Vector2f p0 = randomVector();
            Vector2f p1 = randomVector();
            Vector2f p2 = randomVector();

            int fastResult = fastCounterClockwise(p0, p1, p2);
            int slowResult = FastMath.counterClockwise(p0, p1, p2);

            assert fastResult == slowResult;
        }

        // duplicate test
        Vector2f p0 = new Vector2f(0, 0);
        Vector2f p1 = new Vector2f(0, 0);
        Vector2f p2 = new Vector2f(0, 1);

        int fastResult = fastCounterClockwise(p0, p1, p2);
        int slowResult = FastMath.counterClockwise(p0, p1, p2);

        assertEquals(slowResult, fastResult);
    }
}
