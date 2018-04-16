import com.merzadyan.analyser.SentientAnalyser;
import com.merzadyan.stock.Stock;
import org.ahocorasick.trie.Trie;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeSet;

public class TestSentientAnalyser {
    private static Trie trie;
    
    @BeforeClass
    public static void setup() {
        TreeSet<Stock> set = new TreeSet<>();
        set.add(new Stock("BAE Systems", "BA.", "LSE"));
        set.add(new Stock("Barclays", "BARC", "LSE"));
        ArrayList<String> companyKeys = new ArrayList<>();
        for (Stock stock : set) {
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
    public void shouldFindCompanyWithNGramNameFromText() {
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
    public void shouldFindCompanyWithUniGramNameFromText() {
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
    
    @Test
    public void shouldFindDateFromText() {
        String text = " BAE Systems plc (LON:BA.): Poised For Long-Term Success?\n" +
                "\n" +
                "March 3, 2018\n" +
                "\n" +
                "Since BAE Systems plc (LSE:BA.) released its earnings in December 2017, analysts seem cautiously optimistic, with earnings expected to grow by 34.60% in the upcoming year compared with the past 5-year average growth rate of -0.73%.";
        
        String date = null;
        try {
            date = SentientAnalyser.findSUTime(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Assert.assertEquals("2018-03-03", date);
    }
}
