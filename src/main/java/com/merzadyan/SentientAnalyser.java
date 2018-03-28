package com.merzadyan;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;
import org.ahocorasick.trie.Trie;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

public class SentientAnalyser {
    // org.apache.log4j.Logger.
    private static final Logger LOGGER = Logger.getLogger(SentientAnalyser.class.getName());
    
    /**
     * Taken from https://blog.openshift
     * .com/day-20-stanford-corenlp-performing-sentiment-analysis-of-twitter-using-java/
     *
     * @param text
     * @return fine-grain sentiment score in a rage of 0-4; returns -1 in the case of an error.
     * <ol start=0>
     * <li>Negative</li>
     * <li>Somewhat negative</li>
     * <li>Neutral</li>
     * <li>Somewhat positive</li>
     * <li>Positive</li>
     * </ol>
     */
    public static int findSentiment(String text) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        
        // Disable logs.
        RedwoodConfiguration.empty().capture(System.out).apply();
        
        Properties properties = new Properties();
        // Remove tokenizer warnings.
        properties.setProperty("tokenize.options", "untokenizable=NoneKeep");
        // Parse annotator is required for sentiment?
        properties.setProperty("annotators", "tokenize, ssplit, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        
        // Re-enable logs.
        RedwoodConfiguration.current().clear().apply();
        
        Annotation document = pipeline.process(text);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        
        ArrayList<Integer> documentSentiment = new ArrayList<>();
        
        // TODO: train NER classifier to recognise SOI from SOIRegistry.
        // Retrieve the sentiment from each sentence.
        for (CoreMap sentence : sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentenceSentiment = RNNCoreAnnotations.getPredictedClass(tree);
            
            if (sentenceSentiment == -1) {
                // #getPredictedClass returns sentiment 0-4 or -1 if none available.
                sentenceSentiment = 0;
            }
            
            documentSentiment.add(sentenceSentiment);
        }
        
        // Set the sentiment of entire document using the weighted average.
        int average = 0;
        for (int i : documentSentiment) {
            average += i;
        }
        average = average / documentSentiment.size();
        
        return average;
    }
    
    /**
     * Extracts the company name based on the reasonable assumption that the company name is usually present
     * in the title of the article.
     *
     * @param title
     * @param trie
     * @return
     */
    public static String identifyEntity(String title, org.ahocorasick.trie.Trie trie) {
        if (title == null || title.isEmpty()) {
            return null;
        }
        
        // Disable logs.
        RedwoodConfiguration.empty().capture(System.out).apply();
        
        Properties properties = new Properties();
        // Remove tokenizer warnings.
        properties.setProperty("tokenize.options", "untokenizable=NoneKeep");
        properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        
        // Re-enable logs.
        RedwoodConfiguration.current().clear().apply();
        
        
        Annotation document = pipeline.process(title);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        
        ArrayList<Integer> documentSentiment = new ArrayList<>();
        
        String entity = null;
        // TODO: train NER classifier to recognise SOI from SOIRegistry.
        // Retrieve the sentiment from each sentence.
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String namedEntity = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (namedEntity.equals("ORGANIZATION")) {
                    LOGGER.debug("word: " + word + " pos: " + pos + " namedEntity: " + namedEntity);
                    if (trie.containsMatch(word)) {
                        entity = word;
                    }
                }
            }
        }
        
        return entity;
    }
    
    public static void main(String[] args) {
        // Test #identifyEntity.
        HashSet<Stock> stockSet = SOIRegistry.getInstance().getStockSet();
        ArrayList<String> companyKeys = new ArrayList<>();
        for (Stock stock : stockSet) {
            if (stock != null) {
                // NOTE: case does not matter when processed; case is ignored as stated in trie construction.
                if (stock.getCompany() != null && !stock.getCompany().isEmpty()) {
                    companyKeys.add(stock.getCompany());
                }
                if (stock.getSymbol() != null && !stock.getSymbol().isEmpty()) {
                    companyKeys.add(stock.getSymbol());
                }
            }
        }
        
        // See https://github.com/robert-bor/aho-corasick
        Trie trie = Trie.builder()
                // IMPORTANT: ignoreCase() must be called before adding keywords.
                .ignoreCase()
                .addKeywords(companyKeys)
                .build();
        
        String title = "Amid the consumer privacy revelations, Barclays is receiving more and more pressure " +
                "to explain themselves.";
        String company = SentientAnalyser.identifyEntity(title, trie);
        LOGGER.debug("The company is:  " + company);
    }
}