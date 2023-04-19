// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   ClassificationStatistics.java

package nju.SEIII.EASIEST.SentiStrength;

/**
 * @UC <p><ul>
 * <li>UC-1 Assigning Sentiment Scores for Words
 * <li>UC-2 Assigning Sentiment Scores for Phrases
 * <li>UC-3 Spelling Correction
 * <li>UC-4 Booster Word Rule
 * <li>UC-5 Negating Word Rule
 * <li>UC-6 Repeated Letter Rule
 * <li>UC-7 Emoji Rule
 * <li>UC-10 Negative Sentiment Ignored in Questions
 * <li>UC-11 Classify a single text
 * <li>UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]
 * <li>UC-13 Classify texts in a column within a file or folder
 * <li>UC-14 Listen at a port for texts to classify
 * <li>UC-15 Run interactively from the command line
 * <li>UC-27 Optimise sentiment strengths of existing sentiment terms
 * <li>UC-29 Machine learning evaluations
 * </ul><p>
 */
public class ClassificationStatistics {

  public ClassificationStatistics() {
  }

  /**
   * Method to calculate the correlation between the absolute values of two input arrays.
   *
   * @param iCorrect   The first input array.
   * @param iPredicted The second input array.
   * @param iCount     The number of elements in the input arrays.
   * @return The correlation between the absolute values of the two input arrays.
   */
  public static double correlationAbs(int[] iCorrect, int[] iPredicted, int iCount) {
    double fMeanC = 0.0D;
    double fMeanP = 0.0D;
    double fProdCP = 0.0D;
    double fSumCSq = 0.0D;
    double fSumPSq = 0.0D;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      fMeanC += Math.abs(iCorrect[iRow]);
      fMeanP += Math.abs(iPredicted[iRow]);
    }

    fMeanC /= iCount;
    fMeanP /= iCount;
    // Calculate the product of the differences and the sum of the squares of the differences
    for (int iRow = 1; iRow <= iCount; iRow++) {
      fProdCP += (Math.abs(iCorrect[iRow]) - fMeanC) * (Math.abs(iPredicted[iRow]) - fMeanP);
      fSumPSq += Math.pow(Math.abs(iPredicted[iRow]) - fMeanP, 2D);
      fSumCSq += Math.pow(Math.abs(iCorrect[iRow]) - fMeanC, 2D);
    }

