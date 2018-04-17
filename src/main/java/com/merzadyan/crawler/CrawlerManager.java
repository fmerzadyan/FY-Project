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
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_INCLUDE_HTTPS_PAGES;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_INTERVAL;
import static com.merzadyan.crawler.CrawlerManager.DEFAULT.DEFAULT_MAX_CRAWLED_PAGES;
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
        // user.home returns the user's home directory on Windows using backslashes - to maintain pattern, \\ are used
        // in the appending string.
        public static final String DEFAULT_CRAWL_STORAGE_FOLDER = System.getProperty("user.home") +"\\data\\crawler4j";
        
        public static final int DEFAULT_NUMBER_OF_CRAWLERS = 8;
        public static final int DEFAULT_MAX_DEPTH_OF_CRAWLING = 50;
        public static final int DEFAULT_MAX_CRAWLED_PAGES = 1000;
        public static final int DEFAULT_POLITENESS_DELAY = 200;
        
        public static final boolean DEFAULT_INCLUDE_HTTPS_PAGES = true;
        public static final boolean DEFAULT_RESUMABLE_CRAWLING = false;
        // IMPORTANT: enable/disable in-testing feature.
        public static final boolean DEFAULT_TEST = false;
        public static final String DEFAULT_TEST_MODE = MODE.TEST_MODE_COMPLEX;
    }
    
    /**
     * IMPORTANT MODE: for testing, debugging and presentation purposes.
     */
    public static class MODE {
        public static final String TEST_MODE_COMPLEX = "TEST_MODE_COMPLEX";
    }
    
    private static final Logger LOGGER = Logger.getLogger(CrawlerManager.class.getName());
    
    private final CrawlConfig crawlConfig;
    
    private CrawlController controller;
    private final CrawlerFactory crawlerFactory;
    private final Configs configs;
    
    private SeedUrl.Option seedUrlOption;
    private boolean test;
    private String testMode;
    
    private String interval;
    private String userAgentString;
    private String crawlStorageFolder;
    
    private int numberOfCrawlers;
    private int maxDepthOfCrawling;
    private int maxCrawledPages;
    private int politenessDelay;
    
    private boolean includeHttpsPages;
    private boolean resumableCrawling;
    
    public CrawlerManager(CrawlerTerminationListener terminationListener) {
        crawlConfig = new CrawlConfig();
        configs = new Configs(null, null, DEFAULT_MAX_CRAWLED_PAGES);
        crawlerFactory = new CrawlerFactory(terminationListener, configs);
        
        interval = DEFAULT_INTERVAL;
        userAgentString = DEFAULT_USER_AGENT_STRING;
        crawlStorageFolder = DEFAULT_CRAWL_STORAGE_FOLDER;
        
        numberOfCrawlers = DEFAULT_NUMBER_OF_CRAWLERS;
        maxDepthOfCrawling = DEFAULT_MAX_DEPTH_OF_CRAWLING;
        maxCrawledPages = DEFAULT_MAX_CRAWLED_PAGES;
        politenessDelay = DEFAULT_POLITENESS_DELAY;
        
        includeHttpsPages = DEFAULT_INCLUDE_HTTPS_PAGES;
        resumableCrawling = DEFAULT_RESUMABLE_CRAWLING;
        
        test = DEFAULT_TEST;
        testMode = DEFAULT_TEST_MODE;
    }
    
    public void startNonBlockingCrawl() {
        String[] intervalParts = interval.split(DateCategoriser.INTERVAL_DELIMITER);
        LocalDate startDate = LocalDate.parse(intervalParts[0].trim()),
                endDate = LocalDate.parse(intervalParts[1].trim());
        configs.setStartDate(startDate);
        configs.setEndDate(endDate);
        configs.setMaxCrawledPages(maxCrawledPages);
        
        crawlConfig.setCrawlStorageFolder(crawlStorageFolder);
        // Max depth of crawling is set to infinite depth by default
        // where the seed url is 0 depth and its child url is 1.
        crawlConfig.setMaxDepthOfCrawling(maxDepthOfCrawling);
        // Including https pages is set to false by default.
        crawlConfig.setIncludeHttpsPages(includeHttpsPages);
        // Politeness delay is set to 200 milliseconds by default.
        crawlConfig.setPolitenessDelay(politenessDelay);
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
        try {
            controller = new CrawlController(crawlConfig, pageFetcher, robotstxtServer);
        } catch (Exception e) {
            LOGGER.fatal("The crawlers configuration is incorrect. The problem may be due to the data dump location.");
            e.printStackTrace();
            return;
        }
        
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
            if (seedUrlOption == null) {
                // NOTE HACK: in case that seed url option is not set - use default option.
                seedUrlOption = SeedUrl.Option.DEFAULT_ONLY;
            }
            
            LOGGER.debug("#startNonBlockingCrawl: seed URL option: " + seedUrlOption);
            
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
    
    public void setTest(boolean test) {
        this.test = test;
    }
    
    public void setTestMode(String testMode) {
        this.testMode = testMode;
    }
    
    public void setInterval(String interval) {
        this.interval = interval;
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
    
    public void setNumberOfCrawlers(int numberOfCrawlers) {
        if (numberOfCrawlers > 0) {
            this.numberOfCrawlers = numberOfCrawlers;
        }
    }
    
    public void setMaxDepthOfCrawling(int maxDepthOfCrawling) {
        this.maxDepthOfCrawling = maxDepthOfCrawling;
    }
    
    public void setMaxCrawledPages(int maxCrawledPages) {
        this.maxCrawledPages = maxCrawledPages;
    }
    
    public void setPolitenessDelay(int politenessDelay) {
        this.politenessDelay = politenessDelay;
    }
    
    public void setIncludeHttpsPages(boolean includeHttpsPages) {
        this.includeHttpsPages = includeHttpsPages;
    }
    
    public void setResumableCrawling(boolean resumableCrawling) {
        this.resumableCrawling = resumableCrawling;
    }
}