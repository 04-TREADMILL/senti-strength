package nju.SEIII.EASIEST.SentiStrength;

import java.util.Objects;

/**
 * The Term class represents a single word or punctuation mark in a sentence.
 * It contains information about the type of content, original and translated word,
 * sentiment score, emphasis, and other properties.
 *
 * @UC <p><ul>
 * <li>UC-11 Classify a single text
 * <li>UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]
 * <li>UC-13 Classify texts in a column within a file or folder
 * <li>UC-14 Listen at a port for texts to classify
 * <li>UC-15 Run interactively from the command line
 * <li>UC-17 Location of linguistic data folder
 * <li>UC-19 Location of output folder
 * <li>UC-20 File name extension for output
 * <li>UC-26 Set Classification Algorithm Parameters
 * <li>UC-27 Optimise sentiment strengths of existing sentiment terms
 * <li>UC-28 Suggest new sentiment terms (from terms in misclassified texts)
 * </ul></p>
 */
public class Term {
    private final int igContentTypeWord = 1;
    private final int igContentTypePunctuation = 2;
    private final int igContentTypeEmoticon = 3;
    int igEmoticonStrength = 0;
    private int igContentType = 0;
    private String sgOriginalWord = "";
    private String sgLCaseWord = "";
    private String sgTranslatedWord = "";
    private StringBuilder sgWordEmphasis = new StringBuilder();
    private int igWordSentimentID = 0;
    private boolean bgNegatingWord = false;
    private boolean bgNegatingWordCalculated = false;
    private boolean bgWordSentimentIDCalculated = false;
    private boolean bgProperNoun = false;
    private boolean bgProperNounCalculated = false;
    private String sgPunctuation = "";
    private String sgPunctuationEmphasis = "";
    private String sgEmoticon = "";
    private int igBoosterWordScore = 999;
    private ClassificationResources resources;
    private ClassificationOptions options;
    private boolean bgAllCapitals = false;
    private boolean bgAllCaptialsCalculated = false;
    private boolean bgOverrideSentimentScore = false;
    private int igOverrideSentimentScore = 0;

    /**
     * Extracts the next word, punctuation, or emoticon from a given string.
     *
     * @param sWordAndPunctuation the string to extract from
     * @param classResources      the resources used for classification
     * @param classOptions        the options used for classification
     * @return the position of the end of the extracted word, punctuation, or emoticon, or -1 if none was found
     */
    public int extractNextWordOrPunctuationOrEmoticon(String sWordAndPunctuation,
                                                      ClassificationResources classResources,
                                                      ClassificationOptions classOptions) {
        int iWordCharOrAppostrophe = 1;
        int iPunctuation = 1;
        int iPos = 0;
        int iLastCharType = 0;
        String sChar = "";
        this.resources = classResources;
        this.options = classOptions;
        int iTextLength = sWordAndPunctuation.length();
        if (this.codeEmoticon(sWordAndPunctuation)) {
            return -1;
        } else {
            for (; iPos < iTextLength; ++iPos) {
                sChar = sWordAndPunctuation.substring(iPos, iPos + 1);
                if (!Character.isLetterOrDigit(sWordAndPunctuation.charAt(iPos)) &&
                        (this.options.bgAlwaysSplitWordsAtApostrophes || !sChar.equals("'") || iPos <= 0 ||
                                iPos >= iTextLength - 1 ||
                                !Character.isLetter(sWordAndPunctuation.charAt(iPos + 1))) && !sChar.equals("$") &&
                        !sChar.equals("£") && !sChar.equals("@") && !sChar.equals("_")) {
                    if (iLastCharType == 1) {
                        this.codeWord(sWordAndPunctuation.substring(0, iPos));
                        return iPos;
                    }

                    iLastCharType = 2;
                } else {
                    if (iLastCharType == 2) {
                        this.codePunctuation(sWordAndPunctuation.substring(0, iPos));
                        return iPos;
                    }

                    iLastCharType = 1;
                }
            }

            switch (iLastCharType) {
                case 1:
                    this.codeWord(sWordAndPunctuation);
                    break;
                case 2:
                    this.codePunctuation(sWordAndPunctuation);
            }

            return -1;
        }
    }

