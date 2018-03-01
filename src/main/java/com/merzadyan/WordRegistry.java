package com.merzadyan;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Contains sets of positive and negative words. It is used to identify positivity or negativity of a given word.
 */
public class WordRegistry {
    private static final Logger LOGGER = Logger.getLogger(WordRegistry.class.getName());
    
    // TODO: addIfNotZero more positive/negative words in to the word registry in their respective sets.
    private static final WordRegistry instance = new WordRegistry();
    
    private HashSet<String> positiveSet;
    private HashSet<String> negativeSet;
    
    private static String positiveSetFilePath;
    private static String negativeSetFilePath;
    
    // Restrict object construction by third party objects.
    // Enforce singleton pattern.
    private WordRegistry() {
        positiveSet = new HashSet<>();
        negativeSet = new HashSet<>();
        positiveSetFilePath = "C:\\Users\\fmerzadyan\\OneDrive\\Unispace\\Final Year\\FY Project\\SPP\\src\\main\\resources\\positive-set.txt";
        negativeSetFilePath = "C:\\Users\\fmerzadyan\\OneDrive\\Unispace\\Final Year\\FY Project\\SPP\\src\\main\\resources\\negative-set.txt";
        populatePositiveSet();
        populateNegativeSet();
    }
    
    public static WordRegistry getInstance() {
        return instance;
    }
    
    private void populatePositiveSet() {
        if (!isNotNullAndNotEmpty(positiveSetFilePath)) {
            LOGGER.debug("Error: positive-set file is not set.");
            return;
        }
        
        if (!fileExists(positiveSetFilePath)) {
            LOGGER.debug("Error: positive-set file does not exist.");
            return;
        }
        
        positiveSet = extractWords(positiveSetFilePath);
    }
    
    private void populateNegativeSet() {
        if (!isNotNullAndNotEmpty(negativeSetFilePath)) {
            LOGGER.debug("Error: negative-set file is not set.");
            return;
        }
        
        if (!fileExists(negativeSetFilePath)) {
            LOGGER.debug("Error: negative-set file does not exist.");
            return;
        }
        
        negativeSet = extractWords(negativeSetFilePath);
    }
    
    /**
     * Extracts lexicon from the given file.
     *
     * @param setFilePath
     * @return set of words.
     */
    private HashSet<String> extractWords(String setFilePath) {
        if (!isNotNullAndNotEmpty(setFilePath)) {
            LOGGER.debug("Error: file is not set.");
            return null;
        }
        
        if (!fileExists(setFilePath)) {
            LOGGER.debug("Error: " + setFilePath + " does not exist.");
            return null;
        }
        
        HashSet<String> set = new HashSet<>();
        
        try {
            FileReader fileReader = new FileReader(setFilePath);
            
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                set.add(line.trim().toLowerCase());
            }
        } catch (IOException ioEx) {
            LOGGER.debug("Could not read file: " + ioEx);
        }
        
        return set;
    }
    
    private boolean isNotNullAndNotEmpty(String text) {
        return text != null && !text.isEmpty();
    }
    
    private boolean fileExists(String filePath) {
        File file = new File(filePath);
        return !file.isDirectory() && file.exists();
    }
    
    public HashSet<String> getPositiveSet() {
        return positiveSet;
    }
    
    public HashSet<String> getNegativeSet() {
        return negativeSet;
    }
    
    public static String getPositiveSetFilePath() {
        return positiveSetFilePath;
    }
    
    public static void setPositiveSetFilePath(String positiveSetFilePath) {
        WordRegistry.positiveSetFilePath = positiveSetFilePath;
    }
    
    public static String getNegativeSetFilePath() {
        return negativeSetFilePath;
    }
    
    public static void setNegativeSetFilePath(String negativeSetFilePath) {
        WordRegistry.negativeSetFilePath = negativeSetFilePath;
    }
}
