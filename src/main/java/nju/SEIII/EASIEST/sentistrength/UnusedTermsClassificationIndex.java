// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   UnusedTermsClassificationIndex.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.util.Objects;

import nju.SEIII.EASIEST.utilities.Trie;

public class UnusedTermsClassificationIndex
{

    private String[] sgTermList;
    private int igTermListCount;
    private int igTermListMax;
    private int[] igTermListLessPtr;
    private int[] igTermListMorePtr;
    private int[] igTermListFreq;
    private int[] igTermListFreqTemp;
    private int[] igTermListPosClassDiff;
    private int[] iTermsAddedIDTemp;
    private int[] igTermListNegClassDiff;
    private int[] igTermListScaleClassDiff;
    private int[] igTermListBinaryClassDiff;
    private int[] igTermListTrinaryClassDiff;
    private int iTermsAddedIDTempCount;
    private int[][] igTermListPosCorrectClass;
    private int[][] igTermListNegCorrectClass;
    private int[][] igTermListScaleCorrectClass;
    private int[][] igTermListBinaryCorrectClass;
    private int[][] igTermListTrinaryCorrectClass;

    public UnusedTermsClassificationIndex()
    {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = 50000;
    }

    public static void main(String[] args1)
    {
    }

    public void addTermToNewTermIndex(String sTerm)
    {
        if(sgTermList == null)
            initialise(true, true, true, true);
        if(Objects.equals(sTerm, ""))
            return;
        boolean bDontAddMoreElements = false;
        if(igTermListCount == igTermListMax)
            bDontAddMoreElements = true;
        int iTermID = Trie.i_GetTriePositionForString(sTerm, sgTermList, igTermListLessPtr, igTermListMorePtr, 1, igTermListCount, bDontAddMoreElements);
        if(iTermID > 0)
        {
            iTermsAddedIDTemp[++iTermsAddedIDTempCount] = iTermID;
            igTermListFreqTemp[iTermID]++;
            if(iTermID > igTermListCount)
                igTermListCount = iTermID;
        }
    }

    public void addNewIndexToMainIndexWithPosNegValues(int iCorrectPosClass, int iEstPosClass, int iCorrectNegClass, int iEstNegClass)
    {
        if(iCorrectNegClass > 0 && iCorrectPosClass > 0)
        {
            for(int iTerm = 1; iTerm <= iTermsAddedIDTempCount; iTerm++)
            {
                int iTermID = iTermsAddedIDTemp[iTerm];
                if(igTermListFreqTemp[iTermID] != 0)
                    try
                    {
                        igTermListNegCorrectClass[iTermID][iCorrectNegClass - 1]++;
                        igTermListPosCorrectClass[iTermID][iCorrectPosClass - 1]++;
                        igTermListPosClassDiff[iTermID] += iCorrectPosClass - iEstPosClass;
                        igTermListNegClassDiff[iTermID] += iCorrectNegClass + iEstNegClass;
                        igTermListFreq[iTermID]++;
                        iTermsAddedIDTemp[iTerm] = 0;
                    }
                    catch(Exception e)
                    {
                        System.out.println("[UnusedTermsClassificationIndex] Error trying to add Pos + Neg to index. " + e.getMessage());
                    }
            }

        }
        iTermsAddedIDTempCount = 0;
    }

    public void addNewIndexToMainIndexWithScaleValues(int iCorrectScaleClass, int iEstScaleClass)
    {
        for(int iTerm = 1; iTerm <= iTermsAddedIDTempCount; iTerm++)
        {
            int iTermID = iTermsAddedIDTemp[iTerm];
            if(igTermListFreqTemp[iTermID] != 0)
                try
                {
                    igTermListScaleCorrectClass[iTermID][iCorrectScaleClass + 4]++;
                    igTermListScaleClassDiff[iTermID] += iCorrectScaleClass - iEstScaleClass;
                    igTermListFreq[iTermID]++;
                    iTermsAddedIDTemp[iTerm] = 0;
                }
                catch(Exception e)
                {
                    System.out.println("Error trying to add scale values to index. " + e.getMessage());
                }
        }

        iTermsAddedIDTempCount = 0;
    }

