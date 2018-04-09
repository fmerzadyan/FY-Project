package com.merzadyan;

import java.util.Comparator;

public class Stock implements Comparator, Comparable {
    private String company;
    private String symbol;
    private String stockExchange;
    /**
     * Default sentiment score is -1 indicating that it has not been updated with a sentiment score yet.
     * Calculated by extracting the mode value of sentiment scores for this stock.
     */
    private int latestSentimentScore;
    /**
     * Default values is {-1, -1, -1, -1, -1} indicating it has been updated with sentiment scores yet.
     */
    private int[] histogram;
    
    public Stock() {
    
    }
    
    public Stock(String company, String symbol, String stockExchange) {
        this.company = company;
        this.symbol = symbol;
        this.stockExchange = stockExchange;
        latestSentimentScore = -1;
        histogram = new int[]{-1, -1, -1, -1, -1};
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        Stock s1 = (Stock) o1, s2 = (Stock) o2;
        
        return s1.company.compareToIgnoreCase(s2.company);
    }
    
    @Override
    public int compareTo(Object o) {
        return company.compareToIgnoreCase(((Stock) o).company);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        
        if (!(object instanceof Stock)) {
            return false;
        }
        
        Stock comparee = (Stock) object;
        return latestSentimentScore == comparee.latestSentimentScore &&
                (company == null ? comparee.company == null : company.equals(comparee.company)) &&
                (symbol == null ? comparee.symbol == null : symbol.equals(comparee.symbol)) &&
                (stockExchange == null ? comparee.stockExchange == null : stockExchange.equals(comparee.stockExchange));
    }
    
    @Override
    public int hashCode() {
        // See Josh Bloch's Effective Java for reference.
        // Arbitrary prime number.
        int result = 23;
        
        result = 37 * result + latestSentimentScore;
        result = 37 * result + (company == null ? 0 : company.hashCode());
        result = 37 * result + (symbol == null ? 0 : symbol.hashCode());
        result = 37 * result + (stockExchange == null ? 0 : stockExchange.hashCode());
        
        return result;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        if (company != null) {
            this.company = company.trim();
            return;
        }
        this.company = null;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        if (symbol != null) {
            this.symbol = symbol.trim();
            return;
        }
        this.symbol = null;
    }
    
    public String getStockExchange() {
        return stockExchange;
    }
    
    public void setStockExchange(String stockExchange) {
        if (stockExchange != null) {
            this.stockExchange = stockExchange.trim();
            return;
        }
        this.stockExchange = null;
    }
    
    public void setLatestSentimentScore(int sentimentScore) {
        this.latestSentimentScore = sentimentScore;
    }
    
    public int getLatestSentimentScore() {
        return latestSentimentScore;
    }
    
    public void setHistogram(int[] histogram) {
        this.histogram = histogram;
    }
    
    public int[] getHistogram() {
        return histogram;
    }
}
