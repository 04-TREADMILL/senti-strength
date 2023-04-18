package nju.SEIII.EASIEST.SentiStrength;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import nju.SEIII.EASIEST.SentiStrength.Corpus.BaseCorpus;
import nju.SEIII.EASIEST.SentiStrength.Corpus.BinaryModeCorpus;
import nju.SEIII.EASIEST.SentiStrength.Corpus.ScaleModeCorpus;
import nju.SEIII.EASIEST.SentiStrength.Corpus.TrinaryModeCorpus;
import nju.SEIII.EASIEST.Utilities.FileOps;
import nju.SEIII.EASIEST.Utilities.VO.OutputVO;

/**
 * The SentiStrength entry class, which provides a number of command-line arguments to drive SentiStrength.
 *
 * @UC <p><ul>
 * <li>UC-1 Assigning Sentiment Scores for Words</li>
 * <li>UC-2 Assigning Sentiment Scores for Phrases</li>
 * <li>UC-3 Spelling Correction</li>
 * <li>UC-4 Booster Word Rule</li>
 * <li>UC-5 Negating Word Rule</li>
 * <li>UC-11 Classify a single text</li>
 * <li>UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]</li>
 * <li>UC-13 Classify texts in a column within a file or folder</li>
 * <li>UC-14 Listen at a port for texts to classify</li>
 * <li>UC-15 Run interactively from the command line</li>
 * <li>UC-21 Classify positive (1 to 5) and negative (-1 to -5) sentiment strength separately</li>
 * <li>UC-22 Use trinary classification (positive-negative-neutral)</li>
 * <li>UC-23 Use binary classification (positive-negative)</li>
 * <li>UC-24 Use a single positive-negative scale classification</li>
 * <li>UC-25 Explain the classification</li>
 * <li>UC-26 Set Classification Algorithm Parameters</li>
 * <li>UC-27 Optimise sentiment strengths of existing sentiment terms</li>
 * <li>UC-28 Suggest new sentiment terms (from terms in misclassified texts)</li>
 * <li>UC-29 Machine learning evaluations</li>
 * </ul></p>
 */
public class SentiStrength {
    BaseCorpus c;
    String sInputFile = "";
    String sInputFolder = "";
    String sTextToParse = "";
    String sOptimalTermStrengths = "";
    String sFileSubString = "\t";
    String sResultsFolder = "";
    String sResultsFileExtension = "_out.txt";
    int iIterations = 1;
    int iMinImprovement = 2;
    int iMultiOptimisations = 1;
    int iListenPort = 0;
    int iTextColForAnnotation = -1;
    int iIdCol = -1;
    int iTextCol = -1;
    boolean bDoAll = false;
    boolean bOkToOverwrite = false;
    boolean bTrain = false;
    boolean bReportNewTermWeightsForBadClassifications = false;
    boolean bStdIn = false;
    boolean bCmd = false;
    boolean bWait = false;
    boolean bUseTotalDifference = true;
    boolean bURLEncoded = false;
    String sLanguage = "";

    public SentiStrength() {
        this.c = new BaseCorpus();
    }

    public SentiStrength(String[] args) {
        this.c = new BaseCorpus();
        this.initialiseAndRun(args);
    }

    public static void main(String[] args) {
        SentiStrength classifier = new SentiStrength();
        classifier.initialiseAndRun(args);
    }

    public String[] getInput() {
        return new String[0];
    }

