package com.merzadyan;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import javafx.util.Pair;
import org.ahocorasick.trie.Trie;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class Crawler extends WebCrawler {
    private static final Logger LOGGER = Logger.getLogger(Crawler.class.getName());
    
    private int linksVisited;
    
    private SOIRegistry soiRegistry;
    private WordRegistry wordRegistry;
    private HashSet<TrendIndicator> trendIndicatorHashSet;
    private HashSet<Forecast> forecastHashSet;
    
    private Trie trie;
    
    /**
     * List of file extensions to filter out urls which are non-text, non-readable resources.
     */
    // TODO: addIfNotZero more filters.
    private static final Pattern FILTERS = compile(".*(\\.(css|js|gif|jpg"
            + "|png|mp3|mp4|zip|gz))$");
    
    Crawler() {
        linksVisited = 0;
        soiRegistry = SOIRegistry.getInstance();
        wordRegistry = WordRegistry.getInstance();
        trendIndicatorHashSet = new HashSet<>();
        forecastHashSet = new HashSet<>();
        constructTrie();
    }
    
    /**
     * This method receives two parameters. The first parameter is the page
     * in which we have discovered this new url and the second parameter is
     * the new url. You should implement this function to specify whether
     * the given url should be crawled or not (based on your crawling logic).
     * In this example, we are instructing the crawler to ignore urls that
     * have css, js, git, ... extensions and to only accept urls that start
     * with "http://www.ics.uci.edu/". In this case, we didn't need the
     * referringPage parameter to make the decision.
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        // TODO: narrow the conditions for selecting pages to visit and process for optimisation.
        // e.g. select specific companies, indexes, stock exchanges, countries, regions, etc.
        
        // TODO: feed the text from this web page into the algorithm. The text is compared to the patterns to match.
        // the text hits up/matches the keys in the trie then process to visit that page...
        // TODO: benchmark testing to see performance...
        
        String href = url.getURL().toLowerCase();
        boolean matchesFilter = FILTERS.matcher(href).matches();
        
        // Return false if the URL extension does not comply with the filters.
        if (!matchesFilter) {
            return false;
        }
        
        // Only perform Aho-Corasick on non-seed URLs.
        if (linksVisited >= 1) {
            // Use Aho-Corasick to further filter which pages to ones containing info about select-companies.
            if (referringPage.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) referringPage.getParseData();
                Document document = Jsoup.parseBodyFragment(htmlParseData.getHtml());
                
                if (trie != null) {
                    return trie.firstMatch(document.text()) != null;
                }
            }
        }
        
        // Method defaults to returning false.
        return false;
    }
    
    private void constructTrie() {
        SOIRegistry soiRegistry = SOIRegistry.getInstance();
        if (soiRegistry == null) {
            LOGGER.debug("SOIRegistry is null.");
            return;
        }
        
        HashSet<Stock> stockSet = soiRegistry.getStockSet();
        ArrayList<String> companyKeys = new ArrayList<>();
        for (Stock stock : stockSet) {
            if (stock != null) {
                // NOTE: case does not matter when processed; case is ignored as stated in trie construction.
                if (stock.getCompany() != null && !stock.getCompany().isEmpty()) {
                    companyKeys.add(stock.getCompany());
                }
                if (stock.getSymbol() != null && !stock.getSymbol().isEmpty()) {
                    companyKeys.add(stock.getSymbol());
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
     * This function is called when a page is fetched and ready
     * to be processed by your program.
     */
    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        // ++linksVisited has a prefix operation - increment variable and get value.
        LOGGER.debug("links visited: " + ++linksVisited + " URL: " + url);
        
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            String html = htmlParseData.getHtml();
            Document document = Jsoup.parseBodyFragment(html);
            Set<WebURL> links = htmlParseData.getOutgoingUrls();
            
            LOGGER.debug("Number of outgoing links: " + links.size());
            LOGGER.debug("Text length: " + document.text().length());
            // LOGGER.debug(document.text());
            
            Pair<Stock, PolarityScale> pair = SentientAnalyser.analysePolarity(document.text(), soiRegistry, wordRegistry,
                    new PolarityScale());
            if (pair != null && pair.getKey() != null && pair.getValue() != null) {
                TrendIndicator list = new TrendIndicator(pair.getKey(), new ArrayList<>());
                list.addIfNotZero(pair.getValue().getPolarity());
                trendIndicatorHashSet.add(list);
            }
        }
        
        // TODO: remove pesky/unnecessary logs in the console output from crawler4j and Stanford CoreNLP.
    }
    
    /**
     * Called just before the termination of the current
     * crawler instance. It can be used for persisting in-memory data or other
     * finalization tasks.
     */
    @Override
    public void onBeforeExit() {
        // TODO: display forecastHashSet.
        for (TrendIndicator trendIndicator : trendIndicatorHashSet) {
            if (trendIndicator != null) {
                forecastHashSet.add(trendIndicator.getForecast());
            }
        }
    }
    
    @Override
    protected void onPageBiggerThanMaxSize(String urlStr, long pageSize) {
    
    }
}