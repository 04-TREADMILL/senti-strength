package nju.SEIII.EASIEST.SentiStrength.Corpus;

import java.io.BufferedWriter;
import java.io.IOException;
import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;
import nju.SEIII.EASIEST.SentiStrength.ClassificationResources;
import nju.SEIII.EASIEST.SentiStrength.ClassificationStatistics;
import nju.SEIII.EASIEST.SentiStrength.Paragraph;
import nju.SEIII.EASIEST.Utilities.Sort;

/**
 * @title: Corpus_Scale
 * @Author: Stanton JoY
 * @Date: 2023/4/18 20:46
 */
public class ScaleModeCorpus extends BaseCorpus {
  public ScaleModeCorpus() {
    super();
  }

  public ScaleModeCorpus(ClassificationOptions options, ClassificationResources resources) {
    this.options = options;
    this.resources = resources;
  }

  @Override
  public void indexClassifiedCorpus() {
    unusedTermsClassificationIndex.initialise(true, false, false, false);
    for (int i = 1; i <= igParagraphCount; i++) {
      paragraph[i].addParagraphToIndexWithScaleValues(unusedTermsClassificationIndex,
          igScaleCorrect[i], igScaleClass[i]);
    }
  }

  @Override
  public void printCorpusUnusedTermsClassificationIndex(String saveFile, int iMinFreq) {
    if (!bgCorpusClassified) {
      calculateCorpusSentimentScores();
    }
    if (unusedTermsClassificationIndex == null) {
      indexClassifiedCorpus();
    }
    unusedTermsClassificationIndex.printIndexWithScaleValues(saveFile, iMinFreq);
    System.out.println("Term weights saved to " + saveFile);
  }

  @Override
  public void processorForSetCorpus(String sLine) {
    paragraph[++igParagraphCount] = new Paragraph();
    int iLastTabPos = sLine.lastIndexOf("\t");
    int iFirstTabPos = sLine.indexOf("\t");
    if (iFirstTabPos < iLastTabPos ||
        iFirstTabPos > 0) {
      paragraph[igParagraphCount].setParagraph(sLine.substring(iLastTabPos + 1), resources,
          options);
      try {
        igScaleCorrect[igParagraphCount] =
            Integer.parseInt(sLine.substring(0, iFirstTabPos).trim());
      } catch (Exception e) {
        System.out.println(
            "Scale classification could not be read and will be ignored!: " + sLine);
        igScaleCorrect[igParagraphCount] = 999;
      }
      if (igScaleCorrect[igParagraphCount] > 4 || igTrinaryCorrect[igParagraphCount] < -4) {
        System.out.println(
            "Scale classification out of bounds (-4 to +4) and will be ignored!: " + sLine);
        igParagraphCount--;
      }
    } else {
      if (iFirstTabPos >= 0) {
        sLine = sLine.substring(iFirstTabPos + 1);
      }
      igTrinaryCorrect[igParagraphCount] = 0;
      paragraph[igParagraphCount].setParagraph(sLine, resources, options);
      igPosCorrect[igParagraphCount] = 0;
      igNegCorrect[igParagraphCount] = 0;
    }
  }

  @Override
  public void calculateCorpusSentimentScores() {
    if (igParagraphCount == 0) {
      return;
    }
    if (igPosClass == null || igPosClass.length < igPosCorrect.length) {
      igPosClass = new int[igParagraphCount + 1];
      igNegClass = new int[igParagraphCount + 1];
      igScaleClass = new int[igParagraphCount + 1];
    }
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
        igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
        igScaleClass[i] = paragraph[i].getParagraphScaleSentiment();
      }
    }
    bgCorpusClassified = true;
  }

  @Override
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
    for (int i = 1; i <= igParagraphCount; i++) {
      if (bgSupcorpusMember[i]) {
        paragraph[i].reClassifyClassifiedParagraphForSentimentChange(iSentimentWordID);
        igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
        igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
        igScaleClass[i] = paragraph[i].getParagraphScaleSentiment();
      }
    }
    bgCorpusClassified = true;
  }

  @Override
  protected void processorForClassifyAllLinesAndRecordWithID(String sLine, int iTextCol, int iIDCol,
                                                             BufferedWriter wWriter)
      throws IOException {
    int iScale = -10;
    String[] sData = sLine.split("\t");
    if (sData.length > iTextCol && sData.length > iIDCol) {
      Paragraph paragraph = new Paragraph();
      paragraph.setParagraph(sData[iTextCol], resources, options);
      iScale = paragraph.getParagraphScaleSentiment();
      wWriter.write(sData[iIDCol] + "\t" + iScale + "\n");
    }
  }

  @Override
  protected void processorForAnnotateAllLinesInInputFile(String sLine, int iTextCol,
                                                         BufferedWriter wWriter)
      throws IOException {
    int iScale = -10;
    String[] sData = sLine.split("\t");
    if (sData.length > iTextCol) {
      Paragraph paragraph = new Paragraph();
      paragraph.setParagraph(sData[iTextCol], resources, options);
      iScale = paragraph.getParagraphScaleSentiment();
      wWriter.write(sLine + "\t" + iScale + "\n");
    } else {
      wWriter.write(sLine + "\n");
    }
  }

  @Override
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
      iTrinaryCorrect = ClassificationStatistics.accuracy(igScaleCorrect, iTrinaryOrScaleClassAll,
          igParagraphCount, false);
      iTrinaryCorrectWithin1 =
          ClassificationStatistics.accuracyWithin1(igScaleCorrect, iTrinaryOrScaleClassAll,
              igParagraphCount, false);
      fTrinaryCorrectProportion = (float) iTrinaryCorrect / (float) igParagraphCount;
      fTrinaryCorrectWithin1Proportion = (float) iTrinaryCorrectWithin1 / (float) igParagraphCount;
      fPosOrScaleCorr =
          ClassificationStatistics.correlation(igScaleCorrect, iTrinaryOrScaleClassAll,
              igParagraphCount);
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
}
