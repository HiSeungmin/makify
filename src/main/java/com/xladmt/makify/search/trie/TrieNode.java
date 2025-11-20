package com.xladmt.makify.search.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class TrieNode {
    private Character character;
    private Map<Character, TrieNode> children;
    private boolean isEndOfWord;
    private int frequency;
    private List<String> topResults;

    public TrieNode() {
        this.children = new HashMap<>();
        this.isEndOfWord = false;
        this.frequency = 0;
        this.topResults = null;
    }

    public TrieNode(Character character) {
        this();
        this.character = character;
    }


    public boolean isEndOfWord() {
        return isEndOfWord;
    }

}
