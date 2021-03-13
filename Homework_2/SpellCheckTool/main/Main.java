import bg.sofia.uni.fmi.mjt.spellchecker.Metadata;
import bg.sofia.uni.fmi.mjt.spellchecker.NaiveSpellChecker;
import bg.sofia.uni.fmi.mjt.spellchecker.SpellChecker;

import java.io.*;

import java.util.List;

public class Main {
    public static void main(String[] argc) throws IOException {
        /*var spellChecker = new NaiveSpellChecker(new FileReader("./resources/dictionary.txt"),
                new FileReader("./resources/stopwords.txt"));
        System.out.println("test");
        //System.out.println(spellChecker.getDictionaryWordGrams().containsKey("man's"));
        //Metadata metadata = spellChecker.metadata(new FileReader("./resources/test.txt"));
        //List<String> test = spellChecker.findClosestWords("valio", 5);
        //System.out.println("hopeItWorks");
        //double multiplication = spellChecker.getVectorMultiplication("s", "abalone");
        //double wordLength = spellChecker.getVectorLength("s");
        //double dictionaryWordLength = spellChecker.getVectorLength("abalone");
        //System.out.println(multiplication / (wordLength * dictionaryWordLength));
        /*spellChecker.analyze(new FileReader("./resources/test.txt"),
                new FileWriter("./resources/test2.txt"),3);
        */
        String dictionary = """
            asd1
            sd1KKK
            freE
            trEe
            hay
            they
             asd2
            asd3 
            $asd4
            asd5$
            mechka
            chka
            ka
            bong
            rong
            d
            """;
            String stopwordList = """
            sw1
            sw2
            sw3
            """;
        SpellChecker testSpellChecker;
        String text = """
                --- wrongword asd1 $asd2 sw1 sw2 sw3 sw1 sw1
                d
                """;
        testSpellChecker = new NaiveSpellChecker(
                new StringReader(dictionary),
                new StringReader(stopwordList));
        Metadata meta = testSpellChecker.metadata(new StringReader(text));
    }

}
