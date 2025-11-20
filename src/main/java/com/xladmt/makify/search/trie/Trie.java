package com.xladmt.makify.search.trie;

import java.util.*;
import java.util.stream.Collectors;

public class Trie {
    private TrieNode root;
    private static final int TOP_K = 5;

    public Trie() {
        this.root = new TrieNode();
        
    }

    public void insert(String word, int frequency) {
        if (word == null || word.isEmpty()) {
            return;
        }

        TrieNode current = root;

        // 각 문자에 대해 노드 생성 및 이동
        for (char ch : word.toCharArray()) {
            current.getChildren().putIfAbsent(ch, new TrieNode());
            current = current.getChildren().get(ch);
        }

        current.setEndOfWord(true);
        current.setFrequency(frequency);
    }

    /**
     * 주어진 prefix로 자동완성 검색
     *
     * @param prefix 검색 prefix
     * @return 상위 5개 검색어 리스트
     */
    public List<String> autocomplete(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return root.getTopResults() != null ? root.getTopResults() : new ArrayList<>();
        }

        TrieNode node = findNode(prefix);

        if (node == null) {
            return new ArrayList<>();
        }

        // prefix 노드의 topResults 반환
        return node.getTopResults() != null ? node.getTopResults() : new ArrayList<>();
    }

    /**
     * 특정 prefix로 시작하는 모든 단어 검색
     *
     * @param prefix 검색 prefix
     * @return prefix로 시작하는 모든 단어와 빈도수
     */
    public List<String> searchAllWordsWithPrefix(String prefix) {
        TrieNode node = findNode(prefix);

        if (node == null) {
            return new ArrayList<>();
        }

        List<String> results = new ArrayList<>();
        dfsCollectWords(node, prefix, results);
        return results;
    }

    /**
     * 트라이 전체 재구축 (매주 1회 갱신)
     *
     * @param searchData Redis에서 읽은 검색어-빈도수 맵
     */
    public void rebuild(Map<String, Integer> searchData) {
        // 새로운 트라이 생성
        this.root = new TrieNode();

        // 모든 검색어 삽입
        for (Map.Entry<String, Integer> entry : searchData.entrySet()) {
            insert(entry.getKey(), entry.getValue());
        }

        // Top-K 계산
        calculateTopK();
    }

    /**
     * 각 노드마다 Top-K 결과 계산
     * DFS를 사용하여 각 노드를 루트로 하는 서브트리의 상위 5개 단어를 구함
     */
    private void calculateTopK() {
        dfsCalculateTopK(root, "");
    }

    /**
     * DFS를 통해 각 노드의 topResults 계산
     *
     * @param node   현재 노드
     * @param prefix 현재까지의 경로 (단어 재구성용)
     * @return 현재 노드를 루트로 하는 서브트리의 모든 단어들
     */
    private List<WordFrequency> dfsCalculateTopK(TrieNode node, String prefix) {
        if (node == null) {
            return new ArrayList<>();
        }

        // 현재 노드의 모든 단어 수집
        List<WordFrequency> allWords = new ArrayList<>();

        // 현재 노드가 단어의 끝이면 추가
        if (node.isEndOfWord()) {
            allWords.add(new WordFrequency(prefix, node.getFrequency()));
        }

        // 자식 노드들 순회
        if (node.getChildren() != null) {
            for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
                allWords.addAll(dfsCalculateTopK(entry.getValue(), prefix + entry.getKey()));
            }
        }

        // 상위 TOP_K 개 선택
        List<String> topK = allWords.stream()
                .sorted(Comparator.comparingInt(WordFrequency::getFrequency).reversed())
                .limit(TOP_K)
                .map(WordFrequency::getWord)
                .collect(Collectors.toList());

        node.setTopResults(topK);

        return allWords;
    }

    /**
     * DFS를 통해 특정 노드를 루트로 하는 모든 단어 수집
     *
     * @param node    현재 노드
     * @param prefix  현재까지 만들어진 문자열
     * @param results 결과 리스트
     */
    private void dfsCollectWords(TrieNode node, String prefix, List<String> results) {
        if (node == null) {
            return;
        }

        // 현재 노드가 단어의 끝이면 결과에 추가
        if (node.isEndOfWord()) {
            results.add(prefix);
        }

        // 자식 노드들 순회
        if (node.getChildren() != null) {
            for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
                dfsCollectWords(entry.getValue(), prefix + entry.getKey(), results);
            }
        }
    }

    /**
     * 주어진 prefix에 해당하는 노드 찾기
     *
     * @param prefix 검색할 prefix
     * @return prefix에 해당하는 노드, 없으면 null
     */
    private TrieNode findNode(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return root;
        }

        TrieNode current = root;

        for (char ch : prefix.toCharArray()) {
            if (current.getChildren() == null || !current.getChildren().containsKey(ch)) {
                return null;
            }
            current = current.getChildren().get(ch);
        }

        return current;
    }

    /**
     * 트라이의 크기 (저장된 단어 개수) 반환
     *
     * @return 단어 개수
     */
    public int size() {
        return countWords(root);
    }

    /**
     * DFS를 통해 단어 개수 계산
     */
    private int countWords(TrieNode node) {
        if (node == null) {
            return 0;
        }

        int count = node.isEndOfWord() ? 1 : 0;

        if (node.getChildren() != null) {
            for (TrieNode child : node.getChildren().values()) {
                count += countWords(child);
            }
        }

        return count;
    }

    /**
     * 트라이 디버깅용 출력
     */
    public void printTrie() {
        System.out.println("=== Trie Contents ===");
        System.out.println("Total words: " + size());
        dfsPrintTrie(root, "");
    }

    /**
     * DFS를 통해 트라이 출력
     */
    private void dfsPrintTrie(TrieNode node, String prefix) {
        if (node == null) {
            return;
        }

        if (node.isEndOfWord()) {
            System.out.println(prefix + " (frequency: " + node.getFrequency() + ")");
            if (node.getTopResults() != null) {
                System.out.println("  Top Results: " + node.getTopResults());
            }
        }

        if (node.getChildren() != null) {
            for (Map.Entry<Character, TrieNode> entry : node.getChildren().entrySet()) {
                dfsPrintTrie(entry.getValue(), prefix + entry.getKey());
            }
        }
    }

    /**
     * 단어와 빈도수를 저장하는 내부 클래스
     */
    private static class WordFrequency {
        private final String word;
        private final int frequency;

        public WordFrequency(String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        public String getWord() {
            return word;
        }

        public int getFrequency() {
            return frequency;
        }
    }

    public TrieNode getRoot() {
        return root;
    }

    public static int getTopK() {
        return TOP_K;
    }
}