package bg.sofia.uni.fmi.mjt.spellchecker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NaiveSpellChecker implements SpellChecker, Verifications, StringReworker {
    private Map<String, Map<String, Integer>> dictionaryWordGrams; // <word, <gram, gramCount>>
    private Set<String> stopwords;
    private Map<String, Map<String, Integer>> gramsToWords; //<gram, <word, gramCount>>
    private Set<String> wrongWords;

    /**
     * Creates a new instance of NaiveSpellCheckTool, based on a dictionary of words and stop words
     *
     * @param dictionaryReader a java.io.Reader input stream containing list of words which
     *                         will serve as a dictionary for the tool
     * @param stopwordsReader  a java.io.Reader input stream containing list of stopwords
     */

    public NaiveSpellChecker(Reader dictionaryReader, Reader stopwordsReader) {
        verifyNullDictionary(dictionaryReader);
        verifyNullStopwords(stopwordsReader);
        reworkDictionary(dictionaryReader);
        reworkStopwords(stopwordsReader);
        gramsToWords = new HashMap<>();
        setGramsToWords();
    }

    @Override
    public void analyze(Reader textReader, Writer output, int suggestionsCount) {
        verifyNullText(textReader);
        validateSuggestedWords(suggestionsCount);
        try (var bufferedReader = new BufferedReader(textReader)) {
            List<String> textReaderToList = bufferedReader.lines().collect(Collectors.toList());
            String returnString = createAnalyzeReportMessage(textReaderToList, suggestionsCount);
            try (var bufferedWriter = new BufferedWriter(output)) {
                bufferedWriter.write(returnString);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Metadata metadata(Reader textReader) {
        verifyNullText(textReader);
        try (var bufferedReader = new BufferedReader(textReader)) {
            List<String> textReaderToList = bufferedReader.lines().collect(Collectors.toList());
            wrongWords = new HashSet<>();
            return createMetadata(textReaderToList);
        } catch (IOException exception) {
            throw new IllegalStateException("An error occurred when reading the text", exception);
        }
    }

    @Override
    public List<String> findClosestWords(String word, int n) {
        validateNullWord(word);
        validateSuggestedWords(n);
        Set<String> similarWords = new HashSet<>();
        var thisWordGrams = getWordGrams(word);
        for (String currGram : thisWordGrams.keySet()) {
            if (gramsToWords.containsKey(currGram)) {
                similarWords.addAll(gramsToWords.get(currGram).keySet());
            }
        }
        Map<Double, String> similarityMap = new TreeMap<>(Collections.reverseOrder());
        calculateSimularity(word, similarWords, similarityMap);
        int counter = 1;
        List<String> suggestedWords = new ArrayList<>();
        for (Map.Entry<Double, String> currEntry : similarityMap.entrySet()) {
            if (counter > n) {
                break;
            }
            suggestedWords.add(currEntry.getValue());
            counter++;
        }
        return suggestedWords;
    }

    private void setGramsToWords() {
        for (String currWord : dictionaryWordGrams.keySet()) {
            for (String currGram : dictionaryWordGrams.get(currWord).keySet()) {
                if (!gramsToWords.containsKey(currGram)) {
                    gramsToWords.put(currGram, new TreeMap<>());
                }
                gramsToWords.get(currGram).put(currWord,
                        dictionaryWordGrams.get(currWord).get(currGram));
            }
        }
    }


    private String wrongWordsLineCreator(String word, int line, int suggestionsCount) {
        String toReturn = "Line #" + line
                + ", {" + word + "} - Possible suggestions are {";
        List<String> suggestedWords = findClosestWords(word, suggestionsCount);
        if (suggestedWords.isEmpty()) {
            return toReturn + "}";
        }
        for (int i = 0; i < suggestedWords.size() - 1; i++) {
            toReturn = toReturn + suggestedWords.get(i) + ", ";
        }
        toReturn = toReturn + suggestedWords.get(suggestedWords.size() - 1) + "}";
        return toReturn;
    }

    private void reworkDictionary(Reader dictionaryReader) {
        dictionaryWordGrams = new HashMap<>();
        try (var bufferedReader = new BufferedReader(dictionaryReader)) {
            List<String> readerToList = bufferedReader.lines().collect(Collectors.toList());
            List<String> cleanedList = readerToList.stream()
                    .map(this::cleanUpString)
                    .filter(x -> x.length() > 1)
                    .collect(Collectors.toList());
            for (String curr : cleanedList) {
                dictionaryWordGrams.put(curr, new HashMap<>(getWordGrams(curr)));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("An error occurred when reading from dictionary", exception);
        }
    }

    private void reworkStopwords(Reader stopwordsReader) {
        stopwords = new TreeSet<>();
        try (var bufferedReader = new BufferedReader(stopwordsReader)) {
            List<String> readerToList = bufferedReader.lines().collect(Collectors.toList());
            stopwords = readerToList.stream()
                    .map(String::toLowerCase)
                    .map(String::trim)
                    .collect(Collectors.toSet());

        } catch (IOException exception) {
            throw new IllegalStateException("An error occurred when reading from stopwords", exception);
        }
    }

    private String createAnalyzeReportMessage(List<String> textReaderToList, int suggestionsCount) {
        String returnString = String.join(System.lineSeparator(), textReaderToList)
                .concat(System.lineSeparator());
        returnString = returnString.concat("= = = Metadata = = =" + System.lineSeparator());
        Metadata textReaderMetadata;
        textReaderMetadata = metadata(new StringReader(String.join(System.lineSeparator(), textReaderToList)));
        returnString = returnString.concat(textReaderMetadata.formattedMetadata() + System.lineSeparator());
        returnString = returnString.concat("= = = Findings = = =" + System.lineSeparator());
        int lineCounter = 1;
        List<String> wrongWordLines = new ArrayList<>();
        for (String currLine : textReaderToList) {
            for (String currWrongWord : wrongWords) {
                if (currLine.toLowerCase().contains(currWrongWord)) {
                    wrongWordLines.add(wrongWordsLineCreator(currWrongWord, lineCounter, suggestionsCount));
                }
            }
            lineCounter++;
        }
        String wrongWordLinesTogether = wrongWordLines.stream()
                .collect(Collectors.joining(System.lineSeparator()));
        returnString = returnString + wrongWordLinesTogether;
        return returnString;
    }

    private Metadata createMetadata(List<String> textReaderToList) {
        int characters = 0;
        int words = 0;
        int mistakes = 0;
        List<String> textReaderWords = textReaderToList.stream()
                .flatMap(line -> Stream.of(line.split("\\s+")))
                .map(this::cleanUpString)
                .filter(x -> !x.isBlank())
                .collect(Collectors.toList());
        for (String curr : textReaderWords) {
            if (!stopwords.contains(curr)) {
                words++;
            }
            if (!stopwords.contains(curr) && !dictionaryWordGrams.containsKey(curr)) {
                mistakes++;
                wrongWords.add(curr);
            }
        }
        for (String curr : textReaderToList) {
            curr = curr.replaceAll("\\s+", "");
            characters = characters + curr.length();
        }
        return new Metadata(characters, words, mistakes);
    }

    private void calculateSimularity(String word, Set<String> similarWords, Map<Double, String> similarityMap) {
        double wordLength = getVectorLength(word);
        for (String currWord : similarWords) {
            double currWordLength = getVectorLength(currWord);
            double multiplication = getVectorMultiplication(word, currWord, dictionaryWordGrams);
            double result = multiplication / (wordLength * currWordLength);
            similarityMap.put(result, currWord);
        }
    }

}
