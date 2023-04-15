// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   CorrectSpellingsList.java

package nju.SEIII.EASIEST.SentiStrength;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import nju.SEIII.EASIEST.Utilities.FileOps;
import nju.SEIII.EASIEST.Utilities.Sort;

/**
 * A class for storing and checking a list of correctly spelled words.
 *
 * @UC <p><ul>
 * <li>UC-6 Repeated Letter Rule
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-18 Location of sentiment term weights
 * </ul><p>
 */
public class CorrectSpellingsList {
  /**
   * An array for storing correct words.
   * prefix sg probably stand for “String global”
   */
  private String[] sgCorrectWord;

  /**
   * The number of correct words stored in the array.
   */
  private int igCorrectWordCount;

  /**
   * The maximum number of correct words that can be stored in the array.
   */
  private int igCorrectWordMax;

  /**
   * Constructor that initializes instance variables.
   */
  public CorrectSpellingsList() {
    igCorrectWordCount = 0;
    igCorrectWordMax = 0;
  }

  /**
   * Initializes the list of correct words from a file.
   *
   * @param sFilename The name of the file containing the list of correct words
   * @param options   ClassificationOptions object containing options for spell checking
   * @return true if initialization was successful, false otherwise
   */
  public boolean initialise(String sFilename, ClassificationOptions options) {
    // If the maximum number of correct words is already set or spell checking using a dictionary is not enabled, return true
    if (igCorrectWordMax > 0) {
      return true;
    }
    if (!options.bgCorrectSpellingsUsingDictionary) {
      return true;
    }

    // Set the maximum number of correct words to the number of lines in the file plus 2 and create a new array for storing correct words
    igCorrectWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
    sgCorrectWord = new String[igCorrectWordMax];
    igCorrectWordCount = 0;

    // Check if the file exists
    File f = new File(sFilename);
    if (!f.exists()) {
      System.out.println("Could not find the spellings file: " + sFilename);
      return false;
    }

    try {
      BufferedReader rReader;
      // Create a BufferedReader for reading from the file using UTF-8 encoding if specified in options
      if (options.bgForceUTF8) {
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sFilename), StandardCharsets.UTF_8));
      } else {
        rReader = new BufferedReader(new FileReader(sFilename));
      }

      String sLine;
      // Read each line from the file and store it in the array if it's not empty
      while ((sLine = rReader.readLine()) != null) {
        if (!sLine.equals("")) {
          igCorrectWordCount++;
          sgCorrectWord[igCorrectWordCount] = sLine;
        }
      }

      rReader.close();

      // Sort the array using quicksort
      Sort.quickSortStrings(sgCorrectWord, 1, igCorrectWordCount);
    } catch (FileNotFoundException e) {
      System.out.println("Could not find the spellings file: " + sFilename);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found spellings file but could not read from it: " + sFilename);
      e.printStackTrace();
      return false;
    }

    // Return true if initialization was successful
    return true;
  }

  /**
   * Checks if a word is spelled correctly.
   *
   * @param sWord The word to check
   * @return true if the word is spelled correctly, false otherwise
   */
  public boolean correctSpelling(String sWord) {
    // Use binary search to check if the word is in the sorted array of correct words
    return Sort.i_FindStringPositionInSortedArray(sWord, sgCorrectWord, 1, igCorrectWordCount) >= 0;
  }
}