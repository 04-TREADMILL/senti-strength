// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   ClassificationResources.java

package nju.SEIII.EASIEST.sentistrength;

import java.io.File;


import java.io.PrintStream;
import nju.SEIII.EASIEST.utilities.FileOps;

// Referenced classes of package nju.SEIII.EASIEST.sentistrength:
//            EmoticonsList, CorrectSpellingsList, SentimentWords, NegatingWordList, 
//            QuestionWords, BoosterWordsList, IdiomList, EvaluativeTerms, 
//            IronyList, Lemmatiser, ClassificationOptions

/**
 * @UC
 * <p><ul>
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
public class ClassificationResources
{

    public EmoticonsList emoticons;
    public CorrectSpellingsList correctSpellings;
    public SentimentWords sentimentWords;
    public NegatingWordList negatingWords;
    public QuestionWords questionWords;
    public BoosterWordsList boosterWords;
    public IdiomList idiomList;
    public EvaluativeTerms evaluativeTerms;
    public IronyList ironyList;
    public Lemmatiser lemmatiser;
    public String sgSentiStrengthFolder;
    public String sgSentimentWordsFile;
    public String sgSentimentWordsFile2;
    public String sgEmoticonLookupTable;
    public String sgCorrectSpellingFileName;
    public String sgCorrectSpellingFileName2;
    public String sgSlangLookupTable;
    public String sgNegatingWordListFile;
    public String sgBoosterListFile;
    public String sgIdiomLookupTableFile;
    public String sgQuestionWordListFile;
    public String sgIronyWordListFile;
    public String sgAdditionalFile;
    public String sgLemmaFile;

    public ClassificationResources()
    {
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
        sgSentiStrengthFolder = System.getProperty("user.dir")+"/src/main/resources/static/data/";
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
    public boolean initialise(ClassificationOptions options)
    {   
        // Reserve extra lines for additional file
    	int iExtraLinesToReserve = 0;
        if(sgAdditionalFile.compareTo("") != 0)
        {
            iExtraLinesToReserve = FileOps.i_CountLinesInTextFile(sgSentiStrengthFolder + sgAdditionalFile);
            if(iExtraLinesToReserve < 0)
            {
                System.out.println("No lines found in additional file! Ignoring " + sgAdditionalFile);
                return false;
            }
        }
        // Check if the useLemmatisation option is true and if the lemmatiser object fails to initialize
        if(options.bgUseLemmatisation && !lemmatiser.initialise(sgSentiStrengthFolder + sgLemmaFile, false))
        {
            System.out.println("Can't load lemma file! " + sgLemmaFile);
            return false;
        }
        File f = new File(sgSentiStrengthFolder + sgSentimentWordsFile);
        if(!f.exists() || f.isDirectory())
            sgSentimentWordsFile = sgSentimentWordsFile2;
        File f2 = new File(sgSentiStrengthFolder + sgCorrectSpellingFileName);
        if(!f2.exists() || f2.isDirectory())
            sgCorrectSpellingFileName = sgCorrectSpellingFileName2;
        // Initialize several objects with their respective files and options
        if(emoticons.initialise(sgSentiStrengthFolder + sgEmoticonLookupTable, options) && correctSpellings.initialise(sgSentiStrengthFolder + sgCorrectSpellingFileName, options) && sentimentWords.initialise(sgSentiStrengthFolder + sgSentimentWordsFile, options, iExtraLinesToReserve) && negatingWords.initialise(sgSentiStrengthFolder + sgNegatingWordListFile, options) && questionWords.initialise(sgSentiStrengthFolder + sgQuestionWordListFile, options) && ironyList.initialise(sgSentiStrengthFolder + sgIronyWordListFile, options) && boosterWords.initialise(sgSentiStrengthFolder + sgBoosterListFile, options, iExtraLinesToReserve) && idiomList.initialise(sgSentiStrengthFolder + sgIdiomLookupTableFile, options, iExtraLinesToReserve))
        {
            if(iExtraLinesToReserve > 0)
                // Return the result of calling the initialise method on the evaluativeTerms object
                return evaluativeTerms.initialise(sgSentiStrengthFolder + sgAdditionalFile, options, idiomList, sentimentWords);
            else
                return true;
        } else
        {
            return false;
        }
    }
}
