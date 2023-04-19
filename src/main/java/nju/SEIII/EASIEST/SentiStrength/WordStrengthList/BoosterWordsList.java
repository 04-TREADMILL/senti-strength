// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   BoosterWordsList.java

package nju.SEIII.EASIEST.SentiStrength.WordStrengthList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;
import nju.SEIII.EASIEST.Utilities.FileOps;
import nju.SEIII.EASIEST.Utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions


/**
 * This class stores data about booster words and their strengths into two arrays.
 *
 * @UC <p><ul>
 * <li>UC-2 Assigning Sentiment Scores for Phrases
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-18 Location of sentiment term weights
 * <li>UC-19 Location of output folder
 * <li>UC-20 File name extension for output
 * </ul><p>
 */
public class BoosterWordsList extends WordStrengthList {
  /**
   * An array of Strings representing booster words.
   */
  private String[] sgBoosterWords;
  /**
   * An array of integers representing booster word strengths.
   */
  private int[] igBoosterWordStrength;
  /**
   * The number of booster words stored in the arrays.
   */
  private int igBoosterWordsCount;

  /**
   * Constructor that initializes the value of igBoosterWordsCount to 0.
   */
  public BoosterWordsList() {
    igBoosterWordsCount = 0;
  }

  /**
   * This method reads data from a file specified by its filename parameter (sFilename) and stores that data into two arrays (sgBoosterWords and igBoosterWordStrength).
   * Each line in that file represents one entry in those two arrays.
   * The first part of each line (before its first tab character) represents one element in the String array (sgBoosterWords)
   * The second part (between its first and second tab characters) represents one element in the int array (igBoosterWordStrength).
   *
   * @param sFilename The name of the file to read data from
   * @param options   A ClassificationOptions object
   * @return true if initialization is successful; false otherwise
   */
  public boolean initialise(String sFilename, ClassificationOptions options) {
    int iLinesInFile;
    int iWordStrength;
    if (Objects.equals(sFilename, "")) {
      System.out.println("No booster words file specified");
      return false;
    }
    File f = new File(sFilename);
    if (!f.exists()) {
      System.out.println("Could not find booster words file: " + sFilename);
      return false;
    }
    iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
    if (iLinesInFile < 1) {
      System.out.println("No booster words specified");
      return false;
    }
    // Initialize sgBoosterWords and igBoosterWordStrength arrays with given size
    sgBoosterWords = new String[iLinesInFile + 1];
    igBoosterWordStrength = new int[iLinesInFile + 1];
    igBoosterWordsCount = 0;
    try {
      BufferedReader rReader;
      if (options.bgForceUTF8) {
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sFilename), StandardCharsets.UTF_8));
      } else {
        rReader = new BufferedReader(new FileReader(sFilename));
      }
      String sLine;
      while ((sLine = rReader.readLine()) != null) {
        if (!sLine.equals("")) {
          // Find the first tab character in sLine
          int iFirstTabLocation = sLine.indexOf("\t");
          if (iFirstTabLocation >= 0) {
            // Find the second tab character in sLine after its first tab character
            int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
            try {
              // Try to parse an integer value from substring between first and second tab characters or after first tab character (if no second tab character found)
              if (iSecondTabLocation > 0) {
                iWordStrength =
                    Integer.parseInt(
                        sLine.substring(iFirstTabLocation + 1, iSecondTabLocation));
              } else {
                iWordStrength =
                    Integer.parseInt(sLine.substring(iFirstTabLocation + 1).trim());
              }
            } catch (NumberFormatException e) {
              System.out.println(
                  "Failed to identify integer weight for booster word! Assuming it is zero");
              System.out.println("Line: " + sLine);
              iWordStrength = 0;
            }
            // Update sLine to be substring before first tab character and trim leading/trailing spaces (if any)
            sLine = sLine.substring(0, iFirstTabLocation);
            if (sLine.contains(" ")) {
              sLine = sLine.trim();
            }
            if (!sLine.equals("")) {
              // Update
              igBoosterWordsCount++;
              sgBoosterWords[igBoosterWordsCount] = sLine;
              igBoosterWordStrength[igBoosterWordsCount] = iWordStrength;
            }
          }
        }
      }
      Sort.quickSortStringsWithInt(sgBoosterWords, igBoosterWordStrength, 1, igBoosterWordsCount);
      rReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("Could not find booster words file: " + sFilename);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found booster words file but could not read from it: " + sFilename);
      e.printStackTrace();
      return false;
    }
    return true;
  }

//  /**
//   * Adds an extra term into the two arrays (sgBoosterWords and igBoosterWordStrength).
//   *
//   * @param sText                           The text of the extra term to add
//   * @param iWordStrength                   The strength value of the extra term to add
//   * @param bSortBoosterListAfterAddingTerm Whether or not to sort both arrays after adding the extra term
//   * @return true if adding the extra term is successful; false otherwise
//   */
//  public boolean addExtraTerm(String sText, int iWordStrength,
//                              boolean bSortBoosterListAfterAddingTerm) {
//    try {
//      igBoosterWordsCount++;
//      sgBoosterWords[igBoosterWordsCount] = sText;
//      igBoosterWordStrength[igBoosterWordsCount] = iWordStrength;
//      if (bSortBoosterListAfterAddingTerm) {
//        Sort.quickSortStringsWithInt(sgBoosterWords, igBoosterWordStrength, 1,
//            igBoosterWordsCount);
//      }
//    } catch (Exception e) {
//      System.out.println("Could not add extra booster word: " + sText);
//      e.printStackTrace();
//      return false;
//    }
//    return true;
//  }
//
//  /**
//   * Sorts both arrays (sgBoosterWords and igBoosterWordStrength) using a quick sort algorithm.
//   */
//  public void sortBoosterWordList() {
//    Sort.quickSortStringsWithInt(sgBoosterWords, igBoosterWordStrength, 1, igBoosterWordsCount);
//  }

  /**
   * Returns the strength value of a given word.
   *
   * @param sWord The word to get its strength value
   * @return The strength value of the given word; 0 if that word is not found in sgBoosterWords array
   */
  public int getStrength(String sWord) {
    int iWordID = Sort.i_FindStringPositionInSortedArray(sWord.toLowerCase(), sgBoosterWords, 1,
        igBoosterWordsCount);
    if (iWordID >= 0) {
      return igBoosterWordStrength[iWordID];
    } else {
      return 0;
    }
  }
}
