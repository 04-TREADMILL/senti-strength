// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   NegatingWordList.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import nju.SEIII.EASIEST.utilities.FileOps;
import nju.SEIII.EASIEST.utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

/**
 * This class represents a list of negating words used in sentiment analysis.
 * It provides methods for initializing the list from a file and checking if a given word is a negating word.
 * <p>
 * UC-5 Negating Word Rule
 * UC-7 Emoji Rule
 * UC-8 Exclamation Mark Rule
 * UC-16 Process stdin and send to stdout
 * UC-17 Location of linguistic data folder
 * UC-18 Location of sentiment term weights
 */
public class NegatingWordList {

  /**
   * The array of negating words.
   */
  private String[] sgNegatingWord;

  /**
   * The number of negating words currently in the array.
   */
  private int igNegatingWordCount;

  /**
   * The maximum number of negating words that can be stored in the array.
   */
  private int igNegatingWordMax;

  /**
   * Creates a new NegatingWordList object with no negating words.
   */
  public NegatingWordList() {
    igNegatingWordCount = 0;
    igNegatingWordMax = 0;
  }

  /**
   * Initializes the negating word list from the given file.
   *
   * @param sFilename the name of the file containing the negating words
   * @param options   the classification options to use when reading the file
   * @return true if the list was successfully initialized, false otherwise
   */
  public boolean initialise(String sFilename, ClassificationOptions options) {
    if (igNegatingWordMax > 0) {
      return true;
    }
    File f = new File(sFilename);
    if (!f.exists()) {
      System.out.println("Could not find the negating words file: " + sFilename);
      return false;
    }
    igNegatingWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
    sgNegatingWord = new String[igNegatingWordMax];
    igNegatingWordCount = 0;
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
          igNegatingWordCount++;
          sgNegatingWord[igNegatingWordCount] = sLine;
        }
      }
      rReader.close();
      Sort.quickSortStrings(sgNegatingWord, 1, igNegatingWordCount);
    } catch (FileNotFoundException e) {
      System.out.println("Could not find negating words file: " + sFilename);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found negating words file but could not read from it: " + sFilename);
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Checks if the given word is a negating word.
   *
   * @param sWord the word to check
   * @return true if the word is a negating word, false otherwise
   */
  public boolean negatingWord(String sWord) {
    return Sort.i_FindStringPositionInSortedArray(sWord, sgNegatingWord, 1, igNegatingWordCount) >=
        0;
  }
}
