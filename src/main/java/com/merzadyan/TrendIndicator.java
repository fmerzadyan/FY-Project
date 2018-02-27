package com.merzadyan;

import java.util.ArrayList;

public class TrendIndicator {
    private Stock stock;
    private ArrayList<Integer> polarityList;
    
    public TrendIndicator(Stock stock, ArrayList<Integer> polarityList) {
        this.stock = stock;
        this.polarityList = polarityList;
    }
    
    public void addIfNotZero(int polarity) {
        if (polarity != 0) {
            polarityList.add(polarity);
        }
    }
    
    public Forecast getForecast() {
        int stockValueIndicator = 1;
        for (Integer element : polarityList) {
            stockValueIndicator = stockValueIndicator * element;
        }
    
        Forecast forecast = new Forecast(stock);
        if (stockValueIndicator > 0) {
            forecast.setTrend(Forecast.Trend.UP);
        } else if (stockValueIndicator < 0) {
            forecast.setTrend(Forecast.Trend.DOWN);
        } else {
            forecast.setTrend(Forecast.Trend.SAME);
        }
        return forecast;
    }
    
    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof TrendIndicator &&
                ((TrendIndicator) o).stock.getSymbol().equals(stock.getSymbol());
    }
    
    public Stock getStock() {
        return stock;
    }
}
