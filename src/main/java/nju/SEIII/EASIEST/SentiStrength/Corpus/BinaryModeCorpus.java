package nju.SEIII.EASIEST.SentiStrength.Corpus;

import nju.SEIII.EASIEST.SentiStrength.*;
import nju.SEIII.EASIEST.Utilities.Sort;

import java.io.*;
import java.util.Objects;

/**
 * @title: BinaryModeCorpus
 * @Author: Stanton JoY
 * @Date: 2023/4/18 20:49
 */
public class BinaryModeCorpus extends BaseCorpus {
    public BinaryModeCorpus() {
        super();
    }
    public BinaryModeCorpus(ClassificationOptions options, ClassificationResources resources){
        this.options=options;
        this.resources=resources;
    }

    @Override
    public void indexClassifiedCorpus() {
        unusedTermsClassificationIndex = new UnusedTermsClassificationIndex();
        unusedTermsClassificationIndex.initialise(false, false, true, false);
        for (int i = 1; i <= igParagraphCount; i++) {
            paragraph[i].addParagraphToIndexWithBinaryValues(unusedTermsClassificationIndex,
                    igTrinaryCorrect[i], igTrinaryClass[i]);
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
        unusedTermsClassificationIndex.printIndexWithBinaryValues(saveFile, iMinFreq);
        System.out.println("Term weights saved to " + saveFile);
    }

    @Override
    protected void processorForSetCorpus(String sLine) {
        paragraph[++igParagraphCount] = new Paragraph();
        int iLastTabPos = sLine.lastIndexOf("\t");
        int iFirstTabPos = sLine.indexOf("\t");
        if (iFirstTabPos < iLastTabPos ||
                iFirstTabPos > 0) {
            paragraph[igParagraphCount].setParagraph(sLine.substring(iLastTabPos + 1), resources,
                    options);
            try {
                igTrinaryCorrect[igParagraphCount] =
                        Integer.parseInt(sLine.substring(0, iFirstTabPos).trim());
            } catch (Exception e) {
                System.out.println(
                        "Trinary classification could not be read and will be ignored!: " + sLine);
                igTrinaryCorrect[igParagraphCount] = 999;
            }
            if (igTrinaryCorrect[igParagraphCount] > 1 ||
                    igTrinaryCorrect[igParagraphCount] < -1) {
                System.out.println(
                        "Trinary classification out of bounds and will be ignored!: " + sLine);
                igParagraphCount--;
            } else if (igTrinaryCorrect[igParagraphCount] == 0) {
                System.out.println("Warning, unexpected 0 in binary classification!: " + sLine);
            }
        } else {
            if (iFirstTabPos >= 0) {
                igTrinaryCorrect[igParagraphCount] =
                        Integer.parseInt(sLine.substring(0, iFirstTabPos).trim());
                sLine = sLine.substring(iFirstTabPos + 1);
            } else {
                igTrinaryCorrect[igParagraphCount] = 0;
            }
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
            igTrinaryClass = new int[igParagraphCount + 1];
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
                igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
                igTrinaryClass[i] = paragraph[i].getParagraphTrinarySentiment();
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
        igTrinaryClass = new int[igParagraphCount + 1];
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                paragraph[i].reClassifyClassifiedParagraphForSentimentChange(iSentimentWordID);
                igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
                igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
                igTrinaryClass[i] = paragraph[i].getParagraphTrinarySentiment();
            }
        }
        bgCorpusClassified = true;
    }

    @Override
    protected void processorForClassifyAllLinesAndRecordWithID(String sLine, int iTextCol, int iIDCol, BufferedWriter wWriter) throws IOException {
        int iTrinary = -3;
        String[] sData = sLine.split("\t");
        if (sData.length > iTextCol && sData.length > iIDCol) {
            Paragraph paragraph = new Paragraph();
            paragraph.setParagraph(sData[iTextCol], resources, options);
            iTrinary = paragraph.getParagraphTrinarySentiment();
            wWriter.write(sData[iIDCol] + "\t" + iTrinary + "\n");
        }
    }

    @Override
    protected void processorForAnnotateAllLinesInInputFile(String sLine, int iTextCol, BufferedWriter wWriter) throws IOException {
        String[] sData = sLine.split("\t");
        int iTrinary = -3;
        if (sData.length > iTextCol) {
            Paragraph paragraph = new Paragraph();
            paragraph.setParagraph(sData[iTextCol], resources, options);
            iTrinary = paragraph.getParagraphTrinarySentiment();
            wWriter.write(sLine + "\t" + iTrinary + "\n");
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
        int[][] estCorr = {
                new int[3], new int[3], new int[3]
        };
        try {
            iTrinaryCorrect =
                    ClassificationStatistics.accuracy(igTrinaryCorrect, iTrinaryOrScaleClassAll,
                            igParagraphCount, false);
            iTrinaryCorrectWithin1 =
                    ClassificationStatistics.accuracyWithin1(igTrinaryCorrect, iTrinaryOrScaleClassAll,
                            igParagraphCount, false);
            fTrinaryCorrectProportion = (float) iTrinaryCorrect / (float) igParagraphCount;
            fTrinaryCorrectWithin1Proportion = (float) iTrinaryCorrectWithin1 / (float) igParagraphCount;
            ClassificationStatistics.trinaryOrBinaryConfusionTable(iTrinaryOrScaleClassAll,
                    igTrinaryCorrect, igParagraphCount, estCorr);
            writeClassificationResult(wWriter, iPosCorrect, iNegCorrect, iPosWithin1, iNegWithin1, iTrinaryCorrect, iTrinaryCorrectWithin1, fPosCorrectProportion, fNegCorrectProportion, fPosWithin1Proportion, fNegWithin1Proportion, fTrinaryCorrectProportion, fTrinaryCorrectWithin1Proportion, fPosOrScaleCorr, fNegCorr, fPosMPE, fNegMPE, fPosMPEnoDiv, fNegMPEnoDiv, estCorr);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