    public void addNewIndexToMainIndexWithTrinaryValues(int iCorrectTrinaryClass, int iEstTrinaryClass)
    {
        for(int iTerm = 1; iTerm <= iTermsAddedIDTempCount; iTerm++)
        {
            int iTermID = iTermsAddedIDTemp[iTerm];
            if(igTermListFreqTemp[iTermID] != 0)
                try
                {
                    igTermListTrinaryCorrectClass[iTermID][iCorrectTrinaryClass + 1]++;
                    igTermListTrinaryClassDiff[iTermID] += iCorrectTrinaryClass - iEstTrinaryClass;
                    igTermListFreq[iTermID]++;
                    iTermsAddedIDTemp[iTerm] = 0;
                }
                catch(Exception e)
                {
                    System.out.println("Error trying to add trinary values to index. " + e.getMessage());
                }
        }

        iTermsAddedIDTempCount = 0;
    }

    public void addNewIndexToMainIndexWithBinaryValues(int iCorrectBinaryClass, int iEstBinaryClass)
    {
        for(int iTerm = 1; iTerm <= iTermsAddedIDTempCount; iTerm++)
        {
            int iTermID = iTermsAddedIDTemp[iTerm];
            if(igTermListFreqTemp[iTermID] != 0)
                try
                {
                    igTermListBinaryClassDiff[iTermID] += iCorrectBinaryClass - iEstBinaryClass;
                    if(iCorrectBinaryClass == -1)
                        iCorrectBinaryClass = 0;
                    igTermListBinaryCorrectClass[iTermID][iCorrectBinaryClass]++;
                    igTermListFreq[iTermID]++;
                    iTermsAddedIDTemp[iTerm] = 0;
                }
                catch(Exception e)
                {
                    System.out.println("Error trying to add scale values to index. " + e.getMessage());
                }
        }

        iTermsAddedIDTempCount = 0;
    }

    public void initialise(boolean bInitialiseScale, boolean bInitialisePosNeg, boolean bInitialiseBinary, boolean bInitialiseTrinary)
    {
        igTermListCount = 0;
        igTermListMax = 50000;
        iTermsAddedIDTempCount = 0;
        sgTermList = new String[igTermListMax];
        igTermListLessPtr = new int[igTermListMax + 1];
        igTermListMorePtr = new int[igTermListMax + 1];
        igTermListFreq = new int[igTermListMax + 1];
        igTermListFreqTemp = new int[igTermListMax + 1];
        iTermsAddedIDTemp = new int[igTermListMax + 1];
        if(bInitialisePosNeg)
        {
            igTermListNegCorrectClass = new int[igTermListMax + 1][5];
            igTermListPosCorrectClass = new int[igTermListMax + 1][5];
            igTermListNegClassDiff = new int[igTermListMax + 1];
            igTermListPosClassDiff = new int[igTermListMax + 1];
        }
        if(bInitialiseScale)
        {
            igTermListScaleCorrectClass = new int[igTermListMax + 1][9];
            igTermListScaleClassDiff = new int[igTermListMax + 1];
        }
        if(bInitialiseBinary)
        {
            igTermListBinaryCorrectClass = new int[igTermListMax + 1][2];
            igTermListBinaryClassDiff = new int[igTermListMax + 1];
        }
        if(bInitialiseTrinary)
        {
            igTermListTrinaryCorrectClass = new int[igTermListMax + 1][3];
            igTermListTrinaryClassDiff = new int[igTermListMax + 1];
        }
    }

    public void printIndexWithPosNegValues(String sOutputFile, int iMinFreq)
    {
        try
        {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile));
            wWriter.write("Term\tTermFreq >= " + iMinFreq + "\t" + "PosClassDiff (correct-estimate)\t" + "NegClassDiff\t" + "PosClassAvDiff\t" + "NegClassAvDiff\t");
            for(int i = 1; i <= 5; i++)
                wWriter.write("CorrectClass" + i + "pos\t");

