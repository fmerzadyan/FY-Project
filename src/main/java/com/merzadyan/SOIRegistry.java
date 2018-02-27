package com.merzadyan;

import java.util.HashSet;

/**
 * SOIRegistry (Stocks of Interest Registry) contains a list of stocks to watch during crawling process.
 */
public class SOIRegistry {
    // TODO: addIfNotZero FTSE-100 companies to the stocks of interest registry.
    private static final SOIRegistry instance = new SOIRegistry();
    
    private HashSet<Stock> stockSet;
    
    private SOIRegistry() {
        stockSet = new HashSet<>();
    }
    
    public static SOIRegistry getInstance() {
        return instance;
    }
    
    public HashSet<Stock> getStockSet() {
        return stockSet;
    }
}
