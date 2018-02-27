package com.merzadyan;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Maintains a balance indicating the value of a sentence in terms of positivity, negativity or neutrality.
 */
public class NegPosBalance {
    private AtomicInteger balance = new AtomicInteger(0);
    
    public synchronized void positive() {
        balance.set(balance.get() + 1);
    }
    
    public synchronized void negative() {
        balance.set(balance.get() - 1);
    }
    
    public int getBalance() {
        return balance.get();
    }
}
