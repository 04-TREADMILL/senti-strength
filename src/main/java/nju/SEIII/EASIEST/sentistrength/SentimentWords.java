// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   SentimentWords.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            Corpus, ClassificationOptions

/**
 * This class represents a list of sentiment words used in sentiment analysis.
 * It provides methods for accessing individual sentiment words from the list.
 *
 * UC-1 Assigning Sentiment Scores for Words
 * UC-16 Process stdin and send to stdout
 * UC-17 Location of linguistic data folder
 * UC-18 Location of sentiment term weights
 * UC-27 Optimise sentiment strengths of existing sentiment terms
 * UC-29 Machine learning evaluations
 */
public class SentimentWords
{

    /**
     * An array of sentiment words without the '*' character at the start.
     */
    private String[] sgSentimentWords;

    /**
     * An array of the strength scores associated with each sentiment word in {@link #sgSentimentWords}.
     */
    private int[] igSentimentWordsStrengthTake1;

    /**
     * The number of sentiment words in {@link #sgSentimentWords}.
     */
    private int igSentimentWordsCount;

    /**
     * An array of sentiment words with the '*' character at the start.
     *
     * @see #bgSentimentWordsWithStarAtStartHasStarAtEnd
     */
    private String[] sgSentimentWordsWithStarAtStart;

    /**
     * An array of the strength scores associated with each sentiment word in {@link #sgSentimentWordsWithStarAtStart}.
     */
    private int[] igSentimentWordsWithStarAtStartStrengthTake1;

    /**
     * The number of sentiment words in {@link #sgSentimentWordsWithStarAtStart}.
     */
    private int igSentimentWordsWithStarAtStartCount;

    /**
     * An array indicating whether each sentiment word in {@link #sgSentimentWordsWithStarAtStart} has a '*' character at the end.
     */
    private boolean[] bgSentimentWordsWithStarAtStartHasStarAtEnd;

    /**
     Constructs a new SentimentWords object.
     */
    public SentimentWords()
    {
        igSentimentWordsCount = 0;
        igSentimentWordsWithStarAtStartCount = 0;
    }

    /**
     * Returns the sentiment word with the given ID.
     *
     * @param iWordID the ID of the sentiment word to retrieve
     * @return the sentiment word with the given ID, or an empty string if the ID is invalid
     */
    public String getSentimentWord(int iWordID)
    {
        if(iWordID > 0)
        {
            if(iWordID <= igSentimentWordsCount)
                return sgSentimentWords[iWordID];
            if(iWordID <= igSentimentWordsCount + igSentimentWordsWithStarAtStartCount)
                return sgSentimentWordsWithStarAtStart[iWordID - igSentimentWordsCount];
        }
        return "";
    }

    /**
     * Returns the sentiment strength of a given word. The method first checks if the word is present in the sgSentimentWords array.
     * If it is, the corresponding sentiment strength is returned from the igSentimentWordsStrengthTake1 array.
     * If the word is not present in sgSentimentWords array, the method checks if it matches any of the words with a star at the beginning, and returns the corresponding sentiment strength from the igSentimentWordsWithStarAtStartStrengthTake1 array.
     * If the word is not found in either array, it returns 999.
     * @param sWord the word to find the sentiment strength of
     * @return the sentiment strength of the given word
     */
    public int getSentiment(String sWord)
    {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if(iWordID >= 0)
            return igSentimentWordsStrengthTake1[iWordID];
        int iStarWordID = getMatchingStarAtStartRawWordID(sWord);
        if(iStarWordID >= 0)
            return igSentimentWordsWithStarAtStartStrengthTake1[iStarWordID];
        else
            return 999;
    }

    /**
     * Sets the sentiment value of a given word.
     *
     * @param sWord The word to set the sentiment value for.
     * @param iNewSentiment The new sentiment value for the word.
     * @return True if the sentiment value was successfully set, false otherwise.
     */
    public boolean setSentiment(String sWord, int iNewSentiment)
    {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if(iWordID >= 0)
        {
            if(iNewSentiment > 0)
                setSentiment(iWordID, iNewSentiment - 1);
            else
                setSentiment(iWordID, iNewSentiment + 1);
            return true;
        }
        if(sWord.indexOf("*") == 0)
        {
            sWord = sWord.substring(1);
            if(sWord.indexOf("*") > 0)
                sWord.substring(0, sWord.length() - 1);
        }
        if(igSentimentWordsWithStarAtStartCount > 0)
        {
            for(int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++)
                if(sWord.equals(sgSentimentWordsWithStarAtStart[i]))
                {
                    if(iNewSentiment > 0)
                        setSentiment(igSentimentWordsCount + i, iNewSentiment - 1);
                    else
                        setSentiment(igSentimentWordsCount + i, iNewSentiment + 1);
                    return true;
                }

        }
        return false;
    }

