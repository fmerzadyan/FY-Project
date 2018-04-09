package com.merzadyan;

import java.io.Serializable;
import java.util.ArrayList;

public class History implements Serializable {
    private ArrayList<Stock> lastSaved = new ArrayList<>();
    
    public History() {
    
    }
    
    public ArrayList<Stock> getLastSaved() {
        return lastSaved;
    }
    
    public void setLastSaved(ArrayList<Stock> lastSaved) {
        this.lastSaved = lastSaved;
    }
}
