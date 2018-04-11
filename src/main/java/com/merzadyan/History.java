package com.merzadyan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class History implements Serializable {
    private HashMap<String, ArrayList<Stock>> lastSaved = new HashMap<>();
    
    public History() {
    
    }
    
    public HashMap<String, ArrayList<Stock>> getLastSaved() {
        return lastSaved;
    }
    
    public void setLastSaved(HashMap<String, ArrayList<Stock>> lastSaved) {
        this.lastSaved = lastSaved;
    }
}
