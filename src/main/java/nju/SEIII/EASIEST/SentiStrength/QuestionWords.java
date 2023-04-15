// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   QuestionWords.java

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

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions

/**
 * The QuestionWords class is used to load and manage a list of question words from a file.
 * It provides a method to check whether a given word is a question word or not.
 *
 * @UC <p><ul>
 * <li>UC-9 Repeated Punctuation Rule
 * <li>UC-16 Process stdin and send to stdout
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-18 Location of sentiment term weights
 * </ul></p>
 */

public class QuestionWords {
  private String[] sgQuestionWord;
  private int igQuestionWordCount;
  private int igQuestionWordMax;

  /**
   * Initializes a new instance of the QuestionWords class with default values.
   * The count of loaded question words is set to zero, and the maximum count is set to zero as well.
   */
  public QuestionWords() {
    igQuestionWordCount = 0;
    igQuestionWordMax = 0;
  }

  /**
   * Loads the list of question words from a file and sorts it in alphabetical order.
   *
   * @param sFilename the name of the file containing the list of question words.
   * @param options   the classification options to use when loading the file.
   * @return true if the file was successfully loaded and sorted, false otherwise.
   */

  public boolean initialise(String sFilename, ClassificationOptions options) {
    if (igQuestionWordMax > 0) {
      return true;
    }
    File f = new File(sFilename);
    if (!f.exists()) {
      System.out.println("Could not find the question word file: " + sFilename);
      return false;
    }
    igQuestionWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
    sgQuestionWord = new String[igQuestionWordMax];
    igQuestionWordCount = 0;
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
          igQuestionWordCount++;
          sgQuestionWord[igQuestionWordCount] = sLine;
        }
      }
      rReader.close();
      Sort.quickSortStrings(sgQuestionWord, 1, igQuestionWordCount);
    } catch (FileNotFoundException e) {
      System.out.println("Could not find the question word file: " + sFilename);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found question word file but could not read from it: " + sFilename);
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Determines whether a given word is a question word or not.
   *
   * @param sWord the word to check.
   * @return true if the word is a question word, false otherwise.
   */
  public boolean questionWord(String sWord) {
    return Sort.i_FindStringPositionInSortedArray(sWord, sgQuestionWord, 1, igQuestionWordCount) >=
        0;
  }
}
