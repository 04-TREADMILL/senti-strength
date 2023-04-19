// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   ClassificationResources.java

package nju.SEIII.EASIEST.SentiStrength;

import java.io.File;
import nju.SEIII.EASIEST.SentiStrength.WordPresenceList.CorrectSpellingsList;
import nju.SEIII.EASIEST.SentiStrength.WordPresenceList.IronyList;
import nju.SEIII.EASIEST.SentiStrength.WordPresenceList.NegatingWordList;
import nju.SEIII.EASIEST.SentiStrength.WordPresenceList.QuestionWords;
import nju.SEIII.EASIEST.SentiStrength.WordPresenceList.WordPresenceList;
import nju.SEIII.EASIEST.SentiStrength.WordStrengthList.BoosterWordsList;
import nju.SEIII.EASIEST.SentiStrength.WordStrengthList.EmoticonsList;
import nju.SEIII.EASIEST.SentiStrength.WordStrengthList.WordStrengthList;
import nju.SEIII.EASIEST.Utilities.FileOps;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            EmoticonsList, CorrectSpellingsList, SentimentWords, NegatingWordList, 
//            QuestionWords, BoosterWordsList, IdiomList, EvaluativeTerms, 
//            IronyList, Lemmatiser, ClassificationOptions

/**
 * This class represents a collection of resources used for text classification.
 *
 * @UC <p><ul>
 * <li>UC-6 Repeated Letter Rule
 * <li>UC-7 Emoji Rule
 * <li>UC-8 Exclamation Mark Rule
 * <li>UC-9 Repeated Punctuation Rule
 * <li>UC-10 Negative Sentiment Ignored in Questions
 * <li>UC-11 Classify a single text
 * <li>UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]
 * <li>UC-13 Classify texts in a column within a file or folder
 * <li>UC-14 Listen at a port for texts to classify
 * <li>UC-15 Run interactively from the command line
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-18 Location of sentiment term weights
 * <li>UC-19 Location of output folder
 * <li>UC-20 File name extension for output
 * <li>UC-29 Machine learning evaluations
 * </ul><p>
 */
public class ClassificationResources {

  /**
   * A list of emoticons used for text classification.
   */
  public WordStrengthList emoticons;

  /**
   * A list of correct spellings used for text classification.
   */
  public WordPresenceList correctSpellings;

  /**
   * A list of sentiment words used for text classification.
   */
  public SentimentWords sentimentWords;

  /**
   * A list of negating words used for text classification.
   */
  public WordPresenceList negatingWords;

  /**
   * A list of question words used for text classification.
   */
  public WordPresenceList questionWords;

  /**
   * A list of booster words used for text classification.
   */
  public WordStrengthList boosterWords;

  /**
   * A list of idioms used for text classification.
   */
  public IdiomList idiomList;
  /**
   * A list of evaluative terms used for text classification.
   */
  public EvaluativeTerms evaluativeTerms;

  /**
   * A list of irony words used for text classification.
   */
  public WordPresenceList ironyList;

  /**
   * A lemmatiser used for text classification.
   */
  public Lemmatiser lemmatiser;
  /**
   * The path to the SentiStrength folder.
   */
  public String sgSentiStrengthFolder;

  /**
   * The name of the sentiment words file.
   */
  public String sgSentimentWordsFile;
  /**
   * The file path for sentiment words.
   */
  public String sgSentimentWordsFile2;

  /**
   * The file path for emoticon lookup table.
   */
  public String sgEmoticonLookupTable;

  /**
   * The file path for correct spelling file.
   */
  public String sgCorrectSpellingFileName;

  /**
   * The file path for second correct spelling file.
   */
  public String sgCorrectSpellingFileName2;

  /**
   * The file path for slang lookup table.
   */
  public String sgSlangLookupTable;

  /**
   * The file path for negating word list.
   */
  public String sgNegatingWordListFile;

  /**
   * The file path for booster list.
   */
  public String sgBoosterListFile;

  /**
   * The file path for idiom lookup table.
   */
  public String sgIdiomLookupTableFile;

  /**
   * The file path for question word list.
   */
  public String sgQuestionWordListFile;

  /**
   * The file path for irony word list.
   */
  public String sgIronyWordListFile;

  /**
   * Additional File
   */
  public String sgAdditionalFile;

