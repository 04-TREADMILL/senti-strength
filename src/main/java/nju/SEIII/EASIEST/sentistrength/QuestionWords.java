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
 * The QuestionWords class is used to label question words.
 */
public class QuestionWords
{
    private String[] sgQuestionWord;
    private int igQuestionWordCount;
    private int igQuestionWordMax;

    public QuestionWords()
    {
        igQuestionWordCount = 0;
        igQuestionWordMax = 0;
    }

    /**
     *
     * @param sFilename question words file
     * @param options file encoding format
     * @return if the questionwordsFile has been successfully imported
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
     *
     * @param sWord
     * @return if sWord is question word
     */
    public boolean questionWord(String sWord)
    {
        return Sort.i_FindStringPositionInSortedArray(sWord, sgQuestionWord, 1, igQuestionWordCount) >= 0;
    }
}
