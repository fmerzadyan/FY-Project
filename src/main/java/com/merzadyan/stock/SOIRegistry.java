package com.merzadyan.stock;

import com.merzadyan.Common;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeSet;

/**
 * SOIRegistry (Stocks of Interest Registry) contains a list of stocks to watch during crawling process.
 */
public class SOIRegistry {
    private static final Logger LOGGER = Logger.getLogger(SOIRegistry.class.getName());
    
    private static final SOIRegistry instance = new SOIRegistry();
    
    private TreeSet<Stock> stockSet;
    private TreeSet<Stock> defaultStockSet;
    
    private static final String DEFAULT_SOI_FILE_PATH = "C:\\Users\\fmerzadyan\\OneDrive\\Unispace\\Final Year\\FY Project\\SPP\\src\\main\\resources\\dictionary\\default-soi.txt";
    
    private SOIRegistry() {
        stockSet = new TreeSet<>();
        String soiFilePath = "C:\\Users\\fmerzadyan\\OneDrive\\Unispace\\Final Year\\FY Project\\SPP\\src\\main\\resources\\dictionary\\soi.txt";
        stockSet = extractStocks(soiFilePath, false);
        defaultStockSet = extractStocks(DEFAULT_SOI_FILE_PATH, true);
        
    }
    
    public static SOIRegistry getInstance() {
        return instance;
    }
    
    private TreeSet<Stock> extractStocks(String soiFilePath, boolean useDefault) {
        if (Common.isNullOrEmptyString(soiFilePath)) {
            return null;
        }
        
        if (!Common.isFile(soiFilePath)) {
            return null;
        }
        
        String filePath = soiFilePath;
        if (Common.isEmptyFile(filePath) && useDefault) {
            filePath = DEFAULT_SOI_FILE_PATH;
        } else if (Common.isEmptyFile(filePath) && !useDefault) {
            return null;
        }
        
        TreeSet<Stock> set = new TreeSet<>();
        
        try {
            FileReader fileReader = new FileReader(filePath);
            
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
                Stock stock = new Stock(company, symbol, "LSE");
                if (!set.contains(stock)) {
                    set.add(stock);
                }
            }
        } catch (IOException ioEx) {
            LOGGER.error(ioEx);
        }
        
        return set;
    }
    
    /**
     * IMPORTANT NOTE: for every time, stocks are "added" on the UI, this method must be called to keep
     * the the registry in sync with the UI.
     *
     * @param stock
     */
    public void add(Stock stock) {
        if (!stockSet.contains(stock)) {
            stockSet.add(stock);
        }
    }
    
    /**
     * IMPORTANT NOTE: for every time, stocks are "removed" on the UI, this method must be called to keep
     * the the registry in sync with the UI.
     *
     * @param stock
     */
    public void remove(Stock stock) {
        if (stockSet.contains(stock)) {
            stockSet.remove(stock);
        }
    }
    
    public TreeSet<Stock> getStockSet() {
        return stockSet;
    }
    
    public TreeSet<Stock> getDefaultStockSet() {
        return defaultStockSet;
    }
}
