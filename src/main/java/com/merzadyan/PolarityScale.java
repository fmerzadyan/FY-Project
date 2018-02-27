package com.merzadyan;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains a polarity indicating the value of a sentence in terms of positivity, negativity or neutrality.
 */
public class PolarityScale {
    private AtomicInteger polarity = new AtomicInteger(0);
    
    public synchronized void positive() {
        polarity.set(polarity.get() + 1);
    }
    
    public synchronized void negative() {
        polarity.set(polarity.get() - 1);
    }
    
    public int getPolarity() {
        return polarity.get();
    }
}
