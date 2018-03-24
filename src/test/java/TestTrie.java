import com.merzadyan.Trie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestTrie {
    private static String[] keys;
    
    @BeforeClass
    public static void setup() {
        keys = new String[]{"and", "app", "append", "apple", "appoint", "appointment",
                "attain", "attribute"};
    }
    
    @Test
    public void keysPresentInTrie() {
        Trie trie = new Trie();
        for (String key : keys) {
            trie.insert(key);
        }
        
        Assert.assertTrue(trie.search("attain"));
        Assert.assertTrue(trie.search("appoint"));
    }
    
    @Test
    public void keysNotPresentInTrie() {
        Trie trie = new Trie();
        for (String key : keys) {
            trie.insert(key);
        }
        
        Assert.assertFalse(trie.search("appendix"));
        Assert.assertFalse(trie.search("atone"));
    }
    
}
