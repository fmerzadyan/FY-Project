package com.merzadyan;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.apache.log4j.Logger;

/**
 * Provides a method for convenient crawler instantiation and allows data to be passed into the newly created crawler.
 */
public class CrawlerFactory implements CrawlController.WebCrawlerFactory {
    private static final Logger LOGGER = Logger.getLogger(CrawlerFactory.class.getName());
    
    public CrawlerFactory() {
    
    }
    
    @Override
    public WebCrawler newInstance() throws Exception {
        return new Crawler();
    }
}