    /**
     * Returns the XML tag for the current word, punctuation, or emoticon.
     *
     * @return the XML tag for the current item, or an empty string if the item type is invalid
     */
    public String getTag() {
        switch (this.igContentType) {
            case 1:
                if (!Objects.equals(this.sgWordEmphasis, "")) {
                    return "<w equiv=\"" + this.sgTranslatedWord + "\" em=\"" + this.sgWordEmphasis + "\">" +
                            this.sgOriginalWord + "</w>";
                }

                return "<w>" + this.sgOriginalWord + "</w>";
            case 2:
                if (!Objects.equals(this.sgPunctuationEmphasis, "")) {
                    return "<p equiv=\"" + this.sgPunctuation + "\" em=\"" + this.sgPunctuationEmphasis +
                            "\">" + this.sgPunctuation + this.sgPunctuationEmphasis + "</p>";
                }

                return "<p>" + this.sgPunctuation + "</p>";
            case 3:
                if (this.igEmoticonStrength == 0) {
                    return "<e>" + this.sgEmoticon + "</e>";
                } else {
                    if (this.igEmoticonStrength == 1) {
                        return "<e em=\"+\">" + this.sgEmoticon + "</e>";
                    }

                    return "<e em=\"-\">" + this.sgEmoticon + "</e>";
                }
            default:
                return "";
        }
    }

    /**
     * Returns the sentiment ID for the current word, if it has not been calculated yet.
     *
     * @return the sentiment ID of the word
     */
    public int getSentimentID() {
        if (!this.bgWordSentimentIDCalculated) {
            this.igWordSentimentID =
                    this.resources.sentimentWords.getSentimentID(this.sgTranslatedWord.toLowerCase());
            this.bgWordSentimentIDCalculated = true;
        }

        return this.igWordSentimentID;
    }

    /**
     * Sets the override sentiment score to the given value.
     *
     * @param iSentiment the sentiment score to set as override value
     */
    public void setSentimentOverrideValue(int iSentiment) {
        this.bgOverrideSentimentScore = true;
        this.igOverrideSentimentScore = iSentiment;
    }

    /**
     * Returns the sentiment value calculated for this object. If an override value has been set using,
     * that value will be returned. Otherwise, the sentiment value will be calculated based on the sentiment ID of the word,
     * using the sentiment words resources provided during initialization.
     *
     * @return the sentiment value of this object
     */
    public int getSentimentValue() {
        if (this.bgOverrideSentimentScore) {
            return this.igOverrideSentimentScore;
        } else {
            return this.getSentimentID() < 1 ? 0 :
                    this.resources.sentimentWords.getSentiment(this.igWordSentimentID);
        }
    }

    /**
     * Returns the length of the word emphasis string.
     *
     * @return the length of the word emphasis string.
     */
    public int getWordEmphasisLength() {
        return this.sgWordEmphasis.length();
    }

    /**
     * Returns the emphasis string for the word.
     *
     * @return the emphasis string for the word.
     */
    public String getWordEmphasis() {
        return this.sgWordEmphasis.toString();
    }

    /**
     * Checks whether the word or punctuation contains any emphasis.
     *
     * @return true if the word or punctuation contains any emphasis, false otherwise.
     */
    public boolean containsEmphasis() {
        if (this.igContentType == 1) {
            return this.sgWordEmphasis.length() > 1;
        } else if (this.igContentType == 2) {
            return this.sgPunctuationEmphasis.length() > 1;
        } else {
            return false;
        }
    }

    /**
     * Returns the translated word of this Word object.
     *
     * @return the translated word of this Word object
     */
    public String getTranslatedWord() {
        return this.sgTranslatedWord;
    }

