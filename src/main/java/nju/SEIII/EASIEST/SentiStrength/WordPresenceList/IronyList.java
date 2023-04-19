package nju.SEIII.EASIEST.SentiStrength.WordPresenceList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;
import nju.SEIII.EASIEST.Utilities.FileOps;
import nju.SEIII.EASIEST.Utilities.Sort;

/**
 * IronyList is a class that stores a list of ironic terms and provides a method to check whether a term is ironic or not.
 *
 * @UC <p><ul>
 * <li> UC-19 Location of output folder
 * <li> UC-20 File name extension for output
 * </ul></p>
 */
public class IronyList extends WordPresenceList {

  /**
   * Array of irony terms.
   */
  private String[] sgIronyTerm;

  /**
   * Number of irony terms.
   */
  private int igIronyTermCount;

  /**
   * Capacity of irony terms.
   */
  private int igIronyTermMax;

  /**
   * Constructs an IronyList object with zero count of ironic terms and maximum capacity of zero.
   */
  public IronyList() {
    igIronyTermCount = 0;
    igIronyTermMax = 0;
  }

  /**
   * Checks if a given term is ironic by searching for it in the list of ironic terms.
   *
   * @param term The term to check.
   * @return true if the term is found in the list of ironic terms, false otherwise.
   */
  public boolean contains(String term) {
    int iIronyTermCount =
        Sort.i_FindStringPositionInSortedArray(term, sgIronyTerm, 1, igIronyTermCount);
    return iIronyTermCount >= 0;
  }

  /**
   * Initializes the list of ironic terms by reading from a file.
   *
   * @param sSourceFile The path to the file containing the list of ironic terms.
   * @param options     The options to use when initializing the list of ironic terms.
   * @return true if the initialization was successful, false otherwise.
   */
  public boolean initialise(String sSourceFile, ClassificationOptions options) {
    if (igIronyTermCount > 0) {
      return true;
    }
    File f = new File(sSourceFile);
    if (!f.exists()) {
      return true;
    }
    try {
      igIronyTermMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
      igIronyTermCount = 0;
      sgIronyTerm = new String[igIronyTermMax];
      BufferedReader rReader;
      if (options.bgForceUTF8) {
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
      } else {
        rReader = new BufferedReader(new FileReader(sSourceFile));
      }
      String sLine;
      while ((sLine = rReader.readLine()) != null) {
        if (!sLine.equals("")) {
          String[] sData = sLine.split("\t");
          if (sData.length > 0) {
            sgIronyTerm[++igIronyTermCount] = sData[0];
          }
        }
      }
      rReader.close();
    } catch (FileNotFoundException e) {
      System.out.println("Could not find IronyTerm file: " + sSourceFile);
      e.printStackTrace();
      return false;
    } catch (IOException e) {
      System.out.println("Found IronyTerm file but could not read from it: " + sSourceFile);
      e.printStackTrace();
      return false;
    }
    Sort.quickSortStrings(sgIronyTerm, 1, igIronyTermCount);
    return true;
  }
}