  /**
   * Lemma File
   */
  public String sgLemmaFile;

  /**
   * constructor
   */
  public ClassificationResources() {
    emoticons = new EmoticonsList();
    correctSpellings = new CorrectSpellingsList();
    sentimentWords = new SentimentWords();
    negatingWords = new NegatingWordList();
    questionWords = new QuestionWords();
    boosterWords = new BoosterWordsList();
    idiomList = new IdiomList();
    evaluativeTerms = new EvaluativeTerms();
    ironyList = new IronyList();
    lemmatiser = new Lemmatiser();
    sgSentiStrengthFolder = System.getProperty("user.dir") + "/src/main/resources/static/data/";
    sgSentimentWordsFile = "EmotionLookupTable.txt";
    sgSentimentWordsFile2 = "SentimentLookupTable.txt";
    sgEmoticonLookupTable = "EmoticonLookupTable.txt";
    sgCorrectSpellingFileName = "Dictionary.txt";
    sgCorrectSpellingFileName2 = "EnglishWordList.txt";
    sgSlangLookupTable = "SlangLookupTable_NOT_USED.txt";
    sgNegatingWordListFile = "NegatingWordList.txt";
    sgBoosterListFile = "BoosterWordList.txt";
    sgIdiomLookupTableFile = "IdiomLookupTable.txt";
    sgQuestionWordListFile = "QuestionWords.txt";
    sgIronyWordListFile = "IronyTerms.txt";
    sgAdditionalFile = "";
    sgLemmaFile = "";
  }

  /**
   * This method initializes several objects with their respective files and options.
   *
   * @param options the ClassificationOptions object to use for initialization
   * @return true if initialization is successful, false otherwise
   */
  public boolean initialise(ClassificationOptions options) {
    // Reserve extra lines for additional file
    int iExtraLinesToReserve = 0;
    if (sgAdditionalFile.compareTo("") != 0) {
      iExtraLinesToReserve =
          FileOps.i_CountLinesInTextFile(sgSentiStrengthFolder + sgAdditionalFile);
      if (iExtraLinesToReserve < 0) {
        System.out.println("No lines found in additional file! Ignoring " + sgAdditionalFile);
        return false;
      }
    }
    // Check if the useLemmatisation option is true and if the lemmatiser object fails to initialize
    if (options.bgUseLemmatisation &&
        !lemmatiser.initialise(sgSentiStrengthFolder + sgLemmaFile, false)) {
      System.out.println("Can't load lemma file! " + sgLemmaFile);
      return false;
    }
    File f = new File(sgSentiStrengthFolder + sgSentimentWordsFile);
    if (!f.exists() || f.isDirectory()) {
      sgSentimentWordsFile = sgSentimentWordsFile2;
    }
    File f2 = new File(sgSentiStrengthFolder + sgCorrectSpellingFileName);
    if (!f2.exists() || f2.isDirectory()) {
      sgCorrectSpellingFileName = sgCorrectSpellingFileName2;
    }
    // Initialize several objects with their respective files and options
    if (emoticons.initialise(sgSentiStrengthFolder + sgEmoticonLookupTable, options) &&
        correctSpellings.initialise(sgSentiStrengthFolder + sgCorrectSpellingFileName, options) &&
        sentimentWords.initialise(sgSentiStrengthFolder + sgSentimentWordsFile, options,
            iExtraLinesToReserve) &&
        negatingWords.initialise(sgSentiStrengthFolder + sgNegatingWordListFile, options) &&
        questionWords.initialise(sgSentiStrengthFolder + sgQuestionWordListFile, options) &&
        ironyList.initialise(sgSentiStrengthFolder + sgIronyWordListFile, options) &&
        boosterWords.initialise(sgSentiStrengthFolder + sgBoosterListFile, options) &&
        idiomList.initialise(sgSentiStrengthFolder + sgIdiomLookupTableFile, options,
            iExtraLinesToReserve)) {
      if (iExtraLinesToReserve > 0)
      // Return the result of calling the initialise method on the evaluativeTerms object
      {
        return evaluativeTerms.initialise(sgSentiStrengthFolder + sgAdditionalFile, options,
            idiomList, sentimentWords);
      } else {
        return true;
      }
    } else {
      return false;
    }
  }
}
