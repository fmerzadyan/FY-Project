package com.merzadyan.seed;

import org.apache.log4j.Logger;

import java.util.TreeSet;

public class SeedUrlRegistry {
    private static final Logger LOGGER = Logger.getLogger(SeedUrlRegistry.class.getName());
    
    private static final SeedUrlRegistry instance = new SeedUrlRegistry();
    
    private final TreeSet<SeedUrl> urlSet;
    
    // IMPORTANT NOTE: http:// is required for the url to be valid.
    public static final String TEST_MODE_COMPLEX_URL = "http://merzadyan.com/bae-systems-complex/";
    
    private SeedUrlRegistry() {
        urlSet = new TreeSet<>();
        urlSet.add(new SeedUrl("https://uk.finance.yahoo.com/", SeedUrl.Type.DEFAULT));
        urlSet.add(new SeedUrl("http://www.bbc.co.uk/news/business/markets/europe/lse_ukx", SeedUrl.Type.DEFAULT));
        urlSet.add(new SeedUrl("https://uk.investing.com/indices/uk-100-news", SeedUrl.Type.DEFAULT));
        urlSet.add(new SeedUrl("https://www.economist.com/topics/ftse-100-index", SeedUrl.Type.DEFAULT));
    }
    
    public static SeedUrlRegistry getInstance() {
        return instance;
    }
    
    public void add(SeedUrl url) {
        if (!urlSet.contains(url)) {
            urlSet.add(url);
        }
    }
    
    public void remove(SeedUrl url) {
        if (urlSet.contains(url)) {
            urlSet.remove(url);
        }
    }
    
    public TreeSet<SeedUrl> getUrlSet() {
        return urlSet;
    }
}
