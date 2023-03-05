package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

/**
 * The Lemmatiser class provides a method to map a given word to its lemma form using a specified lemma file.
 * It reads the lemma file and stores the word-lemma pairs in two arrays.
 * Then, given a word, it searches for the word in the array of words and returns its corresponding lemma.
 */
public class Lemmatiser {

    /**
     * An array that stores the original words from the lemma file.
     */
    private String[] sgWord;

    /**
     * An array that stores the corresponding lemmas for the original words in the lemma file.
     */
    private String[] sgLemma;

    /**
     * The index of the last word in the arrays.
     */
    private int igWordLast;

    /**
     * Constructs a new Lemmatiser object with igWordLast initialized to -1.
     */
    public Lemmatiser() {
        igWordLast = -1;
    }

    /**
     * Initializes the Lemmatiser with the specified lemma file.
     *
     * @param sFileName  The name of the file containing word-lemma pairs.
     * @param bForceUTF8 Whether or not to force UTF-8 encoding for the file.
     * @return true if initialization is successful, false otherwise.
     */
    public boolean initialise(String sFileName, boolean bForceUTF8) {
        int iLinesInFile = 0;
        if (sFileName.equals("")) {
            System.out.println("No lemma file specified!");
            return false;
        }
        File f = new File(sFileName);
        if (!f.exists()) {
            System.out.println("Could not find lemma file: " + sFileName);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFileName);
        if (iLinesInFile < 2) {
            System.out.println("Less than 2 lines in sentiment file: " + sFileName);
            return false;
        }
        sgWord = new String[iLinesInFile + 1];
        sgLemma = new String[iLinesInFile + 1];
        igWordLast = -1;
        try {
            BufferedReader rReader;
            if (bForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFileName), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sFileName));
            String sLine;
            while ((sLine = rReader.readLine()) != null)
                if (!sLine.equals("")) {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if (iFirstTabLocation >= 0) {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        sgWord[++igWordLast] = sLine.substring(0, iFirstTabLocation);
                        if (iSecondTabLocation > 0)
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1, iSecondTabLocation);
                        else
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1);
                        if (sgWord[igWordLast].contains(" "))
                            sgWord[igWordLast] = sgWord[igWordLast].trim();
                        if (sgLemma[igWordLast].contains(" "))
                            sgLemma[igWordLast] = sgLemma[igWordLast].trim();
                    }
                }
            rReader.close();
            Sort.quickSortStringsWithStrings(sgWord, sgLemma, 0, igWordLast);
        } catch (FileNotFoundException e) {
            System.out.println("Couldn't find lemma file: " + sFileName);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("Found lemma file but couldn't read from it: " + sFileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Returns the lemma of a given word. If the word is not found in the lemma
     * dictionary, the original word is returned.
     *
     * @param sWord the word to lemmatise
     * @return the lemma of the word, or the original word if it is not found
     */
    public String lemmatise(String sWord) {
        int iLemmaID = Sort.i_FindStringPositionInSortedArray(sWord, sgWord, 0, igWordLast);
        if (iLemmaID >= 0)
            return sgLemma[iLemmaID];
        else
            return sWord;
    }
}
