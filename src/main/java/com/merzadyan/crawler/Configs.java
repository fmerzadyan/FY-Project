package com.merzadyan.crawler;

import java.time.LocalDate;

class Configs {
    private LocalDate startDate,
            endDate;
    private int maxCrawledPages;
    
    public Configs(LocalDate startDate, LocalDate endDate, int maxCrawledPages) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxCrawledPages = maxCrawledPages;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public int getMaxCrawledPages() {
        return maxCrawledPages;
    }
    
    public void setMaxCrawledPages(int maxCrawledPages) {
        this.maxCrawledPages = maxCrawledPages;
    }
}
