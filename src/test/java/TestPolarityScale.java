import com.merzadyan.PolarityScale;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPolarityScale {
    private PolarityScale polarityScale;
    
    @Before
    public void setup() {
        polarityScale = new PolarityScale();
    }
    
    
    @Test
    public void isInitialisedAsNeutral() {
        Assert.assertTrue(polarityScale.getPolarity() == 0);
    }
    
    @Test
    public void isIncremented() {
        final int prevBalance = polarityScale.getPolarity();
        polarityScale.positive();
        final int curBalance = polarityScale.getPolarity();
        Assert.assertEquals(prevBalance + 1, curBalance);
    }
    
    @Test
    public void isDecremented() {
        final int prevBalance = polarityScale.getPolarity();
        polarityScale.negative();
        final int currBalance = polarityScale.getPolarity();
        Assert.assertEquals(prevBalance - 1, currBalance);
    }
}
