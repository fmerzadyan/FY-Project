import com.merzadyan.stock.History;
import com.merzadyan.stock.Stock;
import com.merzadyan.ui.MainWindow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class TestHistory {
    private static final String IMMEDIATE_DIR = "src/test/resources/ser";
    private static final String SERIALISED_FILE_PATH = IMMEDIATE_DIR + "/TestHistory.ser";
    private HashMap<String, ArrayList<Stock>> lastSaved;
    private History history;
    private ArrayList<Stock> list;
    private Stock stock;
    
    @Before
    public void beforeTest() {
        lastSaved = new HashMap<>();
        history = new History();
        stock = new Stock("BAE Systems", "LSE", "LSE");
        stock.setHistogram(new int[]{1, 2, 3, 4, 5});
        stock.setLatestSentimentScore(4);
        stock.setStartDate(LocalDate.parse("2018-03-01"));
        stock.setEndDate(LocalDate.parse("2018-03-08"));
        list = new ArrayList<>();
        list.add(stock);
        
        // Delete file before each test.
        File file = new File(SERIALISED_FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    public void shouldContainStockAfterAdding() {
        // One stock is added in #beforeTest.
        Assert.assertTrue(list.contains(stock));
    }
    
    @Test
    public void shouldNotContainStockAfterRemoving() {
        list = new ArrayList<>();
        Assert.assertFalse(list.contains(stock));
        list.add(stock);
        Assert.assertTrue(list.contains(stock));
        list.remove(stock);
        Assert.assertFalse(list.contains(stock));
    }
    
    @Test
    public void shouldNotContainStockAfterInitialisation() {
        HashMap<String, ArrayList<Stock>> lastSaved = history.getLastSaved();
        Assert.assertFalse(lastSaved.containsKey(stock.getCompany()));
        Assert.assertFalse(lastSaved.containsValue(list));
    }
    
    @Test
    public void serialisedFileShouldBeCreatedAfterSerialisation() {
        File immediateDirs = new File(IMMEDIATE_DIR);
        immediateDirs.mkdirs();
        
        Assert.assertNotNull(lastSaved);
        Assert.assertNotNull(stock.getCompany());
        Assert.assertNotNull(list);
        
        lastSaved.put(stock.getCompany(), list);
        MainWindow.serialise(lastSaved, SERIALISED_FILE_PATH);
        
        File file = new File(SERIALISED_FILE_PATH);
        boolean existsAndNotEmpty = file.exists() && file.length() > 0;
        Assert.assertTrue(existsAndNotEmpty);
    }
    
    @Test
    public void shouldContainStockAfterDeserialisation() {
        File immediateDirs = new File(IMMEDIATE_DIR);
        immediateDirs.mkdirs();
        
        lastSaved.put(stock.getCompany(), list);
        MainWindow.serialise(lastSaved, SERIALISED_FILE_PATH);
        
        File file = new File(SERIALISED_FILE_PATH);
        boolean existsAndNotEmpty = file.exists() && file.length() > 0;
        Assert.assertTrue(existsAndNotEmpty);
        
        // Mock app shutdown by resetting the hash map.
        lastSaved = new HashMap<>();
        
        lastSaved = MainWindow.deserialise(lastSaved, SERIALISED_FILE_PATH);
        Assert.assertTrue(lastSaved.containsKey(stock.getCompany()));
        Assert.assertTrue(lastSaved.containsValue(list));
    }
}
