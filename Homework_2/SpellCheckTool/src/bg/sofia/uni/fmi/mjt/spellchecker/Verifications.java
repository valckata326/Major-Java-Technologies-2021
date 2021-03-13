package bg.sofia.uni.fmi.mjt.spellchecker;

import java.io.Reader;

public interface Verifications {
    default void verifyNullDictionary(Reader dictionaryReader) {
        if (dictionaryReader == null) {
            String exceptionMessage = "Dictionary is null";
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    default void verifyNullStopwords(Reader stopwordsReader) {
        if (stopwordsReader == null) {
            String exceptionMessage = "Stopwords is null";
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    default void verifyNullText(Reader textReader) {
        if (textReader == null) {
            String exceptionMessage = "Text is null";
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    default void validateSuggestedWords(int suggestionsCount) {
        if (suggestionsCount < 0) {
            String exceptionMessage = "Number of suggestions is non-negative";
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    default void validateNullWord(String word) {
        if (word == null) {
            String exceptionMessage = "Cannot find closest words of null word";
            throw new IllegalArgumentException(exceptionMessage);
        }
    }
}
