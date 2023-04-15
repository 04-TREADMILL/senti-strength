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
 * This class represents a list of emoticons and their strengths.
 * It has methods for initializing the list from a file,
 * getting the strength of an emoticon.
 *
 * @UC <p><ul>
 * <li> UC-7 Emoji Rule
 * <li> UC-17 Location of linguistic data folder
 * <li> UC-18 Location of sentiment term weights
 * </ul></p>
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
    int iEmoticon =
        Sort.i_FindStringPositionInSortedArray(emoticon, sgEmoticon, 1, igEmoticonCount);
    if (iEmoticon >= 0) {
      return igEmoticonStrength[iEmoticon];
    } else {
      return 999;
    }
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
    if (igEmoticonCount > 0) {
      return true;
    }
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
      if (options.bgForceUTF8) {
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
      } else {
        rReader = new BufferedReader(new FileReader(sSourceFile));
      }

      String sLine;

      // Read each line from the file
      while ((sLine = rReader.readLine()) != null) {
        if (!sLine.equals("")) {
          String[] sData = sLine.split("\t"); // Split line using tab character as delimiter

          if (sData.length > 1) {
            igEmoticonCount++;
            sgEmoticon[igEmoticonCount] = sData[0];

            try {
              igEmoticonStrength[igEmoticonCount] = Integer.parseInt(sData[1].trim());
            } catch (NumberFormatException e) {
              System.out.println(
                  "Failed to identify integer weight for emoticon! Ignoring emoticon");
              System.out.println("Line: " + sLine);
              igEmoticonCount--;
            }
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
    if (igEmoticonCount > 1) {
      Sort.quickSortStringsWithInt(sgEmoticon, igEmoticonStrength, 1, igEmoticonCount);
    }

    return true;
  }
}