// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   Corpus.java

package nju.SEIII.EASIEST.SentiStrength.Corpus;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;
import nju.SEIII.EASIEST.SentiStrength.ClassificationResources;
import nju.SEIII.EASIEST.SentiStrength.ClassificationStatistics;
import nju.SEIII.EASIEST.SentiStrength.Paragraph;
import nju.SEIII.EASIEST.SentiStrength.UnusedTermsClassificationIndex;
import nju.SEIII.EASIEST.Utilities.FileOps;
import nju.SEIII.EASIEST.Utilities.Sort;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            ClassificationOptions, ClassificationResources, UnusedTermsClassificationIndex, Paragraph, 
//            ClassificationStatistics, SentimentWords

/**
 * A class representing a corpus of text documents, with methods for indexing, classifying, and analyzing the corpus.
 *
 * @UC <p><ul>
 * <li> UC-1 Assigning Sentiment Scores for Words
 * <li> UC-2 Assigning Sentiment Scores for Phrases
 * <li> UC-3 Spelling Correction
 * <li> UC-4 Booster Word Rule
 * <li> UC-5 Negating Word Rule
 * <li> UC-10 Negative Sentiment Ignored in Questions
 * <li> UC-11 Classify a single text
 * <li> UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]
 * <li> UC-13 Classify texts in a column within a file or folder
 * <li> UC-14 Listen at a port for texts to classify
 * <li> UC-15 Run interactively from the command line
 * <li> UC-19 Location of output folder
 * <li> UC-20 File name extension for output
 * <li> UC-21 Classify positive (1 to 5) and negative (-1 to -5) sentiment strength separately
 * <li> UC-22 Use trinary classification (positive-negative-neutral)
 * <li> UC-23 Use binary classification (positive-negative)
 * <li> UC-24 Use a single positive-negative scale classification
 * <li> UC-25 Explain the classification
 * <li> UC-26 Set Classification Algorithm Parameters
 * <li> UC-27 Optimise sentiment strengths of existing sentiment terms
 * <li> UC-28 Suggest new sentiment terms (from terms in misclassified texts)
 * <li> UC-29 Machine learning evaluations
 * </ul>
 * <p>
 */
public class BaseCorpus {

  public ClassificationOptions options;
  public ClassificationResources resources;
  UnusedTermsClassificationIndex unusedTermsClassificationIndex;
  int igSupcorpusMemberCount;
  protected Paragraph[] paragraph;
  protected int igParagraphCount;
  protected int[] igPosCorrect;
  protected int[] igNegCorrect;
  protected int[] igTrinaryCorrect;
  protected int[] igScaleCorrect;
  protected int[] igPosClass;
  protected int[] igNegClass;
  protected int[] igTrinaryClass;
  protected int[] igScaleClass;
  protected boolean bgCorpusClassified;
  protected int[] igSentimentIDList;
  protected int igSentimentIDListCount;
  protected int[] igSentimentIDParagraphCount;
  protected boolean bSentimentIDListMade;
  protected boolean[] bgSupcorpusMember;

  /**
   * Constructs a new Corpus object.
   */
  public BaseCorpus() {
    options = new ClassificationOptions();
    resources = new ClassificationResources();
    igParagraphCount = 0;
    bgCorpusClassified = false;
    igSentimentIDListCount = 0;
    bSentimentIDListMade = false;
    unusedTermsClassificationIndex = null;
  }

  /**
   * Indexes the classified corpus.
   */
  public void indexClassifiedCorpus() {
    unusedTermsClassificationIndex = new UnusedTermsClassificationIndex();
    unusedTermsClassificationIndex.initialise(false, true, false, false);
    for (int i = 1; i <= igParagraphCount; i++) {
      paragraph[i].addParagraphToIndexWithPosNegValues(unusedTermsClassificationIndex,
          igPosCorrect[i], igPosClass[i], igNegCorrect[i], igNegClass[i]);
    }
  }

  /**
   * Prints the classification index for unused terms in the corpus to a file.
   * The minimum frequency of the terms to be included in the index can be specified
   *
   * @param saveFile the location and name of the file to save the index to
   * @param iMinFreq the minimum frequency of the terms to be included in the index
   */
  public void printCorpusUnusedTermsClassificationIndex(String saveFile, int iMinFreq) {
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    if (unusedTermsClassificationIndex == null) {
      indexClassifiedCorpus();
    }
    unusedTermsClassificationIndex.printIndexWithPosNegValues(saveFile, iMinFreq);
    System.out.println("Term weights saved to " + saveFile);
  }

  /**
   * Sets the subcorpus by specifying which paragraphs belong to it.
   *
   * @param bSubcorpusMember an array of booleans indicating which paragraphs belong to the subcorpus
   */
  public void setSubcorpus(boolean[] bSubcorpusMember) {
    igSupcorpusMemberCount = 0;
    for (int i = 0; i <= igParagraphCount; i++) {
      if (bSubcorpusMember[i]) {
        bgSupcorpusMember[i] = true;
        igSupcorpusMemberCount++;
      } else {
        bgSupcorpusMember[i] = false;
      }
    }

  }

  /**
   * Sets all paragraphs in the corpus as members of the super corpus, effectively using the entire corpus instead of a subcorpus.
   * Updates the super corpus member count accordingly.
   */
  public void useWholeCorpusNotSubcorpus() {
    for (int i = 0; i <= igParagraphCount; i++) {
      bgSupcorpusMember[i] = true;
    }

    igSupcorpusMemberCount = igParagraphCount;
  }

  /**
   * Returns the total number of paragraphs in the corpus.
   *
   * @return the corpus size as an integer.
   */
  public int getCorpusSize() {
    return igParagraphCount;
  }

  /**
   * Sets a single text as the corpus, with the given positive and negative correct classifications.
   *
   * @param sText       the text to set as the corpus
   * @param iPosCorrect the number of positive correct classifications for the text
   * @param iNegCorrect the number of negative correct classifications for the text (if negative, will be multiplied by -1)
   * @return true if the corpus was successfully set, false otherwise
   */
  public boolean setSingleTextAsCorpus(String sText, int iPosCorrect, int iNegCorrect) {
    if (resources == null && !resources.initialise(options)) {
      return false;
    }
    igParagraphCount = 2;
    paragraph = new Paragraph[igParagraphCount];
    igPosCorrect = new int[igParagraphCount];
    igNegCorrect = new int[igParagraphCount];
    igTrinaryCorrect = new int[igParagraphCount];
    igScaleCorrect = new int[igParagraphCount];
    bgSupcorpusMember = new boolean[igParagraphCount];
    igParagraphCount = 1;
    paragraph[igParagraphCount] = new Paragraph();
    paragraph[igParagraphCount].setParagraph(sText, resources, options);
    igPosCorrect[igParagraphCount] = iPosCorrect;
    if (iNegCorrect < 0) {
      iNegCorrect *= -1;
    }
    igNegCorrect[igParagraphCount] = iNegCorrect;
    useWholeCorpusNotSubcorpus();
    return true;
  }

  /**
   * Sets the corpus by reading in a text file and creating Paragraph objects for each non-empty line in the file.
   * It also parses the classification information for each paragraph according to the options set in the resources object.
   *
   * @param sInFilenameAndPath the filename and path of the input text file.
   * @return true if the corpus was successfully set, false otherwise.
   */
  public boolean setCorpus(String sInFilenameAndPath) {
    if (resources == null && !resources.initialise(options)) {
      return false;
    }
    igParagraphCount = FileOps.i_CountLinesInTextFile(sInFilenameAndPath) + 1;
    if (igParagraphCount <= 2) {
      igParagraphCount = 0;
      return false;
    }
    paragraph = new Paragraph[igParagraphCount];
    igPosCorrect = new int[igParagraphCount];
    igNegCorrect = new int[igParagraphCount];
    igTrinaryCorrect = new int[igParagraphCount];
    igScaleCorrect = new int[igParagraphCount];
    bgSupcorpusMember = new boolean[igParagraphCount];
    igParagraphCount = 0;
    try {
      BufferedReader rReader = new BufferedReader(new FileReader(sInFilenameAndPath));
      String sLine;
      if (rReader.ready()) {
        rReader.readLine();
      }
      while ((sLine = rReader.readLine()) != null) {
        if (!sLine.equals("")) {
          processorForSetCorpus(sLine);
        }
      }
      rReader.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    useWholeCorpusNotSubcorpus();
    System.out.println("Number of texts in corpus: " + igParagraphCount);
    return true;
  }

  protected void processorForSetCorpus(String sLine) {
    paragraph[++igParagraphCount] = new Paragraph();
    int iLastTabPos = sLine.lastIndexOf("\t");
    int iFirstTabPos = sLine.indexOf("\t");
    if (iFirstTabPos < iLastTabPos) {
      paragraph[igParagraphCount].setParagraph(sLine.substring(iLastTabPos + 1), resources,
          options);
      try {
        igPosCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(0, iFirstTabPos).trim());
        igNegCorrect[igParagraphCount] =
            Integer.parseInt(sLine.substring(iFirstTabPos + 1, iLastTabPos).trim());
        if (igNegCorrect[igParagraphCount] < 0) {
          igNegCorrect[igParagraphCount] = -igNegCorrect[igParagraphCount];
        }
      } catch (Exception e) {
        System.out.println(
            "Positive or negative classification could not be read and will be ignored!: " + sLine);
        igPosCorrect[igParagraphCount] = 0;
      }
      if (igPosCorrect[igParagraphCount] > 5 || igPosCorrect[igParagraphCount] < 1) {
        System.out.println(
            "Warning, positive classification out of bounds and line will be ignored!: " + sLine);
        igParagraphCount--;
      } else if (igNegCorrect[igParagraphCount] > 5 || igNegCorrect[igParagraphCount] < 1) {
        System.out.println(
            "Warning, negative classification out of bounds (must be 1,2,3,4, or 5, with or without -) and line will be ignored!: " +
                sLine);
        igParagraphCount--;
      }
    } else {
      if (iFirstTabPos >= 0) {
        sLine = sLine.substring(iFirstTabPos + 1);
      }
      paragraph[igParagraphCount].setParagraph(sLine, resources, options);
      igPosCorrect[igParagraphCount] = 0;
      igNegCorrect[igParagraphCount] = 0;
    }
  }

