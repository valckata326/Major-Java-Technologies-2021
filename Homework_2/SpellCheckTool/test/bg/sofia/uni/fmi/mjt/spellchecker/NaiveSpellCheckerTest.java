package bg.sofia.uni.fmi.mjt.spellchecker;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NaiveSpellCheckerTest {
    private static final String dictionaryWords = """
                        
            valio
            123
            hello
            you
            there
            Nostradamus
            football
            ball12-3
            asdf
            EnglishMan
            New
            York
            pitch
            stupid
            FMI
            JAVA
            """;
    private static final String stopwords = """
            is
            a
            to
            be
            or
            not
            the
            there
            there's
            are
            he
            she
            it
            I
            who
            be
            to
            val
            """;
    private SpellChecker testSpellChecker;

    @Before
    public void createSpellChecker() {
        testSpellChecker = new NaiveSpellChecker(new StringReader(dictionaryWords),
                new StringReader(stopwords));

    }

    @Test(expected = NullPointerException.class)
    public void testAnalyzeWithNullText() {
        String text = null;
        int suggestionCount = 3;
        Reader textReader = new StringReader(text);
        Writer returnMessage = new StringWriter();
        testSpellChecker.analyze(textReader, returnMessage, suggestionCount);
    }

    @Test
    public void testAnalyzeWithEmptyText() {
        String text = "";
        int suggestionCount = 2;
        Writer returnMessage = new StringWriter();
        testSpellChecker.analyze(new StringReader(text), returnMessage, suggestionCount);
        String expected = System.lineSeparator()
                + "= = = Metadata = = =" + System.lineSeparator()
                + new Metadata(0, 0, 0).formattedMetadata() + System.lineSeparator()
                + "= = = Findings = = =" + System.lineSeparator();
        assertEquals(expected, returnMessage.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAnalyzeWithNegativeSuggestionCounter() {
        String reader = "";
        Reader textReader = new StringReader(reader);
        Writer writer = new StringWriter();
        testSpellChecker.analyze(textReader, writer, -3);
    }

    @Test(expected = IOException.class)
    public void testAnalyzeWithInvalidTextReader() throws FileNotFoundException {
        Writer writer = new StringWriter();
        Reader textReader = new FileReader("invalid path name");
        testSpellChecker.analyze(textReader, writer, 3);
    }

    @Test
    public void testAnalyzeWithTextContainingOnlySymbols() {
        String reader = "^@#&^& ^$&#^$ ^#*&$^" + System.lineSeparator()
                + "     $#$#@$ $ (()) )     ";
        Writer writer = new StringWriter();
        String expected = "^@#&^& ^$&#^$ ^#*&$^" + System.lineSeparator()
                + "     $#$#@$ $ (()) )     " + System.lineSeparator()
                + "= = = Metadata = = =" + System.lineSeparator()
                + "30 characters, 0 words, 0 spelling issue(s) found" + System.lineSeparator()
                + "= = = Findings = = =" + System.lineSeparator();
        testSpellChecker.analyze(new StringReader(reader), writer, 3);
        assertEquals(expected, writer.toString());
    }

    @Test
    public void testAnalyzeWithSimpleText() {
        String reader = "Studying JaVa in fmi feels like" + System.lineSeparator()
                + "englishMan in NEW YORK";
        Writer writer = new StringWriter();
        testSpellChecker.analyze(new StringReader(reader), writer, 2);
        String expected = "Studying JaVa in fmi feels like" + System.lineSeparator()
                + "englishMan in NEW YORK" + System.lineSeparator()
                + "= = = Metadata = = =" + System.lineSeparator()
                + "45 characters, 10 words, 5 spelling issue(s) found" + System.lineSeparator()
                + "= = = Findings = = =" + System.lineSeparator()
                + "Line #1, {feels} - Possible suggestions are {hello}" + System.lineSeparator()
                + "Line #1, {studying} - Possible suggestions are {stupid, englishman}" + System.lineSeparator()
                + "Line #1, {in} - Possible suggestions are {}" + System.lineSeparator()
                + "Line #1, {like} - Possible suggestions are {valio, englishman}" + System.lineSeparator()
                + "Line #2, {in} - Possible suggestions are {}";
        assertEquals(expected, writer.toString());
    }

    @Test
    public void testAnalyzeWithComplicateText() {
        String reader = "D^ar assistant, " + System.lineSeparator()
                + "He (Valio) is$ currently struggling with @jaVA" + System.lineSeparator()
                + "while \"123\" is playing foot0ball on the new pitch" + System.lineSeparator()
                + "            #e%%y Christmas!";
        Writer writer = new StringWriter();
        testSpellChecker.analyze(new StringReader(reader), writer, 3);
        String expected = "D^ar assistant, " + System.lineSeparator()
                + "He (Valio) is$ currently struggling with @jaVA" + System.lineSeparator()
                + "while \"123\" is playing foot0ball on the new pitch" + System.lineSeparator()
                + "            #e%%y Christmas!" + System.lineSeparator()
                + "= = = Metadata = = =" + System.lineSeparator()
                + "110 characters, 16 words, 11 spelling issue(s) found" + System.lineSeparator()
                + "= = = Findings = = =" + System.lineSeparator()
                + "Line #1, {d^ar} - Possible suggestions are {}" + System.lineSeparator()
                + "Line #1, {assistant} - Possible suggestions are {englishman, asdf, stupid}"
                + System.lineSeparator()
                + "Line #2, {with} - Possible suggestions are {pitch}" + System.lineSeparator()
                + "Line #2, {currently} - Possible suggestions are {there, englishman}" + System.lineSeparator()
                + "Line #2, {struggling} - Possible suggestions are {englishman, nostradamus, valio}"
                + System.lineSeparator()
                + "Line #3, {playing} - Possible suggestions are {englishman}" + System.lineSeparator()
                + "Line #3, {while} - Possible suggestions are {}" + System.lineSeparator()
                + "Line #3, {foot0ball} - Possible suggestions are {football, ball12-3, valio}"
                + System.lineSeparator()
                + "Line #3, {on} - Possible suggestions are {}" + System.lineSeparator()
                + "Line #4, {e%%y} - Possible suggestions are {}" + System.lineSeparator()
                + "Line #4, {christmas} - Possible suggestions are {englishman, asdf, pitch}";
        assertEquals(expected, writer.toString());
    }

    @Test
    public void testMetadataWithSimpleText() {
        String testString = "Let's check if this text returns right:" + System.lineSeparator()
                + "characters, words, mistakes in this order";
        Reader reader = new StringReader(testString);
        Metadata metadata = testSpellChecker.metadata(reader);
        Metadata expected = new Metadata(69, 13, 13);
        assertEquals(expected, metadata);
    }

    @Test
    public void testMetadataWithEmptyText() {
        String testString = "";
        Reader reader = new StringReader(testString);
        Metadata metadata = testSpellChecker.metadata(reader);
        Metadata expected = new Metadata(0, 0, 0);
        assertEquals(expected, metadata);
    }

    @Test
    public void testMetadataWithSymbolsAndWhitespacesOnlyText() {
        String testString = "^#$&*^&$# $&#*^$*# #$" + System.lineSeparator()
                + "$# # )( -= ___#          ";
        Reader reader = new StringReader(testString);
        Metadata metadata = testSpellChecker.metadata(reader);
        Metadata expected = new Metadata(30, 0, 0);
        assertEquals(expected, metadata);
    }

    @Test
    public void testMetadataWithComplicatedText() {
        String firstTest = "####!a 12)##### A****!  " + System.lineSeparator()
                + "()()() 23 abc 09abc *123#";
        Reader firstReader = new StringReader(firstTest);
        Metadata firstMeta = testSpellChecker.metadata(firstReader);
        Metadata firstExpected = new Metadata(41, 5, 4);
        assertEquals(firstExpected, firstMeta);

        String secondTest = "wrong wrong script" + System.lineSeparator()
                + "hope it does not fail the test";
        Reader secondReader = new StringReader(secondTest);
        Metadata secondMeta = testSpellChecker.metadata(secondReader);
        Metadata secondExpected = new Metadata(40, 7, 7);
        assertEquals(secondExpected, secondMeta);

        String thirdTest = "to be or not to be";
        Reader thirdReader = new StringReader(thirdTest);
        Metadata thirdData = testSpellChecker.metadata(thirdReader);
        Metadata thirdExpected = new Metadata(13, 0, 0);
        assertEquals(thirdExpected, thirdData);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindClosestWordWithNull() {
        int suggestedWords = 2;
        testSpellChecker.findClosestWords(null, suggestedWords);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindClosestWordsWithNegativeCounter() {
        String exampleString = "Valio";
        int suggestedWords = -2;
        testSpellChecker.findClosestWords(exampleString, suggestedWords);
    }

    @Test
    public void testFindClosestWordsWithOneDigitWord() {
        String word = "v";
        List<String> closestWords = testSpellChecker.findClosestWords(word, 3);
        assertEquals(0, closestWords.size());
        assertTrue(closestWords.isEmpty());
    }

    @Test
    public void testFindClosestWordsForWordOfSymbols() {
        String word = " @$#%%$% ";
        List<String> closestWords = testSpellChecker.findClosestWords(word, 1);
        assertEquals(0, closestWords.size());
    }

    @Test
    public void testFindClosestWordsWithRegularWords() {
        String firstWord = "valioo";
        List<String> firstWordList = testSpellChecker.findClosestWords(firstWord, 1);
        assertTrue(firstWordList.contains("valio"));
        assertEquals(1, firstWordList.size());

        String secondWord = "ball";
        List<String> secondWordList = testSpellChecker.findClosestWords(secondWord, 2);
        assertTrue(secondWordList.contains("football"));
        assertTrue(secondWordList.contains("valio"));
        assertEquals(2, secondWordList.size());

        String thirdWord = "out";
        List<String> thirdWordList = testSpellChecker.findClosestWords(thirdWord, 1);
        assertTrue(thirdWordList.contains("you"));
        assertEquals(1, thirdWordList.size());
    }

    @Test
    public void testCleanUpFunction() {
        StringReworker reworker = new NaiveSpellChecker(new StringReader(dictionaryWords),
                new StringReader(stopwords));
        String testCleanUp = "   &#jAVa-123*#&*$#)(    ";
        String actual = reworker.cleanUpString(testCleanUp);
        String expected = "java-123";
        assertEquals(expected, actual);
    }

    @Test
    public void testGetWordGramsMethod() {
        String word = "getWordGramsGET";
        StringReworker reworker = new NaiveSpellChecker(new StringReader(dictionaryWords),
                new StringReader(stopwords));
        Map<String, Integer> wordGrams = reworker.getWordGrams(word);
        assertEquals(12, wordGrams.size());
        assertEquals(2, (long) wordGrams.get("ge"));
        assertEquals(2, (long) wordGrams.get("et"));
    }

    @Test
    public void testGetVectorLengthMethod() {
        String firstWord = "length";
        String secondWord = "getGET";
        StringReworker reworker = new NaiveSpellChecker(new StringReader(dictionaryWords),
                new StringReader(stopwords));
        double firstWordLength = reworker.getVectorLength(firstWord);
        double secondWordLength = reworker.getVectorLength(secondWord);
        assertEquals(Math.sqrt(5), firstWordLength, 0.0001);
        assertEquals(3.0, secondWordLength, 0.0001);
    }
}
