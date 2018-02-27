package com.merzadyan;

public class Stock {
    private String company;
    private String symbol;
    private String stockExchange;
    
    public Stock() {
    
    }
    
    public Stock(String company, String symbol, String stockExchange) {
        this.company = company;
        this.symbol = symbol;
        this.stockExchange = stockExchange;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || object instanceof Stock) {
            return false;
        }
        
        return this.symbol.equals(((Stock) object).symbol);
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    
    public String getStockExchange() {
        return stockExchange;
    }
    
    public void setStockExchange(String stockExchange) {
        this.stockExchange = stockExchange;
    }
}