    /**

     Saves a sentiment list to a file.
     @param sFilename the name of the file to save to
     @param c the corpus containing the sentiment list
     @return true if the operation is successful, false otherwise
     */
    public boolean saveSentimentList(String sFilename, Corpus c)
    {
        try
        {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sFilename));
            for(int i = 1; i <= igSentimentWordsCount; i++)
            {
                int iSentimentStrength = igSentimentWordsStrengthTake1[i];
                if(iSentimentStrength < 0)
                    iSentimentStrength--;
                else
                    iSentimentStrength++;
                String sOutput = sgSentimentWords[i] + "\t" + iSentimentStrength + "\n";
                if(c.options.bgForceUTF8) {
                    sOutput = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                }
                wWriter.write(sOutput);
            }

            for(int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++)
            {
                int iSentimentStrength = igSentimentWordsWithStarAtStartStrengthTake1[i];
                if(iSentimentStrength < 0)
                    iSentimentStrength--;
                else
                    iSentimentStrength++;
                String sOutput = "*" + sgSentimentWordsWithStarAtStart[i];
                if(bgSentimentWordsWithStarAtStartHasStarAtEnd[i])
                    sOutput = sOutput + "*";
                sOutput = sOutput + "\t" + iSentimentStrength + "\n";
                if(c.options.bgForceUTF8) {
                    sOutput = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                }
                wWriter.write(sOutput);
            }

            wWriter.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**

