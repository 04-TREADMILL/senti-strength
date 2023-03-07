// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   Lemmatiser.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

/**
 * This class implements a lemmatizer, which is used to reduce words to their base or dictionary form,
 * in order to improve the accuracy of text analysis.
 * It reads a file containing a list of word-lemma pairs, and uses them to lemmatize input words.
 *
 * @UC
 * <p><ul>
 * <li> UC-17 Location of linguistic data folder
 * <li> UC-18 Location of sentiment term weights
 * <li> UC-19 Location of output folder
 * <li> UC-20 File name extension for output
 * </ul>
 * <p>
 */
public class Lemmatiser
{

    private String[] sgWord;
    private String[] sgLemma;
    private int igWordLast;

    public Lemmatiser()
    {
        igWordLast = -1;
    }

    /**
     * Initializes the lemmatizer with a lemma file.
     *
     * @param sFileName   the name of the lemma file to be used
     * @param bForceUTF8  specifies whether the file should be read as UTF-8 or not
     * @return true if the initialization was successful, false otherwise
     */
    public boolean initialise(String sFileName, boolean bForceUTF8)
    {
        int iLinesInFile = 0;
        if(sFileName.equals(""))
        {
            System.out.println("No lemma file specified!");
            return false;
        }
        File f = new File(sFileName);
        if(!f.exists())
        {
            System.out.println("Could not find lemma file: " + sFileName);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFileName);
        if(iLinesInFile < 2)
        {
            System.out.println("Less than 2 lines in sentiment file: " + sFileName);
            return false;
        }
        sgWord = new String[iLinesInFile + 1];
        sgLemma = new String[iLinesInFile + 1];
        igWordLast = -1;
        try
        {
            BufferedReader rReader;
            if(bForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sFileName), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sFileName));
            String sLine;
            while((sLine = rReader.readLine()) != null) 
                if(!sLine.equals(""))
                {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if(iFirstTabLocation >= 0)
                    {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        sgWord[++igWordLast] = sLine.substring(0, iFirstTabLocation);
                        if(iSecondTabLocation > 0)
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1, iSecondTabLocation);
                        else
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1);
                        if(sgWord[igWordLast].contains(" "))
                            sgWord[igWordLast] = sgWord[igWordLast].trim();
                        if(sgLemma[igWordLast].contains(" "))
                            sgLemma[igWordLast] = sgLemma[igWordLast].trim();
                    }
                }
            rReader.close();
            Sort.quickSortStringsWithStrings(sgWord, sgLemma, 0, igWordLast);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Couldn't find lemma file: " + sFileName);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found lemma file but couldn't read from it: " + sFileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Maps a word to its lemma form.
     * @param sWord the word to be lemmatised
     * @return the lemma form of the word if it is found in the lemma file, otherwise returns the original word
     */
    public String lemmatise(String sWord)
    {
        int iLemmaID = Sort.i_FindStringPositionInSortedArray(sWord, sgWord, 0, igWordLast);
        if(iLemmaID >= 0)
            return sgLemma[iLemmaID];
        else
            return sWord;
    }
}
