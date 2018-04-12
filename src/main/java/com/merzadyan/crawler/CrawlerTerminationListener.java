package com.merzadyan.crawler;

import com.merzadyan.stock.Stock;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used to signal the termination of a crawler's life.
 */
public interface CrawlerTerminationListener {
    void onTermination(HashMap<Stock, ArrayList<Integer>> soiScoreMap);
}