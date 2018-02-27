import com.merzadyan.SOIRegistry;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestSOIRegistry {
    private static SOIRegistry instance;
    
    @BeforeClass
    public static void setup() {
        instance = SOIRegistry.getInstance();
    }
    
    @Test
    public void instanceIsNotNull() {
        Assert.assertTrue(instance != null);
    }
    
    @Test
    public void stockSetNotNullAndNotEmpty() {
        Assert.assertTrue(instance.getStockSet() != null && !instance.getStockSet().isEmpty());
    }
}
