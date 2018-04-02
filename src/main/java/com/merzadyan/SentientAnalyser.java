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
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SentientAnalyser {
    // org.apache.log4j.Logger.
    private static final Logger LOGGER = Logger.getLogger(SentientAnalyser.class.getName());
    
    private static final StanfordCoreNLP sentimentPipeline = getSentimentPipeline();
    
    private static final StanfordCoreNLP namedEntityPipeline = getNamedEntityPipeline();
    
    private static StanfordCoreNLP getSentimentPipeline() {
        // Disable logs.
        RedwoodConfiguration.empty().capture(System.out).apply();
        
        Properties properties = new Properties();
        // Remove tokenizer warnings.
        // NOTE: 6 options for tokenize.options: noneDelete, firstDelete, allDelete, noneKeep, firstKeep, allKeep
        properties.setProperty("tokenize.options", "untokenizable=noneKeep");
        // Parse annotator is required for sentiment?
        properties.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        
        // Re-enable logs.
        RedwoodConfiguration.current().clear().apply();
        
        return pipeline;
    }
    
    private static StanfordCoreNLP getNamedEntityPipeline() {
        // Disable logs.
        RedwoodConfiguration.empty().capture(System.out).apply();
        
        Properties properties = new Properties();
        // Remove tokenizer warnings.
        // NOTE: 6 options for tokenize.options: noneDelete, firstDelete, allDelete, noneKeep, firstKeep, allKeep
        properties.setProperty("tokenize.options", "untokenizable=noneKeep");
        properties.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
        
        // Re-enable logs.
        RedwoodConfiguration.current().clear().apply();
        
        return pipeline;
    }
    
    /**
     * Taken from https://blog.openshift
     * .com/day-20-stanford-corenlp-performing-sentiment-analysis-of-twitter-using-java/
     *
     * @param text
     * @param text
     * @return fine-grain sentiment score in a rage of 0-4; returns -1 in the case of an error.
     * <ol start=0>
     * <li>Negative</li>
     * <li>Somewhat negative</li>
     * <li>Neutral</li>
     * <li>Somewhat positive</li>
     * <li>Positive</li>
     * </ol>
     * @throws Exception unknown/unexpected emitted from Stanford CoreNLP.
     */
    public static int findSentiment(String text) throws Exception {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        
        Annotation document = sentimentPipeline.process(text);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        
        ArrayList<Integer> documentSentiment = new ArrayList<>();
        
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
     * in the text of the article.
     *
     * @param text
     * @param trie
     * @return null in the case of an error or target company was not found in the (text| soi.txt| trie).
     * @throws Exception unknown/unexpected emitted from Stanford CoreNLP.
     */
    public static String identifyOrganisationEntity(String text, org.ahocorasick.trie.Trie trie) throws Exception {
        if (text == null || text.isEmpty()) {
            return null;
        }
        
        Annotation document = namedEntityPipeline.process(text);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> organisations = new ArrayList<>();
        
        for (CoreMap sentence : sentences) {
            int previousCount = 0;
            int count = 0;
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                int previousWordIndex;
                String unknownEntityType = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                if (unknownEntityType.equals("ORGANIZATION")) {
                    count++;
                    if (previousCount != 0 && (previousCount + 1) == count) {
                        previousWordIndex = organisations.size() - 1;
                        String previousWord = organisations.get(previousWordIndex);
                        organisations.remove(previousWordIndex);
                        previousWord = previousWord.concat(" " + word);
                        organisations.add(previousWordIndex, previousWord);
                        continue;
                    }
                    organisations.add(word);
                    previousCount = count;
                } else {
                    count = 0;
                    previousCount = 0;
                }
            }
        }
        
        for (String entity : organisations) {
            if (trie.containsMatch(entity)) {
                return entity;
            }
        }
        
        // Defaults to returning null.
        return null;
    }
}