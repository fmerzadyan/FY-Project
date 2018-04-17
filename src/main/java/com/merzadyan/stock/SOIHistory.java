package com.merzadyan.stock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SOIHistory implements Serializable {
    private HashMap<String, ArrayList<Stock>> lastSaved = new HashMap<>();
    
    public SOIHistory() {
    
    }
    
    public HashMap<String, ArrayList<Stock>> getLastSaved() {
        return lastSaved;
    }
    
    public void setLastSaved(HashMap<String, ArrayList<Stock>> lastSaved) {
        this.lastSaved = lastSaved;
    }
}
