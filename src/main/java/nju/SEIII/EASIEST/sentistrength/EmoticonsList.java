// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   EmoticonsList.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;

import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

/**
 * @UC
 * <p><ul>
 * <li>UC-7 Emoji Rule
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-18 Location of sentiment term weights
 * </ul><p>
 */
public class EmoticonsList {

    /**
     * Array of emoticons
     */
    private String[] sgEmoticon;
    /**
     * Array of emoticon strengths
    */
    private int[] igEmoticonStrength;
    /**
     * Number of emoticons
     */
    private int igEmoticonCount;
    /**
     * Maximum number of emoticons
     */
    private int igEmoticonMax;

     /**
     * Constructor for EmoticonsList. Initializes instance variables.
     */
    public EmoticonsList() {
        igEmoticonCount = 0;
        igEmoticonMax = 0;
    }

    /**
     * Method to get the strength of an emoticon.
     *
     * @param emoticon The emoticon to get the strength of.
     * @return The strength of the given emoticon if it exists in the list. Otherwise, returns 999.
     */
    public int getEmoticon(String emoticon) {
        int iEmoticon = Sort.i_FindStringPositionInSortedArray(emoticon, sgEmoticon, 1, igEmoticonCount);
        if (iEmoticon >= 0)
            return igEmoticonStrength[iEmoticon];
        else
            return 999;
    }

    /**
     * Method to initialize the EmoticonsList from a file.
     *
     * @param sSourceFile The path to the file containing emoticons and their strengths.
     * @param options     The ClassificationOptions object containing options for initialization.
     * @return True if initialization was successful. False otherwise.
     */
    public boolean initialise(String sSourceFile, ClassificationOptions options) {
        // Method to initialize the EmoticonsList from a file
        if (igEmoticonCount > 0)
            return true;
        File f = new File(sSourceFile);
        if (!f.exists()) {
            System.out.println("Could not find file: " + sSourceFile);
            return false;
        }
        try {
            igEmoticonMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
            // Initialize instance variables
            igEmoticonCount = 0;
            sgEmoticon = new String[igEmoticonMax];
            igEmoticonStrength = new int[igEmoticonMax];
            
            // Create a BufferedReader to read from the file
            BufferedReader rReader;
            if (options.bgForceUTF8)
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
            else
                rReader = new BufferedReader(new FileReader(sSourceFile));
            
            String sLine;
            
            // Read each line from the file
            while ((sLine = rReader.readLine()) != null)
                if (!sLine.equals("")) {
                    String[] sData = sLine.split("\t"); // Split line using tab character as delimiter
                    
                    if (sData.length > 1) {
                        igEmoticonCount++;
                        sgEmoticon[igEmoticonCount] = sData[0];
                        
                        try {
                            igEmoticonStrength[igEmoticonCount] = Integer.parseInt(sData[1].trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Failed to identify integer weight for emoticon! Ignoring emoticon");
                            System.out.println("Line: " + sLine);
                            igEmoticonCount--;
                        }
                    }
                }
            
            // Close reader
            rReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Could not find emoticon file: " + sSourceFile);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.out.println("Found emoticon file but could not read from it: " + sSourceFile);
            e.printStackTrace();
            return false;
        }
        
        // Sort emoticons and their strengths if there is more than one emoticon
        if (igEmoticonCount > 1)
            Sort.quickSortStringsWithInt(sgEmoticon, igEmoticonStrength, 1, igEmoticonCount);
        
        return true;
    }
}