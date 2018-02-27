import com.merzadyan.NegPosBalance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestNegPosBalance {
    private NegPosBalance negPosBalance;
    
    @Before
    public void setup() {
        negPosBalance = new NegPosBalance();
    }
    
    
    @Test
    public void isInitialisedAsZero() {
        Assert.assertTrue(negPosBalance.getBalance() == 0);
    }
    
    @Test
    public void isIncremented() {
        final int prevBalance = negPosBalance.getBalance();
        negPosBalance.positive();
        final int curBalance = negPosBalance.getBalance();
        Assert.assertEquals(prevBalance + 1, curBalance);
    }
    
    @Test
    public void isDecremented() {
        final int prevBalance = negPosBalance.getBalance();
        negPosBalance.negative();
        final int currBalance = negPosBalance.getBalance();
        Assert.assertEquals(prevBalance - 1, currBalance);
    }
}
