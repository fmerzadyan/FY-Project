package com.merzadyan;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * Contains sets of positive and negative words. It is used to identify positivity or negativity of a given word.
 */
public class WordRegistry {
    private static final Logger LOGGER = Logger.getLogger(WordRegistry.class.getName());
    
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
        if (Common.isNullOrEmptyString(positiveSetFilePath)) {
            LOGGER.debug("Error: positive-set file is not set.");
            return;
        }
        
        if (!Common.isFile(positiveSetFilePath)) {
            LOGGER.debug("Error: positive-set file does not exist.");
            return;
        }
        
        positiveSet = extractWords(positiveSetFilePath);
    }
    
    private void populateNegativeSet() {
        if (Common.isNullOrEmptyString(negativeSetFilePath)) {
            LOGGER.debug("Error: negative-set file is not set.");
            return;
        }
        
        if (!Common.isFile(negativeSetFilePath)) {
            LOGGER.debug("Error: negative-set file does not exist.");
            return;
        }
        
        negativeSet = extractWords(negativeSetFilePath);
    }
    
    /**
     * Extracts lexicon from the given file.
     *
     * @param filePath
     * @return set of words.
     */
    private HashSet<String> extractWords(String filePath) {
        if (Common.isNullOrEmptyString(filePath)) {
            LOGGER.debug("Error: file is not set.");
            return null;
        }
        
        if (!Common.isFile(filePath)) {
            LOGGER.debug("Error: " + filePath + " does not exist.");
            return null;
        }
        
        if (Common.isEmptyFile(filePath)) {
            LOGGER.debug("Error: " + filePath + " is empty.");
            return null;
        }
        
        HashSet<String> set = new HashSet<>();
        
        try {
            FileReader fileReader = new FileReader(filePath);
            
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
    
    public HashSet<String> getPositiveSet() {
        return positiveSet;
    }
    
    public HashSet<String> getNegativeSet() {
        return negativeSet;
    }
}
