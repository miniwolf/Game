package mini;

import org.junit.Test;

/**
 * @author miniwolf
 */
public class SetupTest {

    @Test(expected = AssertionError.class)
    public void testAssertionEnabled() {
        assert false;
    }
}
