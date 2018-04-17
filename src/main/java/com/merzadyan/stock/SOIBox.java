package com.merzadyan.stock;

import java.io.Serializable;
import java.util.TreeSet;

/**
 * Used to maintain a persistent list of SOI for the SOIRegistry tab.
 */
public class SOIBox implements Serializable {
    public TreeSet<Stock> set;
    
    public SOIBox() {
        set = new TreeSet<>();
    }
}
