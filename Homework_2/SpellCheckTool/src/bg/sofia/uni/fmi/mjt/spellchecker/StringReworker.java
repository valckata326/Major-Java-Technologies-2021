package bg.sofia.uni.fmi.mjt.spellchecker;

import java.util.HashMap;
import java.util.Map;

public interface StringReworker {
    default String cleanUpString(String toClean) {
        return toClean.toLowerCase()
                .trim()
                .replaceAll("^[^a-zA-Z0-9\\s]+|[^a-zA-Z0-9\\s]+$", "");

    }

    default Map<String, Integer> getWordGrams(String word) {
        word = cleanUpString(word);
        Map<String, Integer> returnMap = new HashMap<>();
        for (int i = 0; i < word.length() - 1; i++) {
            String currGram = String.valueOf(word.charAt(i)) + word.charAt(i + 1);
            int count = returnMap.getOrDefault(currGram, 0);
            returnMap.put(currGram, count + 1);
        }
        return returnMap;
    }

    default double getVectorLength(String word) {
        var getWordGram = getWordGrams(word);
        double sum = 0;
        for (String currGram : getWordGram.keySet()) {
            sum = sum + Math.pow(getWordGram.get(currGram), 2);
        }
        return Math.sqrt(sum);
    }

    default double getVectorMultiplication(String word, String dictionaryWord,
                                           Map<String, Map<String, Integer>> dictionaryWordGrams) {
        var getWordGram = getWordGrams(word);
        double multiplication = 0;
        for (String currWordGram : getWordGram.keySet()) {
            if (dictionaryWordGrams.get(dictionaryWord).containsKey(currWordGram)) {
                multiplication += getWordGram.get(currWordGram)
                        * dictionaryWordGrams.get(dictionaryWord).get(currWordGram);
            }
        }
        return multiplication;
    }
}
