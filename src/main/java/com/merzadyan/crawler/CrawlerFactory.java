package com.merzadyan.crawler;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import org.apache.log4j.Logger;

import java.time.LocalDate;

/**
 * Provides a method for convenient crawler instantiation and allows data to be passed into the newly created crawler.
 */
public class CrawlerFactory implements CrawlController.WebCrawlerFactory {
    private static final Logger LOGGER = Logger.getLogger(CrawlerFactory.class.getName());
    private CrawlerTerminationListener terminationListener;
    private LocalDate startDate,
            endDate;
    
    public CrawlerFactory(CrawlerTerminationListener terminationListener, LocalDate startDate, LocalDate endDate) {
        this.terminationListener = terminationListener;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    @Override
    public WebCrawler newInstance() throws Exception {
        return new Crawler(terminationListener, startDate, endDate);
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}
