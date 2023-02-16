package service;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

public class Morphology {

    private static final String[] SERVICE_WORDS_OF_SPEECH_RUS = {"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ПРЕДК", "ЧАСТ"};
    private static final String[] SERVICE_WORDS_OF_SPEECH_ENG = {"PN", "PREP", "PART", "ARTICLE", "CONJ"};
    private static final String REG_RUS = "[А-Яа-я]+";
    private static final String REG_ENG = "[A-Za-z]+";
    private static final String REGEX = "[^A-Za-zА-Яа-я\\s]+";
    private HashMap<String, Integer> lemmas = new HashMap<>();
    private TreeMap<String, String> lemmasWords = new TreeMap<>();

    private static LuceneMorphology lucMorphEng;
    private static LuceneMorphology lucMorphRus;

    static {
        try {
            lucMorphRus = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            lucMorphEng = new EnglishLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Morphology() {

    }


    public HashMap<String, Integer> getLemmas(String text) {
        String[] words = text.toLowerCase().replaceAll(REGEX, " ").split("\\s+");

        for (String word : words) {
            if (word.matches(REG_RUS) && (word.trim().length() > 1 || word.trim().matches("я"))) {
                List<String> wordInfo = lucMorphRus.getMorphInfo(word.trim());
                for (String w : wordInfo) {
                    if (!getRusInfo(w)) {
                        List<String> wordBaseForms = lucMorphRus.getNormalForms(word.trim());
                        getLemmas(wordBaseForms);
                    }
                }
            } else if (word.matches(REG_ENG) && word.trim().length() > 1) {
                List<String> wordInfo = lucMorphEng.getMorphInfo(word.trim());
                for (String w : wordInfo) {
                    if (!getEngInfo(w)) {
                        List<String> wordBaseForms = lucMorphEng.getNormalForms(word.trim());
                        getLemmas(wordBaseForms);
                    }
                }
            }
        }
        return lemmas;
    }

    private void getLemmas (List<String> wordBaseForms) {
        for (String lemma : wordBaseForms) {
            if (lemmas.containsKey(lemma)) {
                lemmas.put(lemma, lemmas.get(lemma) + 1);
            } else {
                lemmas.put(lemma, 1);
            }
        }
    }

    private  boolean getRusInfo(String word) {
        HashSet<Boolean> wordsInfo = new HashSet<>();
        for(int j=0; j<SERVICE_WORDS_OF_SPEECH_RUS.length; j++){
            wordsInfo.add(word.contains(SERVICE_WORDS_OF_SPEECH_RUS[j]));}
        return wordsInfo.contains(true);
    }

    private  boolean getEngInfo(String word) {
        HashSet<Boolean> wordsInfo = new HashSet<>();
        for(int j=0; j<SERVICE_WORDS_OF_SPEECH_ENG.length; j++){
            wordsInfo.add(word.contains(SERVICE_WORDS_OF_SPEECH_ENG[j]));}
        return wordsInfo.contains(true);
    }


    public TreeMap<String, String> getLemmaForWord(String[] words) {

        for (String word : words) {
            if (word.replaceAll(REGEX, "").trim().matches(REG_RUS)
                    && (word.replaceAll(REGEX, "").trim().length() > 1
                    || word.replaceAll(REGEX, "").trim().matches("я"))) {
                List<String> wordInfo = lucMorphRus.getMorphInfo(word.replaceAll(REGEX, "").trim());
                for (String w : wordInfo) {
                    if (!getRusInfo(w)) {
                        List<String> wordBaseForms = lucMorphRus.getNormalForms(word.replaceAll(REGEX, "").trim());
                        getLemmasWords(wordBaseForms,word);
                    }
                }
            } else if (word.replaceAll(REGEX, "").trim().matches(REG_ENG)
                    && word.replaceAll(REGEX, "").trim().length() > 1) {
                List<String> wordInfo = lucMorphEng.getMorphInfo(word.replaceAll(REGEX, "").trim());
                for (String w : wordInfo) {
                    if (!getEngInfo(w)) {
                        List<String> wordBaseForms = lucMorphEng.getNormalForms(word.replaceAll(REGEX, "").trim());
                        getLemmasWords(wordBaseForms, word);
                    }
                }
            }
        }
        return lemmasWords;
    }

    private void getLemmasWords(List<String> wordBaseForms, String word) {
        if (!lemmasWords.containsKey(word.trim())) {
            lemmasWords.put(word.trim(), wordBaseForms.get(0));
        }
    }

}