  /**
   * Initializes the resources with the given options.
   *
   * @return true if the initialization was successful, false otherwise
   */
  public boolean initialise() {
    return resources.initialise(options);
  }

  /**
   * Recalculates the sentiment scores for each paragraph in the corpus that is a member of the supcorpus,
   * and then calculates the overall sentiment scores for the corpus.
   */
  public void reCalculateCorpusSentimentScores() {
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        paragraph[i].recalculateParagraphSentimentScores();
      }
    }
    calculateCorpusSentimentScores();
  }


  /**
   * Returns the positive sentiment score of the corpus member at index i.
   * If the index is out of range, it returns 0.
   *
   * @param i the index of the corpus member
   * @return the positive sentiment score of the corpus member at index i
   */
  public int getCorpusMemberPositiveSentimentScore(int i) {
    if (i < 1 || i > igParagraphCount) {
      return 0;
    } else {
      return paragraph[i].getParagraphPositiveSentiment();
    }
  }

  /**
   * Returns the negative sentiment score of the corpus member at index i.
   * If the index is out of range, it returns 0.
   *
   * @param i the index of the corpus member
   * @return the negative sentiment score of the corpus member at index i
   */
  public int getCorpusMemberNegativeSentimentScore(int i) {
    if (i < 1 || i > igParagraphCount) {
      return 0;
    } else {
      return paragraph[i].getParagraphNegativeSentiment();
    }
  }

  /**
   * Calculates the sentiment scores for all corpus members.
   * If there are no corpus members, it does nothing.
   * If the sentiment classification options are set, it also calculates the trinary and/or scale sentiment scores.
   */
  public void calculateCorpusSentimentScores() {
    if (igParagraphCount == 0) {
      return;
    }
    if (igPosClass == null || igPosClass.length < igPosCorrect.length) {
      igPosClass = new int[igParagraphCount + 1];
      igNegClass = new int[igParagraphCount + 1];
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
        igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
      }
    }
    bgCorpusClassified = true;
  }

  /**
   * Re-classifies the classified corpus for a change in sentiment of the given sentiment word ID.
   *
   * @param iSentimentWordID       the ID of the sentiment word
   * @param iMinParasToContainWord the minimum number of paragraphs the sentiment word needs to appear in to be considered
   */
  public void reClassifyClassifiedCorpusForSentimentChange(int iSentimentWordID,
                                                           int iMinParasToContainWord) {
    if (igParagraphCount == 0) {
      return;
    }
    if (!bSentimentIDListMade) {
      makeSentimentIDListForCompleteCorpusIgnoringSubcorpus();
    }
    int iSentimentWordIDArrayPos =
        Sort.i_FindIntPositionInSortedArray(iSentimentWordID, igSentimentIDList, 1,
            igSentimentIDListCount);
    if (iSentimentWordIDArrayPos == -1 ||
        igSentimentIDParagraphCount[iSentimentWordIDArrayPos] < iMinParasToContainWord) {
      return;
    }
    igPosClass = new int[igParagraphCount + 1];
    igNegClass = new int[igParagraphCount + 1];
    if (options.bgTrinaryMode) {
      igTrinaryClass = new int[igParagraphCount + 1];
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        paragraph[i].reClassifyClassifiedParagraphForSentimentChange(iSentimentWordID);
        igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
        igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
        if (options.bgTrinaryMode) {
          igTrinaryClass[i] = paragraph[i].getParagraphTrinarySentiment();
        }
        if (options.bgScaleMode) {
          igScaleClass[i] = paragraph[i].getParagraphScaleSentiment();
        }
      }
    }
    bgCorpusClassified = true;
  }

  /**
   * Prints the sentiment scores for each paragraph in the supcorpus to a file.
   *
   * @param sOutFilenameAndPath the filename and path of the output file.
   * @return true if the file was written successfully, false otherwise.
   */
  public boolean printCorpusSentimentScores(String sOutFilenameAndPath) {
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    try {
      BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFilenameAndPath));
      wWriter.write("Correct+\tCorrect-\tPredict+\tPredict-\tText\n");
      for (int i = 1; i <= igParagraphCount; i++) {
        if (bgSupcorpusMember[i]) {
          wWriter.write(igPosCorrect[i] + "\t" + igNegCorrect[i] + "\t" + igPosClass[i] + "\t" +
              igNegClass[i] + "\t" + paragraph[i].getTaggedParagraph() + "\n");
        }
      }
      wWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  /**
   * Returns the proportion of supcorpus members that were correctly classified as positive.
   *
   * @return the positive classification accuracy proportion.
   */
  public float getClassificationPositiveAccuracyProportion() {
    if (igSupcorpusMemberCount == 0) {
      return 0.0F;
    } else {
      return (float) getClassificationPositiveNumberCorrect() / (float) igSupcorpusMemberCount;
    }
  }

  /**
   * Returns the proportion of supcorpus members that were correctly classified as negative.
   *
   * @return the negative classification accuracy proportion.
   */
  public float getClassificationNegativeAccuracyProportion() {
    if (igSupcorpusMemberCount == 0) {
      return 0.0F;
    } else {
      return (float) getClassificationNegativeNumberCorrect() / (float) igSupcorpusMemberCount;
    }
  }

  /**
   * Returns the proportion of correctly classified negative supcorpus members that would result from always predicting the majority class.
   *
   * @return the baseline negative classification accuracy proportion.
   */
  public double getBaselineNegativeAccuracyProportion() {
    if (igParagraphCount == 0) {
      return 0.0D;
    } else {
      return ClassificationStatistics.baselineAccuracyMajorityClassProportion(igNegCorrect,
          igParagraphCount);
    }
  }

  /**
   * Returns the proportion of correctly classified positive supcorpus members that would result from always predicting the majority class.
   *
   * @return the baseline positive classification accuracy proportion.
   */
  public double getBaselinePositiveAccuracyProportion() {
    if (igParagraphCount == 0) {
      return 0.0D;
    } else {
      return ClassificationStatistics.baselineAccuracyMajorityClassProportion(igPosCorrect,
          igParagraphCount);
    }
  }

  /**
   * Returns the number of supcorpus members correctly classified as negative.
   *
   * @return the number of correct negative classifications.
   */
  public int getClassificationNegativeNumberCorrect() {
    if (igParagraphCount == 0) {
      return 0;
    }
    int iMatches = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i] && igNegCorrect[i] == -igNegClass[i]) {
        iMatches++;
      }
    }
    return iMatches;
  }

  /**
   * Returns the number of supcorpus members correctly classified as positive.
   *
   * @return the number of correct positive classifications.
   */
  public int getClassificationPositiveNumberCorrect() {
    return calculateCorrect(igPosCorrect, igPosClass);
  }


  /**
   * Calculates the mean absolute difference between the correct and classified positive scores for the
   * paragraphs in the corpus that are members of the super-corpus.
   *
   * @return the mean absolute difference between the correct and classified positive scores
   */
  public double getClassificationPositiveMeanDifference() {
    if (igParagraphCount == 0) {
      return 0.0D;
    }
    double fTotalDiff = 0.0D;
    int iTotal = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        fTotalDiff += Math.abs(igPosCorrect[i] - igPosClass[i]);
        iTotal++;
      }
    }

    if (iTotal > 0) {
      return fTotalDiff / iTotal;
    } else {
      return 0.0D;
    }
  }

  /**
   * Calculates the total absolute difference between the correct and classified positive scores for the
   * paragraphs in the corpus that are members of the super-corpus.
   *
   * @return the total absolute difference between the correct and classified positive scores
   */
  public int getClassificationPositiveTotalDifference() {
    if (igParagraphCount == 0) {
      return 0;
    }
    int iTotalDiff = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        iTotalDiff += Math.abs(igPosCorrect[i] - igPosClass[i]);
      }
    }

    return iTotalDiff;
  }

  /**
   * Calculates the number of paragraphs in the corpus that are members of the super-corpus and have
   * correctly classified trinary sentiment scores.
   *
   * @return the number of paragraphs with correctly classified trinary sentiment scores
   */
  public int getClassificationTrinaryNumberCorrect() {
    return calculateCorrect(igTrinaryCorrect, igTrinaryClass);
  }

  /**
   * Calculates the correlation between the correct and classified scale scores for the whole corpus.
   *
   * @return the correlation between the correct and classified scale scores
   */
  public float getClassificationScaleCorrelationWholeCorpus() {
    if (igParagraphCount == 0) {
      return 0.0F;
    } else {
      return (float) ClassificationStatistics.correlation(igScaleCorrect, igScaleClass,
          igParagraphCount);
    }
  }

  /**
   * Calculates the proportion of paragraphs in the corpus that are members of the super-corpus and have
   * correctly classified scale sentiment scores.
   *
   * @return the proportion of paragraphs with correctly classified scale sentiment scores
   */
  public float getClassificationScaleAccuracyProportion() {
    if (igSupcorpusMemberCount == 0) {
      return 0.0F;
    } else {
      return (float) getClassificationScaleNumberCorrect() / (float) igSupcorpusMemberCount;
    }
  }

  /**
   * Returns the correlation coefficient between the correct and classified positive sentiment scores for the whole corpus.
   *
   * @return the correlation coefficient between the correct and classified positive sentiment scores for the whole corpus
   */
  public float getClassificationPosCorrelationWholeCorpus() {
    if (igParagraphCount == 0) {
      return 0.0F;
    } else {
      return (float) ClassificationStatistics.correlationAbs(igPosCorrect, igPosClass,
          igParagraphCount);
    }
  }

  /**
   * Returns the correlation coefficient between the correct and classified negative sentiment scores for the whole corpus.
   *
   * @return the correlation coefficient between the correct and classified negative sentiment scores for the whole corpus
   */
  public float getClassificationNegCorrelationWholeCorpus() {
    if (igParagraphCount == 0) {
      return 0.0F;
    } else {
      return (float) ClassificationStatistics.correlationAbs(igNegCorrect, igNegClass,
          igParagraphCount);
    }
  }

  /**
   * Returns the number of paragraphs where the correct and classified sentiment scale values are equal.
   *
   * @return the number of paragraphs where the correct and classified sentiment scale values are equal
   */
  public int getClassificationScaleNumberCorrect() {
    return calculateCorrect(igScaleCorrect, igScaleClass);
  }

  /**
   * Returns the total difference between the correct and classified negative sentiment scores for the whole corpus.
   *
   * @return the total difference between the correct and classified negative sentiment scores for the whole corpus
   */
  public int getClassificationNegativeTotalDifference() {
    if (igParagraphCount == 0) {
      return 0;
    }
    int iTotalDiff = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        iTotalDiff += Math.abs(igNegCorrect[i] + igNegClass[i]);
      }
    }

    return iTotalDiff;
  }

  /**
   * Calculates the mean difference between the negative sentiment classification and the correct negative sentiment
   * classification for all paragraphs in the corpus.
   *
   * @return The mean difference between the negative sentiment classification and the correct negative sentiment
   * classification for all paragraphs in the corpus, or 0.0D if there are no paragraphs in the corpus.
   */
  public double getClassificationNegativeMeanDifference() {
    if (igParagraphCount == 0) {
      return 0.0D;
    }
    double fTotalDiff = 0.0D;
    int iTotal = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        fTotalDiff += Math.abs(igNegCorrect[i] + igNegClass[i]);
        iTotal++;
      }
    }

    if (iTotal > 0) {
      return fTotalDiff / iTotal;
    } else {
      return 0.0D;
    }
  }

  ///**
  // * Prints a summary of the sentiment classification results for all paragraphs in the corpus to a specified file.
  // *
  // * @param sOutFilenameAndPath The file path and name for the output file.
  // * @return true if the summary was successfully printed to the output file, false otherwise.
  // */
  //public boolean printClassificationResultsSummary_NOT_DONE(String sOutFilenameAndPath) {
  //    if (!bgCorpusClassified) {
  //        calculateCorpusSentimentScores();
  //    }
  //    try {
  //        BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFilenameAndPath));
  //        for (int i = 1; i <= igParagraphCount; i++) {
  //            boolean _tmp = bgSupcorpusMember[i];
  //        }
  //
  //        wWriter.close();
  //    } catch (FileNotFoundException e) {
  //        e.printStackTrace();
  //        return false;
  //    } catch (IOException e) {
  //        e.printStackTrace();
  //        return false;
  //    }
  //    return true;
  //}

  /**
   * Creates a list of all the sentiment IDs in the complete corpus (ignoring subcorpus divisions) and counts how many
   * times each ID appears in the corpus.
   */
  public void makeSentimentIDListForCompleteCorpusIgnoringSubcorpus() {
    igSentimentIDListCount = 0;
    for (int i = 1; i <= igParagraphCount; i++) {
      paragraph[i].makeSentimentIDList();
      if (paragraph[i].getSentimentIDList() != null) {
        igSentimentIDListCount += paragraph[i].getSentimentIDList().length;
      }
    }
    if (igSentimentIDListCount > 0) {
      igSentimentIDList = new int[igSentimentIDListCount + 1];
      igSentimentIDParagraphCount = new int[igSentimentIDListCount + 1];
      igSentimentIDListCount = 0;
      for (int i = 1; i <= igParagraphCount; i++) {
        int[] sentenceIDList = paragraph[i].getSentimentIDList();
        if (sentenceIDList != null) {
          for (int k : sentenceIDList) {
            if (k != 0) {
              igSentimentIDList[++igSentimentIDListCount] = k;
            }
          }
        }
      }
      Sort.quickSortInt(igSentimentIDList, 1, igSentimentIDListCount);
      for (int i = 1; i <= igParagraphCount; i++) {
        int[] sentenceIDList = paragraph[i].getSentimentIDList();
        if (sentenceIDList != null) {
          for (int k : sentenceIDList) {
            if (k != 0) {
              igSentimentIDParagraphCount[Sort.i_FindIntPositionInSortedArray(k, igSentimentIDList,
                  1, igSentimentIDListCount)]++;
            }
          }
        }
      }
    }
    bSentimentIDListMade = true;
  }

  /**
   * Runs 10-fold cross-validation multiple times and writes the results to a file.
   *
   * @param iMinImprovement     the minimum improvement required for a multi-optimisation to be used
   * @param bUseTotalDifference whether to use total difference instead of binary difference for multi-optimisation
   * @param iReplications       the number of times to run the cross-validation
   * @param iMultiOptimisations the number of times to perform multi-optimisation for each replication
   * @param sWriter             the writer to output the results to
   * @param wTermStrengthWriter the writer to output the term strength variables to
   */
  private void run10FoldCrossValidationMultipleTimes(int iMinImprovement,
                                                     boolean bUseTotalDifference, int iReplications,
                                                     int iMultiOptimisations,
                                                     BufferedWriter sWriter,
                                                     BufferedWriter wTermStrengthWriter) {
    for (int i = 1; i <= iReplications; i++) {
      run10FoldCrossValidationOnce(iMinImprovement, bUseTotalDifference, iMultiOptimisations,
          sWriter, wTermStrengthWriter);
    }

    System.out.println("Set of " + iReplications + " 10-fold cross validations finished");
  }

  /**
   * Runs 10-fold cross-validation multiple times and writes the results to a file.
   *
   * @param iMinImprovement     the minimum improvement required for a multi-optimisation to be used
   * @param bUseTotalDifference whether to use total difference instead of binary difference for multi-optimisation
   * @param iReplications       the number of times to run the cross-validation
   * @param iMultiOptimisations the number of times to perform multi-optimisation for each replication
   * @param sOutFileName        the name of the file to output the results to
   */

  public void run10FoldCrossValidationMultipleTimes(int iMinImprovement,
                                                    boolean bUseTotalDifference, int iReplications,
                                                    int iMultiOptimisations, String sOutFileName) {
    try {
      BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFileName));
      BufferedWriter wTermStrengthWriter = new BufferedWriter(
          new FileWriter(FileOps.s_ChopFileNameExtension(sOutFileName) + "_termStrVars.txt"));
      options.printClassificationOptionsHeadings(wWriter);
      writeClassificationStatsHeadings(wWriter);
      options.printClassificationOptionsHeadings(wTermStrengthWriter);
      resources.sentimentWords.printSentimentTermsInSingleHeaderRow(wTermStrengthWriter);
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wWriter, wTermStrengthWriter);
      wWriter.close();
      wTermStrengthWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Classifies all lines of text in a file and writes the results, including IDs, to a file.
   *
   * @param sInputFile  the name of the file to read text from
   * @param iTextCol    the column number of the text to classify
   * @param iIDCol      the column number of the ID for each text
   * @param sOutputFile the name of the file to output the results to
   */
  public void classifyAllLinesAndRecordWithID(String sInputFile, int iTextCol, int iIDCol,
                                              String sOutputFile) {

    int iCount1 = 0;
    String sLine = "";
    try {
      BufferedReader rReader = new BufferedReader(new FileReader(sInputFile));
      BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile));
      while (rReader.ready()) {
        sLine = rReader.readLine();
        iCount1++;
        if (!Objects.equals(sLine, "")) {
          processorForClassifyAllLinesAndRecordWithID(sLine, iTextCol, iIDCol, wWriter);
        }
      }
      Thread.sleep(10L);
      if (rReader.ready()) {
        System.out.println("Reader ready again after pause!");
      }
      int character;
      if ((character = rReader.read()) != -1) {
        System.out.println("Reader returns char after reader.read() false! " + character);
      }
      rReader.close();
      wWriter.close();
    } catch (FileNotFoundException e) {
      System.out.println("Could not find input file: " + sInputFile);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Error reading or writing from file: " + sInputFile);
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("Error reading from or writing to file: " + sInputFile);
      e.printStackTrace();
    }
    System.out.println(
        "Processed " + iCount1 + " lines from file: " + sInputFile + ". Last line was:\n" + sLine);
  }

  protected void processorForClassifyAllLinesAndRecordWithID(String sLine, int iTextCol, int iIDCol,
                                                             BufferedWriter wWriter)
      throws IOException {
    int iPos = 0;
    int iNeg = 0;
    String[] sData = sLine.split("\t");
    if (sData.length > iTextCol && sData.length > iIDCol) {
      Paragraph paragraph = new Paragraph();
      paragraph.setParagraph(sData[iTextCol], resources, options);
      iPos = paragraph.getParagraphPositiveSentiment();
      iNeg = paragraph.getParagraphNegativeSentiment();
      wWriter.write(sData[iIDCol] + "\t" + iPos + "\t" + iNeg + "\n");
    }
  }

  /**
   * This method reads in an input file, annotates each line with sentiment analysis using the specified text column,
   * and writes the annotated lines to a temporary file. The original input file is then deleted and the temporary file
   * is renamed to the original file name. The annotation can be done in trinary mode (positive, negative, or neutral),
   * scale mode (a sentiment score between -10 and 10), or binary mode (positive or negative). The mode is determined
   * by the options specified in the calling code.
   * 此方法读入一个输入文件，使用指定的文本列用情感分析注释每一行，并将注释的行写入一个临时文件。
   * 然后删除原始输入文件并将临时文件重命名为原始文件名。注释可以在三元模式（正面、负面或中性）下完成，
   * 比例模式（-10 到 10 之间的情绪分数）或二进制模式（正面或负面）。模式由调用代码中指定的选项决定
   *
   * @param sInputFile The path to the input file to be annotated
   * @param iTextCol   The index of the text column in the input file to be analyzed
   */
  public void annotateAllLinesInInputFile(String sInputFile, int iTextCol) {
    String sTempFile = sInputFile + "_temp";
    try {
      BufferedReader rReader = new BufferedReader(new FileReader(sInputFile));
      BufferedWriter wWriter = new BufferedWriter(new FileWriter(sTempFile));
      while (rReader.ready()) {
        String sLine = rReader.readLine();
        if (!Objects.equals(sLine, "")) {
          processorForAnnotateAllLinesInInputFile(sLine, iTextCol, wWriter);
        }
      }
      rReader.close();
      wWriter.close();
      Path original = Paths.get(sInputFile);
      Files.delete(original);
      File newFile = new File(sTempFile);
      newFile.renameTo(new File(sInputFile));
    } catch (FileNotFoundException e) {
      System.out.println("Could not find input file: " + sInputFile);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println("Error reading or writing from file: " + sInputFile);
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("Error reading from or writing to file: " + sInputFile);
      e.printStackTrace();
    }
  }

  protected void processorForAnnotateAllLinesInInputFile(String sLine, int iTextCol,
                                                         BufferedWriter wWriter)
      throws IOException {
    int iPos = 0;
    int iNeg = 0;
    String[] sData = sLine.split("\t");
    if (sData.length > iTextCol) {
      Paragraph paragraph = new Paragraph();
      paragraph.setParagraph(sData[iTextCol], resources, options);

      iPos = paragraph.getParagraphPositiveSentiment();
      iNeg = paragraph.getParagraphNegativeSentiment();
      wWriter.write(sLine + "\t" + iPos + "\t" + iNeg + "\n");
    } else {
      wWriter.write(sLine + "\n");
    }
  }

  /**
   * This method reads the input file, classifies all lines in it using the parameters specified in the method
   * signature, and writes the classification results to the output file.
   *
   * @param sInputFile  The name of the input file to be classified.
   * @param iTextCol    The index of the text column in the input file.
   * @param sOutputFile The name of the output file to be written with the classification results.
   */
  public void classifyAllLinesInInputFile(String sInputFile, int iTextCol, String sOutputFile) {
    int iPos = 0;
    int iNeg = 0;
    int iTrinary = -3;
    int iScale = -10;
    int iFileTrinary = -2;
    int iFileScale = -9;
    int iClassified = 0;
    int iCorrectPosCount = 0;
    int iCorrectNegCount = 0;
    int iCorrectTrinaryCount = 0;
    int iCorrectScaleCount = 0;
    int iPosAbsDiff = 0;
    int iNegAbsDiff = 0;
    int[][] confusion = {new int[3], new int[3], new int[3]};
    int maxClassifyForCorrelation = 20000;
    int[] iPosClassCorr = new int[maxClassifyForCorrelation];
    int[] iNegClassCorr = new int[maxClassifyForCorrelation];
    int[] iPosClassPred = new int[maxClassifyForCorrelation];
    int[] iNegClassPred = new int[maxClassifyForCorrelation];
    int[] iScaleClassCorr = new int[maxClassifyForCorrelation];
    int[] iScaleClassPred = new int[maxClassifyForCorrelation];
    String sRationale = "";
    String sOutput = "";
    try {
      BufferedReader rReader;
      BufferedWriter wWriter;
      if (options.bgForceUTF8) {
        wWriter = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(sOutputFile), StandardCharsets.UTF_8));
        rReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(sInputFile), StandardCharsets.UTF_8));
      } else {
        wWriter = new BufferedWriter(new FileWriter(sOutputFile));
        rReader = new BufferedReader(new FileReader(sInputFile));
      }
      if (options.bgTrinaryMode || options.bgScaleMode) {
        wWriter.write("Overall\tText");
      } else if (options.getBgTensiStrength()) {
        wWriter.write("Relax\tStress\tText");
      } else {
        wWriter.write("Positive\tNegative\tText");
      }
      if (options.bgExplainClassification) {
        wWriter.write("\tExplanation\n");
      } else {
        wWriter.write("\n");
      }
      while (rReader.ready()) {
        String sLine = rReader.readLine();
        if (!Objects.equals(sLine, "")) {
          int iTabPos = sLine.lastIndexOf("\t");
          int iFilePos = 0;
          int iFileNeg = 0;
          if (iTabPos >= 0) {
            String[] sData = sLine.split("\t");
            if (sData.length > 1) {
              if (iTextCol > -1) {
                wWriter.write(sLine + "\t");
                if (iTextCol < sData.length) {
                  sLine = sData[iTextCol];
                }
              } else if (options.bgTrinaryMode) {
                iFileTrinary = -2;
                try {
                  iFileTrinary = Integer.parseInt(sData[0].trim());
                  if (iFileTrinary > 1 || iFileTrinary < -1) {
                    System.out.println("Invalid trinary sentiment " + iFileTrinary +
                        " (expected -1,0,1) at line: " + sLine);
                    iFileTrinary = 0;
                  }
                } catch (NumberFormatException numberformatexception) {
                }
              } else if (options.bgScaleMode) {
                iFileScale = -9;
                try {
                  iFileScale = Integer.parseInt(sData[0].trim());
                  if (iFileScale > 4 || iFileScale < -4) {
                    System.out.println("Invalid overall sentiment " + iFileScale +
                        " (expected -4 to +4) at line: " + sLine);
                    iFileScale = 0;
                  }
                } catch (NumberFormatException numberformatexception1) {
                }
              } else {
                try {
                  iFilePos = Integer.parseInt(sData[0].trim());
                  iFileNeg = Integer.parseInt(sData[1].trim());
                  if (iFileNeg < 0) {
                    iFileNeg = -iFileNeg;
                  }
                } catch (NumberFormatException numberformatexception2) {
                }
              }
            }
            sLine = sLine.substring(iTabPos + 1);
          }
          Paragraph paragraph = new Paragraph();
          paragraph.setParagraph(sLine, resources, options);
          if (options.bgTrinaryMode) {
            iTrinary = paragraph.getParagraphTrinarySentiment();
            if (options.bgExplainClassification) {
              sRationale = "\t" + paragraph.getClassificationRationale();
            }
            sOutput = iTrinary + "\t" + sLine + sRationale + "\n";
          } else if (options.bgScaleMode) {
            iScale = paragraph.getParagraphScaleSentiment();
            if (options.bgExplainClassification) {
              sRationale = "\t" + paragraph.getClassificationRationale();
            }
            sOutput = iScale + "\t" + sLine + sRationale + "\n";
          } else {
            iPos = paragraph.getParagraphPositiveSentiment();
            iNeg = paragraph.getParagraphNegativeSentiment();
            if (options.bgExplainClassification) {
              sRationale = "\t" + paragraph.getClassificationRationale();
            }
            sOutput = iPos + "\t" + iNeg + "\t" + sLine + sRationale + "\n";
          }
          wWriter.write(sOutput);
          if (options.bgTrinaryMode) {
            if (iFileTrinary > -2 && iFileTrinary < 2 && iTrinary > -2 && iTrinary < 2) {
              iClassified++;
              if (iFileTrinary == iTrinary) {
                iCorrectTrinaryCount++;
              }
              confusion[iTrinary + 1][iFileTrinary + 1]++;
            }
          } else if (options.bgScaleMode) {
            if (iFileScale > -9) {
              iClassified++;
              if (iFileScale == iScale) {
                iCorrectScaleCount++;
              }
              if (iClassified < maxClassifyForCorrelation) {
                iScaleClassCorr[iClassified] = iFileScale;
              }
              iScaleClassPred[iClassified] = iScale;
            }
          } else if (iFileNeg != 0) {
            iClassified++;
            if (iPos == iFilePos) {
              iCorrectPosCount++;
            }
            iPosAbsDiff += Math.abs(iPos - iFilePos);
            if (iClassified < maxClassifyForCorrelation) {
              iPosClassCorr[iClassified] = iFilePos;
            }
            iPosClassPred[iClassified] = iPos;
            if (iNeg == -iFileNeg) {
              iCorrectNegCount++;
            }
            iNegAbsDiff += Math.abs(iNeg + iFileNeg);
            iNegClassCorr[iClassified] = iFileNeg;
            iNegClassPred[iClassified] = iNeg;
          }
        }
      }
      rReader.close();
      wWriter.close();
      if (iClassified > 0) {
        if (options.bgTrinaryMode) {
          System.out.println("Trinary correct: " + iCorrectTrinaryCount + " (" +
              ((float) iCorrectTrinaryCount / (float) iClassified) * 100F + "%).");
          System.out.println("Correct -> -1   0   1");
          System.out.println(
              "Est = -1   " + confusion[0][0] + " " + confusion[0][1] + " " + confusion[0][2]);
          System.out.println(
              "Est =  0   " + confusion[1][0] + " " + confusion[1][1] + " " + confusion[1][2]);
          System.out.println(
              "Est =  1   " + confusion[2][0] + " " + confusion[2][1] + " " + confusion[2][2]);
        } else if (options.bgScaleMode) {
          System.out.println("Scale correct: " + iCorrectScaleCount + " (" +
              ((float) iCorrectScaleCount / (float) iClassified) * 100F + "%) out of " +
              iClassified);
          System.out.println("  Correlation: " +
              ClassificationStatistics.correlation(iScaleClassCorr, iScaleClassPred, iClassified));
        } else {
          System.out.print(options.sgProgramPos + " correct: " + iCorrectPosCount + " (" +
              ((float) iCorrectPosCount / (float) iClassified) * 100F + "%).");
          System.out.println(" Mean abs diff: " + (float) iPosAbsDiff / (float) iClassified);
          if (iClassified < maxClassifyForCorrelation) {
            System.out.println(" Correlation: " +
                ClassificationStatistics.correlationAbs(iPosClassCorr, iPosClassPred, iClassified));
            int corrWithin1 =
                ClassificationStatistics.accuracyWithin1(iPosClassCorr, iPosClassPred, iClassified,
                    false);
            System.out.println(" Correct +/- 1: " + corrWithin1 + " (" +
                (float) (100 * corrWithin1) / (float) iClassified + "%)");
          }
          System.out.print(options.sgProgramNeg + " correct: " + iCorrectNegCount + " (" +
              ((float) iCorrectNegCount / (float) iClassified) * 100F + "%).");
          System.out.println(" Mean abs diff: " + (float) iNegAbsDiff / (float) iClassified);
          if (iClassified < maxClassifyForCorrelation) {
            System.out.println(" Correlation: " +
                ClassificationStatistics.correlationAbs(iNegClassCorr, iNegClassPred, iClassified));
            int corrWithin1 =
                ClassificationStatistics.accuracyWithin1(iNegClassCorr, iNegClassPred, iClassified,
                    true);
            System.out.println(" Correct +/- 1: " + corrWithin1 + " (" +
                (float) (100 * corrWithin1) / (float) iClassified + "%)");
          }
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Could not find input file: " + sInputFile);
      e.printStackTrace();
    } catch (IOException e) {
      System.out.println(
          "Error reading from input file: " + sInputFile + " or writing to output file " +
              sOutputFile);
      e.printStackTrace();
    }
  }

  /**
   * Writes the headings for classification statistics to the given BufferedWriter.
   *
   * @param w the BufferedWriter to write the headings to
   * @throws IOException if an I/O error occurs while writing to the BufferedWriter
   */
  private void writeClassificationStatsHeadings(BufferedWriter w) throws IOException {
    String sPosOrScale;
    if (options.bgScaleMode) {
      sPosOrScale = "ScaleCorrel";
    } else {
      sPosOrScale = "PosCorrel";
    }
    w.write(
        "\tPosCorrect\tiPosCorrect/Total\tNegCorrect\tNegCorrect/Total\tPosWithin1\tPosWithin1/Total\tNegWithin1\tNegWithin1/Total\t" +
            sPosOrScale + "\tNegCorrel" + "\tPosMPE\tNegMPE\tPosMPEnoDiv\tNegMPEnoDiv" +
            "\tTrinaryOrScaleCorrect\tTrinaryOrScaleCorrect/TotalClassified" +
            "\tTrinaryOrScaleCorrectWithin1\tTrinaryOrScaleCorrectWithin1/TotalClassified" +
            "\test-1corr-1\test-1corr0\test-1corr1" + "\test0corr-1\test0corr0\test0corr1" +
            "\test1corr-1\test1corr0\test1corr1" + "\tTotalClassified\n");
  }

  /**
   * Runs 10-fold cross-validation for all possible option variations.
   *
   * @param iMinImprovement     The minimum improvement required to stop early
   * @param bUseTotalDifference Whether to use the total difference or the average difference in convergence calculations
   * @param iReplications       The number of replications to perform
   * @param iMultiOptimisations The number of multi-optimisations to perform
   * @param sOutFileName        The name of the output file to write the results to
   */
  public void run10FoldCrossValidationForAllOptionVariations(int iMinImprovement,
                                                             boolean bUseTotalDifference,
                                                             int iReplications,
                                                             int iMultiOptimisations,
                                                             String sOutFileName) {
    try {
      BufferedWriter wResultsWriter = new BufferedWriter(new FileWriter(sOutFileName));
      BufferedWriter wTermStrengthWriter = new BufferedWriter(
          new FileWriter(FileOps.s_ChopFileNameExtension(sOutFileName) + "_termStrVars.txt"));
      if (igPosClass == null || igPosClass.length < igPosCorrect.length) {
        igPosClass = new int[igParagraphCount + 1];
        igNegClass = new int[igParagraphCount + 1];
        igTrinaryClass = new int[igParagraphCount + 1];
      }
      options.printClassificationOptionsHeadings(wResultsWriter);
      writeClassificationStatsHeadings(wResultsWriter);
      options.printClassificationOptionsHeadings(wTermStrengthWriter);
      resources.sentimentWords.printSentimentTermsInSingleHeaderRow(wTermStrengthWriter);
      System.out.println("About to start classifications for 20 different option variations");
      if (options.bgTrinaryMode) {
        ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igTrinaryCorrect,
            igTrinaryClass, igParagraphCount, false);
      } else if (options.bgScaleMode) {
        ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igScaleCorrect,
            igScaleClass, igParagraphCount, false);
      } else {
        ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igPosCorrect,
            igPosClass, igParagraphCount, false);
        ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igNegCorrect,
            igNegClass, igParagraphCount, true);
      }
      options.printBlankClassificationOptions(wResultsWriter);
      if (options.bgTrinaryMode) {
        printClassificationResultsRow(igPosClass, igNegClass, igTrinaryClass, wResultsWriter);
      } else {
        printClassificationResultsRow(igPosClass, igNegClass, igScaleClass, wResultsWriter);
      }
      options.printClassificationOptions(wResultsWriter, igParagraphCount, bUseTotalDifference,
          iMultiOptimisations);
      calculateCorpusSentimentScores();
      if (options.bgTrinaryMode) {
        printClassificationResultsRow(igPosClass, igNegClass, igTrinaryClass, wResultsWriter);
      } else {
        printClassificationResultsRow(igPosClass, igNegClass, igScaleClass, wResultsWriter);
      }
      options.printBlankClassificationOptions(wTermStrengthWriter);
      resources.sentimentWords.printSentimentValuesInSingleRow(wTermStrengthWriter);
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.igEmotionParagraphCombineMethod = 1 - options.igEmotionParagraphCombineMethod;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.igEmotionParagraphCombineMethod = 1 - options.igEmotionParagraphCombineMethod;
      options.igEmotionSentenceCombineMethod = 1 - options.igEmotionSentenceCombineMethod;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.igEmotionSentenceCombineMethod = 1 - options.igEmotionSentenceCombineMethod;
      options.bgReduceNegativeEmotionInQuestionSentences =
          !options.bgReduceNegativeEmotionInQuestionSentences;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgReduceNegativeEmotionInQuestionSentences =
          !options.bgReduceNegativeEmotionInQuestionSentences;
      options.bgMissCountsAsPlus2 = !options.bgMissCountsAsPlus2;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgMissCountsAsPlus2 = !options.bgMissCountsAsPlus2;
      options.bgYouOrYourIsPlus2UnlessSentenceNegative =
          !options.bgYouOrYourIsPlus2UnlessSentenceNegative;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgYouOrYourIsPlus2UnlessSentenceNegative =
          !options.bgYouOrYourIsPlus2UnlessSentenceNegative;
      options.bgExclamationInNeutralSentenceCountsAsPlus2 =
          !options.bgExclamationInNeutralSentenceCountsAsPlus2;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgExclamationInNeutralSentenceCountsAsPlus2 =
          !options.bgExclamationInNeutralSentenceCountsAsPlus2;
      options.bgUseIdiomLookupTable = !options.bgUseIdiomLookupTable;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgUseIdiomLookupTable = !options.bgUseIdiomLookupTable;
      int iTemp = options.igMoodToInterpretNeutralEmphasis;
      options.igMoodToInterpretNeutralEmphasis = -options.igMoodToInterpretNeutralEmphasis;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.igMoodToInterpretNeutralEmphasis = 0;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.igMoodToInterpretNeutralEmphasis = iTemp;
      System.out.println("About to start 10th option variation classification");
      options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion =
          !options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion =
          !options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion;
      options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion =
          !options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion =
          !options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion;
      options.bgIgnoreBoosterWordsAfterNegatives = !options.bgIgnoreBoosterWordsAfterNegatives;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgIgnoreBoosterWordsAfterNegatives = !options.bgIgnoreBoosterWordsAfterNegatives;
      options.bgMultipleLettersBoostSentiment = !options.bgMultipleLettersBoostSentiment;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgMultipleLettersBoostSentiment = !options.bgMultipleLettersBoostSentiment;
      options.bgBoosterWordsChangeEmotion = !options.bgBoosterWordsChangeEmotion;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgBoosterWordsChangeEmotion = !options.bgBoosterWordsChangeEmotion;
      if (options.bgNegatingWordsFlipEmotion) {
        options.bgNegatingWordsFlipEmotion = false;
        run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
            iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
        options.bgNegatingWordsFlipEmotion = !options.bgNegatingWordsFlipEmotion;
      } else {
        options.bgNegatingPositiveFlipsEmotion = !options.bgNegatingPositiveFlipsEmotion;
        run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
            iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
        options.bgNegatingPositiveFlipsEmotion = !options.bgNegatingPositiveFlipsEmotion;
        options.bgNegatingNegativeNeutralisesEmotion =
            !options.bgNegatingNegativeNeutralisesEmotion;
        run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
            iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
        options.bgNegatingNegativeNeutralisesEmotion =
            !options.bgNegatingNegativeNeutralisesEmotion;
      }
      options.bgCorrectSpellingsWithRepeatedLetter = !options.bgCorrectSpellingsWithRepeatedLetter;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgCorrectSpellingsWithRepeatedLetter = !options.bgCorrectSpellingsWithRepeatedLetter;
      options.bgUseEmoticons = !options.bgUseEmoticons;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgUseEmoticons = !options.bgUseEmoticons;
      options.bgCapitalsBoostTermSentiment = !options.bgCapitalsBoostTermSentiment;
      run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      options.bgCapitalsBoostTermSentiment = !options.bgCapitalsBoostTermSentiment;
      if (iMinImprovement > 1) {
        run10FoldCrossValidationMultipleTimes(iMinImprovement - 1, bUseTotalDifference,
            iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      }
      run10FoldCrossValidationMultipleTimes(iMinImprovement + 1, bUseTotalDifference, iReplications,
          iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
      wResultsWriter.close();
      wTermStrengthWriter.close();
      summariseMultiple10FoldValidations(sOutFileName, sOutFileName + "_sum.txt");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Runs 10-fold cross-validation once.
   *
   * @param iMinImprovement     the minimum improvement in accuracy required to stop the optimisation process
   * @param bUseTotalDifference whether to use the total difference instead of the difference in proportions in the optimisation process
   * @param iMultiOptimisations the number of times to perform optimisation on the dictionary weightings
   * @param wWriter             the BufferedWriter object for writing the results to a file
   * @param wTermStrengthWriter the BufferedWriter object for writing the term strengths to a file
   */
  private void run10FoldCrossValidationOnce(int iMinImprovement, boolean bUseTotalDifference,
                                            int iMultiOptimisations, BufferedWriter wWriter,
                                            BufferedWriter wTermStrengthWriter) {
    int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
    int[] iParagraphRand = new int[igParagraphCount + 1];
    int[] iPosClassAll = new int[igParagraphCount + 1];
    int[] iNegClassAll = new int[igParagraphCount + 1];
    int[] iTrinaryOrScaleClassAll = new int[igParagraphCount + 1];
    int iTotalClassified = 0;
    Sort.makeRandomOrderList(iParagraphRand);
    int[] iOriginalSentimentStrengths = new int[iTotalSentimentWords + 1];
    for (int i = 1; i < iTotalSentimentWords; i++) {
      iOriginalSentimentStrengths[i] = resources.sentimentWords.getSentiment(i);
    }

    for (int iFold = 1; iFold <= 10; iFold++) {
      selectDecileAsSubcorpus(iParagraphRand, iFold, true);
      reCalculateCorpusSentimentScores();
      optimiseDictionaryWeightingsForCorpusMultipleTimes(iMinImprovement, bUseTotalDifference,
          iMultiOptimisations);
      options.printClassificationOptions(wTermStrengthWriter, iMinImprovement, bUseTotalDifference,
          iMultiOptimisations);
      resources.sentimentWords.printSentimentValuesInSingleRow(wTermStrengthWriter);
      selectDecileAsSubcorpus(iParagraphRand, iFold, false);
      reCalculateCorpusSentimentScores();
      for (int i = 1; i <= igParagraphCount; i++) {
        if (bgSupcorpusMember[i]) {
          iPosClassAll[i] = igPosClass[i];
          iNegClassAll[i] = igNegClass[i];
          if (options.bgTrinaryMode) {
            iTrinaryOrScaleClassAll[i] = igTrinaryClass[i];
          } else {
            iTrinaryOrScaleClassAll[i] = igScaleClass[i];
          }
        }
      }

      iTotalClassified += igSupcorpusMemberCount;
      for (int i = 1; i < iTotalSentimentWords; i++) {
        resources.sentimentWords.setSentiment(i, iOriginalSentimentStrengths[i]);
      }

    }

    useWholeCorpusNotSubcorpus();
    options.printClassificationOptions(wWriter, iMinImprovement, bUseTotalDifference,
        iMultiOptimisations);
    printClassificationResultsRow(iPosClassAll, iNegClassAll, iTrinaryOrScaleClassAll, wWriter);
  }

  /**
   * Prints a row of classification results to the specified output file.
   *
   * @param iPosClassAll            an array of integers representing the correct positive classification for each paragraph
   * @param iNegClassAll            an array of integers representing the correct negative classification for each paragraph
   * @param iTrinaryOrScaleClassAll an array of integers representing the correct trinary or scale classification for each paragraph
   * @param wWriter                 a BufferedWriter object used to write the results to the output file
   * @return true if the row was successfully printed, false otherwise
   */
  protected boolean printClassificationResultsRow(int[] iPosClassAll, int[] iNegClassAll,
                                                  int[] iTrinaryOrScaleClassAll,
                                                  BufferedWriter wWriter) {
    int iPosCorrect = -1;
    int iNegCorrect = -1;
    int iPosWithin1 = -1;
    int iNegWithin1 = -1;
    int iTrinaryCorrect = -1;
    int iTrinaryCorrectWithin1 = -1;
    double fPosCorrectProportion = -1D;
    double fNegCorrectProportion = -1D;
    double fPosWithin1Proportion = -1D;
    double fNegWithin1Proportion = -1D;
    double fTrinaryCorrectProportion = -1D;
    double fTrinaryCorrectWithin1Proportion = -1D;
    double fPosOrScaleCorr = 9999D;
    double fNegCorr = 9999D;
    double fPosMPE = 9999D;
    double fNegMPE = 9999D;
    double fPosMPEnoDiv = 9999D;
    double fNegMPEnoDiv = 9999D;
    int[][] estCorr = {new int[3], new int[3], new int[3]};
    try {
      iPosCorrect =
          ClassificationStatistics.accuracy(igPosCorrect, iPosClassAll, igParagraphCount, false);
      iNegCorrect =
          ClassificationStatistics.accuracy(igNegCorrect, iNegClassAll, igParagraphCount, true);
      iPosWithin1 =
          ClassificationStatistics.accuracyWithin1(igPosCorrect, iPosClassAll, igParagraphCount,
              false);
      iNegWithin1 =
          ClassificationStatistics.accuracyWithin1(igNegCorrect, iNegClassAll, igParagraphCount,
              true);
      fPosOrScaleCorr =
          ClassificationStatistics.correlationAbs(igPosCorrect, iPosClassAll, igParagraphCount);
      fNegCorr =
          ClassificationStatistics.correlationAbs(igNegCorrect, iNegClassAll, igParagraphCount);
      fPosMPE = ClassificationStatistics.absoluteMeanPercentageError(igPosCorrect, iPosClassAll,
          igParagraphCount, false);
      fNegMPE = ClassificationStatistics.absoluteMeanPercentageError(igNegCorrect, iNegClassAll,
          igParagraphCount, true);
      fPosMPEnoDiv =
          ClassificationStatistics.absoluteMeanPercentageErrorNoDivision(igPosCorrect, iPosClassAll,
              igParagraphCount, false);
      fNegMPEnoDiv =
          ClassificationStatistics.absoluteMeanPercentageErrorNoDivision(igNegCorrect, iNegClassAll,
              igParagraphCount, true);
      fPosCorrectProportion = (float) iPosCorrect / (float) igParagraphCount;
      fNegCorrectProportion = (float) iNegCorrect / (float) igParagraphCount;
      fPosWithin1Proportion = (float) iPosWithin1 / (float) igParagraphCount;
      fNegWithin1Proportion = (float) iNegWithin1 / (float) igParagraphCount;
      writeClassificationResult(wWriter, iPosCorrect, iNegCorrect, iPosWithin1, iNegWithin1,
          iTrinaryCorrect, iTrinaryCorrectWithin1, fPosCorrectProportion, fNegCorrectProportion,
          fPosWithin1Proportion, fNegWithin1Proportion, fTrinaryCorrectProportion,
          fTrinaryCorrectWithin1Proportion, fPosOrScaleCorr, fNegCorr, fPosMPE, fNegMPE,
          fPosMPEnoDiv, fNegMPEnoDiv, estCorr);
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  protected void writeClassificationResult(BufferedWriter wWriter, int iPosCorrect, int iNegCorrect,
                                           int iPosWithin1, int iNegWithin1, int iTrinaryCorrect,
                                           int iTrinaryCorrectWithin1, double fPosCorrectProportion,
                                           double fNegCorrectProportion,
                                           double fPosWithin1Proportion,
                                           double fNegWithin1Proportion,
                                           double fTrinaryCorrectProportion,
                                           double fTrinaryCorrectWithin1Proportion,
                                           double fPosOrScaleCorr, double fNegCorr, double fPosMPE,
                                           double fNegMPE, double fPosMPEnoDiv, double fNegMPEnoDiv,
                                           int[][] estCorr) throws IOException {
    wWriter.write("\t" + iPosCorrect + "\t" + fPosCorrectProportion + "\t" + iNegCorrect + "\t" +
        fNegCorrectProportion + "\t" + iPosWithin1 + "\t" + fPosWithin1Proportion + "\t" +
        iNegWithin1 + "\t" + fNegWithin1Proportion + "\t" + fPosOrScaleCorr + "\t" + fNegCorr +
        "\t" + fPosMPE + "\t" + fNegMPE + "\t" + fPosMPEnoDiv + "\t" + fNegMPEnoDiv + "\t" +
        iTrinaryCorrect + "\t" + fTrinaryCorrectProportion + "\t" + iTrinaryCorrectWithin1 + "\t" +
        fTrinaryCorrectWithin1Proportion + "\t" + estCorr[0][0] + "\t" + estCorr[0][1] + "\t" +
        estCorr[0][2] + "\t" + estCorr[1][0] + "\t" + estCorr[1][1] + "\t" + estCorr[1][2] + "\t" +
        estCorr[2][0] + "\t" + estCorr[2][1] + "\t" + estCorr[2][2] + "\t" + igParagraphCount +
        "\n");
  }

  /**
   * Selects a decile of paragraphs from the corpus based on the given decile and optionally inverts the selection.
   *
   * @param iParagraphRand an array of randomly generated integers representing the paragraphs in the corpus.
   * @param iDecile        the decile of paragraphs to select (1-10).
   * @param bInvert        a boolean value indicating whether or not to invert the selection.
   */
  private void selectDecileAsSubcorpus(int[] iParagraphRand, int iDecile, boolean bInvert) {
    if (igParagraphCount == 0) {
      return;
    }
    int iMin = (int) ((igParagraphCount / 10F) * (iDecile - 1)) + 1;
    int iMax = (int) ((igParagraphCount / 10F) * iDecile);
    if (iDecile == 10) {
      iMax = igParagraphCount;
    }
    if (iDecile == 0) {
      iMin = 0;
    }
    igSupcorpusMemberCount = 0;
    for (int i = 1; i <= igParagraphCount; i++) {
      if (i >= iMin && i <= iMax) {
        bgSupcorpusMember[iParagraphRand[i]] = !bInvert;
        if (!bInvert) {
          igSupcorpusMemberCount++;
        }
      } else {
        bgSupcorpusMember[iParagraphRand[i]] = bInvert;
        if (bInvert) {
          igSupcorpusMemberCount++;
        }
      }
    }

  }

  /**
   * Optimizes the dictionary weightings for the corpus multiple times by running the
   * {@link #optimiseDictionaryWeightingsForCorpus(int, boolean)} method repeatedly.
   *
   * @param iMinImprovement     the minimum improvement required for the optimization to continue
   * @param bUseTotalDifference if true, the total difference in score is used to determine improvement,
   *                            otherwise the average difference is used
   * @param iOptimisationTotal  the number of times to run the optimization
   */
  public void optimiseDictionaryWeightingsForCorpusMultipleTimes(int iMinImprovement,
                                                                 boolean bUseTotalDifference,
                                                                 int iOptimisationTotal) {
    if (iOptimisationTotal < 1) {
      return;
    }
    if (iOptimisationTotal == 1) {
      optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
      return;
    }
    int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
    int[] iOriginalSentimentStrengths = new int[iTotalSentimentWords + 1];
    for (int j = 1; j <= iTotalSentimentWords; j++) {
      iOriginalSentimentStrengths[j] = resources.sentimentWords.getSentiment(j);
    }

    int[] iTotalWeight = new int[iTotalSentimentWords + 1];
    for (int j = 1; j <= iTotalSentimentWords; j++) {
      iTotalWeight[j] = 0;
    }

    for (int i = 0; i < iOptimisationTotal; i++) {
      optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
      for (int j = 1; j <= iTotalSentimentWords; j++) {
        iTotalWeight[j] += resources.sentimentWords.getSentiment(j);
      }

      for (int j = 1; j <= iTotalSentimentWords; j++) {
        resources.sentimentWords.setSentiment(j, iOriginalSentimentStrengths[j]);
      }

    }

    for (int j = 1; j <= iTotalSentimentWords; j++) {
      resources.sentimentWords.setSentiment(j,
          (int) (((float) iTotalWeight[j] / (float) iOptimisationTotal) + 0.5D));
    }

    optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
  }

  /**
   * Optimizes dictionary weightings for the entire corpus based on the specified parameters.
   *
   * @param iMinImprovement     The minimum improvement required to continue optimizing.
   * @param bUseTotalDifference Whether to use the total difference or not.
   */
  public void optimiseDictionaryWeightingsForCorpus(int iMinImprovement,
                                                    boolean bUseTotalDifference) {
    if (options.bgTrinaryMode) {
      optimiseDictionaryWeightingsForCorpusTrinaryOrBinary(iMinImprovement);
    } else if (options.bgScaleMode) {
      optimiseDictionaryWeightingsForCorpusScale(iMinImprovement);
    } else {
      optimiseDictionaryWeightingsForCorpusPosNeg(iMinImprovement, bUseTotalDifference);
    }
  }

  /**
   * Optimizes the weighting of sentiment words in the dictionary for a corpus using a scale-based approach.
   * The method modifies the sentiment strengths of the words in the dictionary and reclassifies the corpus to evaluate the improvement.
   * If the improvement is greater than or equal to the specified minimum improvement, the change is kept; otherwise, it is reverted.
   * The method continues modifying the dictionary until no more changes result in improvement.
   *
   * @param iMinImprovement The minimum improvement required to continue optimizing.
   */
  public void optimiseDictionaryWeightingsForCorpusScale(int iMinImprovement) {
    boolean bFullListChanges = true;
    int iLastScaleNumberCorrect = getClassificationScaleNumberCorrect();
    int iNewScaleNumberCorrect;
    int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
    int[] iWordRand = new int[iTotalSentimentWords + 1];
    while (bFullListChanges) {
      Sort.makeRandomOrderList(iWordRand);
      bFullListChanges = false;
      for (int i = 1; i <= iTotalSentimentWords; i++) {
        int iOldTermSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
        boolean bCurrentIDChange = false;
        int iAddOneImprovement;
        int iSubtractOneImprovement;
        if (iOldTermSentimentStrength < 4) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength + 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          iNewScaleNumberCorrect = getClassificationScaleNumberCorrect();
          iAddOneImprovement = iNewScaleNumberCorrect - iLastScaleNumberCorrect;
          if (iAddOneImprovement >= iMinImprovement) {
            bCurrentIDChange = true;
            iLastScaleNumberCorrect += iAddOneImprovement;
          }
        }
        if (iOldTermSentimentStrength > -4 && !bCurrentIDChange) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength - 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          iNewScaleNumberCorrect = getClassificationScaleNumberCorrect();
          iSubtractOneImprovement = iNewScaleNumberCorrect - iLastScaleNumberCorrect;
          if (iSubtractOneImprovement >= iMinImprovement) {
            bCurrentIDChange = true;
            iLastScaleNumberCorrect += iSubtractOneImprovement;
          }
        }
        if (bCurrentIDChange) {
          bFullListChanges = true;
        } else {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
        }
      }

    }
  }

  /**
   * Optimizes the weightings of sentiment words in the sentiment dictionary for binary or trinary classification.
   * The optimization process involves randomly selecting a sentiment word from the dictionary and modifying its sentiment score.
   * If the modified score results in an improvement in the classification accuracy, the score is updated, and the process continues until no further improvements are made.
   *
   * @param iMinImprovement The minimum improvement required to continue optimizing.
   */
  public void optimiseDictionaryWeightingsForCorpusTrinaryOrBinary(int iMinImprovement) {
    boolean bFullListChanges = true;
    int iLastTrinaryCorrect = getClassificationTrinaryNumberCorrect();
    int iNewTrinary;
    int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
    int[] iWordRand = new int[iTotalSentimentWords + 1];
    while (bFullListChanges) {
      Sort.makeRandomOrderList(iWordRand);
      bFullListChanges = false;
      for (int i = 1; i <= iTotalSentimentWords; i++) {
        int iOldSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
        boolean bCurrentIDChange = false;
        int iAddOneImprovement;
        int iSubtractOneImprovement;
        if (iOldSentimentStrength < 4) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength + 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          iNewTrinary = getClassificationTrinaryNumberCorrect();
          iAddOneImprovement = iNewTrinary - iLastTrinaryCorrect;
          if (iAddOneImprovement >= iMinImprovement) {
            bCurrentIDChange = true;
            iLastTrinaryCorrect += iAddOneImprovement;
          }
        }
        if (iOldSentimentStrength > -4 && !bCurrentIDChange) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength - 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          iNewTrinary = getClassificationTrinaryNumberCorrect();
          iSubtractOneImprovement = iNewTrinary - iLastTrinaryCorrect;
          if (iSubtractOneImprovement >= iMinImprovement) {
            bCurrentIDChange = true;
            iLastTrinaryCorrect += iSubtractOneImprovement;
          }
        }
        if (bCurrentIDChange) {
          bFullListChanges = true;
        } else {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
        }
      }

    }
  }

  /**
   * Optimizes the weightings of sentiment words in the dictionary for a corpus of positive and negative texts.
   *
   * @param iMinImprovement     The minimum improvement required to continue optimizing.
   * @param bUseTotalDifference a flag to determine whether to use total difference or number of correct classifications to measure improvement.
   */
  public void optimiseDictionaryWeightingsForCorpusPosNeg(int iMinImprovement,
                                                          boolean bUseTotalDifference) {
    boolean bFullListChanges = true;
    int iLastPos = 0;
    int iLastNeg = 0;
    int iLastPosTotalDiff = 0;
    int iLastNegTotalDiff = 0;
    if (bUseTotalDifference) {
      iLastPosTotalDiff = getClassificationPositiveTotalDifference();
      iLastNegTotalDiff = getClassificationNegativeTotalDifference();
    } else {
      iLastPos = getClassificationPositiveNumberCorrect();
      iLastNeg = getClassificationNegativeNumberCorrect();
    }
    int iNewPos = 0;
    int iNewNeg = 0;
    int iNewPosTotalDiff = 0;
    int iNewNegTotalDiff = 0;
    int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
    int[] iWordRand = new int[iTotalSentimentWords + 1];
    while (bFullListChanges) {
      Sort.makeRandomOrderList(iWordRand);
      bFullListChanges = false;
      for (int i = 1; i <= iTotalSentimentWords; i++) {
        int iOldSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
        boolean bCurrentIDChange = false;
        if (iOldSentimentStrength < 4) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength + 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          if (bUseTotalDifference) {
            iNewPosTotalDiff = getClassificationPositiveTotalDifference();
            iNewNegTotalDiff = getClassificationNegativeTotalDifference();
            if (((iNewPosTotalDiff - iLastPosTotalDiff) + iNewNegTotalDiff) - iLastNegTotalDiff <=
                -iMinImprovement) {
              bCurrentIDChange = true;
            }
          } else {
            iNewPos = getClassificationPositiveNumberCorrect();
            iNewNeg = getClassificationNegativeNumberCorrect();
            if (((iNewPos - iLastPos) + iNewNeg) - iLastNeg >= iMinImprovement) {
              bCurrentIDChange = true;
            }
          }
        }
        if (iOldSentimentStrength > -4 && !bCurrentIDChange) {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength - 1);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
          if (bUseTotalDifference) {
            iNewPosTotalDiff = getClassificationPositiveTotalDifference();
            iNewNegTotalDiff = getClassificationNegativeTotalDifference();
            if (((iNewPosTotalDiff - iLastPosTotalDiff) + iNewNegTotalDiff) - iLastNegTotalDiff <=
                -iMinImprovement) {
              bCurrentIDChange = true;
            }
          } else {
            iNewPos = getClassificationPositiveNumberCorrect();
            iNewNeg = getClassificationNegativeNumberCorrect();
            if (((iNewPos - iLastPos) + iNewNeg) - iLastNeg >= iMinImprovement) {
              bCurrentIDChange = true;
            }
          }
        }
        if (bCurrentIDChange) {
          if (bUseTotalDifference) {
            iLastNegTotalDiff = iNewNegTotalDiff;
            iLastPosTotalDiff = iNewPosTotalDiff;
          } else {
            iLastNeg = iNewNeg;
            iLastPos = iNewPos;
          }
          bFullListChanges = true;
        } else {
          resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength);
          reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
        }
      }

    }
  }

  /**
   * Summarizes the results of multiple 10-fold validations from an input file and writes them to an output file.
   * The input file is assumed to be tab-separated and have 28 rows of data and 24 option columns.
   * The output file will have the same format as the input file, with the addition of a "Number" column that indicates the number of rows used to calculate each set of averages.
   *
   * @param sInputFile  The path of the input file to read.
   * @param sOutputFile The path of the output file to write.
   */
  public void summariseMultiple10FoldValidations(String sInputFile, String sOutputFile) {
    int iDataRows = 28;
    int iLastOptionCol = 24;
    BufferedReader rResults;
    BufferedWriter wSummary;
    String sLine = null;
    String[] sPrevData = null;
    String[] sData = null;
    float[] total = new float[iDataRows];
    int iRows = 0;
    int i = 0;
    try {
      rResults = new BufferedReader(new FileReader(sInputFile));
      wSummary = new BufferedWriter(new FileWriter(sOutputFile));
      sLine = rResults.readLine();
      wSummary.write(sLine + "\tNumber\n");
      while (rResults.ready()) {
        sLine = rResults.readLine();
        sData = sLine.split("\t");
        boolean bMatching = true;
        if (sPrevData != null) {
          for (i = 0; i < iLastOptionCol; i++) {
            if (!sData[i].equals(sPrevData[i])) {
              bMatching = false;
            }
          }
        }

        if (!bMatching) {
          for (i = 0; i < iLastOptionCol; i++) {
            wSummary.write(sPrevData[i] + "\t");
          }

          for (i = 0; i < iDataRows; i++) {
            wSummary.write(total[i] / iRows + "\t");
          }

          wSummary.write(iRows + "\n");
          for (i = 0; i < iDataRows; i++) {
            total[i] = 0.0F;
          }

          iRows = 0;
        }
        for (i = iLastOptionCol; i < iLastOptionCol + iDataRows; i++) {
          try {
            total[i - iLastOptionCol] += Float.parseFloat(sData[i]);
          } catch (Exception e) {
            total[i - iLastOptionCol] += 9999999F;
          }
        }

        iRows++;
        sPrevData = sLine.split("\t");
      }
      for (i = 0; i < iLastOptionCol; i++) {
        wSummary.write(sPrevData[i] + "\t");
      }

      for (i = 0; i < iDataRows; i++) {
        wSummary.write(total[i] / iRows + "\t");
      }

      wSummary.write(iRows + "\n");
      wSummary.close();
      rResults.close();
    } catch (IOException e) {
      System.out.println("SummariseMultiple10FoldValidations: File I/O error: " + sInputFile);
      e.printStackTrace();
    } catch (Exception e) {
      System.out.println("SummariseMultiple10FoldValidations: Error at line: " + sLine);
      System.out.println("Value of i: " + i);
      e.printStackTrace();
    }
  }

  private int calculateCorrect(int[] igPosCorrect, int[] igPosClass) {
    if (igParagraphCount == 0) {
      return 0;
    }
    int iMatches = 0;
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i] && igPosCorrect[i] == igPosClass[i]) {
        iMatches++;
      }
    }
    return iMatches;
  }
}
