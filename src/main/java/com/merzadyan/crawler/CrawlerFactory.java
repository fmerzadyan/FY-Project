package com.merzadyan.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.apache.log4j.Logger;

/**
 * Provides a method for convenient crawler instantiation and allows data to be passed into the newly created crawler.
 */
class CrawlerFactory implements CrawlController.WebCrawlerFactory {
    private final CrawlerTerminationListener terminationListener;
    private final Configs configs;
    
    public CrawlerFactory(CrawlerTerminationListener terminationListener, Configs configs) {
        this.terminationListener = terminationListener;
        this.configs = configs;
    }
    
    @Override
    public WebCrawler newInstance() {
        return new Crawler(terminationListener, configs);
    }
}