     Prints the sentiment values in a single row to a BufferedWriter.
     @param wWriter the BufferedWriter to print to
     @return true if the operation is successful, false otherwise
     */
    public boolean printSentimentValuesInSingleRow(BufferedWriter wWriter)
    {
        try
        {
            for(int i = 1; i <= igSentimentWordsCount; i++)
            {
                int iSentimentStrength = igSentimentWordsStrengthTake1[i];
                wWriter.write("\t" + iSentimentStrength);
            }

            for(int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++)
            {
                int iSentimentStrength = igSentimentWordsWithStarAtStartStrengthTake1[i];
                wWriter.write("\t" + iSentimentStrength);
            }

            wWriter.write("\n");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**

     Prints the sentiment terms in a single header row to a BufferedWriter.
     @param wWriter the BufferedWriter to print to
     @return true if the operation is successful, false otherwise
     */
    public boolean printSentimentTermsInSingleHeaderRow(BufferedWriter wWriter)
    {
        try
        {
            for(int i = 1; i <= igSentimentWordsCount; i++)
                wWriter.write("\t" + sgSentimentWords[i]);

            for(int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++)
            {
                wWriter.write("\t*" + sgSentimentWordsWithStarAtStart[i]);
                if(bgSentimentWordsWithStarAtStartHasStarAtEnd[i])
                    wWriter.write("*");
            }

            wWriter.write("\n");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**

     Retrieves the sentiment score for a given word ID.
     @param iWordID the ID of the word whose sentiment score is to be retrieved.
     @return the sentiment score of the specified word, or 999 if the word ID is negative.
     */
    public int getSentiment(int iWordID)
    {
        if(iWordID > 0)
        {
            if(iWordID <= igSentimentWordsCount)
                return igSentimentWordsStrengthTake1[iWordID];
            else
                return igSentimentWordsWithStarAtStartStrengthTake1[iWordID - igSentimentWordsCount];
        } else
        {
            return 999;
        }
    }

    /**

     Sets the sentiment score for a given word ID.
     @param iWordID the ID of the word whose sentiment score is to be set.
     @param iNewSentiment the new sentiment score to set for the specified word.
     */
    public void setSentiment(int iWordID, int iNewSentiment)
    {
        if(iWordID <= igSentimentWordsCount)
            igSentimentWordsStrengthTake1[iWordID] = iNewSentiment;
        else
            igSentimentWordsWithStarAtStartStrengthTake1[iWordID - igSentimentWordsCount] = iNewSentiment;
    }

    /**

     Retrieves the ID of the word with the given string in the sentiment words list.
     @param sWord the string of the word whose ID is to be retrieved.
     @return the ID of the word with the specified string, or -1 if the word is not found in the sentiment words list.
     */
    public int getSentimentID(String sWord)
    {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if(iWordID >= 0)
            return iWordID;
        iWordID = getMatchingStarAtStartRawWordID(sWord);
        if(iWordID >= 0)
            return iWordID + igSentimentWordsCount;
        else
            return -1;
    }

    /**

     Retrieves the ID of the word in the "sentiment words with star at start" list that matches the given string.
     @param sWord the string of the word to match.
     @return the ID of the word that matches the specified string, or -1 if no match is found.
     */
    private int getMatchingStarAtStartRawWordID(String sWord)
    {
        int iSubStringPos = 0;
        if(igSentimentWordsWithStarAtStartCount > 0)
        {
            for(int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++)
            {
                iSubStringPos = sWord.indexOf(sgSentimentWordsWithStarAtStart[i]);
                if(iSubStringPos >= 0 && (bgSentimentWordsWithStarAtStartHasStarAtEnd[i] || iSubStringPos + sgSentimentWordsWithStarAtStart[i].length() == sWord.length()))
                    return i;
            }

        }
        return -1;
    }

    /**

     Get the count of sentiment words.
     @return the count of sentiment words.
     */
    public int getSentimentWordCount()
    {
        return igSentimentWordsCount;
    }

    /**
     * Initialize the sentiment analyzer with a sentiment file.
     * @param sFilename the path of the sentiment file.
     * @param options the classification options.
     * @param iExtraBlankArrayEntriesToInclude the number of extra blank array entries to include.
     * @return true if initialization is successful; false otherwise.
     */
    public boolean initialise(String sFilename, ClassificationOptions options, int iExtraBlankArrayEntriesToInclude)
    {
        int iWordStrength = 0;
        int iWordsWithStarAtStart = 0;
        if(Objects.equals(sFilename, ""))
        {
            System.out.println("No sentiment file specified");
            return false;
        }
        File f = new File(sFilename);
        if(!f.exists())
        {
            System.out.println("Could not find sentiment file: " + sFilename);
            return false;
        }
        int iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
        if(iLinesInFile < 2)
        {
            System.out.println("Less than 2 lines in sentiment file: " + sFilename);
            return false;
        }
        igSentimentWordsStrengthTake1 = new int[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        sgSentimentWords = new String[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        igSentimentWordsCount = 0;
        try
        {
            BufferedReader rReader;
            if(options.bgForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFilename), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sFilename));
            String sLine;
            while((sLine = rReader.readLine()) != null) 
                if(!sLine.equals(""))
                    if(sLine.indexOf("*") == 0)
                    {
                        iWordsWithStarAtStart++;
                    } else
                    {
                        int iFirstTabLocation = sLine.indexOf("\t");
                        if(iFirstTabLocation >= 0)
                        {
                            int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                            try
                            {
                                if(iSecondTabLocation > 0)
                                    iWordStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1, iSecondTabLocation).trim());
                                else
                                    iWordStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1).trim());
                            }
                            catch(NumberFormatException e)
                            {
                                System.out.println("Failed to identify integer weight for sentiment word! Ignoring word\nLine: " + sLine);
                                iWordStrength = 0;
                            }
                            sLine = sLine.substring(0, iFirstTabLocation);
                            if(sLine.contains(" "))
                                sLine = sLine.trim();
                            if(!sLine.equals(""))
                            {
                                sgSentimentWords[++igSentimentWordsCount] = sLine;
                                if(iWordStrength > 0)
                                    iWordStrength--;
                                else
                                if(iWordStrength < 0)
                                    iWordStrength++;
                                igSentimentWordsStrengthTake1[igSentimentWordsCount] = iWordStrength;
                            }
                        }
                    }
            rReader.close();
            Sort.quickSortStringsWithInt(sgSentimentWords, igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Couldn't find sentiment file: " + sFilename);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found sentiment file but couldn't read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        if(iWordsWithStarAtStart > 0)
            return initialiseWordsWithStarAtStart(sFilename, options, iWordsWithStarAtStart, iExtraBlankArrayEntriesToInclude);
        else
            return true;
    }

    /**

     * Initializes sentiment words with a star at the beginning from a file and stores them in arrays.
     * @param sFilename the filename of the sentiment words file
     * @param options an instance of ClassificationOptions
     * @param iWordsWithStarAtStart the number of words with a star at the beginning to initialize
     * @param iExtraBlankArrayEntriesToInclude the number of extra blank array entries to include
     * @return true if initialization is successful, false otherwise
     */
    public boolean initialiseWordsWithStarAtStart(String sFilename, ClassificationOptions options, int iWordsWithStarAtStart, int iExtraBlankArrayEntriesToInclude)
    {
        int iWordStrength = 0;
        File f = new File(sFilename);
        if(!f.exists())
        {
            System.out.println("Could not find sentiment file: " + sFilename);
            return false;
        }
        igSentimentWordsWithStarAtStartStrengthTake1 = new int[iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        sgSentimentWordsWithStarAtStart = new String[iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        bgSentimentWordsWithStarAtStartHasStarAtEnd = new boolean[iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        igSentimentWordsWithStarAtStartCount = 0;
        try
        {
            BufferedReader rReader;
            if(options.bgForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFilename), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sFilename));
            while(rReader.ready()) 
            {
                String sLine = rReader.readLine();
                if(!Objects.equals(sLine, "") && sLine.indexOf("*") == 0)
                {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if(iFirstTabLocation >= 0)
                    {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        try
                        {
                            if(iSecondTabLocation > 0)
                                iWordStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1, iSecondTabLocation));
                            else
                                iWordStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1));
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("Failed to identify integer weight for *sentiment* word! Ignoring word\nLine: " + sLine);
                            iWordStrength = 0;
                        }
                        sLine = sLine.substring(1, iFirstTabLocation);
                        if(sLine.indexOf("*") > 0)
                        {
                            sLine = sLine.substring(0, sLine.indexOf("*"));
                            bgSentimentWordsWithStarAtStartHasStarAtEnd[++igSentimentWordsWithStarAtStartCount] = true;
                        } else
                        {
                            bgSentimentWordsWithStarAtStartHasStarAtEnd[++igSentimentWordsWithStarAtStartCount] = false;
                        }
                        if(sLine.contains(" "))
                            sLine = sLine.trim();
                        if(!sLine.equals(""))
                        {
                            sgSentimentWordsWithStarAtStart[igSentimentWordsWithStarAtStartCount] = sLine;
                            if(iWordStrength > 0)
                                iWordStrength--;
                            else
                            if(iWordStrength < 0)
                                iWordStrength++;
                            igSentimentWordsWithStarAtStartStrengthTake1[igSentimentWordsWithStarAtStartCount] = iWordStrength;
                        } else
                        {
                            igSentimentWordsWithStarAtStartCount--;
                        }
                    }
                }
            }
            rReader.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Couldn't find *sentiment file*: " + sFilename);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found *sentiment file* but couldn't read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**

     Adds or modifies a sentiment term in the sentiment words array.
     If the term already exists, it modifies its strength, otherwise it adds it to the array.
     @param sTerm the sentiment term to add or modify
     @param iTermStrength the strength of the sentiment term
     @param bSortSentimentListAfterAddingTerm whether to sort the sentiment list after adding the term
     @return true if the term was added or modified successfully, false otherwise
     */
    public boolean addOrModifySentimentTerm(String sTerm, int iTermStrength, boolean bSortSentimentListAfterAddingTerm)
    {
        int iTermPosition = getSentimentID(sTerm);
        if(iTermPosition > 0)
        {
            if(iTermStrength > 0)
                iTermStrength--;
            else
            if(iTermStrength < 0)
                iTermStrength++;
            igSentimentWordsStrengthTake1[iTermPosition] = iTermStrength;
        } else
        {
            try
            {
                sgSentimentWords[++igSentimentWordsCount] = sTerm;
                if(iTermStrength > 0)
                    iTermStrength--;
                else
                if(iTermStrength < 0)
                    iTermStrength++;
                igSentimentWordsStrengthTake1[igSentimentWordsCount] = iTermStrength;
                if(bSortSentimentListAfterAddingTerm)
                    Sort.quickSortStringsWithInt(sgSentimentWords, igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
            }
            catch(Exception e)
            {
                System.out.println("Could not add extra sentiment term: " + sTerm);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**

     Sorts the sentiment words array.
     */
    public void sortSentimentList()
    {
        Sort.quickSortStringsWithInt(sgSentimentWords, igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
    }
}