    /**
     * Returns the translation of the word, punctuation or emoticon represented by this Token object.
     *
     * @return A String object representing the translation of the word, punctuation or emoticon represented by this Token object.
     */
    public String getTranslation() {
        if (this.igContentType == 1) {
            return this.sgTranslatedWord;
        } else if (this.igContentType == 2) {
            return this.sgPunctuation;
        } else {
            return this.igContentType == 3 ? this.sgEmoticon : "";
        }
    }

    /**
     * Returns the booster word score of this token. If the score has not been
     * calculated yet, it is calculated by calling setBoosterWordScore().
     *
     * @return The booster word score of this token.
     */
    public int getBoosterWordScore() {
        if (this.igBoosterWordScore == 999) {
            this.setBoosterWordScore();
        }

        return this.igBoosterWordScore;
    }

    /**
     * Determines if the string is all in uppercase.
     * If the calculation hasn't been done before, it will be performed and saved for future use.
     *
     * @return true if the string is all in uppercase, false otherwise
     */
    public boolean isAllCapitals() {
        if (!this.bgAllCaptialsCalculated) {
            if (Objects.equals(this.sgOriginalWord, this.sgOriginalWord.toUpperCase())) {
                this.bgAllCapitals = true;
            } else {
                this.bgAllCapitals = false;
            }

            this.bgAllCaptialsCalculated = true;
        }

        return this.bgAllCapitals;
    }

    /**
     * Sets the booster word score for the translated word.
     * The booster word score is determined by the strength of the booster word in the resources.
     */
    public void setBoosterWordScore() {
        this.igBoosterWordScore = this.resources.boosterWords.getBoosterStrength(this.sgTranslatedWord);
    }

    /**
     * Determines if the punctuation in the string contains a specific punctuation mark.
     *
     * @param sPunctuation the punctuation mark to check for
     * @return true if the punctuation contains the specified punctuation mark, false otherwise
     */
    public boolean punctuationContains(String sPunctuation) {
        if (this.igContentType != 2) {
            return false;
        } else if (this.sgPunctuation.contains(sPunctuation)) {
            return true;
        } else {
            return !Objects.equals(this.sgPunctuationEmphasis, "") &&
                    this.sgPunctuationEmphasis.contains(sPunctuation);
        }
    }

    /**
     * Returns the length of the string containing emphasis on punctuation.
     *
     * @return the length of the string containing emphasis on punctuation
     */
    public int getPunctuationEmphasisLength() {
        return this.sgPunctuationEmphasis.length();
    }

    /**
     * Returns the sentiment strength of an emoticon.
     *
     * @return the sentiment strength of the emoticon
     */
    public int getEmoticonSentimentStrength() {
        return this.igEmoticonStrength;
    }

    /**
     * Returns the emoticon string.
     *
     * @return the emoticon string
     */
    public String getEmoticon() {
        return this.sgEmoticon;
    }

    /**
     * Returns the string containing the translated punctuation.
     *
     * @return the string containing the translated punctuation
     */
    public String getTranslatedPunctuation() {
        return this.sgPunctuation;
    }

    /**
     * Determines if the object is a word.
     *
     * @return true if the object is a word, false otherwise
     */
    public boolean isWord() {
        return this.igContentType == 1;
    }

    /**
     * Determines if the object is punctuation.
     *
     * @return true if the object is punctuation, false otherwise
     */
    public boolean isPunctuation() {
        return this.igContentType == 2;
    }

    /**
     * Determines if the word is a proper noun.
     * If the calculation hasn't been done before, it will be performed and saved for future use.
     *
     * @return true if the word is a proper noun, false otherwise
     */
    public boolean isProperNoun() {
        if (this.igContentType != 1) {
            return false;
        } else {
            if (!this.bgProperNounCalculated) {
                if (this.sgOriginalWord.length() > 1) {
                    String sFirstLetter = this.sgOriginalWord.substring(0, 1);
                    if (!sFirstLetter.toLowerCase().equals(sFirstLetter.toUpperCase()) &&
                            !this.sgOriginalWord.substring(0, 2).equalsIgnoreCase("I'")) {
                        String sWordRemainder = this.sgOriginalWord.substring(1);
                        if (sFirstLetter.equals(sFirstLetter.toUpperCase()) &&
                                sWordRemainder.equals(sWordRemainder.toLowerCase())) {
                            this.bgProperNoun = true;
                        }
                    }
                }

                this.bgProperNounCalculated = true;
            }

            return this.bgProperNoun;
        }
    }

