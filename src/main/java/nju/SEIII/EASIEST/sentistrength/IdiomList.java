package nju.SEIII.EASIEST.sentistrength;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import nju.SEIII.EASIEST.utilities.FileOps;

/**
 * This class represents a list of idioms and their strengths.
 * It has methods for initializing the list from a file, adding an extra idiom,
 * converting idiom strings to word lists, and getting the count of idioms.
 *
 * @UC <p><ul>
 * <li> UC-2 Assigning Sentiment Scores for Phrases
 * <li> UC-19 Location of output folder
 * <li> UC-20 File name extension for output
 * </ul></p>
 */

public class IdiomList {
  /**
   * Array of idiom strings.
   */
  public String[] sgIdioms;

  /**
   * Array of idiom strengths.
   */
  public int[] igIdiomStrength;

  /**
   * Total count of idioms.
   */
  public int igIdiomCount;

  /**
   * Array of word lists corresponding to each idiom string.
   */
  public String[][] sgIdiomWords;

  /**
   * Array of word counts corresponding to each idiom string.
   */
  int[] igIdiomWordCount;

  /**
   * Constructor for creating an empty IdiomList object.
   */
  public IdiomList() {
    igIdiomCount = 0;
  }

  /**
   * Initializes the idiom list from a file.
   *
   * @param sFilename                        path to the idiom list file
   * @param options                          ClassificationOptions object containing options for the classification algorithm
   * @param iExtraBlankArrayEntriesToInclude number of extra blank array entries to include
   * @return true if initialization is successful, false otherwise
   */
  public boolean initialise(String sFilename, ClassificationOptions options,
                            int iExtraBlankArrayEntriesToInclude) {
    int iLinesInFile = 0;
    int iIdiomStrength = 0;
    if (Objects.equals(sFilename, "")) {
      return false;
    }
    File f = new File(sFilename);
    if (!f.exists()) {
      System.out.println("Could not find idiom list file: " + sFilename);
      return false;
    }
    iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
    sgIdioms = new String[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
    igIdiomStrength = new int[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
    igIdiomCount = 0;
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
          int iFirstTabLocation = sLine.indexOf("\t");
          if (iFirstTabLocation >= 0) {
            int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
            try {
              if (iSecondTabLocation > 0) {
                iIdiomStrength = Integer.parseInt(
                    sLine.substring(iFirstTabLocation + 1, iSecondTabLocation).trim());
              } else {
                iIdiomStrength = Integer.parseInt(sLine.substring(iFirstTabLocation + 1).trim());
              }
              if (iIdiomStrength > 0) {
                iIdiomStrength--;
              } else if (iIdiomStrength < 0) {
                iIdiomStrength++;
              }
            } catch (NumberFormatException e) {
              System.out.println("Failed to identify integer weight for idiom! Ignoring idiom");
              System.out.println("Line: " + sLine);
              iIdiomStrength = 0;
            }
            sLine = sLine.substring(0, iFirstTabLocation);
            if (sLine.contains(" ")) {
              sLine = sLine.trim();
            }
            if (sLine.indexOf("  ") > 0) {
              sLine = sLine.replace("  ", " ");
            }
            if (sLine.indexOf("  ") > 0) {
              sLine = sLine.replace("  ", " ");
            }
            if (!sLine.equals("")) {
              igIdiomCount++;
              sgIdioms[igIdiomCount] = sLine;
              igIdiomStrength[igIdiomCount] = iIdiomStrength;
            }
          }
        }
      }
      rReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("Could not find idiom list file: " + sFilename);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found idiom list file but could not read from it: " + sFilename);
      e.printStackTrace();
      return false;
    }
    convertIdiomStringsToWordLists();
    return true;
  }

  /**
   * Adds a new idiom to the idiom database.
   *
   * @param sIdiom                                          the string representation of the new idiom to be added
   * @param iIdiomStrength                                  the strength level of the new idiom, ranging from -5 (very negative) to 5 (very positive)
   * @param bConvertIdiomStringsToWordListsAfterAddingIdiom a boolean flag indicating whether the idiom string should be converted to a word list after adding the new idiom
   */
  public boolean addExtraIdiom(String sIdiom, int iIdiomStrength,
                               boolean bConvertIdiomStringsToWordListsAfterAddingIdiom) {
    try {
      igIdiomCount++;
      sgIdioms[igIdiomCount] = sIdiom;
      if (iIdiomStrength > 0) {
        iIdiomStrength--;
      } else if (iIdiomStrength < 0) {
        iIdiomStrength++;
      }
      igIdiomStrength[igIdiomCount] = iIdiomStrength;
      if (bConvertIdiomStringsToWordListsAfterAddingIdiom) {
        convertIdiomStringsToWordLists();
      }
    } catch (Exception e) {
      System.out.println("Could not add extra idiom: " + sIdiom);
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Converts each idiom string in the idiom database to a word list.
   */
  public void convertIdiomStringsToWordLists() {
    sgIdiomWords = new String[igIdiomCount + 1][10];
    igIdiomWordCount = new int[igIdiomCount + 1];
    for (int iIdiom = 1; iIdiom <= igIdiomCount; iIdiom++) {
      String[] sWordList = sgIdioms[iIdiom].split(" ");
      if (sWordList.length >= 9) {
        System.out.println("Ignoring idiom! Too many words in it! (>9): " + sgIdioms[iIdiom]);
      } else {
        igIdiomWordCount[iIdiom] = sWordList.length;
        System.arraycopy(sWordList, 0, sgIdiomWords[iIdiom], 0, sWordList.length);
      }
    }
  }

  /**
   * Returns the strength level of the idiom contained in the given phrase.
   *
   * @param sPhrase the phrase to search for idioms
   * @return the strength level of the idiom, ranging from -5 (very negative) to 5 (very positive), or 999 if no idiom is found
   * @deprecated This method is no longer useful
   */
  @Deprecated
  public int getIdiomStrength_oldNotUseful(String sPhrase) {
    sPhrase = sPhrase.toLowerCase();
    for (int i = 1; i <= igIdiomCount; i++) {
      if (sPhrase.contains(sgIdioms[i])) {
        return igIdiomStrength[i];
      }
    }
    return 999;
  }

  /**
   * Returns the string representation of the idiom with the given ID.
   *
   * @param iIdiomID the ID of the idiom to retrieve
   * @return the string representation of the idiom with the given ID, or an empty string if no idiom with the given ID exists
   */
  public String getIdiom(int iIdiomID) {
    if (iIdiomID > 0 && iIdiomID < igIdiomCount) {
      return sgIdioms[iIdiomID];
    } else {
      return "";
    }
  }
}
