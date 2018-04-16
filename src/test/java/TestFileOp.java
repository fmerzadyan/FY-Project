import com.merzadyan.FileOp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestFileOp {
    private final String FILE_PATH = "src/test/resources/TestFileOp.txt";
    
    @Before
    public void beforeTest() {
        // Delete file before each test.
        File file = new File(FILE_PATH);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Test
    public void shouldNotBeNullOrEmpty() {
        Assert.assertFalse(FileOp.isNullOrEmpty(FILE_PATH));
    }
    
    @Test
    public void shouldBeNullOrEmpty() {
        String text = null;
        Assert.assertTrue(FileOp.isNullOrEmpty(text));
        text = "";
        Assert.assertTrue(FileOp.isNullOrEmpty(text));
    }
    
    @Test
    public void fileShouldExist() {
        File file = new File(FILE_PATH);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(FileOp.isFile(FILE_PATH));
    }
    
    @Test
    public void shouldBeEmpty() {
        File file = new File(FILE_PATH);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(FileOp.isEmptyFile(FILE_PATH));
    }
    
    @Test
    public void fileShouldNotExist() {
        String path = "src/test/resources/fileShouldNotExist.txt";
        Assert.assertFalse(FileOp.isFile(path));
    }
    
    @Test
    public void shouldOnlyMatchFilesNotDirectories() {
        String dir = "src/test/resources/shouldOnlyMatchFilesNotDirectoriesDir";
        File file = new File(dir);
        file.mkdirs();
        Assert.assertFalse(FileOp.isFile(dir));
        file.deleteOnExit();
    }
}
