import com.merzadyan.PolarityScale;
import com.merzadyan.SOIRegistry;
import com.merzadyan.SentientAnalyser;
import com.merzadyan.Stock;
import com.merzadyan.WordRegistry;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.HashSet;

public class TestSentientAnalyser {
    private SOIRegistry soiRegistry;
    private WordRegistry wordRegistry;
    private PolarityScale polarityScale;
    
    private HashSet<String> positiveSentencesDataSet;
    private HashSet<String> negativeSentencesDataSet;
    
    // TODO: Could compare the accuracy to the Recursive Deep Models for Semantic Compositionality
    // Over a Sentiment Treebank.
    
    @BeforeClass
    public void setupOnlyOnce() {
        soiRegistry = SOIRegistry.getInstance();
        wordRegistry = WordRegistry.getInstance();
        
        positiveSentencesDataSet = new HashSet<>();
        // TODO: Populate the word registries - positive and negative - assign polarities to words.
        positiveSentencesDataSet.add("engaging; best; powerful; love; beautiful");
        positiveSentencesDataSet.add("excellent performances; A masterpiece; masterful\n" +
                "film; wonderful movie; marvelous performances");
        positiveSentencesDataSet.add("an amazing performance; wonderful all-ages triumph;\n" +
                "a wonderful movie; most visually stunning");
        positiveSentencesDataSet.add("nicely acted and beautifully shot; gorgeous imagery,\n" +
                "effective performances; the best of the\n" +
                "year; a terrific American sports movie; refreshingly\n" +
                "honest and ultimately touching");
        positiveSentencesDataSet.add("one of the best films of the year; A love for films\n" +
                "shines through each frame; created a masterful\n" +
                "piece of artistry right here; A masterful film from\n" +
                "a master filmmaker,");
        
        negativeSentencesDataSet = new HashSet<>();
        negativeSentencesDataSet.add("bad; dull; boring; fails; worst; stupid; painfully");
        negativeSentencesDataSet.add("worst movie; very bad; shapeless mess; worst\n" +
                "thing; instantly forgettable; complete failure");
        negativeSentencesDataSet.add("for worst movie; A lousy movie; a complete failure;\n" +
                "most painfully marginal; very bad sign");
        negativeSentencesDataSet.add("silliest and most incoherent movie; completely\n" +
                "crass and forgettable movie; just another bad\n" +
                "movie. A cumbersome and cliche-ridden movie;\n" +
                "a humorless, disjointed mess");
        negativeSentencesDataSet.add("A trashy, exploitative, thoroughly unpleasant experience\n" +
                "; this sloppy drama is an empty vessel.;\n" +
                "quickly drags on becoming boring and predictable.;\n" +
                "be the worst special-effects creation of\n" +
                "the year");
    }
    
    @Before
    public void setup() {
        polarityScale = new PolarityScale();
    }
    
    // TODO: testing - positive sentences - most-positive sentences should return most-positive polarities.
    public void isPositive() {
        for (String sentence : positiveSentencesDataSet) {
            // IMPORTANT: SentientAnalyser#analyseePolarity may skip if stock is not in SOI registry. // TODO: circumnavigate this issue.
            Pair<Stock, PolarityScale> pair = SentientAnalyser.analysePolarity(sentence, soiRegistry,
                    wordRegistry, polarityScale);
            
            // Assert that result is not null.
            Assert.assertNotNull(pair);
            Assert.assertNotNull(pair.getKey());
            Assert.assertNotNull(pair.getValue());
            
            // TODO: decide how to measure the positivity/negativity e.g. negative = 0 or -5 or -2, etc?
            // TODO: assert that the sentence is positive.
        }
    }
    
    // TODO: testing - negative sentences - most-negative sentences should return most-negative polarities.
    
    // TODO: testing - negating positive sentences.
    
    // TODO: testing - negative negative sentences.
}
