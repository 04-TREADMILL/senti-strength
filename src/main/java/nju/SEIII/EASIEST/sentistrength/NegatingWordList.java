// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   NegatingWordList.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

public class NegatingWordList
{

    private String[] sgNegatingWord;
    private int igNegatingWordCount;
    private int igNegatingWordMax;

    public NegatingWordList()
    {
        igNegatingWordCount = 0;
        igNegatingWordMax = 0;
    }

    public boolean initialise(String sFilename, ClassificationOptions options)
    {
        if(igNegatingWordMax > 0)
            return true;
        File f = new File(sFilename);
        if(!f.exists())
        {
            System.out.println("Could not find the negating words file: " + sFilename);
            return false;
        }
        igNegatingWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgNegatingWord = new String[igNegatingWordMax];
        igNegatingWordCount = 0;
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
                    igNegatingWordCount++;
                    sgNegatingWord[igNegatingWordCount] = sLine;
                }
            rReader.close();
            Sort.quickSortStrings(sgNegatingWord, 1, igNegatingWordCount);
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Could not find negating words file: " + sFilename);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found negating words file but could not read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean negatingWord(String sWord)
    {
        return Sort.i_FindStringPositionInSortedArray(sWord, sgNegatingWord, 1, igNegatingWordCount) >= 0;
    }
}
