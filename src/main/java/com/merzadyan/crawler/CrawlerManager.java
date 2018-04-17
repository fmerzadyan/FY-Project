package com.merzadyan.crawler;

import com.merzadyan.seed.SeedUrl;
import com.merzadyan.seed.SeedUrlRegistry;
import com.merzadyan.stock.DateCategoriser;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.util.HashSet;

import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_CRAWL_STORAGE_FOLDER;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_INCLUDE_HTTPS_PAGES;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_INTERVAL;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_MAX_DEPTH_OF_CRAWLING;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_NUMBER_OF_CRAWLERS;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_POLITENESS_DELAY;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_RESUMABLE_CRAWLING;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_TEST;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_TEST_MODE;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_USER_AGENT_STRING;

@SuppressWarnings("unchecked")
public class CrawlerManager {
    public static class DEFAULT {
        public static final String DEFAULT_INTERVAL = new DateCategoriser(null).extractIntervals()[0];
        public static final String DEFAULT_USER_AGENT_STRING = "crawler4j (https://github.com/yasserg/crawler4j/)";
        public static final String DEFAULT_CRAWL_STORAGE_FOLDER = "/Users/fmerzadyan/data/crawler4j/";
        
        public static final int DEFAULT_NUMBER_OF_CRAWLERS = 8;
        public static final int DEFAULT_MAX_DEPTH_OF_CRAWLING = 50;
        public static final int DEFAULT_POLITENESS_DELAY = 200;
        
        public static final boolean DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING = false;
        public static final boolean DEFAULT_INCLUDE_HTTPS_PAGES = true;
        public static final boolean DEFAULT_RESUMABLE_CRAWLING = false;
        
        public static final boolean DEFAULT_TEST = true;
        public static final String DEFAULT_TEST_MODE = MODE.TEST_MODE_COMPLEX;
    }
    
    /**
     * IMPORTANT MODE: for testing, debugging and presentation purposes.
     */
    public static class MODE {
        public static final String TEST_MODE_COMPLEX = "TEST_MODE_COMPLEX";
    }
    
    private static final Logger LOGGER = Logger.getLogger(CrawlerManager.class.getName());
    
    private CrawlConfig crawlConfig;
    
    private CrawlController controller;
    private CrawlerFactory crawlerFactory;
    
    private SeedUrl.Option seedUrlOption;
    private HashSet<String> seedUrlSet;
    private boolean test;
    private String testMode;
    
    private String interval;
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
        
        crawlerFactory = new CrawlerFactory(terminationListener, null, null);
        
        seedUrlSet = new HashSet<>();
        interval = DEFAULT_INTERVAL;
        userAgentString = DEFAULT_USER_AGENT_STRING;
        // Data dump is located in C:\Users\fmerzadyan\data\crawler4j.
        crawlStorageFolder = DEFAULT_CRAWL_STORAGE_FOLDER;
        
        numberOfCrawlers = DEFAULT_NUMBER_OF_CRAWLERS;
        maxDepthOfCrawling = DEFAULT_MAX_DEPTH_OF_CRAWLING;
        politenessDelay = DEFAULT_POLITENESS_DELAY;
        
        includeBinaryContentInCrawling = DEFAULT_INCLUDE_BINARY_CONTENT_IN_CRAWLING;
        includeHttpsPages = DEFAULT_INCLUDE_HTTPS_PAGES;
        resumableCrawling = DEFAULT_RESUMABLE_CRAWLING;
        
