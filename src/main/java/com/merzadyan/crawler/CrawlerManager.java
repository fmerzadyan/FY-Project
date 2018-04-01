package com.merzadyan.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.log4j.Logger;

import static com.merzadyan.crawler.CrawlerManager.DEFAULT.*;

public class CrawlerManager {
    public static class DEFAULT {
        public static final String DEFAULT_USER_AGENT_STRING = "crawler4j (https://github.com/yasserg/crawler4j/)";
        public static final String DEFAULT_CRAWL_STORAGE_FOLDER = "/Users/fmerzadyan/data/crawler4j/";
        
        public static final int DEFAULT_NUMBER_OF_CRAWLERS = 2;
        public static final int DEFAULT_MAX_DEPTH_OF_CRAWLING = 50;
        public static final int DEFAULT_POLITENESS_DELAY = 1000;
        
        public static final boolean DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING = false;
        public static final boolean DEFAULT_INCLUDE_HTTPS_PAGES = true;
        public static final boolean DEFAULT_RESUMABLE_CRAWLING = true;
    }
    
    private static final Logger LOGGER = Logger.getLogger(CrawlerManager.class.getName());
    
    private CrawlConfig crawlConfig;
    
    private CrawlController controller;
    private CrawlerFactory crawlerFactory;
    
    private String userAgentString;
    private String crawlStorageFolder;
    
    private int numberOfCrawlers;
    private int maxDepthOfCrawling;
    private int politenessDelay;
    
    private boolean includeHttpsPages;
    private boolean includeBinaryContentInCrawling;
    private boolean resumableCrawling;
    
    public CrawlerManager(CrawlerTerminationListener terminationListener) {
        crawlConfig = new CrawlConfig();
        
        crawlerFactory = new CrawlerFactory(terminationListener);
        
        userAgentString = DEFAULT_USER_AGENT_STRING;
        // Data dump is located in C:\Users\fmerzadyan\data\crawler4j.
        crawlStorageFolder = DEFAULT_CRAWL_STORAGE_FOLDER;
        
        numberOfCrawlers = DEFAULT_NUMBER_OF_CRAWLERS;
        maxDepthOfCrawling = DEFAULT_MAX_DEPTH_OF_CRAWLING;
        politenessDelay = DEFAULT_POLITENESS_DELAY;
        
        includeBinaryContentInCrawling = DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING;
        includeHttpsPages = DEFAULT_INCLUDE_HTTPS_PAGES;
        resumableCrawling = DEFAULT_RESUMABLE_CRAWLING;
    }
    
    public void startNonBlockingCrawl() throws Exception {
        
        crawlConfig.setCrawlStorageFolder(crawlStorageFolder);
        // Max depth of crawling is set to infinite depth by default
        // where the seed url is 0 depth and its child url is 1.
        // TODO: max depth for crawling?
        crawlConfig.setMaxDepthOfCrawling(maxDepthOfCrawling);
        // Including https pages is set to false by default.
        crawlConfig.setIncludeHttpsPages(includeHttpsPages);
        // Politeness delay is set to 200 milliseconds by default.
        crawlConfig.setPolitenessDelay(politenessDelay);
        // Including binary content is set to false by default.
        crawlConfig.setIncludeBinaryContentInCrawling(includeBinaryContentInCrawling);
        // Resumable crawling continue crawling in the event of crawler process timing out.
        // Resumable crawling is set to false by default.
        // since would not have to delete the data dump for fresh run of program.
        crawlConfig.setResumableCrawling(resumableCrawling);
        // User agent string represents crawler to the web servers.
        // User agent string is set to "crawler4j (https://github.com/yasserg/crawler4j/)" by default.
        // This user agent string is perfectly succinct at identifying our crawler bots; no reason to change.
        crawlConfig.setUserAgentString(userAgentString);
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(crawlConfig);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
        
        /*
         * For each crawl, you need to addIfNotZero some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        // TODO: find suitable seed URLs.
        // TODO: seed URL management - e.g. separate class?
        controller.addSeed("https://uk.finance.yahoo.com/");
        controller.addSeed("http://www.bbc.co.uk/news/business/markets/europe/lse_ukx");
        controller.addSeed("https://uk.investing.com/indices/uk-100-news");
        controller.addSeed("https://www.economist.com/topics/ftse-100-index");
        
        // A crawler factory is required to feed data into the crawler.
        // Runs the crawlers in a non-blocking thread.
        controller.startNonBlocking(crawlerFactory, numberOfCrawlers);
    }
    
    public void stopCrawl() {
        if (controller == null) {
            return;
        }
        
        // Shuts down the crawlers.
        controller.shutdown();
        controller.waitUntilFinish();
    }
    
    public CrawlConfig getCrawlConfig() {
        return crawlConfig;
    }
    
    public void setCrawlConfig(CrawlConfig crawlConfig) {
        this.crawlConfig = crawlConfig;
    }
    
    public CrawlController getController() {
        return controller;
    }
    
    public void setController(CrawlController controller) {
        this.controller = controller;
    }
    
    public CrawlerFactory getCrawlerFactory() {
        return crawlerFactory;
    }
    
    public void setCrawlerFactory(CrawlerFactory crawlerFactory) {
        this.crawlerFactory = crawlerFactory;
    }
    
    public String getUserAgentString() {
        return userAgentString;
    }
    
    public void setUserAgentString(String userAgentString) {
        this.userAgentString = userAgentString;
    }
    
    public String getCrawlStorageFolder() {
        return crawlStorageFolder;
    }
    
    public void setCrawlStorageFolder(String crawlStorageFolder) {
        this.crawlStorageFolder = crawlStorageFolder;
    }
    
    public int getNumberOfCrawlers() {
        return numberOfCrawlers;
    }
    
    public void setNumberOfCrawlers(int numberOfCrawlers) {
        this.numberOfCrawlers = numberOfCrawlers;
    }
    
    public int getMaxDepthOfCrawling() {
        return maxDepthOfCrawling;
    }
    
    public void setMaxDepthOfCrawling(int maxDepthOfCrawling) {
        this.maxDepthOfCrawling = maxDepthOfCrawling;
    }
    
    public int getPolitenessDelay() {
        return politenessDelay;
    }
    
    public void setPolitenessDelay(int politenessDelay) {
        this.politenessDelay = politenessDelay;
    }
    
    public boolean isIncludeHttpsPages() {
        return includeHttpsPages;
    }
    
    public void setIncludeHttpsPages(boolean includeHttpsPages) {
        this.includeHttpsPages = includeHttpsPages;
    }
    
    public boolean isIncludeBinaryContentInCrawling() {
        return includeBinaryContentInCrawling;
    }
    
    public void setIncludeBinaryContentInCrawling(boolean includeBinaryContentInCrawling) {
        this.includeBinaryContentInCrawling = includeBinaryContentInCrawling;
    }
    
    public boolean isResumableCrawling() {
        return resumableCrawling;
    }
    
    public void setResumableCrawling(boolean resumableCrawling) {
        this.resumableCrawling = resumableCrawling;
    }
    
    public static void main(String[] args) {
        CrawlerManager crawlerManager = new CrawlerManager(null);
        try {
            crawlerManager.startNonBlockingCrawl();
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }
}