    public void initialiseAndRun(String[] args) {

        boolean[] bArgumentRecognised = recogniseArgs(args);
        if (bArgumentRecognised == null) return;
        this.parseParametersForCorpusOptions(args, bArgumentRecognised);
        if (sLanguage.length() > 1) {
            Locale l = new Locale(sLanguage);
            Locale.setDefault(l);
        }
        for (int i = 0; i < args.length; ++i) {
            if (!bArgumentRecognised[i]) {
                System.out.println("Unrecognised command - wrong spelling or case?: " + args[i]);
                this.showBriefHelp();
                return;
            }
        }

        if (c.initialise()) {
            if (!Objects.equals(sTextToParse, "")) {
                if (bURLEncoded) {
                    try {
                        sTextToParse = URLDecoder.decode(sTextToParse, String.valueOf(StandardCharsets.UTF_8));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    sTextToParse = sTextToParse.replace("+", " ");
                }

                this.parseOneText(c, sTextToParse, bURLEncoded);
                return;
            }
            // 接下来的都是 sTextToParse == ""
            if (iListenPort > 0) {
                this.listenAtPort(c, iListenPort);
                return;
            }
            if (bCmd) {
                this.listenForCmdInput(c);
                return;
            }
            if (bStdIn) {
                this.listenToStdIn(c, iTextCol);
                return;
            }
            if (!bWait) {
                if (!Objects.equals(sOptimalTermStrengths, "")) {
                    // OptimalTermStrengths != ""
                    if (Objects.equals(sInputFile, "")) {
                        System.out.println("Input file must be specified to optimise term weights");
                        return;
                    }

                    if (c.setCorpus(sInputFile)) {
                        c.optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
                        c.resources.sentimentWords.saveSentimentList(sOptimalTermStrengths, c);
                        System.out.println("Saved optimised term weights to " + sOptimalTermStrengths);
                    } else {
                        System.out.println("Error: Too few texts in " + sInputFile);
                    }
                    return;
                }
                if (bReportNewTermWeightsForBadClassifications) {
                    if (c.setCorpus(sInputFile)) {
                        c.printCorpusUnusedTermsClassificationIndex(
                                FileOps.s_ChopFileNameExtension(sInputFile) + "_unusedTerms.txt", 1);
                    } else {
                        System.out.println("Error: Too few texts in " + sInputFile);
                    }
                    return;
                }
                if (iTextCol > 0 && iIdCol > 0) {
                    this.classifyAndSaveWithID(c, sInputFile, sInputFolder, iTextCol, iIdCol);
                    return;
                }
                if (iTextColForAnnotation > 0) {
                    this.annotationTextCol(c, sInputFile, sInputFolder, sFileSubString, iTextColForAnnotation,
                            bOkToOverwrite);
                    return;
                }
                if (!Objects.equals(sInputFolder, "")) {
                    System.out.println(
                            "Input folder specified but textCol and IDcol or annotateCol needed");
                }

                if (Objects.equals(sInputFile, "")) {
                    System.out.println("No action taken because no input file nor text specified");
                    this.showBriefHelp();
                    return;
                }
                //获取返回路径
                String sOutputFile =
                        FileOps.getNextAvailableFilename(FileOps.s_ChopFileNameExtension(sInputFile),
                                sResultsFileExtension);
                if (sResultsFolder.length() > 0) {
                    sOutputFile = sResultsFolder + (new File(sOutputFile)).getName();
                }

                if (bTrain) {
                    this.runMachineLearning(c, sInputFile, bDoAll, iMinImprovement, bUseTotalDifference,
                            iIterations, iMultiOptimisations, sOutputFile);
                } else {
                    --iTextCol;
                    c.classifyAllLinesInInputFile(sInputFile, iTextCol, sOutputFile);
                }

                System.out.println("Finished! Results in: " + sOutputFile);
                return;
            }
        }
        //fail
        System.out.println("Failed to initialise!");

        try {
            File f = new File(c.resources.sgSentiStrengthFolder);
            if (!f.exists()) {
                System.out.println("Folder does not exist! " + c.resources.sgSentiStrengthFolder);
            }
        } catch (Exception var30) {
            System.out.println("Folder doesn't exist! " + c.resources.sgSentiStrengthFolder);
        }

        this.showBriefHelp();

    }

    private void parseParametersForCorpusOptions(String[] args, boolean[] bArgumentRecognised) {
        for (int i = 0; i < args.length; ++i) {
            try {
                if (args[i].equalsIgnoreCase("sentidata")) {
                    this.c.resources.sgSentiStrengthFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("emotionlookuptable")) {
                    this.c.resources.sgSentimentWordsFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("additionalfile")) {
                    this.c.resources.sgAdditionalFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("keywords")) {
                    this.c.options.parseKeywordList(args[i + 1].toLowerCase());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("wordsBeforeKeywords")) {
                    this.c.options.igWordsToIncludeBeforeKeyword = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("wordsAfterKeywords")) {
                    this.c.options.igWordsToIncludeAfterKeyword = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("sentiment")) {
                    this.c.options.nameProgram(false);
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("stress")) {
                    this.c.options.nameProgram(true);
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("trinary")) {
                    this.c.options.bgTrinaryMode = true;
                    bArgumentRecognised[i] = true;
                    this.c = new TrinaryModeCorpus(c.options, c.resources);
                }

                if (args[i].equalsIgnoreCase("binary")) {
                    this.c.options.bgBinaryVersionOfTrinaryMode = true;
                    this.c.options.bgTrinaryMode = true;
                    bArgumentRecognised[i] = true;
                    this.c = new BinaryModeCorpus(c.options, c.resources);
                }

                if (args[i].equalsIgnoreCase("scale")) {
                    this.c.options.bgScaleMode = true;
                    bArgumentRecognised[i] = true;
                    if (this.c.options.bgTrinaryMode) {
                        System.out.println("Must choose binary/trinary OR scale mode");
                        return;
                    }
                    this.c = new ScaleModeCorpus(c.options, c.resources);
                }

                ClassificationOptions var10000;
                if (args[i].equalsIgnoreCase("sentenceCombineAv")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionSentenceCombineMethod = 1;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("sentenceCombineTot")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionSentenceCombineMethod = 2;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("paragraphCombineAv")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionParagraphCombineMethod = 1;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("paragraphCombineTot")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionParagraphCombineMethod = 2;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("negativeMultiplier")) {
                    this.c.options.fgNegativeSentimentMultiplier = Float.parseFloat(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("noBoosters")) {
                    this.c.options.bgBoosterWordsChangeEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noNegatingPositiveFlipsEmotion")) {
                    this.c.options.bgNegatingPositiveFlipsEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noNegatingNegativeNeutralisesEmotion")) {
                    this.c.options.bgNegatingNegativeNeutralisesEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noNegators")) {
                    this.c.options.bgNegatingWordsFlipEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noIdioms")) {
                    this.c.options.bgUseIdiomLookupTable = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("questionsReduceNeg")) {
                    this.c.options.bgReduceNegativeEmotionInQuestionSentences = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noEmoticons")) {
                    this.c.options.bgUseEmoticons = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("exclamations2")) {
                    this.c.options.bgExclamationInNeutralSentenceCountsAsPlus2 = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("minPunctuationWithExclamation")) {
                    this.c.options.igMinPunctuationWithExclamationToChangeSentenceSentiment =
                            Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("mood")) {
                    this.c.options.igMoodToInterpretNeutralEmphasis = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("noMultiplePosWords")) {
                    this.c.options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noMultipleNegWords")) {
                    this.c.options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noIgnoreBoosterWordsAfterNegatives")) {
                    this.c.options.bgIgnoreBoosterWordsAfterNegatives = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noDictionary")) {
                    this.c.options.bgCorrectSpellingsUsingDictionary = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("noDeleteExtraDuplicateLetters")) {
                    this.c.options.bgCorrectExtraLetterSpellingErrors = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("illegalDoubleLettersInWordMiddle")) {
                    this.c.options.sgIllegalDoubleLettersInWordMiddle = args[i + 1].toLowerCase();
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("illegalDoubleLettersAtWordEnd")) {
                    this.c.options.sgIllegalDoubleLettersAtWordEnd = args[i + 1].toLowerCase();
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("noMultipleLetters")) {
                    this.c.options.bgMultipleLettersBoostSentiment = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("negatedWordStrengthMultiplier")) {
                    this.c.options.fgStrengthMultiplierForNegatedWords = Float.parseFloat(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("maxWordsBeforeSentimentToNegate")) {
                    this.c.options.igMaxWordsBeforeSentimentToNegate = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("negatingWordsDontOccurBeforeSentiment")) {
                    this.c.options.bgNegatingWordsOccurBeforeSentiment = false;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("maxWordsAfterSentimentToNegate")) {
                    this.c.options.igMaxWordsAfterSentimentToNegate = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("negatingWordsOccurAfterSentiment")) {
                    this.c.options.bgNegatingWordsOccurAfterSentiment = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("alwaysSplitWordsAtApostrophes")) {
                    this.c.options.bgAlwaysSplitWordsAtApostrophes = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("capitalsBoostTermSentiment")) {
                    this.c.options.bgCapitalsBoostTermSentiment = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("lemmaFile")) {
                    this.c.options.bgUseLemmatisation = true;
                    this.c.resources.sgLemmaFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("MinSentencePosForQuotesIrony")) {
                    this.c.options.igMinSentencePosForQuotesIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("MinSentencePosForPunctuationIrony")) {
                    this.c.options.igMinSentencePosForPunctuationIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("MinSentencePosForTermsIrony")) {
                    this.c.options.igMinSentencePosForTermsIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("MinSentencePosForAllIrony")) {
                    this.c.options.igMinSentencePosForTermsIrony = Integer.parseInt(args[i + 1]);
                    this.c.options.igMinSentencePosForPunctuationIrony =
                            this.c.options.igMinSentencePosForTermsIrony;
                    this.c.options.igMinSentencePosForQuotesIrony =
                            this.c.options.igMinSentencePosForTermsIrony;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("explain")) {
                    this.c.options.bgExplainClassification = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("echo")) {
                    this.c.options.bgEchoText = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("UTF8")) {
                    this.c.options.bgForceUTF8 = true;
                    bArgumentRecognised[i] = true;
                }

            } catch (NumberFormatException var5) {
                System.out.println("Error in argument for " + args[i] + ". Integer expected!");
                return;
            } catch (Exception var6) {
                System.out.println("Error in argument for " + args[i] + ". Argument missing?");
                return;
            }
        }

    }

    public void initialise(String[] args) {
        boolean[] bArgumentRecognised = new boolean[args.length];

        int i;
        for (i = 0; i < args.length; ++i) {
            bArgumentRecognised[i] = false;
        }

        this.parseParametersForCorpusOptions(args, bArgumentRecognised);

        for (i = 0; i < args.length; ++i) {
            if (!bArgumentRecognised[i]) {
                System.out.println("Unrecognised command - wrong spelling or case?: " + args[i]);
                this.showBriefHelp();
                return;
            }
        }

        if (!this.c.initialise()) {
            System.out.println("Failed to initialise!");
        }

    }

    public OutputVO computeSentimentScores(String sentence, String separator) {
        OutputVO output = new OutputVO();
        Paragraph paragraph = new Paragraph();
        paragraph.setParagraph(sentence, this.c.resources, this.c.options);
        output.setINeg(paragraph.getParagraphNegativeSentiment());
        output.setIPos(paragraph.getParagraphPositiveSentiment());
        output.setITrinary(paragraph.getParagraphTrinarySentiment());
        output.setIScale(paragraph.getParagraphScaleSentiment());
        String sRationale = "";
        if (this.c.options.bgEchoText) {
            sRationale = separator + sentence;
        }

        if (this.c.options.bgExplainClassification) {
            sRationale = separator + paragraph.getClassificationRationale();
        }

        if (this.c.options.bgTrinaryMode) {
            output.setOutputMessage(output.getIPos() + separator + output.getINeg() + separator + output.getITrinary() + sRationale);
        } else {
            output.setOutputMessage(this.c.options.bgScaleMode ? output.getIPos() + separator + output.getINeg() + separator + output.getIScale() + sRationale :
                    output.getIPos() + separator + output.getINeg() + sRationale);
        }
        return output;
    }

    private void runMachineLearning(BaseCorpus c, String sInputFile, boolean bDoAll, int iMinImprovement,
                                    boolean bUseTotalDifference, int iIterations,
                                    int iMultiOptimisations, String sOutputFile) {
        if (iMinImprovement < 1) {
            System.out.println("No action taken because min improvement < 1");
            this.showBriefHelp();
            return;
        }
        c.setCorpus(sInputFile);
        c.calculateCorpusSentimentScores();
        int corpusSize = c.getCorpusSize();
        if (c.options.bgTrinaryMode) {
            if (c.options.bgBinaryVersionOfTrinaryMode) {
                System.out.print(
                        "Before training, binary accuracy: " + c.getClassificationTrinaryNumberCorrect() +
                                " " +
                                c.getClassificationTrinaryNumberCorrect() / corpusSize * 100.0F +
                                "%");
            } else {
                System.out.print(
                        "Before training, trinary accuracy: " + c.getClassificationTrinaryNumberCorrect() +
                                " " +
                                c.getClassificationTrinaryNumberCorrect() / corpusSize * 100.0F +
                                "%");
            }
        } else if (c.options.bgScaleMode) {
            System.out.print(
                    "Before training, scale accuracy: " + c.getClassificationScaleNumberCorrect() + " " +
                            c.getClassificationScaleNumberCorrect() * 100.0F / corpusSize +
                            "% corr " + c.getClassificationScaleCorrelationWholeCorpus());
        } else {
            System.out.print(
                    "Before training, positive: " + c.getClassificationPositiveNumberCorrect() + " " +
                            c.getClassificationPositiveAccuracyProportion() * 100.0F + "% negative " +
                            c.getClassificationNegativeNumberCorrect() + " " +
                            c.getClassificationNegativeAccuracyProportion() * 100.0F + "% ");
            System.out.print(
                    "   Positive corr: " + c.getClassificationPosCorrelationWholeCorpus() + " negative " +
                            c.getClassificationNegCorrelationWholeCorpus());
        }

        System.out.println(" out of " + c.getCorpusSize());
        // core
        if (bDoAll) {
            System.out.println(
                    "Running " + iIterations + " iteration(s) of all options on file " + sInputFile +
                            "; results in " + sOutputFile);
            c.run10FoldCrossValidationForAllOptionVariations(iMinImprovement, bUseTotalDifference,
                    iIterations, iMultiOptimisations, sOutputFile);
        } else {
            System.out.println(
                    "Running " + iIterations + " iteration(s) for standard or selected options on file " +
                            sInputFile + "; results in " + sOutputFile);
            c.run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iIterations,
                    iMultiOptimisations, sOutputFile);
        }
    }

    private void classifyAndSaveWithID(BaseCorpus c, String sInputFile, String sInputFolder, int iTextCol,
                                       int iIdCol) {
        if (!sInputFile.equals("")) {
            c.classifyAllLinesAndRecordWithID(sInputFile, iTextCol - 1, iIdCol - 1,
                    FileOps.s_ChopFileNameExtension(sInputFile) + "_classID.txt");
            return;
        }
        if (sInputFolder.equals("")) {
            System.out.println("No annotations done because no input file or folder specfied");
            this.showBriefHelp();
            return;
        }
        // inputFile == "" and InputFolder != ""
        File folder = new File(sInputFolder);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            System.out.println("Incorrect or empty input folder specfied");
            this.showBriefHelp();
            return;
        }

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                System.out.println("Classify + save with ID: " + listOfFile.getName());
                c.classifyAllLinesAndRecordWithID(sInputFolder + "/" + listOfFile.getName(),
                        iTextCol - 1, iIdCol - 1,
                        sInputFolder + "/" + FileOps.s_ChopFileNameExtension(listOfFile.getName()) +
                                "_classID.txt");
            }
        }
    }

    private void annotationTextCol(BaseCorpus c, String sInputFile, String sInputFolder,
                                   String sFileSubString, int iTextColForAnnotation,
                                   boolean bOkToOverwrite) {
        if (!bOkToOverwrite) {
            System.out.println("Must include parameter overwrite to annotate");
            return;
        }
        if (!sInputFile.equals("")) {
            c.annotateAllLinesInInputFile(sInputFile, iTextColForAnnotation - 1);
            return;
        }
        if (sInputFolder.equals("")) {
            System.out.println("No annotations done because no input file or folder specfied");
            this.showBriefHelp();
            return;
        }
        File folder = new File(sInputFolder);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                if (!sFileSubString.equals("") &&
                        listOfFile.getName().indexOf(sFileSubString) <= 0) {
                    System.out.println("  Ignoring " + listOfFile.getName());
                } else {
                    System.out.println("Annotate: " + listOfFile.getName());
                    c.annotateAllLinesInInputFile(sInputFolder + "/" + listOfFile.getName(),
                            iTextColForAnnotation - 1);
                }
            }
        }
    }

    private void parseOneText(BaseCorpus c, String sTextToParse, boolean bURLEncodedOutput) {
        //int iPos = 1;
        OutputVO output = computeSentimentScores(sTextToParse, " ");

        if (bURLEncodedOutput) {
            try {
                System.out.println(URLEncoder.encode(output.getOutputMessage(), "UTF-8"));
            } catch (UnsupportedEncodingException var13) {
                var13.printStackTrace();
            }
        } else if (c.options.bgForceUTF8) {
            System.out.println(new String(output.getOutputMessage().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } else {
            System.out.println(output.getOutputMessage());
        }

    }

    private void listenToStdIn(BaseCorpus c, int iTextCol) {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        String sTextToParse;
        try {
            while ((sTextToParse = stdin.readLine()) != null) {
                boolean bSuccess;
                if (sTextToParse.contains("#Change_TermWeight")) {
                    String[] sData = sTextToParse.split("\t");
                    bSuccess = c.resources.sentimentWords.setSentiment(sData[1], Integer.parseInt(sData[2]));
                    if (bSuccess) {
                        System.out.println("1");
                    } else {
                        System.out.println("0");
                    }
                } else {
                    int iPos;
                    int iTrinary;
                    int iScale;
                    Paragraph paragraph = new Paragraph();
                    if (iTextCol > -1) {
                        String[] sData = sTextToParse.split("\t");
                        if (sData.length >= iTextCol) {
                            paragraph.setParagraph(sData[iTextCol], c.resources, c.options);
                        }
                    } else {
                        paragraph.setParagraph(sTextToParse, c.resources, c.options);
                    }

                    int iNeg = paragraph.getParagraphNegativeSentiment();
                    iPos = paragraph.getParagraphPositiveSentiment();
                    iTrinary = paragraph.getParagraphTrinarySentiment();
                    iScale = paragraph.getParagraphScaleSentiment();
                    String sRationale = "";
                    String sOutput;
                    if (c.options.bgEchoText) {
                        sOutput = sTextToParse + "\t";
                    } else {
                        sOutput = "";
                    }

                    if (c.options.bgExplainClassification) {
                        sRationale = paragraph.getClassificationRationale();
                    }

                    if (c.options.bgTrinaryMode) {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + iTrinary + "\t" + sRationale;
                    } else if (c.options.bgScaleMode) {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + iScale + "\t" + sRationale;
                    } else {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + sRationale;
                    }

                    if (c.options.bgForceUTF8) {
                        System.out.println(
                                new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                    } else {
                        System.out.println(sOutput);
                    }
                }
            }
        } catch (IOException var14) {
            System.out.println("Error reading input");
            var14.printStackTrace();
        }
    }

    private void listenForCmdInput(BaseCorpus c) {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                while (true) {
                    String sTextToParse = stdin.readLine();
                    if (sTextToParse.equalsIgnoreCase("@end")) {
                        return;
                    }
                    OutputVO output = computeSentimentScores(sTextToParse, " ");

                    if (!c.options.bgForceUTF8) {
                        System.out.println(output.getOutputMessage());
                    } else {
                        System.out.println(
                                new String(output.getOutputMessage().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException var13) {
                System.out.println(var13);
            }
        }
    }

    private void listenAtPort(BaseCorpus c, int iListenPort) {
        ServerSocket serverSocket;
        String decodedText = "";

        try {
            serverSocket = new ServerSocket(iListenPort);
        } catch (IOException var23) {
            System.out.println(
                    "Could not listen on port " + iListenPort + " because\n" + var23.getMessage());
            return;
        }

        System.out.println(
                "Listening on port: " + iListenPort + " IP: " + serverSocket.getInetAddress());

        while (true) {
            Socket clientSocket;

            try {
                clientSocket = serverSocket.accept();
            } catch (IOException var20) {
                System.out.println("Accept failed at port: " + iListenPort);
                return;
            }

            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException var19) {
                System.out.println("IOException clientSocket.getOutputStream " + var19.getMessage());
                var19.printStackTrace();
                return;
            }

            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            } catch (IOException var18) {
                System.out.println("IOException InputStreamReader " + var18.getMessage());
                var18.printStackTrace();
                return;
            }

            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.indexOf("GET /") == 0) {
                        int lastSpacePos = inputLine.lastIndexOf(" ");
                        if (lastSpacePos < 5) {
                            lastSpacePos = inputLine.length();
                        }
                        try {
                            decodedText =
                                    URLDecoder.decode(inputLine.substring(5, lastSpacePos), String.valueOf(StandardCharsets.UTF_8));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Analysis of text: " + decodedText);
                        break;
                    }
                }
            } catch (IOException var24) {
                System.out.println("IOException " + var24.getMessage());
                var24.printStackTrace();
                decodedText = "";
            } catch (Exception var25) {
                System.out.println("Non-IOException " + var25.getMessage());
                decodedText = "";
            }
            OutputVO output = computeSentimentScores(decodedText, " ");

            if (c.options.bgForceUTF8) {
                out.print(new String(output.getOutputMessage().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            } else {
                out.print(output.getOutputMessage());
            }
            try {
                out.close();
                in.close();
                clientSocket.close();
            } catch (IOException var21) {
                System.out.println("IOException closing streams or sockets" + var21.getMessage());
                var21.printStackTrace();
            }
        }
    }

    private void showBriefHelp() {
        System.out.println();
        System.out.println("====" + this.c.options.sgProgramName + "Brief Help====");
        System.out.println("For most operations, a minimum of two parameters must be set");
        System.out.println("1) folder location for the linguistic files");
        System.out.println("   e.g., on Windows: C:/mike/Lexical_Data/");
        System.out.println("   e.g., on Mac/Linux/Unix: /usr/Lexical_Data/");
        if (this.c.options.bgTensiStrength) {
            System.out.println("TensiiStrength_Data can be downloaded from...[not completed yet]");
        } else {
            System.out.println(
                    "SentiStrength_Data can be downloaded with the Windows version of SentiStrength from sentistrength.wlv.ac.uk");
        }

        System.out.println();
        System.out.println("2) text to be classified or file name of texts to be classified");
        System.out.println("   e.g., To classify one text: text love+u");
        System.out.println("   e.g., To classify a file of texts: input /bob/data.txt");
        System.out.println();
        System.out.println("Here is an example complete command:");
        if (this.c.options.bgTensiStrength) {
            System.out.println(
                    "java -jar TensiStrength.jar sentidata C:/a/Stress_Data/ text am+stressed");
        } else {
            System.out.println(
                    "java -jar SentiStrength.jar sentidata C:/a/SentStrength_Data/ text love+u");
        }

        System.out.println();
        if (!this.c.options.bgTensiStrength) {
            System.out.println("To list all commands: java -jar SentiStrength.jar help");
        }

    }

    private void printCommandLineOptions() {
        System.out.println("====" + this.c.options.sgProgramName + " Command Line Options====");
        System.out.println("=Source of data to be classified=");
        System.out.println(" text [text to process] OR");
        System.out.println(" input [filename] (each line of the file is classified SEPARATELY");
        System.out.println("        May have +ve 1st col., -ve 2nd col. in evaluation mode) OR");
        System.out.println(" annotateCol [col # 1..] (classify text in col, result at line end) OR");
        System.out.println(
                " textCol, idCol [col # 1..] (classify text in col, result & ID in new file) OR");
        System.out.println(" inputFolder  [foldername] (all files in folder will be *annotated*)");
        System.out.println(
                " outputFolder [foldername where to put the output (default: folder of input)]");
        System.out.println(" resultsExtension [file-extension for output (default _out.txt)]");
        System.out.println("  fileSubstring [text] (string must be present in files to annotate)");
        System.out.println("  Ok to overwrite files [overwrite]");
        System.out.println(" listen [port number to listen at - call http://127.0.0.1:81/text]");
        System.out.println(" cmd (wait for stdin input, write to stdout, terminate on input: @end");
        System.out.println(
                " stdin (read from stdin input, write to stdout, terminate when stdin finished)");
        System.out.println(
                " wait (just initialise; allow calls to public String computeSentimentScores)");
        System.out.println("=Linguistic data source=");
        System.out.println(" sentidata [folder for " + this.c.options.sgProgramName +
                " data (end in slash, no spaces)]");
        System.out.println("=Options=");
        System.out.println(" keywords [comma-separated list - " + this.c.options.sgProgramMeasuring +
                " only classified close to these]");
        System.out.println("   wordsBeforeKeywords [words to classify before keyword (default 4)]");
        System.out.println("   wordsAfterKeywords [words to classify after keyword (default 4)]");
        System.out.println(" trinary (report positive-negative-neutral classifcation instead)");
        System.out.println(" binary (report positive-negative classifcation instead)");
        System.out.println(" scale (report single -4 to +4 classifcation instead)");
        System.out.println(" emotionLookupTable [filename (default: EmotionLookupTable.txt)]");
        System.out.println(" additionalFile [filename] (domain-specific terms and evaluations)");
        System.out.println(" lemmaFile [filename] (word tab lemma list for lemmatisation)");
        System.out.println("=Classification algorithm parameters=");
        System.out.println(" noBoosters (ignore sentiment booster words (e.g., very))");
        System.out.println(" noNegators (don't use negating words (e.g., not) to flip sentiment) -OR-");
        System.out.println(
                " noNegatingPositiveFlipsEmotion (don't use negating words to flip +ve words)");
        System.out.println(
                " bgNegatingNegativeNeutralisesEmotion (negating words don't neuter -ve words)");
        System.out.println(
                " negatedWordStrengthMultiplier (strength multiplier when negated (default=0.5))");
        System.out.println(
                " negatingWordsOccurAfterSentiment (negate " + this.c.options.sgProgramMeasuring +
                        " occurring before negatives)");
        System.out.println(
                "  maxWordsAfterSentimentToNegate (max words " + this.c.options.sgProgramMeasuring +
                        " to negator (default 0))");
        System.out.println(" negatingWordsDontOccurBeforeSentiment (don't negate " +
                this.c.options.sgProgramMeasuring + " after negatives)");
        System.out.println("   maxWordsBeforeSentimentToNegate (max from negator to " +
                this.c.options.sgProgramMeasuring + " (default 0))");
        System.out.println(" noIdioms (ignore idiom list)");
        System.out.println(" questionsReduceNeg (-ve sentiment reduced in questions)");
        System.out.println(" noEmoticons (ignore emoticon list)");
        System.out.println(" exclamations2 (sentence with ! counts as +2 if otherwise neutral)");
        System.out.println(" minPunctuationWithExclamation (min punctuation with ! to boost term " +
                this.c.options.sgProgramMeasuring + ")");
        System.out.println(
                " mood [-1,0,1] (default 1: -1 assume neutral emphasis is neg, 1, assume is pos");
        System.out.println(
                " noMultiplePosWords (multiple +ve words don't increase " + this.c.options.sgProgramPos +
                        ")");
        System.out.println(
                " noMultipleNegWords (multiple -ve words don't increase " + this.c.options.sgProgramNeg +
                        ")");
        System.out.println(
                " noIgnoreBoosterWordsAfterNegatives (don't ignore boosters after negating words)");
        System.out.println(" noDictionary (don't try to correct spellings using the dictionary)");
        System.out.println(" noMultipleLetters (don't use additional letters in a word to boost " +
                this.c.options.sgProgramMeasuring + ")");
        System.out.println(
                " noDeleteExtraDuplicateLetters (don't delete extra duplicate letters in words)");
        System.out.println(
                " illegalDoubleLettersInWordMiddle [letters never duplicate in word middles]");
        System.out.println("    default for English: ahijkquvxyz (specify list without spaces)");
        System.out.println(" illegalDoubleLettersAtWordEnd [letters never duplicate at word ends]");
        System.out.println("    default for English: achijkmnpqruvwxyz (specify list without spaces)");
        System.out.println(" sentenceCombineAv (average " + this.c.options.sgProgramMeasuring +
                " strength of terms in each sentence) OR");
        System.out.println(" sentenceCombineTot (total the " + this.c.options.sgProgramMeasuring +
                " strength of terms in each sentence)");
        System.out.println(" paragraphCombineAv (average " + this.c.options.sgProgramMeasuring +
                " strength of sentences in each text) OR");
        System.out.println(" paragraphCombineTot (total the " + this.c.options.sgProgramMeasuring +
                " strength of sentences in each text)");
        System.out.println(
                "  *the default for the above 4 options is the maximum, not the total or average");
        System.out.println(
                " negativeMultiplier [negative total strength polarity multiplier, default 1.5]");
        System.out.println(" capitalsBoostTermSentiment (" + this.c.options.sgProgramMeasuring +
                " words in CAPITALS are stronger)");
        System.out.println(" alwaysSplitWordsAtApostrophes (e.g., t'aime -> t ' aime)");
        System.out.println(
                " MinSentencePosForQuotesIrony [integer] quotes in +ve sentences indicate irony");
        System.out.println(
                " MinSentencePosForPunctuationIrony [integer] +ve ending in !!+ indicates irony");
        System.out.println(
                " MinSentencePosForTermsIrony [integer] irony terms in +ve sent. indicate irony");
        System.out.println(" MinSentencePosForAllIrony [integer] all of the above irony terms");
        System.out.println(
                " lang [ISO-639 lower-case two-letter langauge code] set processing language");
        System.out.println("=Input and Output=");
        System.out.println(" explain (explain classification after results)");
        System.out.println(" echo (echo original text after results [for pipeline processes])");
        System.out.println(" UTF8 (force all processing to be in UTF-8 format)");
        System.out.println(" urlencoded (input and output text is URL encoded)");
        System.out.println("=Advanced - machine learning [1st input line ignored]=");
        System.out.println(
                " termWeights (list terms in badly classified texts; must specify inputFile)");
        System.out.println(
                " optimise [Filename for optimal term strengths (eg. EmotionLookupTable2.txt)]");
        System.out.println(" train (evaluate " + this.c.options.sgProgramName +
                " by training term strengths on results in file)");
        System.out.println("   all (test all option variations rather than use default)");
        System.out.println(
                "   numCorrect (optimise by # correct - not total classification difference)");
        System.out.println("   iterations [number of 10-fold iterations] (default 1)");
        System.out.println("   minImprovement [min. accuracy improvement to change " +
                this.c.options.sgProgramMeasuring + " weights (default 1)]");
        System.out.println("   multi [# duplicate term strength optimisations to change " +
                this.c.options.sgProgramMeasuring + " weights (default 1)]");
    }

    public BaseCorpus getCorpus() {
        return this.c;
    }

    private boolean[] recogniseArgs(String[] args) {
        boolean[] bArgumentRecognised = new boolean[args.length];


        int i;
        for (i = 0; i < args.length; ++i) {
            bArgumentRecognised[i] = false;
        }
        //String rooty="/Users/mac/Documents/workspace/SentiStrength/src/input/";
        for (i = 0; i < args.length; ++i) {
            try {
                if (args[i].equalsIgnoreCase("input")) {
                    sInputFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("inputfolder")) {
                    sInputFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("outputfolder")) {
                    sResultsFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("resultextension")) {
                    sResultsFileExtension = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("resultsextension")) {
                    sResultsFileExtension = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("filesubstring")) {
                    sFileSubString = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("overwrite")) {
                    bOkToOverwrite = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("text")) {
                    sTextToParse = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("urlencoded")) {
                    bURLEncoded = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("listen")) {
                    iListenPort = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("stdin")) {
                    bStdIn = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("cmd")) {
                    bCmd = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("optimise")) {
                    sOptimalTermStrengths = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("annotatecol")) {
                    iTextColForAnnotation = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("textcol")) {
                    iTextCol = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("idcol")) {
                    iIdCol = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("lang")) {
                    sLanguage = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("train")) {
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("all")) {
                    bDoAll = true;
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("numcorrect")) {
                    bUseTotalDifference = false;
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("iterations")) {
                    iIterations = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("minimprovement")) {
                    iMinImprovement = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("multi")) {
                    iMultiOptimisations = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("termWeights")) {
                    bReportNewTermWeightsForBadClassifications = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("wait")) {
                    bWait = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("help")) {
                    this.printCommandLineOptions();
                    return null;
                }
            } catch (NumberFormatException var32) {
                System.out.println("Error in argument for " + args[i] + ". Integer expected!");
                return null;
            } catch (Exception var33) {
                System.out.println("Error in argument for " + args[i] + ". Argument missing?");
                return null;
            }
        }
        return bArgumentRecognised;
    }
}
