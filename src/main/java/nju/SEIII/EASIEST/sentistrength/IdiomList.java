// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   IdiomList.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


import nju.SEIII.EASIEST.utilities.FileOps;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

public class IdiomList
{

    public String[] sgIdioms;
    public int[] igIdiomStrength;
    public int igIdiomCount;
    public String[][] sgIdiomWords;
    int[] igIdiomWordCount;

    public IdiomList()
    {
        igIdiomCount = 0;
    }

    public boolean initialise(String sFilename, ClassificationOptions options, int iExtraBlankArrayEntriesToInclude)
    {
        int iLinesInFile = 0;
        int iIdiomStrength = 0;
        if(Objects.equals(sFilename, ""))
            return false;
        File f = new File(sFilename);
        if(!f.exists())
        {
            System.out.println("Could not find idiom list file: " + sFilename);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
        sgIdioms = new String[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
        igIdiomStrength = new int[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
        igIdiomCount = 0;
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
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if(iFirstTabLocation >= 0)
                    {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        try
                        {
                            if(iSecondTabLocation > 0)
                                iIdiomStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1, iSecondTabLocation).trim());
                            else
                                iIdiomStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1).trim());
                            if(iIdiomStrength > 0)
                                iIdiomStrength--;
                            else
                            if(iIdiomStrength < 0)
                                iIdiomStrength++;
                        }
                        catch(NumberFormatException e)
                        {
                            System.out.println("Failed to identify integer weight for idiom! Ignoring idiom");
                            System.out.println("Line: " + sLine);
                            iIdiomStrength = 0;
                        }
                        sLine = sLine.substring(0, iFirstTabLocation);
                        if(sLine.contains(" "))
                            sLine = sLine.trim();
                        if(sLine.indexOf("  ") > 0)
                            sLine = sLine.replace("  ", " ");
                        if(sLine.indexOf("  ") > 0)
                            sLine = sLine.replace("  ", " ");
                        if(!sLine.equals(""))
                        {
                            igIdiomCount++;
                            sgIdioms[igIdiomCount] = sLine;
                            igIdiomStrength[igIdiomCount] = iIdiomStrength;
                        }
                    }
                }
            rReader.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("Could not find idiom list file: " + sFilename);
            e.printStackTrace();
            return false;
        }
        catch(IOException e)
        {
            System.out.println("Found idiom list file but could not read from it: " + sFilename);
            e.printStackTrace();
            return false;
        }
        convertIdiomStringsToWordLists();
        return true;
    }

    public boolean addExtraIdiom(String sIdiom, int iIdiomStrength, boolean bConvertIdiomStringsToWordListsAfterAddingIdiom)
    {
        try
        {
            igIdiomCount++;
            sgIdioms[igIdiomCount] = sIdiom;
            if(iIdiomStrength > 0)
                iIdiomStrength--;
            else
            if(iIdiomStrength < 0)
                iIdiomStrength++;
            igIdiomStrength[igIdiomCount] = iIdiomStrength;
            if(bConvertIdiomStringsToWordListsAfterAddingIdiom)
                convertIdiomStringsToWordLists();
        }
        catch(Exception e)
        {
            System.out.println("Could not add extra idiom: " + sIdiom);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void convertIdiomStringsToWordLists()
    {
        sgIdiomWords = new String[igIdiomCount + 1][10];
        igIdiomWordCount = new int[igIdiomCount + 1];
        for(int iIdiom = 1; iIdiom <= igIdiomCount; iIdiom++)
        {
            String[] sWordList = sgIdioms[iIdiom].split(" ");
            if(sWordList.length >= 9)
            {
                System.out.println("Ignoring idiom! Too many words in it! (>9): " + sgIdioms[iIdiom]);
            } else
            {
                igIdiomWordCount[iIdiom] = sWordList.length;
                System.arraycopy(sWordList, 0, sgIdiomWords[iIdiom], 0, sWordList.length);

            }
        }

    }

    public int getIdiomStrength_oldNotUseful(String sPhrase)
    {
        sPhrase = sPhrase.toLowerCase();
        for(int i = 1; i <= igIdiomCount; i++)
            if(sPhrase.contains(sgIdioms[i]))
                return igIdiomStrength[i];

        return 999;
    }

    public String getIdiom(int iIdiomID)
    {
        if(iIdiomID > 0 && iIdiomID < igIdiomCount)
            return sgIdioms[iIdiomID];
        else
            return "";
    }
}
