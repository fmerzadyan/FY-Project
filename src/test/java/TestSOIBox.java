import com.merzadyan.stock.SOIBox;
import com.merzadyan.stock.SOIRegistry;
import com.merzadyan.stock.Stock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.TreeSet;

public class TestSOIBox {
    private static final String IMMEDIATE_DIR = "src/test/resources/ser";
    private static final String SERIALISED_FILE_PATH = IMMEDIATE_DIR + "/TestSOIBox.ser";
    private SOIBox SOIBox;
    private TreeSet<Stock> set;
    private Stock stock;
    
    @Before
    public void beforeTest() {
        SOIBox = new SOIBox();
        
        stock = new Stock("BAE Systems", "LSE", "LSE");
        stock.setHistogram(new int[]{1, 2, 3, 4, 5});
        stock.setLatestSentimentScore(4);
        stock.setStartDate(LocalDate.parse("2018-03-01"));
        stock.setEndDate(LocalDate.parse("2018-03-08"));
        set = new TreeSet<>();
        set.add(stock);
        
        // Delete file before each test.
        File file = new File(SERIALISED_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    public void shouldContainStockAfterAdding() {
        Assert.assertTrue(set.contains(stock));
    }
    
    @Test
    public void shouldNotContainStockAfterRemoving() {
        set = new TreeSet<>();
        Assert.assertFalse(set.contains(stock));
        set.add(stock);
        Assert.assertTrue(set.contains(stock));
        set.remove(stock);
        Assert.assertFalse(set.contains(stock));
    }
    
    @Test
    public void shouldNotContainStockAfterInitialisation() {
        set = SOIBox.set;
        Assert.assertFalse(set.contains(stock));
    }
    
    @Test
    public void serialisedFileShouldBeCreatedAfterSerialisation() {
        File immediateDirs = new File(IMMEDIATE_DIR);
        immediateDirs.mkdirs();
        
        SOIRegistry.serialise(set, SERIALISED_FILE_PATH);
        
        File file = new File(SERIALISED_FILE_PATH);
        boolean existsAndNotEmpty = file.exists() && file.length() > 0;
        Assert.assertTrue(existsAndNotEmpty);
    }
    
    @Test
    public void shouldContainStockAfterDeserialisation() {
        File immediateDirs = new File(IMMEDIATE_DIR);
        immediateDirs.mkdirs();
    
        SOIRegistry.serialise(set, SERIALISED_FILE_PATH);
    
        File file = new File(SERIALISED_FILE_PATH);
        boolean existsAndNotEmpty = file.exists() && file.length() > 0;
        Assert.assertTrue(existsAndNotEmpty);
    
        // Mock app shutdown by resetting the SOIBox.
        SOIBox = new SOIBox();
        
        SOIBox = SOIRegistry.deserialise(SERIALISED_FILE_PATH);
        Assert.assertNotNull(SOIBox);
        Assert.assertNotNull(SOIBox.set);
        Assert.assertTrue(SOIBox.set.contains(stock));
    }
}
