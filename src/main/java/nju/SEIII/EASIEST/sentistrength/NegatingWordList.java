package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

/**
 * This class represents a list of negating words used in sentiment analysis.
 * It provides methods to initialize the list from a file and to check if a given word is a negating word.
 */
public class NegatingWordList {

    /**
     * Array to store negating words.
     */
    private String[] sgNegatingWord;

    /**
     * Count of negating words.
     */
    private int igNegatingWordCount;

    /**
     * Capacity of negating word.
     */
    private int igNegatingWordMax;

    /**
     * Creates a new NegatingWordList object with initial values for the counters set to 0.
     */
    public NegatingWordList() {
        igNegatingWordCount = 0;
        igNegatingWordMax = 0;
    }

    /**
     * Initializes the negating word list from a file.
     *
     * @param sFilename the name of the file containing the negating words
     * @param options   an object of ClassificationOptions class with options for initializing the list
     * @return true if initialization is successful, false otherwise
     */
    public boolean initialise(String sFilename, ClassificationOptions options) {
        if (igNegatingWordMax > 0)
            return true;
        File f = new File(sFilename);
        if (!f.exists()) {
            System.out.println("Could not find the negating words file: " + sFilename);
            return false;
        }
        igNegatingWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgNegatingWord = new String[igNegatingWordMax];
        igNegatingWordCount = 0;
        try {
            BufferedReader rReader;
            if (options.bgForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFilename), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sFilename));
            String sLine;
            while ((sLine = rReader.readLine()) != null)
                if (!sLine.equals("")) {
                    igNegatingWordCount++;
                    sgNegatingWord[igNegatingWordCount] = sLine;
                }
            rReader.close();
            Sort.quickSortStrings(sgNegatingWord, 1, igNegatingWordCount);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find negating words file: " + sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("Found negating words file but could not read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks if a given word is a negating word by searching for it in the negating word list.
     *
     * @param sWord the word to be checked
     * @return true if the word is a negating word, false otherwise
     */
    public boolean negatingWord(String sWord) {
        return Sort.i_FindStringPositionInSortedArray(sWord, sgNegatingWord, 1, igNegatingWordCount) >= 0;
    }
}
