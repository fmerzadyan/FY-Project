package com.merzadyan;

public class Forecast {
    
    public enum Trend {
        UP,
        DOWN,
        SAME
    }
    
    private Stock stock;
    private Trend trend;
    
    public Forecast(Stock stock) {
        this.stock = stock;
    }
    
    public Stock getStock() {
        return stock;
    }
    
    public void setStock(Stock stock) {
        this.stock = stock;
    }
    
    public Trend getTrend() {
        return trend;
    }
    
    public void setTrend(Trend trend) {
        this.trend = trend;
    }
}
