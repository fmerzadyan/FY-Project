package com.merzadyan;

/**
 * Trie data structure.
 */
public class Trie {
    private static final int ALPHABET_SIZE = 26;
    private TrieNode root = new TrieNode();
    
    class TrieNode {
        TrieNode[] children;
        boolean isLeaf;
        
        TrieNode() {
            children = new TrieNode[ALPHABET_SIZE];
        }
    }
    
    /**
     * Inserts keys into the trie.
     *
     * @param key is the key to be inserted.
     */
    public void insert(String key) {
        TrieNode node = root;
        
        // Node (point of comparison) moves downwards (in trie) as level increases.
        for (int level = 0; level < key.length(); level++) {
            int index = key.charAt(level) - 'a';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            
            node = node.children[index];
        }
        
        node.isLeaf = true;
    }
    
    /**
     * Searches for keys in the trie.
     *
     * @param targetKey is the key to be searched for in the trie.
     * @return true if target key in present in trie.
     */
    public boolean search(String targetKey) {
        TrieNode node = root;
        
        // Node (point of comparison) moves downwards (in trie) as level increases.
        for (int level = 0; level < targetKey.length(); level++) {
            int index = targetKey.charAt(level) - 'a';
            
            if (node.children[index] == null) {
                return false;
            }
            
            node = node.children[index];
        }
        
        return (node != null && node.isLeaf);
    }
}