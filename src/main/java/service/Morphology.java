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

    public Morphology() throws IOException {

    }


    public  HashMap<String, Integer> getLemmas(String text) {
        String[] words = text.toLowerCase().replaceAll(REGEX, " ").split("\\s+");

        for (int i = 0; i < words.length; i++) {
            if (words[i].matches(REG_RUS) && (words[i].trim().length() > 1 || words[i].trim() == "я")) {
                List<String> wordInfo = lucMorphRus.getMorphInfo(words[i].trim());
                for (String word : wordInfo) {
                    if (!getRusInfo(word)) {
                        List<String> wordBaseForms = lucMorphRus.getNormalForms(words[i].trim());
                        for (String lemma : wordBaseForms) {
                            if (lemmas.containsKey(lemma)) {
                                lemmas.put(lemma, lemmas.get(lemma) + 1);
                            } else {
                                lemmas.put(lemma, 1);
                            }
                        }
                    } else { continue; }
                }
            } else if (words[i].matches(REG_ENG) && words[i].trim().length() > 1) {
                List<String> wordInfo = lucMorphEng.getMorphInfo(words[i].trim());
                for (String word : wordInfo) {
                    if (!getEngInfo(word)) {
                        List<String> wordBaseForms = lucMorphEng.getNormalForms(words[i].trim());
                        for (String lemma : wordBaseForms) {
                            if (lemmas.containsKey(lemma)) {
                                lemmas.put(lemma, lemmas.get(lemma) + 1);
                            } else {
                                lemmas.put(lemma, 1);
                            }
                        }
                    } else { continue; }
                }
            } else {}
        }

        return lemmas;
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


    public  TreeMap<String, String> getLemmaForWord(String[] words) {

        for (int i = 0; i < words.length; i++) {
            if (words[i].replaceAll(REGEX, "").trim().matches(REG_RUS)
                    && (words[i].replaceAll(REGEX, "").trim().length() > 1
                    || words[i].replaceAll(REGEX, "").trim() == "я")) {
                List<String> wordInfo = lucMorphRus.getMorphInfo(words[i].replaceAll(REGEX, "").trim());
                for (String word : wordInfo) {
                    if (!getRusInfo(word)) {
                        List<String> wordBaseForms = lucMorphRus.getNormalForms(words[i].replaceAll(REGEX, "").trim());
                        if (!lemmasWords.containsKey(words[i].trim())) {
                            lemmasWords.put(words[i].trim(), wordBaseForms.get(0));
                        } else {  }
                    } else { continue; }
                }
            } else if (words[i].replaceAll(REGEX, "").trim().matches(REG_ENG)
                    && words[i].replaceAll(REGEX, "").trim().length() > 1) {
                List<String> wordInfo = lucMorphEng.getMorphInfo(words[i].replaceAll(REGEX, "").trim());
                for (String word : wordInfo) {
                    if (!getEngInfo(word)) {
                        List<String> wordBaseForms = lucMorphEng.getNormalForms(words[i].replaceAll(REGEX, "").trim());
                        if (!lemmasWords.containsKey(words[i].trim())) {
                            lemmasWords.put(words[i].trim(), wordBaseForms.get(0));
                        } else {  }
                    } else { continue; }
                }
            } else {}
        }

        return lemmasWords;
    }


}
