package com.merzadyan.seed;

import com.merzadyan.stock.Stock;

import java.util.Comparator;

public class SeedUrl implements Comparator, Comparable {
    private final String url;
    private final Type type;
    
    public enum Type {
        DEFAULT,
        USER_DEFINED
    }
    
    public enum Option {
        DEFAULT_ONLY,
        CUSTOM_ONLY,
        BOTH
    }
    
    public SeedUrl(String url, Type type) {
        this.url = url;
        this.type = type;
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        SeedUrl s1 = (SeedUrl) o1, s2 = (SeedUrl) o2;
        
        return s1.url.compareToIgnoreCase(s2.url);
    }
    
    @Override
    public int compareTo(Object o) {
        return url.compareToIgnoreCase(((SeedUrl) o).url);
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        
        if (!(object instanceof Stock)) {
            return false;
        }
        
        SeedUrl comparee = (SeedUrl) object;
        return
                (url == null ? comparee.url == null : url.equals(comparee.url)) &&
                        (type == null ? comparee.type == null : type.equals(comparee.type));
    }
    
    @Override
    public int hashCode() {
        // See Josh Bloch's Effective Java for reference.
        // Arbitrary prime number.
        int result = 23;
        
        result = 37 * result + (url == null ? 0 : url.hashCode());
        result = 37 * result + (type == null ? 0 : type.hashCode());
        
        return result;
    }
    
    public String getUrl() {
        return url;
    }
    
    public Type getType() {
        return type;
    }
}