    /**
     * Determines if the content type of this object is an emoticon.
     *
     * @return true if the content type is an emoticon, false otherwise
     */
    public boolean isEmoticon() {
        return this.igContentType == 3;
    }

    /**
     * Returns the translated text for this object.
     * If the content type is a word, the text will be in lower case.
     *
     * @return the translated text
     */
    public String getText() {
        if (this.igContentType == 1) {
            return this.sgTranslatedWord.toLowerCase();
        } else if (this.igContentType == 2) {
            return this.sgPunctuation;
        } else {
            return this.igContentType == 3 ? this.sgEmoticon : "";
        }
    }

    /**
     * Returns the original text for this object.
     * If the content type is punctuation, the emphasis will be included.
     *
     * @return the original text
     */
    public String getOriginalText() {
        if (this.igContentType == 1) {
            return this.sgOriginalWord;
        } else if (this.igContentType == 2) {
            return this.sgPunctuation + this.sgPunctuationEmphasis;
        } else {
            return this.igContentType == 3 ? this.sgEmoticon : "";
        }
    }

    /**
     * Determines if the translated word is a negating word.
     * If the calculation hasn't been done before, it will be performed and saved for future use.
     *
     * @return true if the word is a negating word, false otherwise
     */
    public boolean isNegatingWord() {
        if (!this.bgNegatingWordCalculated) {
            if (this.sgLCaseWord.length() == 0) {
                this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
            }

            this.bgNegatingWord = this.resources.negatingWords.negatingWord(this.sgLCaseWord);
            this.bgNegatingWordCalculated = true;
        }

        return this.bgNegatingWord;
    }

    /**
     * Checks whether the translated word matches the given text.
     *
     * @param sText               the text to compare with.
     * @param bConvertToLowerCase specifies whether to convert both strings to lowercase before comparing.
     * @return true if the translated word matches the given text, false otherwise.
     */
    public boolean matchesString(String sText, boolean bConvertToLowerCase) {
        if (sText.length() != this.sgTranslatedWord.length()) {
            return false;
        } else {
            if (bConvertToLowerCase) {
                if (this.sgLCaseWord.length() == 0) {
                    this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
                }

                if (sText.equals(this.sgLCaseWord)) {
                    return true;
                }
            } else if (sText.equals(this.sgTranslatedWord)) {
                return true;
            }

            return false;
        }
    }

    /**
     * Determines if the current TranslatedToken matches the input string with a wildcard character (*).
     * If the input string ends with a wildcard character, it will be removed before matching.
     *
     * @param sTextWithWildcard   the input string with a wildcard character (*)
     * @param bConvertToLowerCase if true, the token will be converted to lower case before matching
     * @return true if the token matches the input string with a wildcard character (*), false otherwise
     */
    public boolean matchesStringWithWildcard(String sTextWithWildcard, boolean bConvertToLowerCase) {
        int iStarPos = sTextWithWildcard.lastIndexOf("*");
        if (iStarPos >= 0 && iStarPos == sTextWithWildcard.length() - 1) {
            sTextWithWildcard = sTextWithWildcard.substring(0, iStarPos);
            if (bConvertToLowerCase) {
                if (this.sgLCaseWord.length() == 0) {
                    this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
                }

                if (sTextWithWildcard.equals(this.sgLCaseWord)) {
                    return true;
                }

                if (sTextWithWildcard.length() >= this.sgLCaseWord.length()) {
                    return false;
                }

                if (sTextWithWildcard.equals(this.sgLCaseWord.substring(0, sTextWithWildcard.length()))) {
                    return true;
                }
            } else {
                if (sTextWithWildcard.equals(this.sgTranslatedWord)) {
                    return true;
                }

                if (sTextWithWildcard.length() >= this.sgTranslatedWord.length()) {
                    return false;
                }

                if (sTextWithWildcard.equals(
                        this.sgTranslatedWord.substring(0, sTextWithWildcard.length()))) {
                    return true;
                }
            }

            return false;
        } else {
            return this.matchesString(sTextWithWildcard, bConvertToLowerCase);
        }
    }

