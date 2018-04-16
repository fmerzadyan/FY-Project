import org.ahocorasick.trie.Trie;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestAhoCorasickAlgorithm {
    private Trie trie;
    
    @Before
    public void beforeTest() {
        String[] keywords = new String[]{
                "Barclays",
                "GlaxoSmithKline",
                "Vodafone"
        };
        
        trie = Trie.builder()
                // IMPORTANT: ignoreCase() must be called before adding keywords.
                .ignoreCase()
                .addKeywords(keywords)
                .build();
    }
    
    @Test
    public void trieShouldContainKeywords() {
        Assert.assertTrue(trie.containsMatch("Barclays"));
        Assert.assertTrue(trie.containsMatch("GlaxoSmithKline"));
        Assert.assertTrue(trie.containsMatch("Vodafone"));
    }
    
    @Test
    public void trieShouldNotContainKeywords() {
        Assert.assertFalse(trie.containsMatch("Barclay"));
        Assert.assertFalse(trie.containsMatch("Barclayd"));
        Assert.assertFalse(trie.containsMatch("Tesco"));
    }
    
    @Test
    public void trieShouldIgnoreCase() {
        Assert.assertTrue(trie.containsMatch("bArClaYs"));
        Assert.assertTrue(trie.containsMatch("GlaXoSmithKlINe"));
        Assert.assertTrue(trie.containsMatch("VodafoNE"));
    }
    
    @Test
    public void trieShouldContainKeywordsWhenRandomTextBuffer() {
        Assert.assertTrue(trie.containsMatch("Tax payers money is used to bail out Barclays bank amid economic " +
                "crisis."));
        Assert.assertTrue(trie.containsMatch("Decades worth of research produces new methods of mass producing " +
                "psycho-active drugs. This research comes from pharmaceutical giant GlaxoSmithKline."));
        Assert.assertTrue(trie.containsMatch("Vodafone to go into the phone manufacturing industry."));
    }
}