            for(int i = 1; i <= 5; i++)
                wWriter.write("CorrectClass" + i + "neg\t");

            wWriter.write("\n");
            if(igTermListCount > 0)
            {
                for(int iTerm = 1; iTerm <= igTermListCount; iTerm++)
                    if(igTermListFreq[iTerm] >= iMinFreq)
                    {
                        wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListPosClassDiff[iTerm] + "\t" + igTermListNegClassDiff[iTerm] + "\t" + (float) igTermListPosClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t" + (float) igTermListNegClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                        for(int i = 0; i < 5; i++)
                            wWriter.write(igTermListPosCorrectClass[iTerm][i] + "\t");

                        for(int i = 0; i < 5; i++)
                            wWriter.write(igTermListNegCorrectClass[iTerm][i] + "\t");

                        wWriter.write("\n");
                    }

            } else
            {
                wWriter.write("No terms found in corpus!\n");
            }
            wWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error printing index to " + sOutputFile);
            e.printStackTrace();
        }
    }

    public void printIndexWithScaleValues(String sOutputFile, int iMinFreq)
    {
        try
        {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile));
            wWriter.write("Term\tTermFreq\tScaleClassDiff (correct-estimate)\tScaleClassAvDiff\t");
            for(int i = -4; i <= 4; i++)
                wWriter.write("CorrectClass" + i + "\t");

            wWriter.write("\n");
            for(int iTerm = 1; iTerm <= igTermListCount; iTerm++)
                if(igTermListFreq[iTerm] > iMinFreq)
                {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListScaleClassDiff[iTerm] + "\t" + (float) igTermListScaleClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for(int i = 0; i < 9; i++)
                        wWriter.write(igTermListScaleCorrectClass[iTerm][i] + "\t");

                    wWriter.write("\n");
                }

            wWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error printing Scale index to " + sOutputFile);
            e.printStackTrace();
        }
    }

    public void printIndexWithTrinaryValues(String sOutputFile, int iMinFreq)
    {
        try
        {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile));
            wWriter.write("Term\tTermFreq\tTrinaryClassDiff (correct-estimate)\tTrinaryClassAvDiff\t");
            for(int i = -1; i <= 1; i++)
                wWriter.write("CorrectClass" + i + "\t");

            wWriter.write("\n");
            for(int iTerm = 1; iTerm <= igTermListCount; iTerm++)
                if(igTermListFreq[iTerm] > iMinFreq)
                {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListTrinaryClassDiff[iTerm] + "\t" + (float) igTermListTrinaryClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for(int i = 0; i < 3; i++)
                        wWriter.write(igTermListTrinaryCorrectClass[iTerm][i] + "\t");

                    wWriter.write("\n");
                }

            wWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error printing Trinary index to " + sOutputFile);
            e.printStackTrace();
        }
    }

    public void printIndexWithBinaryValues(String sOutputFile, int iMinFreq)
    {
        try
        {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile));
            wWriter.write("Term\tTermFreq\tBinaryClassDiff (correct-estimate)\tBinaryClassAvDiff\t");
            wWriter.write("CorrectClass-1\tCorrectClass1\t");
            wWriter.write("\n");
            for(int iTerm = 1; iTerm <= igTermListCount; iTerm++)
                if(igTermListFreq[iTerm] > iMinFreq)
                {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListBinaryClassDiff[iTerm] + "\t" + (float) igTermListBinaryClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for(int i = 0; i < 2; i++)
                        wWriter.write(igTermListBinaryCorrectClass[iTerm][i] + "\t");

                    wWriter.write("\n");
                }

            wWriter.close();
        }
        catch(IOException e)
        {
            System.out.println("Error printing Binary index to " + sOutputFile);
            e.printStackTrace();
        }
    }
}
