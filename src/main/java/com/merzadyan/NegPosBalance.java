package com.merzadyan;

/**
 * Maintains a balance indicating the value of a sentence in terms of positivity, negativity or neutrality.
 */
public class NegPosBalance {
    private int balance = 0;
    
    public synchronized void positive() {
        --balance;
    }
    
    public synchronized void negative() {
        ++balance;
    }
    
    public int getBalance() {
        return balance;
    }
}
