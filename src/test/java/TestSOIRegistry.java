import com.merzadyan.FileOp;
import com.merzadyan.stock.SOIRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * Testing SOIRegistry is tricky since it is a singleton class. To get around this,
 * tests are carried out in order - however I am not going further as to use reflection
 * as it is really hacky and may not work. Further testing should be carried out manually.
 */
public class TestSOIRegistry {
    
    @Test
    public void instanceShouldNotBeNull() {
        SOIRegistry registry = SOIRegistry.getInstance();
        Assert.assertNotNull(registry);
    }
    
    @Test
    public void ftse100FileShouldExist() {
        // FTSE-100 file is created on object construction thus verify after object construction.
        SOIRegistry registry = SOIRegistry.getInstance();
        Assert.assertNotNull(registry);
        Assert.assertTrue(FileOp.isFile(SOIRegistry.FTSE_100_FILE_PATH));
    }
    
    @Test
    public void ftse100SetShouldNotBeNullOrEmpty() {
        Set ftse100Set = SOIRegistry.getInstance().getFtse100Set();
        Assert.assertNotNull(ftse100Set);
        // FTSE-100 file should not be null or empty as it is created and populated
        // if the file is non-existent or empty on object construction.
        Assert.assertFalse(ftse100Set.isEmpty());
    }
}