    /**
     * This method takes a word as input, processes it according to certain options,
     * and generates an output in the form of a translated word.
     * The generated translated word is then stored in the class variable 'sgTranslatedWord'.
     * If the option 'bgCorrectExtraLetterSpellingErrors' is true,
     * this method corrects extra letter spelling errors in the input word by
     * removing the extra letters and storing the removed letters in 'sgWordEmphasis' variable.
     * The method then sets the 'igContentType' variable to 1 and stores the input word in 'sgOriginalWord' variable.
     * If the translated word does not contain the '@' symbol and
     * the option 'bgCorrectSpellingsUsingDictionary' is true, the method
     * corrects spelling errors in the translated word by using a dictionary.
     * If the option 'bgUseLemmatisation' is true, the method lemmatises the translated word using a lemmatiser.
     *
     * @param sWord the input word to be translated
     */
    private void codeWord(String sWord) {
        StringBuilder sWordNew = new StringBuilder();
        StringBuilder sEm = new StringBuilder();
        if (this.options.bgCorrectExtraLetterSpellingErrors) {
            int iSameCount = 0;
            int iLastCopiedPos = 0;
            int iWordEnd = sWord.length() - 1;

            int iPos;
            for (iPos = 1; iPos <= iWordEnd; ++iPos) {
                if (sWord.substring(iPos, iPos + 1).compareToIgnoreCase(sWord.substring(iPos - 1, iPos)) ==
                        0) {
                    ++iSameCount;
                } else {
                    if (iSameCount > 0 && this.options.sgIllegalDoubleLettersInWordMiddle.contains(
                            sWord.substring(iPos - 1, iPos))) {
                        ++iSameCount;
                    }

                    if (iSameCount > 1) {
                        if (sEm.toString().equals("")) {
                            sWordNew = new StringBuilder(sWord.substring(0, iPos - iSameCount + 1));
                            sEm = new StringBuilder(sWord.substring(iPos - iSameCount, iPos - 1));
                            iLastCopiedPos = iPos;
                        } else {
                            sWordNew.append(sWord, iLastCopiedPos, iPos - iSameCount + 1);
                            sEm.append(sWord, iPos - iSameCount, iPos - 1);
                            iLastCopiedPos = iPos;
                        }
                    }

                    iSameCount = 0;
                }
            }

            if (iSameCount > 0 &&
                    this.options.sgIllegalDoubleLettersAtWordEnd.contains(sWord.substring(iPos - 1, iPos))) {
                ++iSameCount;
            }

            if (iSameCount > 1) {
                if (sEm.toString().equals("")) {
                    sWordNew = new StringBuilder(sWord.substring(0, iPos - iSameCount + 1));
                    sEm = new StringBuilder(sWord.substring(iPos - iSameCount + 1));
                } else {
                    sWordNew.append(sWord, iLastCopiedPos, iPos - iSameCount + 1);
                    sEm.append(sWord.substring(iPos - iSameCount + 1));
                }
            } else if (!sEm.toString().equals("")) {
                sWordNew.append(sWord.substring(iLastCopiedPos));
            }
        }

        if (sWordNew.toString().equals("")) {
            sWordNew = new StringBuilder(sWord);
        }

        this.igContentType = 1;
        this.sgOriginalWord = sWord;
        this.sgWordEmphasis = new StringBuilder(sEm.toString());
        this.sgTranslatedWord = sWordNew.toString();
        if (!this.sgTranslatedWord.contains("@")) {
            if (this.options.bgCorrectSpellingsUsingDictionary) {
                this.correctSpellingInTranslatedWord();
            }

            if (this.options.bgUseLemmatisation) {
                if (this.sgTranslatedWord.equals("")) {
                    sWordNew = new StringBuilder(this.resources.lemmatiser.lemmatise(this.sgOriginalWord));
                    if (!sWordNew.toString().equals(this.sgOriginalWord)) {
                        this.sgTranslatedWord = sWordNew.toString();
                    }
                } else {
                    this.sgTranslatedWord = this.resources.lemmatiser.lemmatise(this.sgTranslatedWord);
                }
            }
        }

    }

