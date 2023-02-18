// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   Lemmatiser.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;


import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

public class Lemmatiser
{

    private String[] sgWord;
    private String[] sgLemma;
    private int igWordLast;

    public Lemmatiser()
    {
        igWordLast = -1;
    }

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

    public String lemmatise(String sWord)
    {
        int iLemmaID = Sort.i_FindStringPositionInSortedArray(sWord, sgWord, 0, igWordLast);
        if(iLemmaID >= 0)
            return sgLemma[iLemmaID];
        else
            return sWord;
    }
}
