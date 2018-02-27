import com.merzadyan.WordRegistry;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestWordRegistry {
    private static WordRegistry instance;
    // Executed only once.
    // See https://stackoverflow.com/questions/20295578/difference-between-before-beforeclass-beforeeach-and-beforeall
    @BeforeClass
    public static void setup() {
        instance = WordRegistry.getInstance();
    }
    
    // Executed only once.
    @AfterClass
    public static void tearDown() {
    
    }
    
    @Test
    public void instanceIsNotNull() {
        Assert.assertTrue(instance != null);
    }
    
    @Test
    public void positiveSetNotNullAndNotEmpty() {
        Assert.assertTrue(instance.getPositiveSet() != null && !instance.getPositiveSet().isEmpty());
    }
    
    @Test
    public void negativeSetNotNullAndNotEmpty() {
        Assert.assertTrue(instance.getNegativeSet() != null && !instance.getPositiveSet().isEmpty());
    }
}
