package com.merzadyan;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerManager {
    public static void main(String[] args) throws Exception {
        // Data dump is located in C:\Users\fmerzadyan\data\crawler4j.
        final String crawlStorageFolder = "/Users/fmerzadyan/data/crawler4j/";
        // TODO: optimal number of crawlers?
        final int numberOfCrawlers = 7;
        
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        // Max depth of crawling is set to infinite depth by default
        // where the seed url is 0 depth and its child url is 1.
        // TODO: max depth for crawling?
        config.setMaxDepthOfCrawling(500);
        // Including https pages is set to false by default.
        config.setIncludeHttpsPages(true);
        // Politeness delay is set to 200 milliseconds by default.
        final int politenessDelay = 200;
        config.setPolitenessDelay(politenessDelay);
        // Including binary content is set to false by default.
        config.setIncludeBinaryContentInCrawling(false);
        // Resumable crawling continue crawling in the event of crawler process timing out.
        // Resumable crawling is set to false by default.
        // TODO: for faster and more convenient test/debugging, set resumable crawling to false.
        // since would not have to delete the data dump for fresh run of program.
        config.setResumableCrawling(true);
        // User agent string represents crawler to the web servers.
        // User agent string is set to "crawler4j (https://github.com/yasserg/crawler4j/)" by default.
        // This user agent string is perfectly succinct at identifying our crawler bots; no reason to change.
        final String userAgentString = "crawler4j (https://github.com/yasserg/crawler4j/)";
        config.setUserAgentString(userAgentString);
        
        /*
         * Instantiate the controller for this crawl.
         */
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
        
        /*
         * For each crawl, you need to add some seed urls. These are the first
         * URLs that are fetched and then the crawler starts following links
         * which are found in these pages
         */
        // TODO: find suitable seed URLs.
        // TODO: seed URL management - e.g. separate class?
        controller.addSeed("https://uk.finance.yahoo.com/");
        
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(Crawler.class, numberOfCrawlers);
    }
}