import com.merzadyan.SOIRegistry;
import com.merzadyan.SentientAnalyser;
import com.merzadyan.Stock;
import org.ahocorasick.trie.Trie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * IMPORTANT NOTE: pass/fail results are dependent on soi.txt contents. If the organisation is not in soi.txt
 * and soi.txt is not empty then it will return false even if the input text has an organisation. which otherwise,
 * that would pass the test.
 */
public class TestSentientAnalyser {
    private static Trie trie;
    
    @BeforeClass
    public static void setup() {
        TreeSet<Stock> stockSet = SOIRegistry.getInstance().getStockSet();
        ArrayList<String> companyKeys = new ArrayList<>();
        for (Stock stock : stockSet) {
            if (stock != null) {
                // NOTE: case does not matter when processed; case is ignored as stated in trie construction.
                if (stock.getCompany() != null && !stock.getCompany().isEmpty()) {
                    companyKeys.add(stock.getCompany());
                }
            }
        }
        
        // See https://github.com/robert-bor/aho-corasick
        trie = Trie.builder()
                // IMPORTANT: ignoreCase() must be called before adding keywords.
                .ignoreCase()
                .addKeywords(companyKeys)
                .build();
    }
    
    /**
     * Test identification of a N-gram organisation entity type.
     */
    @Test
    public void identifyOrganisationEntityNGram() {
        String text = "https://uk.finance.yahoo.com/news/rheinmetall-beats-bae-2-5-085503648.html\n" +
                "\n" +
                "FRANKFURT (Reuters) - Germany's Rheinmetall (RHMG.DE) beat BAE Systems (BAES.L) to a $2.5 billion (1.79 billion pounds) Australian order for armoured reconnaissance vehicles, which comes on top of an existing major contract with the country for utility vehicles.\n" +
                "\n" +
                "The Australian defence ministry will now enter exclusive final negotiations with Rheinmetall, the company said in a statement on Wednesday.\n" +
                "\n" +
                "If the contract is finalised, the 211 Boxer vehicles will be built by Australian workers, using Australian steel and will create up to 1,450 jobs, Prime Minister Malcolm Turnbull and the defence ministry said in a separate statement.\n" +
                "\n" +
                "Rheinmetall estimated the project size at around A$3.15 billion ($2.5 billion).\n" +
                "\n" +
                "Deliveries will likely start in 2019 and run until 2026, safeguarding its business in Australia after its existing A$1.58 billion order for logistical vehicles, won in 2013, runs out at the end of the decade.\n" +
                "\n" +
                "Rheinmetall shares were up 3 percent at the top of the MDAX index (.MDAXI) at market open.";
        
        String organisation = null;
        try {
            organisation = SentientAnalyser.identifyOrganisationEntity(text, trie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals("BAE Systems", organisation);
    }
    
    /**
     * Test identification of a N-gram organisation entity type.
     */
    @Test
    public void identifyOrganisationEntityUniGram() {
        String text = "Amid the consumer privacy revelations, Barclays is receiving more and more pressure " +
                "to explain themselves.";
        
        String organisation = null;
        try {
            organisation = SentientAnalyser.identifyOrganisationEntity(text, trie);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Assert.assertEquals("Barclays", organisation);
    }
}
