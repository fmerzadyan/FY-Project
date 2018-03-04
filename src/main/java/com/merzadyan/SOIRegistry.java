package com.merzadyan;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * SOIRegistry (Stocks of Interest Registry) contains a list of stocks to watch during crawling process.
 */
public class SOIRegistry {
    private static final Logger LOGGER = Logger.getLogger(SOIRegistry.class.getName());
    
    private static final SOIRegistry instance = new SOIRegistry();
    
    private HashSet<Stock> stockSet;
    
    private static String soiFilePath;
    
    private SOIRegistry() {
        stockSet = new HashSet<>();
        soiFilePath = "C:\\Users\\fmerzadyan\\OneDrive\\Unispace\\Final Year\\FY Project\\SPP\\src\\main\\resources\\soi.txt";
        stockSet = extractStocks(soiFilePath);
    }
    
    public static SOIRegistry getInstance() {
        return instance;
    }
    
    private HashSet<Stock> extractStocks(String soiFilePath) {
        if (!CommonOp.isNotNullAndNotEmpty(soiFilePath)) {
            LOGGER.debug("Error: file is not set.");
            return null;
        }
        
        if (!CommonOp.fileExists(soiFilePath)) {
            LOGGER.debug("Error: " + soiFilePath + " does not exist.");
            return null;
        }
        
        HashSet<Stock> set = new HashSet<>();
        
        try {
            FileReader fileReader = new FileReader(soiFilePath);
            
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] pair = line.split("%");
                if (pair[0] == null || pair[1] == null) {
                    // continue to next iteration if format is broken.
                    continue;
                }
                String company = pair[0].trim().toLowerCase();
                String symbol = pair[1].trim().toLowerCase();
                // NOTE: since only FTSE-100 companies are listed in soi.txt then all are on LSE.
                set.add(new Stock(company, symbol, "LSE"));
            }
        } catch (IOException ioEx) {
            LOGGER.error(ioEx);
        }
        
        return set;
    }
    
    public HashSet<Stock> getStockSet() {
        return stockSet;
    }
}