        test = DEFAULT_TEST;
        testMode = DEFAULT_TEST_MODE;
    }
    
    public void startNonBlockingCrawl() throws Exception {
        String[] intervalParts = interval.split(DateCategoriser.INTERVAL_DELIMITER);
        LocalDate startDate = LocalDate.parse(intervalParts[0].trim()),
                endDate = LocalDate.parse(intervalParts[1].trim());
        crawlerFactory.setStartDate(startDate);
        crawlerFactory.setEndDate(endDate);
        
        crawlConfig.setCrawlStorageFolder(crawlStorageFolder);
        // Max depth of crawling is set to infinite depth by default
        // where the seed url is 0 depth and its child url is 1.
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
        
        if (test && testMode != null && !testMode.isEmpty()) {
            LOGGER.debug("Test mode: " + testMode);
            switch (testMode) {
                case MODE.TEST_MODE_COMPLEX:
                    LOGGER.debug("Adding " + SeedUrlRegistry.TEST_MODE_COMPLEX_URL + " as a seed url.");
                    controller.addSeed(SeedUrlRegistry.TEST_MODE_COMPLEX_URL);
                    break;
                default:
                    /*
                     * For each crawl, you need to add some seed urls. These are the first
                     * URLs that are fetched and then the crawler starts following links
                     * which are found in these pages
                     */
                    LOGGER.debug("Test mode invalid - using default configs.");
                    for (SeedUrl url : SeedUrlRegistry.getInstance().getUrlSet()) {
                        if (url.getType() == SeedUrl.Type.DEFAULT) {
                            controller.addSeed(url.getUrl());
                        }
                    }
                    break;
            }
        } else {
            for (SeedUrl url : SeedUrlRegistry.getInstance().getUrlSet()) {
                switch (seedUrlOption) {
                    case DEFAULT_ONLY:
                        if (url.getType() == SeedUrl.Type.DEFAULT) {
                            LOGGER.debug("#startNonBlockingCrawl: adding default seed URL: " + url.getUrl());
                            controller.addSeed(url.getUrl());
                        }
                        break;
                    case CUSTOM_ONLY:
                        if (url.getType() == SeedUrl.Type.USER_DEFINED) {
                            LOGGER.debug("#startNonBlockingCrawl: adding custom seed URL: " + url.getUrl());
                            controller.addSeed(url.getUrl());
                        }
                        break;
                    case BOTH:
                        LOGGER.debug("#startNonBlockingCrawl: adding " + url.getType() + " seed URL: " + url.getUrl());
                        controller.addSeed(url.getUrl());
                        break;
                    default:
                        // Should not be the case that it gets called ever.
                        break;
                }
            }
        }
        
        LOGGER.debug("#startNonBlockingCrawl. " + "\n" +
                "Configs: " + "\n" +
                "process interval: " + interval + "\n" +
                " user agent name: " + userAgentString + "\n" +
                " data dump: " + crawlStorageFolder + "\n" +
                " number of crawlers: " + numberOfCrawlers + "\n" +
                " max depth of crawling: " + maxDepthOfCrawling + "\n" +
                " politeness delay (ms): " + politenessDelay + "\n" +
                " include HTTPs pages: " + includeHttpsPages + "\n" +
                " include binary content crawling: " + includeBinaryContentInCrawling + "\n" +
                " resumable crawling: " + resumableCrawling + "\n" +
                " enable test mode: " + test + "\n" +
                " test mode: " + testMode + "\n"
        );
        
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
    
    public void setSeedUrlOption(SeedUrl.Option seedUrlOption) {
        this.seedUrlOption = seedUrlOption;
    }
    
    public SeedUrl.Option getSeedUrlOption() {
        return seedUrlOption;
    }
    
    public void addSeedUrl(String seedUrl) {
        seedUrlSet.add(seedUrl);
    }
    
    public boolean removeSeedUrl(String seedUrl) {
        return seedUrlSet != null && seedUrlSet.contains(seedUrl) && seedUrlSet.remove(seedUrl);
        
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
    
    public void setSeedUrlSet(HashSet<String> seedUrlSet) {
        this.seedUrlSet = seedUrlSet;
    }
    
    public HashSet<String> getSeedUrlSet() {
        return seedUrlSet;
    }
    
    public boolean isTest() {
        return test;
    }
    
    public void setTest(boolean test) {
        this.test = test;
    }
    
    public String getTestMode() {
        return testMode;
    }
    
    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }
    
    public void setInterval(String interval) {
        this.interval = interval;
    }
    
    public String getInterval() {
        return interval;
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
        if (numberOfCrawlers > 0) {
            this.numberOfCrawlers = numberOfCrawlers;
        }
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
}