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


/**
 * This class represents evaluative terms and initializes evaluative terms from a source file.
 *
 * @UC <p><ul>
 * <li> UC-19 Location of output folder
 * <li> UC-20 File name extension for output
 * </ul></p>
 */
public class EvaluativeTerms {

  public String[] sgObject;
  public String[] sgObjectEvaluation;
  public int[] igObjectEvaluationStrength;
  public int igObjectEvaluationCount;
  private int igObjectEvaluationMax;

  /**
   * Constructor for the EvaluativeTerms class.
   * Initializes the values of igObjectEvaluationMax and igObjectEvaluationCount to 0.
   */
  public EvaluativeTerms() {
    igObjectEvaluationMax = 0;
    igObjectEvaluationCount = 0;
  }

  /**
   * Initializes the evaluative terms from a source file.
   *
   * @param sSourceFile    The path to the source file containing the evaluative terms data
   * @param options        An object representing classification options
   * @param idiomList      An object representing a list of idioms
   * @param sentimentWords An object representing a list of sentiment words
   * @return true if initialization was successful, false otherwise
   */
  public boolean initialise(String sSourceFile, ClassificationOptions options, IdiomList idiomList,
                            SentimentWords sentimentWords) {
    // Check if igObjectEvaluationCount is greater than 0 and return true if it is
    if (igObjectEvaluationCount > 0) {
      return true;
    }
    File f = new File(sSourceFile);
    // Check if the file exists. If it does not exist, print an error message and return false
    if (!f.exists()) {
      System.out.println("Could not find additional (object/evaluation) file: " + sSourceFile);
      return false;
    }
    // local variables
    int iStrength = 0;
    boolean bIdiomsAdded = false;
    boolean bSentimentWordsAdded = false;
    try {
      // Initialize , set igObjectEvaluationMax to the number of lines in the source file plus 2
      igObjectEvaluationMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
      igObjectEvaluationCount = 0;
      sgObject = new String[igObjectEvaluationMax];
      sgObjectEvaluation = new String[igObjectEvaluationMax];
      igObjectEvaluationStrength = new int[igObjectEvaluationMax];
      // Create a BufferedReader to read from the source file
      BufferedReader rReader;
      if (options.bgForceUTF8) {
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
      } else {
        rReader = new BufferedReader(new FileReader(sSourceFile));
      }
      // Read lines from the file until there are no more lines to read
      String sLine;
      while ((sLine = rReader.readLine()) != null)
      // Check if the line is not empty and does not start with "##"
      {
        if (sLine != "" && sLine.indexOf("##") != 0 && sLine.indexOf("\t") > 0) {
          String[] sData = sLine.split("\t");
          // Check if the resulting array has more than two elements and the third element does not start with "##"
          if (sData.length > 2 && sData[2].indexOf("##") != 0) {
            // Assign values to sgObject and sgObjectEvaluation
            sgObject[++igObjectEvaluationCount] = sData[0];
            sgObjectEvaluation[igObjectEvaluationCount] = sData[1];
            try {
              // Attempt to parse the third element as an integer and assign it to igObjectEvaluationStrength
              igObjectEvaluationStrength[igObjectEvaluationCount] =
                  Integer.parseInt(sData[2].trim());
              if (igObjectEvaluationStrength[igObjectEvaluationCount] > 0) {
                igObjectEvaluationStrength[igObjectEvaluationCount]--;
              } else if (igObjectEvaluationStrength[igObjectEvaluationCount] < 0) {
                igObjectEvaluationStrength[igObjectEvaluationCount]++;
              }
            } catch (NumberFormatException e) {
              // If parsing fails, print an error message and decrement igObjectEvaluationCount
              System.out.println(
                  "Failed to identify integer weight for object/evaluation! Ignoring object/evaluation");
              System.out.println("Line: " + sLine);
              igObjectEvaluationCount--;
            }
          } else if (sData[0].indexOf(" ") > 0)
          // if " " exist
          {
            try {
              // Attempt to parse the second element as an integer and assign it to iStrength
              iStrength = Integer.parseInt(sData[1].trim());
              idiomList.addExtraIdiom(sData[0], iStrength, false);
              bIdiomsAdded = true;
            } catch (NumberFormatException e) {
              System.out.println(
                  "Failed to identify integer weight for idiom in additional file! Ignoring it");
              System.out.println("Line: " + sLine);
            }
          } else {
            try {
              iStrength = Integer.parseInt(sData[1].trim());
              sentimentWords.addOrModifySentimentTerm(sData[0], iStrength, false);
              bSentimentWordsAdded = true;
            } catch (NumberFormatException e) {
              System.out.println(
                  "Failed to identify integer weight for sentiment term in additional file! Ignoring it");
              System.out.println("Line: " + sLine);
              igObjectEvaluationCount--;
            }
          }
        }
      }
      rReader.close();
      if (igObjectEvaluationCount > 0) {
        options.bgUseObjectEvaluationTable = true;
      }
      if (bSentimentWordsAdded) {
        sentimentWords.sortSentimentList();
      }
      if (bIdiomsAdded) {
        idiomList.convertIdiomStringsToWordLists();
      }
    } catch (FileNotFoundException e) {
      System.out.println("Could not find additional (object/evaluation) file: " + sSourceFile);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println(
          "Found additional (object/evaluation) file but could not read from it: " + sSourceFile);
      e.printStackTrace();
      return false;
    }
    return true;
  }
}