    return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
  }

  /**
   * Method to calculate the correlation between two input arrays.
   *
   * @param iCorrect   The first input array.
   * @param iPredicted The second input array.
   * @param iCount     The number of elements in the input arrays.
   * @return The correlation between the two input arrays.
   */
  public static double correlation(int[] iCorrect, int[] iPredicted, int iCount) {
    double fMeanC = 0.0D;
    double fMeanP = 0.0D;
    double fProdCP = 0.0D;
    double fSumCSq = 0.0D;
    double fSumPSq = 0.0D;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      fMeanC += iCorrect[iRow];
      fMeanP += iPredicted[iRow];
    }

    fMeanC /= iCount;
    fMeanP /= iCount;
    // Calculate the product of the differences and the sum of the squares of the differences
    for (int iRow = 1; iRow <= iCount; iRow++) {
      fProdCP += (iCorrect[iRow] - fMeanC) * (iPredicted[iRow] - fMeanP);
      fSumPSq += Math.pow(iPredicted[iRow] - fMeanP, 2D);
      fSumCSq += Math.pow(iCorrect[iRow] - fMeanC, 2D);
    }

    return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
  }

  /**
   * This method creates a confusion table for trinary estimates and correct values.
   *
   * @param iTrinaryEstimate an array of trinary estimates
   * @param iTrinaryCorrect  an array of trinary correct values
   * @param iDataCount       the number of data points
   * @param estCorr          a 2D array to store the confusion table
   */
  public static void trinaryOrBinaryConfusionTable(int[] iTrinaryEstimate, int[] iTrinaryCorrect,
                                                   int iDataCount, int[][] estCorr) {
    // Initialize the confusion table to 0
    for (int i = 0; i <= 2; i++) {
      for (int j = 0; j <= 2; j++) {
        estCorr[i][j] = 0;
      }

    }

    for (int i = 1; i <= iDataCount; i++)
    // Check if the estimate and correct values are within the range of -1 to +1
    {
      if (iTrinaryEstimate[i] > -2 && iTrinaryEstimate[i] < 2 && iTrinaryCorrect[i] > -2 &&
          iTrinaryCorrect[i] < 2)
      // Increment the corresponding cell in the confusion table
      {
        estCorr[iTrinaryEstimate[i] + 1][iTrinaryCorrect[i] + 1]++;
      } else {
        System.out.println("Estimate or correct value " + i +
            " out of range -1 to +1 (data count may be wrong): " + iTrinaryEstimate[i] + " " +
            iTrinaryCorrect[i]);
      }
    }

  }

  /**
   * This method calculates the absolute correlation between the correct and predicted values.
   *
   * @param iCorrect   an array of correct values
   * @param iPredicted an array of predicted values
   * @param bSelected  an array of booleans indicating if the data point is selected
   * @param bInvert    a boolean indicating if the selection should be inverted
   * @param iCount     the number of data points
   * @return the absolute correlation between the correct and predicted values
   */
  public static double correlationAbs(int[] iCorrect, int[] iPredicted, boolean[] bSelected,
                                      boolean bInvert, int iCount) {
    double fMeanC = 0.0D;
    double fMeanP = 0.0D;
    double fProdCP = 0.0D;
    double fSumCSq = 0.0D;
    double fSumPSq = 0.0D;
    int iDataCount = 0;
    for (int iRow = 1; iRow <= iCount; iRow++)
    // Check if the data point is selected
    {
      if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
        // Add the absolute values of the correct and predicted values to the means
        fMeanC += Math.abs(iCorrect[iRow]);
        fMeanP += Math.abs(iPredicted[iRow]);
        iDataCount++;
      }
    }

    // Calculate the means
    fMeanC /= iDataCount;
    fMeanP /= iDataCount;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
        // Calculate the product of the differences from the means
        fProdCP += (Math.abs(iCorrect[iRow]) - fMeanC) * (Math.abs(iPredicted[iRow]) - fMeanP);
        // Calculate the sum of the squares of the differences from the means
        fSumPSq += Math.pow(Math.abs(iPredicted[iRow]) - fMeanP, 2D);
        fSumCSq += Math.pow(Math.abs(iCorrect[iRow]) - fMeanC, 2D);
      }
    }

    return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
  }

  /**
   * This method calculates the number of correct predictions.
   *
   * @param iCorrect              an array of correct values
   * @param iPredicted            an array of predicted values
   * @param iCount                the number of elements to compare
   * @param bChangeSignOfOneArray if true, the sign of one array is changed before comparison
   * @return the number of correct predictions
   */
  public static int accuracy(int[] iCorrect, int[] iPredicted, int iCount,
                             boolean bChangeSignOfOneArray) {
    int iCorrectCount = 0;
    if (bChangeSignOfOneArray) {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        if (iCorrect[iRow] == -iPredicted[iRow]) {
          iCorrectCount++;
        }
      }

    } else {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        if (iCorrect[iRow] == iPredicted[iRow]) {
          iCorrectCount++;
        }
      }

    }
    return iCorrectCount;
  }

  /**
   * This method calculates the number of correct predictions for selected elements.
   *
   * @param iCorrect   an array of correct values
   * @param iPredicted an array of predicted values
   * @param bSelected  an array of booleans indicating which elements are selected
   * @param bInvert    if true, the selection is inverted
   * @param iCount     the number of elements to compare
   * @return the number of correct predictions for selected elements
   */
  public static int accuracy(int[] iCorrect, int[] iPredicted, boolean[] bSelected, boolean bInvert,
                             int iCount) {
    int iCorrectCount = 0;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      if ((bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) &&
          iCorrect[iRow] == iPredicted[iRow]) {
        iCorrectCount++;
      }
    }

    return iCorrectCount;
  }

  /**
   * This method calculates the number of correct predictions for selected elements within a margin of 1.
   *
   * @param iCorrect   an array of correct values
   * @param iPredicted an array of predicted values
   * @param bSelected  an array of booleans indicating which elements are selected
   * @param bInvert    if true, the selection is inverted
   * @param iCount     the number of elements to compare
   * @return the number of correct predictions for selected elements within a margin of 1
   */
  public static int accuracyWithin1(int[] iCorrect, int[] iPredicted, boolean[] bSelected,
                                    boolean bInvert, int iCount) {
    int iCorrectCount = 0;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      if ((bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) &&
          Math.abs(iCorrect[iRow] - iPredicted[iRow]) <= 1) {
        iCorrectCount++;
      }
    }

    return iCorrectCount;
  }

  /**
   * This method calculates the number of correct predictions within a margin of 1.
   *
   * @param iCorrect              an array of correct values
   * @param iPredicted            an array of predicted values
   * @param iCount                the number of elements to compare
   * @param bChangeSignOfOneArray if true, the sign of one array is changed before comparison
   * @return the number of correct predictions within a margin of 1
   */
  public static int accuracyWithin1(int[] iCorrect, int[] iPredicted, int iCount,
                                    boolean bChangeSignOfOneArray) {
    int iCorrectCount = 0;
    if (bChangeSignOfOneArray) {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        if (Math.abs(iCorrect[iRow] + iPredicted[iRow]) <= 1) {
          iCorrectCount++;
        }
      }

    } else {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        if (Math.abs(iCorrect[iRow] - iPredicted[iRow]) <= 1) {
          iCorrectCount++;
        }
      }

    }
    return iCorrectCount;
  }

  /**
   * Calculates the absolute mean percentage error without division.
   *
   * @param iCorrect   an array of correct values
   * @param iPredicted an array of predicted values
   * @param bSelected  an array of booleans indicating which values to include in the calculation
   * @param bInvert    a boolean indicating whether to invert the selection
   * @param iCount     the number of values to include in the calculation
   * @return the absolute mean percentage error without division
   */
  public static double absoluteMeanPercentageErrorNoDivision(int[] iCorrect, int[] iPredicted,
                                                             boolean[] bSelected, boolean bInvert,
                                                             int iCount) {
    int iDataCount = 0;
    double fAMeanPE = 0.0D;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
        fAMeanPE += Math.abs(iPredicted[iRow] - iCorrect[iRow]);
        iDataCount++;
      }
    }
    if (iDataCount == 0) {
      return 0;
    }
    return fAMeanPE / (double) iDataCount;
  }

  /**
   * Calculates the absolute mean percentage error with division.
   *
   * @param iCorrect   an array of correct values
   * @param iPredicted an array of predicted values
   * @param bSelected  an array of booleans indicating which values to include in the calculation
   * @param bInvert    a boolean indicating whether to invert the selection
   * @param iCount     the number of values to include in the calculation
   * @return the absolute mean percentage error with division
   */
  public static double absoluteMeanPercentageError(int[] iCorrect, int[] iPredicted,
                                                   boolean[] bSelected, boolean bInvert,
                                                   int iCount) {
    int iDataCount = 0;
    double fAMeanPE = 0.0D;
    for (int iRow = 1; iRow <= iCount; iRow++) {
      if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
        fAMeanPE += Math.abs((iPredicted[iRow] - iCorrect[iRow]) / (double) iCorrect[iRow]);
        iDataCount++;
      }
    }
    if (iDataCount == 0) {
      return 0;
    }
    return fAMeanPE / (double) iDataCount;
  }

  /**
   * Calculates the absolute mean percentage error without division and without selection.
   *
   * @param iCorrect              an array of correct values
   * @param iPredicted            an array of predicted values
   * @param iCount                the number of values to include in the calculation
   * @param bChangeSignOfOneArray a boolean indicating whether to change the sign of one of the arrays
   * @return the absolute mean percentage error without division and without selection
   */
  public static double absoluteMeanPercentageErrorNoDivision(int[] iCorrect, int[] iPredicted,
                                                             int iCount,
                                                             boolean bChangeSignOfOneArray) {
    double fAMeanPE = 0.0D;
    if (bChangeSignOfOneArray) {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        fAMeanPE += Math.abs(iPredicted[iRow] + iCorrect[iRow]);
      }

    } else {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        fAMeanPE += Math.abs(iPredicted[iRow] - iCorrect[iRow]);
      }

    }
    return fAMeanPE / iCount;
  }

  /**
   * This method calculates the absolute mean percentage error.
   *
   * @param iCorrect              an array of correct values
   * @param iPredicted            an array of predicted values
   * @param iCount                the number of values
   * @param bChangeSignOfOneArray a boolean value indicating whether to change the sign of one array
   * @return the absolute mean percentage error
   */
  public static double absoluteMeanPercentageError(int[] iCorrect, int[] iPredicted, int iCount,
                                                   boolean bChangeSignOfOneArray) {
    double fAMeanPE = 0.0D;
    if (bChangeSignOfOneArray) {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        fAMeanPE +=
            Math.abs((double) (iPredicted[iRow] + iCorrect[iRow]) / (double) iCorrect[iRow]);
      }
    } else {
      for (int iRow = 1; iRow <= iCount; iRow++) {
        fAMeanPE +=
            Math.abs((double) (iPredicted[iRow] - iCorrect[iRow]) / (double) iCorrect[iRow]);
      }
    }
    return fAMeanPE / iCount;
  }

  /**
   * This method calculates the baseline accuracy of the majority class proportion.
   *
   * @param iCorrect an array of integers representing the correct class labels
   * @param iCount   the number of instances in the dataset
   * @return the baseline accuracy of the majority class proportion
   */
  public static double baselineAccuracyMajorityClassProportion(int[] iCorrect, int iCount) {
    // If the number of instances is 0, return 0.0
    if (iCount == 0) {
      return 0.0D;
    }
    int[] iClassCount = new int[100];
    int iMinClass = iCorrect[1];
    int iMaxClass = iCorrect[1];
    // Find the minimum and maximum class labels
    for (int i = 2; i <= iCount; i++) {
      if (iCorrect[i] < iMinClass) {
        iMinClass = iCorrect[i];
      }
      if (iCorrect[i] > iMaxClass) {
        iMaxClass = iCorrect[i];
      }
    }
    // If the range of class labels is greater than or equal to 100, return 0.0
    if (iMaxClass - iMinClass >= 100) {
      return 0.0D;
    }
    // Initialize the class count array
    for (int i = 0; i <= iMaxClass - iMinClass; i++) {
      iClassCount[i] = 0;
    }
    // Count the number of instances for each class
    for (int i = 1; i <= iCount; i++) {
      iClassCount[iCorrect[i] - iMinClass]++;
    }
    // Find the maximum class count
    int iMaxClassCount = 0;
    for (int i = 0; i <= iMaxClass - iMinClass; i++) {
      if (iClassCount[i] > iMaxClassCount) {
        iMaxClassCount = iClassCount[i];
      }
    }
    // Return the baseline accuracy of the majority class proportion
    return (double) iMaxClassCount / (double) iCount;
  }

  /**
   * This method calculates the baseline accuracy by making the largest class prediction.
   *
   * @param iCorrect    an array of correct values
   * @param iPredict    an array of predicted values
   * @param iCount      the number of values
   * @param bChangeSign a boolean value indicating whether to change the sign of the largest class
   */
  public static void baselineAccuracyMakeLargestClassPrediction(int[] iCorrect, int[] iPredict,
                                                                int iCount, boolean bChangeSign) {
    if (iCount == 0) {
      return;
    }
    int[] iClassCount = new int[100];
    int iMinClass = iCorrect[1];
    int iMaxClass = iCorrect[1];
    for (int i = 2; i <= iCount; i++) {
      if (iCorrect[i] < iMinClass) {
        iMinClass = iCorrect[i];
      }
      if (iCorrect[i] > iMaxClass) {
        iMaxClass = iCorrect[i];
      }
    }

    if (iMaxClass - iMinClass >= 100) {
      return;
    }
    for (int i = 0; i <= iMaxClass - iMinClass; i++) {
      iClassCount[i] = 0;
    }

    for (int i = 1; i <= iCount; i++) {
      iClassCount[iCorrect[i] - iMinClass]++;
    }

    int iMaxClassCount = 0;
    int iLargestClass = 0;
    for (int i = 0; i <= iMaxClass - iMinClass; i++) {
      if (iClassCount[i] > iMaxClassCount) {
        iMaxClassCount = iClassCount[i];
        iLargestClass = i + iMinClass;
      }
    }

    if (bChangeSign) {
      for (int i = 1; i <= iCount; i++) {
        iPredict[i] = -iLargestClass;
      }

    } else {
      for (int i = 1; i <= iCount; i++) {
        iPredict[i] = iLargestClass;
      }

    }
  }

}
