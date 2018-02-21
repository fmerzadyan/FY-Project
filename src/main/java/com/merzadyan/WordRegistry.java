package com.merzadyan;

import java.util.HashSet;

/**
 * Contains sets of positive and negative words. It is used to identify positivity or negativity of a given word.
 */
public class WordRegistry {
    // TODO: add positive/negative words in to the word registry in their respective sets.
    private static final WordRegistry instance = new WordRegistry();
    private HashSet<String> positiveSet;
    private HashSet<String> negativeSet;
    
    // Restrict object construction by third party objects.
    // Enforce singleton pattern.
    private WordRegistry() {
        positiveSet = new HashSet<>();
        negativeSet = new HashSet<>();
    }
    
    public static WordRegistry getInstance() {
        return instance;
    }
    
    public HashSet<String> getPositiveSet() {
        return positiveSet;
    }
    
    public HashSet<String> getNegativeSet() {
        return negativeSet;
    }
}
