package com.merzadyan;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import javafx.util.Pair;

import java.util.List;
import java.util.Properties;

import static edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import static edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;

public class SentientAnalyser {
    // Is only interested in any stocks listed in the SOIRegistry.
    public static Pair<Stock, PolarityScale> analysePolarity(String text, SOIRegistry soiRegistry, WordRegistry wordRegistry,
                                                             PolarityScale polarityScale) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        if (soiRegistry == null) {
            return null;
        }
        
        if (wordRegistry == null) {
            return null;
        }
        
        if (polarityScale == null) {
            return null;
        }
        
        // TODO: prevent an OOM (Out Of Memory) issue when the input text is too large.
        if (text.length() > 300000) {
            return null;
        }
        
        // Creates a StanfordCoreNLP object, with POS tagging, lemmatization, etc.
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        // Create an empty Annotation just with the given text.
        Annotation document = new Annotation(text);
        
        // Run all Annotators on this text.
        pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    
        // TODO: create another GUI to train the polarity of words and insert data (replace if already exists) into a database/file.
        // TODO: create a fine-grain sentiment classification e.g. Negative -> Somewhat negative -> Neutral -> Somewhat Positive -> Positive.
        // TODO: put the tokens (normalised, lower-cased form) in a binary tree. [Remove HTML or other non-English].
        // TODO: bottom-up parsing of binary tree.
        // TODO: gather words in the binary tree in an n-gram grouping (phrases) and classify the sentiment of group.
        // TODO: sum the polarity.
        // TODO: return the polarity associated with the stock. IMPORTANT: ensure stock is in SOI registry.
        // TODO: create test data to test sentient analyser's correctness and accuracy then focus on feeding sentient analyser data from the crawler.
        
        Stock stock = new Stock();
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                String normalisedNER = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);
                
                // TODO: extremely basic semantic analysis - consider evaluation of entire phrases and terms.
                if (isPositive(lemma, wordRegistry)) {
                    polarityScale.positive();
                } else if (isNegative(lemma, wordRegistry)) {
                    polarityScale.negative();
                }
                
                if (stock.getSymbol() == null && normalisedNER != null && !normalisedNER.isEmpty()) {
                    stock.setSymbol(normalisedNER);
                    if (!soiRegistry.getStockSet().contains(stock)) {
                        stock.setSymbol(null);
                    }
                }
            }
        }
        
        if (stock.getSymbol() == null) {
            return null;
        }
        
        return new Pair<Stock, PolarityScale>(stock, polarityScale);
    }
    
    public static boolean isPositive(String word, WordRegistry wordRegistry) {
        return word != null && !word.isEmpty() && wordRegistry != null && wordRegistry.getPositiveSet().contains(word);
    }
    
    public static boolean isNegative(String word, WordRegistry wordRegistry) {
        return word != null && !word.isEmpty() && wordRegistry != null && wordRegistry.getNegativeSet().contains(word);
    }
}