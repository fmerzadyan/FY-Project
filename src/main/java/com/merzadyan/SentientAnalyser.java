package com.merzadyan;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

import static edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import static edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import static edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import static edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import static edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;

public class SentientAnalyser {
    public static void analyse(String text, WordRegistry wordRegistry, NegPosBalance negPosBalance) {
        // TODO: prevent an OOM (Out Of Memory) issue when the input text is too large.
        if (text.length() > 300000) {
            return;
        }
        
        // Creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, coreference resolution, etc.
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        
        // Create an empty Annotation just with the given text.
        Annotation document = new Annotation(text);
        
        // Run all Annotators on this text.
        pipeline.annotate(document);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        
        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String partOfSpeech = token.get(PartOfSpeechAnnotation.class);
                String namedEntityRecognition = token.get(NamedEntityTagAnnotation.class);
                System.out.println("word: " + word + " POS: " + partOfSpeech + " NER: " + namedEntityRecognition +
                        " lemma: " + token.get(CoreAnnotations.LemmaAnnotation.class));
                String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
                
                // TODO: add positive/negative words in to the word registry in their respective sets.
                if (isPositive(lemma, wordRegistry)) {
                    negPosBalance.positive();
                } else if (isNegative(lemma, wordRegistry)) {
                    negPosBalance.negative();
                }
            }
        }
    }
    
    public static boolean isPositive(String word, WordRegistry wordRegistry) {
        return word != null && !word.isEmpty() && wordRegistry != null && wordRegistry.getPositiveSet().contains(word);
    }
    
    public static boolean isNegative(String word, WordRegistry wordRegistry) {
        return word != null && !word.isEmpty() && wordRegistry != null && wordRegistry.getNegativeSet().contains(word);
    }
}