// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   QuestionWords.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;

import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

/**

 A class to represent a collection of question words.
 */

/**
 * The QuestionWords class represents a list of question words and provides functionality to check if a given word is a question word.
 */
public class QuestionWords
{

    /**

     An array to store the question words.
     */
    private String[] sgQuestionWord;
    /**

     The number of question words currently in the array.
     */
    private int igQuestionWordCount;
    /**

     The maximum number of question words that can be stored in the array.
     */
    private int igQuestionWordMax;

    /**

     Constructs a new QuestionWords object with initial values for the counts and maximum size.
     */
    public QuestionWords()
    {
        igQuestionWordCount = 0;
        igQuestionWordMax = 0;
    }

    /**
     * Initializes the question words array from a file and returns true if successful.
     *
     * @param sFilename the filename of the file containing the question words
     * @param options   an instance of the ClassificationOptions class
     * @return true if the initialization was successful, false otherwise
     */
    public boolean initialise(String sFilename, ClassificationOptions options)
    {
        if(igQuestionWordMax > 0)
            return true;
        File f = new File(sFilename);
        if(!f.exists())
        {
            System.out.println("Could not find the question word file: " + sFilename);
            return false;
        }
        igQuestionWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgQuestionWord = new String[igQuestionWordMax];
        igQuestionWordCount = 0;
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
                {
                    igQuestionWordCount++;
                    sgQuestionWord[igQuestionWordCount] = sLine;
                }
            rReader.close();
            Sort.quickSortStrings(sgQuestionWord, 1, igQuestionWordCount);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Could not find the question word file: " + sFilename);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found question word file but could not read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Checks if a given word is a question word.
     *
     * @param sWord the word to check
     * @return true if the word is a question word, false otherwise
     */
    public boolean questionWord(String sWord)
    {
        return Sort.i_FindStringPositionInSortedArray(sWord, sgQuestionWord, 1, igQuestionWordCount) >= 0;
    }
}