    /**
     * Corrects spelling errors in the translated word using a resource of correct spellings.
     * If the translated word is not spelled correctly, the method will attempt to correct
     * double-letter spelling errors by replacing the repeated letter with a single letter.
     * If the corrected word is in the resource of correct spellings, it will be used as the
     * new translated word. If not, the method will attempt to correct the spelling of the
     * word using other methods. If the word is still not correctly spelled after these attempts,
     * it will remain unchanged.
     */
    private void correctSpellingInTranslatedWord() {
        if (!this.resources.correctSpellings.correctSpelling(this.sgTranslatedWord.toLowerCase())) {
            int iLastChar = this.sgTranslatedWord.length() - 1;

            for (int iPos = 1; iPos <= iLastChar; ++iPos) {
                if (this.sgTranslatedWord.substring(iPos, iPos + 1)
                        .compareTo(this.sgTranslatedWord.substring(iPos - 1, iPos)) == 0) {
                    String sReplaceWord =
                            this.sgTranslatedWord.substring(0, iPos) + this.sgTranslatedWord.substring(iPos + 1);
                    if (this.resources.correctSpellings.correctSpelling(sReplaceWord.toLowerCase())) {
                        this.sgWordEmphasis.append(this.sgTranslatedWord.charAt(iPos));
                        this.sgTranslatedWord = sReplaceWord;
                        return;
                    }
                }
            }

            if (iLastChar > 5) {
                if (this.sgTranslatedWord.indexOf("haha") > 0) {
                    this.sgWordEmphasis.append(
                            this.sgTranslatedWord, 3, this.sgTranslatedWord.indexOf("haha") + 2);
                    this.sgTranslatedWord = "haha";
                    return;
                }

                if (this.sgTranslatedWord.indexOf("hehe") > 0) {
                    this.sgWordEmphasis.append(
                            this.sgTranslatedWord, 3, this.sgTranslatedWord.indexOf("hehe") + 2);
                    this.sgTranslatedWord = "hehe";
                    return;
                }
            }

        }
    }

    /**
     * Check if the given string is a valid emoticon code and set the corresponding properties if so.
     *
     * @param sPossibleEmoticon the string to check if it's an emoticon code
     * @return true if the given string is a valid emoticon code, false otherwise
     */
    private boolean codeEmoticon(String sPossibleEmoticon) {
        int iEmoticonStrength = this.resources.emoticons.getEmoticon(sPossibleEmoticon);
        if (iEmoticonStrength != 999) {
            this.igContentType = 3;
            this.sgEmoticon = sPossibleEmoticon;
            this.igEmoticonStrength = iEmoticonStrength;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Codes a punctuation symbol and sets the appropriate attributes of the object.
     *
     * @param sPunctuation the punctuation symbol to be coded
     */
    private void codePunctuation(String sPunctuation) {
        if (sPunctuation.length() > 1) {
            this.sgPunctuation = sPunctuation.substring(0, 1);
            this.sgPunctuationEmphasis = sPunctuation.substring(1);
        } else {
            this.sgPunctuation = sPunctuation;
            this.sgPunctuationEmphasis = "";
        }

        this.igContentType = 2;
    }
}
