//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package nju.SEIII.EASIEST.SentiStrength;

import java.io.UnsupportedEncodingException;
import nju.SEIII.EASIEST.Utilities.Sort;
import nju.SEIII.EASIEST.Utilities.StringIndex;
import nju.SEIII.EASIEST.WekaClass.Arff;

/**
 * The Sentence class represents a sentence that has been parsed and classified for sentiment analysis.
 * It contains an array of Term objects representing the individual words in the sentence, along with
 * other information such as the sentiment score of the sentence, whether any idioms or object evaluations
 * have been applied to the term strengths, and the classification resources and options used to parse and
 * classify the sentence.
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
 * <li>UC-29 Machine learning evaluations
 * </ul></p>
 */
public class Sentence {
  private Term[] term;
  private boolean[] bgSpaceAfterTerm;
  private int igTermCount = 0;
  private int igSentiCount = 0;
  private int igPositiveSentiment = 0;
  private int igNegativeSentiment = 0;
  private boolean bgNothingToClassify = true;
  private ClassificationResources resources;
  private ClassificationOptions options;
  private int[] igSentimentIDList;
  private int igSentimentIDListCount = 0;
  private boolean bSentimentIDListMade = false;
  private boolean[] bgIncludeTerm;
  private boolean bgIdiomsApplied = false;
  private boolean bgObjectEvaluationsApplied = false;
  private StringBuilder sgClassificationRationale = new StringBuilder();

  public Sentence() {
  }

  public int getIgTermCount() {
    return igTermCount;
  }

  public int getIgSentiCount() {
    return igSentiCount;
  }

  /**
   * Adds each term's in this sentence to UnusedTermsClassificationIndex.
   *
   * @param unusedTermClassificationIndex The UnusedTermsClassificationIndex object to add the terms to
   */
  public void addSentenceToIndex(UnusedTermsClassificationIndex unusedTermClassificationIndex) {
    for (int i = 1; i <= this.igTermCount; ++i) {
      unusedTermClassificationIndex.addTermToNewTermIndex(this.term[i].getText());
    }

  }

  /**
   * Adds strings to a StringIndex object based on text parsing options and other options.
   *
   * @param stringIndex        The StringIndex object to add strings to
   * @param textParsingOptions The TextParsingOptions object to use for parsing the text
   * @param bRecordCount       A boolean indicating whether to record the count of each string added
   * @param bArffIndex         A boolean indicating whether to use ARFF safe encoding for the strings
   * @return The number of terms checked
   */
  public int addToStringIndex(StringIndex stringIndex, TextParsingOptions textParsingOptions,
                              boolean bRecordCount, boolean bArffIndex)
      throws UnsupportedEncodingException {
    String sEncoded;
    int iStringPos;
    int iTermsChecked = 0;

    // Don't need special processing
    if (textParsingOptions.bgIncludePunctuation && textParsingOptions.igNgramSize == 1 &&
        !textParsingOptions.bgUseTranslations && !textParsingOptions.bgAddEmphasisCode) {
      for (int i = 1; i <= this.igTermCount; ++i) {
        stringIndex.addString(this.term[i].getText(), bRecordCount);
      }

      // Update checked term
      iTermsChecked = this.igTermCount;
    } else {
      StringBuilder sText = new StringBuilder();
      int iCurrentTerm = 0;
      int iTermCount = 0;

      while (iCurrentTerm < this.igTermCount) {
        ++iCurrentTerm;
        // Check if punctuation should be included or if term is not punctuation
        if (textParsingOptions.bgIncludePunctuation || !this.term[iCurrentTerm].isPunctuation()) {
          ++iTermCount;
          // Add space if not first term in sequence, otherwise reset StringBuilder
          if (iTermCount > 1) {
            sText.append(" ");
          } else {
            sText = new StringBuilder();
          }

          // Append term text or translation to StringBuilder depending on options
          if (textParsingOptions.bgUseTranslations) {
            sText.append(this.term[iCurrentTerm].getTranslation());
          } else {
            sText.append(this.term[iCurrentTerm].getOriginalText());
          }

          // Append emphasis code if enabled and term contains emphasis
          if (textParsingOptions.bgAddEmphasisCode && this.term[iCurrentTerm].containsEmphasis()) {
            sText.append("+");
          }
        }

        if (iTermCount == textParsingOptions.igNgramSize) {
          // Check if the number of terms in sequence equals the desired n-gram size
          if (bArffIndex) {
            // If using Arff format, encode and find index of n-gram in string index
            sEncoded = Arff.arffSafeWordEncode(sText.toString().toLowerCase(), false);
            iStringPos = stringIndex.findString(sEncoded);
            iTermCount = 0;
            // If n-gram exists in string index, increment count, otherwise add to string index
            if (iStringPos > -1) {
              stringIndex.add1ToCount(iStringPos);
            }
          } else {
            // Add n-gram to string index
            stringIndex.addString(sText.toString().toLowerCase(), bRecordCount);
            iTermCount = 0;
          }
          // Skip over terms already included in n-gram
          iCurrentTerm += 1 - textParsingOptions.igNgramSize;
          ++iTermsChecked;
        }
      }
    }

    return iTermsChecked;
  }

  /**
   * Sets the sentence to be processed by the classifier and extracts the individual terms and punctuation marks from it.
   *
   * @param sSentence                The input sentence to be processed.
   * @param classResources           The resources required for classification.
   * @param newClassificationOptions The new classification options to be used for classification.
   */
  public void setSentence(String sSentence, ClassificationResources classResources,
                          ClassificationOptions newClassificationOptions) {
    // Set the classification resources and options.
    this.resources = classResources;
    this.options = newClassificationOptions;
    // Replace apostrophes with spaces if bgAlwaysSplitWordsAtApostrophes option is enabled.
    if (this.options.bgAlwaysSplitWordsAtApostrophes && sSentence.contains("'")) {
      sSentence = sSentence.replace("'", " ");
    }

    String[] sSegmentList = sSentence.split(" ");
    int iSegmentListLength = sSegmentList.length;
    int iMaxTermListLength = sSentence.length() + 1;
    this.term = new Term[iMaxTermListLength];
    // Create an array to hold the boolean values for whether there is a space after each term.
    this.bgSpaceAfterTerm = new boolean[iMaxTermListLength];
    int iPos;
    this.igTermCount = 0;

    for (String s : sSegmentList) {
      // The extracted word is added to the term array.
      for (iPos = 0; iPos >= 0 && iPos < s.length();
           this.bgSpaceAfterTerm[this.igTermCount] = false) {
        this.term[++this.igTermCount] = new Term();
        // Extract the next word, punctuation or emoticon from the segment.
        int iOffset = this.term[this.igTermCount].extractNextWordOrPunctuationOrEmoticon(
            s.substring(iPos), this.resources, this.options);
        // If no word is extracted, set iPos negative.
        if (iOffset < 0) {
          iPos = iOffset;
        } else {
          iPos += iOffset;
        }
      }

      this.bgSpaceAfterTerm[this.igTermCount] = true;
    }

    this.bgSpaceAfterTerm[this.igTermCount] = false;
  }

  /**
   * Returns an array of integers representing the sentiment ID list of this object.
   * If the sentiment ID list has not been generated yet, it will be generated before being returned.
   *
   * @return An array of integers representing the sentiment ID list of this object.
   */
  public int[] getSentimentIDList() {
    if (!this.bSentimentIDListMade) {
      this.makeSentimentIDList();
    }

    return this.igSentimentIDList;
  }

  /**
   * This method creates a list of sentiment IDs based on the terms in the sentence.
   * It first counts the number of terms with non-zero sentiment IDs, and then creates an array to hold
   * the unique sentiment IDs found. It then iterates over the terms again, checking each one for a non-zero sentiment ID.
   * If a unique sentiment ID is found, it is added to the list. Finally, the list is sorted and the flag for whether
   * the sentiment ID list has been made is set to true.
   */
  public void makeSentimentIDList() {
    int iSentimentIDTemp;
    this.igSentimentIDListCount = 0;

    // Count the number of terms with non-zero sentiment IDs
    int i;
    for (i = 1; i <= this.igTermCount; ++i) {
      if (this.term[i].getSentimentID() > 0) {
        ++this.igSentimentIDListCount;
      }
    }

    if (this.igSentimentIDListCount > 0) {
      // Create an array to hold the unique sentiment IDs found
      this.igSentimentIDList = new int[this.igSentimentIDListCount + 1];
      this.igSentimentIDListCount = 0;

      for (i = 1; i <= this.igTermCount; ++i) {
        iSentimentIDTemp = this.term[i].getSentimentID();
        if (iSentimentIDTemp > 0) {
          // Check if the sentiment ID has already been added to the list
          for (int j = 1; j <= this.igSentimentIDListCount; ++j) {
            if (iSentimentIDTemp == this.igSentimentIDList[j]) {
              iSentimentIDTemp = 0;
              break;
            }
          }

          // If it is a unique sentiment ID, add it to the list
          if (iSentimentIDTemp > 0) {
            this.igSentimentIDList[++this.igSentimentIDListCount] = iSentimentIDTemp;
          }
        }
      }
      // Sort the list of sentiment IDs
      Sort.quickSortInt(this.igSentimentIDList, 1, this.igSentimentIDListCount);
    }

    this.bSentimentIDListMade = true;
  }

  /**
   * Returns a string containing tagged sentences.
   *
   * @return String tagged sentences.
   */
  public String getTaggedSentence() {
    StringBuilder sTagged = new StringBuilder();

    for (int i = 1; i <= this.igTermCount; ++i) {
      if (this.bgSpaceAfterTerm[i]) {
        sTagged.append(this.term[i].getTag()).append(" ");
      } else {
        sTagged.append(this.term[i].getTag());
      }
    }

    return sTagged + "<br>";
  }

  /**
   * Returns the classification rationale.
   *
   * @return String classification rationale.
   */
  public String getClassificationRationale() {
    return this.sgClassificationRationale.toString();
  }

  /**
   * Returns a translated sentence.
   *
   * @return String translated sentence.
   */
  public String getTranslatedSentence() {
    StringBuilder sTranslated = new StringBuilder();

    for (int i = 1; i <= this.igTermCount; ++i) {
      if (this.term[i].isWord()) {
        sTranslated.append(this.term[i].getTranslatedWord());
      } else if (this.term[i].isPunctuation()) {
        sTranslated.append(this.term[i].getTranslatedPunctuation());
      } else if (this.term[i].isEmoticon()) {
        sTranslated.append(this.term[i].getEmoticon());
      }

      if (this.bgSpaceAfterTerm[i]) {
        sTranslated.append(" ");
      }
    }

    return sTranslated + "<br>";
  }

  /**
   * Recalculates the sentence sentiment score.
   * This method calls the "calculateSentenceSentimentScore" method to recalculate
   * the sentence sentiment score.
   */
  public void recalculateSentenceSentimentScore() {
    this.calculateSentenceSentimentScore();
  }

  /**
   * Re-classifies a classified sentence for sentiment change.
   * This method checks if the specified sentiment word ID is in the sentence's
   * sentiment ID list. If it is, the method calls the "calculateSentenceSentimentScore"
   * method to re-calculate the sentence sentiment score. If the sentence's negative
   * sentiment count is zero, the method also calls the "calculateSentenceSentimentScore"
   * method to calculate the sentence sentiment score.
   *
   * @param iSentimentWordID the sentiment word ID to check
   */
  public void reClassifyClassifiedSentenceForSentimentChange(int iSentimentWordID) {
    if (this.igNegativeSentiment == 0) {
      this.calculateSentenceSentimentScore();
    } else {
      if (!this.bSentimentIDListMade) {
        this.makeSentimentIDList();
      }

      if (this.igSentimentIDListCount != 0 &&
          Sort.i_FindIntPositionInSortedArray(iSentimentWordID, this.igSentimentIDList, 1,
              this.igSentimentIDListCount) >= 0) {
        this.calculateSentenceSentimentScore();
      }
    }
  }

  /**
   * Returns the positive sentiment count of the sentence.
   * This method checks if the positive sentiment count of the sentence is zero.
   * If it is, the method calls the "calculateSentenceSentimentScore" method to calculate
   * the sentence sentiment score. The method then returns the positive sentiment count
   * of the sentence.
   *
   * @return the positive sentiment count of the sentence
   */
  public int getSentencePositiveSentiment() {
    if (this.igPositiveSentiment == 0) {
      this.calculateSentenceSentimentScore();
    }

    return this.igPositiveSentiment;
  }

  /**
   * Returns the negative sentiment count of the sentence.
   * This method checks if the negative sentiment count of the sentence is zero.
   * If it is, the method calls the "calculateSentenceSentimentScore" method to calculate
   * the sentence sentiment score. The method then returns the negative sentiment count
   * of the sentence.
   *
   * @return the negative sentiment count of the sentence
   */
  public int getSentenceNegativeSentiment() {
    if (this.igNegativeSentiment == 0) {
      this.calculateSentenceSentimentScore();
    }

    return this.igNegativeSentiment;
  }

  /**
   * bgIncludeTerm[] represents whether a term is valid.
   * This method marks the terms in the sentence that are valid for sentiment classification.
   * If the option to ignore sentences without keywords is set,
   * it sets a term valid if it belongs to the keyword,
   * as well as terms within certain distance(igWordsToIncludeAfterKeyword) from the keywords;
   * Otherwise, it set all the terms valid
   */
  private void markTermsValidToClassify() {
    this.bgIncludeTerm = new boolean[this.igTermCount + 1];
    int iTermsSinceValid;
    if (this.options.bgIgnoreSentencesWithoutKeywords) {
      this.bgNothingToClassify = true;

      // If a term is a keyword, set the term valid
      int iTerm;
      // Iterate through all the terms
      for (iTermsSinceValid = 1; iTermsSinceValid <= this.igTermCount; ++iTermsSinceValid) {
        this.bgIncludeTerm[iTermsSinceValid] = false;
        if (this.term[iTermsSinceValid].isWord()) {
          for (iTerm = 0; iTerm < this.options.sgSentimentKeyWords.length; ++iTerm) {
            // If the term belongs to the keyword
            if (this.term[iTermsSinceValid].matchesString(this.options.sgSentimentKeyWords[iTerm],
                true)) {
              this.bgIncludeTerm[iTermsSinceValid] = true;
              this.bgNothingToClassify = false;
            }
          }
        }
      }

      // If the sentence has keywords, set the words within certain distance
      // (igWordsToIncludeAfterKeyword) from keywords valid
      if (!this.bgNothingToClassify) {
        iTermsSinceValid = 100000;

        // After the keywords
        for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
          if (this.bgIncludeTerm[iTerm]) {
            iTermsSinceValid = 0;
          } else if (iTermsSinceValid < this.options.igWordsToIncludeAfterKeyword) {
            this.bgIncludeTerm[iTerm] = true;
            if (this.term[iTerm].isWord()) {
              ++iTermsSinceValid;
            }
          }
        }

        iTermsSinceValid = 100000;

        // Before the keywords
        for (iTerm = this.igTermCount; iTerm >= 1; --iTerm) {
          if (this.bgIncludeTerm[iTerm]) {
            iTermsSinceValid = 0;
          } else if (iTermsSinceValid < this.options.igWordsToIncludeBeforeKeyword) {
            this.bgIncludeTerm[iTerm] = true;
            if (this.term[iTerm].isWord()) {
              ++iTermsSinceValid;
            }
          }
        }
      }
    } else {
      for (iTermsSinceValid = 1; iTermsSinceValid <= this.igTermCount; ++iTermsSinceValid) {
        this.bgIncludeTerm[iTermsSinceValid] = true;
      }

      this.bgNothingToClassify = false;
    }

  }

  /**
   * Private method to calculate the sentiment score of a sentence.
   */
  private void calculateSentenceSentimentScore() {
    if (this.options.bgExplainClassification && this.sgClassificationRationale.length() > 0) {
      this.sgClassificationRationale = new StringBuilder();
    }

    this.igNegativeSentiment = 1;
    this.igPositiveSentiment = 1;
    int iWordTotal = 0;
    int iLastBoosterWordScore = 0;
    int iTemp = 0;
    // Need to check whether it has terms to classify first
    // since method markTermsValidToClassify() access term list
    if (this.igTermCount == 0) {
      // No terms to classify
      this.bgNothingToClassify = true;
      this.igNegativeSentiment = -1;
      this.igPositiveSentiment = 1;
    } else {
      // Has terms to classify
      this.markTermsValidToClassify();
      if (this.bgNothingToClassify) {
        this.igNegativeSentiment = -1;
        this.igPositiveSentiment = 1;
      } else {
        // indicate whether there is punctuation to boost the sentiment score
        boolean bSentencePunctuationBoost = false;
        // distance between negate word and sentiment word
        int iWordsSinceNegative = this.options.igMaxWordsBeforeSentimentToNegate + 2;
        float[] fSentiment = new float[this.igTermCount + 1];

        // override term strength with idiom strength
        if (this.options.bgUseIdiomLookupTable) {
          this.overrideTermStrengthsWithIdiomStrengths(false);
        }

        // override term strength with object evaluation strength
        if (this.options.bgUseObjectEvaluationTable) {
          this.overrideTermStrengthsWithObjectEvaluationStrengths(false);
        }

        // Caculate sentiment depending on type and all kinds of rules
        for (int iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
          if (this.bgIncludeTerm[iTerm]) {
            int iTermsChecked;

            if (this.term[iTerm].isEmoticon()) {
              // If the term is emoticon
              iTermsChecked = this.term[iTerm].getEmoticonSentimentStrength();
              if (iTermsChecked != 0) {
                if (iWordTotal > 0) {
                  // If there are previous words, add the sentiment score to the word before
                  fSentiment[iWordTotal] +=
                      this.term[iTerm].getEmoticonSentimentStrength();
                  if (this.options.bgExplainClassification) {
                    this.sgClassificationRationale.append(this.term[iTerm].getEmoticon())
                        .append(" [").append(this.term[iTerm].getEmoticonSentimentStrength())
                        .append(" emoticon] ");
                  }
                } else {
                  // if there is no words, store the sentiment score in a new space
                  ++iWordTotal;
                  fSentiment[iWordTotal] = iTermsChecked;
                  if (this.options.bgExplainClassification) {
                    this.sgClassificationRationale.append(this.term[iTerm].getEmoticon())
                        .append(" [").append(this.term[iTerm].getEmoticonSentimentStrength())
                        .append(" emoticon]");
                  }
                }
              }
            } else if (this.term[iTerm].isPunctuation()) {
              // If the term is punctuation,
              // Punctuation long enough && punctuation contains "!" && has word before
              if (this.term[iTerm].getPunctuationEmphasisLength() >=
                  this.options.igMinPunctuationWithExclamationToChangeSentenceSentiment &&
                  this.term[iTerm].punctuationContains("!") && iWordTotal > 0) {
                bSentencePunctuationBoost = true;
              }
              if (this.options.bgExplainClassification) {
                this.sgClassificationRationale.append(this.term[iTerm].getOriginalText());
              }

            } else if (this.term[iTerm].isWord()) {
              // If the term is a word, get the sentiment score
              ++iWordTotal;
              // first term || not a proper noun || previous term is ":" || previous term like "@*"
              if (iTerm == 1 || !this.term[iTerm].isProperNoun() ||
                  this.term[iTerm - 1].getOriginalText().equals(":") ||
                  this.term[iTerm - 1].getOriginalText().length() > 3 &&
                      this.term[iTerm - 1].getOriginalText().charAt(0) == '@') {
                fSentiment[iWordTotal] = this.term[iTerm].getSentimentValue();

                // explain the term
                if (this.options.bgExplainClassification) {
                  iTemp = this.term[iTerm].getSentimentValue();
                  if (iTemp < 0) {
                    --iTemp;
                  } else {
                    ++iTemp;
                  }

                  if (iTemp == 1) {
                    this.sgClassificationRationale.append(this.term[iTerm].getOriginalText())
                        .append(" ");
                  } else {
                    this.sgClassificationRationale.append(this.term[iTerm].getOriginalText())
                        .append("[").append(iTemp).append("] ");
                  }
                }
              } else if (this.options.bgExplainClassification) {
                this.sgClassificationRationale.append(this.term[iTerm].getOriginalText())
                    .append(" [proper noun] ");
              }

              // Multiple letters in a term boost sentiment
              if (this.options.bgMultipleLettersBoostSentiment &&
                  this.term[iTerm].getWordEmphasisLength() >=
                      this.options.igMinRepeatedLettersForBoost &&
                  (iTerm == 1 || !this.term[iTerm - 1].isPunctuation() ||
                      !this.term[iTerm - 1].getOriginalText().equals("@"))) {
                String sEmphasis = this.term[iTerm].getWordEmphasis().toLowerCase();
                if (!sEmphasis.contains("xx") && !sEmphasis.contains("ww") &&
                    !sEmphasis.contains("ha")) {
                  if (fSentiment[iWordTotal] < 0.0F) {
                    fSentiment[iWordTotal] = (float) (fSentiment[iWordTotal] - 0.6D);
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[-0.6 spelling emphasis] ");
                    }
                  } else if (fSentiment[iWordTotal] > 0.0F) {
                    fSentiment[iWordTotal] = (float) ((double) fSentiment[iWordTotal] + 0.6D);
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[+0.6 spelling emphasis] ");
                    }
                  } else if (this.options.igMoodToInterpretNeutralEmphasis > 0) {
                    fSentiment[iWordTotal] = (float) (fSentiment[iWordTotal] + 0.6D);
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[+0.6 spelling mood emphasis] ");
                    }
                  } else if (this.options.igMoodToInterpretNeutralEmphasis < 0) {
                    fSentiment[iWordTotal] = (float) (fSentiment[iWordTotal] - 0.6D);
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[-0.6 spelling mood emphasis] ");
                    }
                  }
                }
              }

              // Term with all capitals boost the sentiment
              int var10002;
              if (this.options.bgCapitalsBoostTermSentiment && fSentiment[iWordTotal] != 0.0F &&
                  this.term[iTerm].isAllCapitals()) {
                if (fSentiment[iWordTotal] > 0.0F) {
                  var10002 = (int) fSentiment[iWordTotal]++;
                  if (this.options.bgExplainClassification) {
                    this.sgClassificationRationale.append("[+1 CAPITALS] ");
                  }
                } else {
                  var10002 = (int) fSentiment[iWordTotal]--;
                  if (this.options.bgExplainClassification) {
                    this.sgClassificationRationale.append("[-1 CAPITALS] ");
                  }
                }
              }

              // Add the last word's booster word score to the current word
              if (this.options.bgBoosterWordsChangeEmotion) {
                if (iLastBoosterWordScore != 0) {
                  if (fSentiment[iWordTotal] > 0.0F) {
                    fSentiment[iWordTotal] += iLastBoosterWordScore;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[+").append(iLastBoosterWordScore)
                          .append(" booster word] ");
                    }
                  } else if (fSentiment[iWordTotal] < 0.0F) {
                    fSentiment[iWordTotal] -= iLastBoosterWordScore;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[-").append(iLastBoosterWordScore)
                          .append(" booster word] ");
                    }
                  }
                }

                iLastBoosterWordScore = this.term[iTerm].getBoosterWordScore();
              }

              // Negating word influence emotion
              if (this.options.bgNegatingWordsOccurBeforeSentiment) {
                // Negating word flip emotion
                if (this.options.bgNegatingWordsFlipEmotion) {
                  if (iWordsSinceNegative <= this.options.igMaxWordsBeforeSentimentToNegate) {
                    fSentiment[iWordTotal] =
                        -fSentiment[iWordTotal] * this.options.fgStrengthMultiplierForNegatedWords;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[*-")
                          .append(this.options.fgStrengthMultiplierForNegatedWords)
                          .append(" approx. negated multiplier] ");
                    }
                  }
                } else {
                  // Negating negative word neutralize emotion
                  if (this.options.bgNegatingNegativeNeutralisesEmotion &&
                      fSentiment[iWordTotal] < 0.0F &&
                      iWordsSinceNegative <= this.options.igMaxWordsBeforeSentimentToNegate) {
                    fSentiment[iWordTotal] = 0.0F;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[=0 negation] ");
                    }
                  }

                  // Negating positive word flip emotion
                  if (this.options.bgNegatingPositiveFlipsEmotion &&
                      fSentiment[iWordTotal] > 0.0F &&
                      iWordsSinceNegative <= this.options.igMaxWordsBeforeSentimentToNegate) {
                    fSentiment[iWordTotal] =
                        -fSentiment[iWordTotal] * this.options.fgStrengthMultiplierForNegatedWords;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[*-")
                          .append(this.options.fgStrengthMultiplierForNegatedWords)
                          .append(" approx. negated multiplier] ");
                    }
                  }
                }
              }

              if (this.term[iTerm].isNegatingWord()) {
                iWordsSinceNegative = -1;
              }

              if (iLastBoosterWordScore == 0) {
                ++iWordsSinceNegative;
              }

              // Negating word influence prior words
              if (this.term[iTerm].isNegatingWord() &&
                  this.options.bgNegatingWordsOccurAfterSentiment) {
                iTermsChecked = 0;

                for (int iPriorWord = iWordTotal - 1; iPriorWord > 0; --iPriorWord) {
                  if (this.options.bgNegatingWordsFlipEmotion) {
                    fSentiment[iPriorWord] =
                        -fSentiment[iPriorWord] * this.options.fgStrengthMultiplierForNegatedWords;
                    if (this.options.bgExplainClassification) {
                      this.sgClassificationRationale.append("[*-")
                          .append(this.options.fgStrengthMultiplierForNegatedWords)
                          .append(" approx. negated multiplier] ");
                    }
                  } else {
                    if (this.options.bgNegatingNegativeNeutralisesEmotion &&
                        fSentiment[iPriorWord] < 0.0F) {
                      fSentiment[iPriorWord] = 0.0F;
                      if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale.append("[=0 negation] ");
                      }
                    }

                    if (this.options.bgNegatingPositiveFlipsEmotion &&
                        fSentiment[iPriorWord] > 0.0F) {
                      fSentiment[iPriorWord] = -fSentiment[iPriorWord] *
                          this.options.fgStrengthMultiplierForNegatedWords;
                      if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale.append("[*-")
                            .append(this.options.fgStrengthMultiplierForNegatedWords)
                            .append(" approx. negated multiplier] ");
                      }
                    }
                  }

                  ++iTermsChecked;
                  if (iTermsChecked > this.options.igMaxWordsAfterSentimentToNegate) {
                    break;
                  }
                }
              }

              // Multiple negative words boost sentiment score
              if (this.options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion &&
                  fSentiment[iWordTotal] < -1.0F && iWordTotal > 1 &&
                  fSentiment[iWordTotal - 1] < -1.0F) {
                var10002 = (int) fSentiment[iWordTotal]--;
                if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale.append("[-1 consecutive negative words] ");
                }
              }

              // Multiple positive words boost sentiment score
              if (this.options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion &&
                  fSentiment[iWordTotal] > 1.0F && iWordTotal > 1 &&
                  fSentiment[iWordTotal - 1] > 1.0F) {
                var10002 = (int) fSentiment[iWordTotal]++;
                if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale.append("[+1 consecutive positive words] ");
                }
              }

            }
          }
        }

        float fTotalNeg = 0.0F;
        float fTotalPos = 0.0F;
        float fMaxNeg = 0.0F;
        float fMaxPos = 0.0F;
        int iPosWords = 0;
        int iNegWords = 0;

        // Calculate max and total score
        int iTerm;
        for (iTerm = 1; iTerm <= iWordTotal; ++iTerm) {
          if (fSentiment[iTerm] < 0.0F) {
            fTotalNeg += fSentiment[iTerm];
            ++iNegWords;
            if (fMaxNeg > fSentiment[iTerm]) {
              fMaxNeg = fSentiment[iTerm];
            }
          } else if (fSentiment[iTerm] > 0.0F) {
            fTotalPos += fSentiment[iTerm];
            ++iPosWords;
            if (fMaxPos < fSentiment[iTerm]) {
              fMaxPos = fSentiment[iTerm];
            }
          }
        }
        igSentiCount = iNegWords + iPosWords;
        --fMaxNeg;
        ++fMaxPos;

        // Calculate positive and negative score based on the method
        if (this.options.igEmotionSentenceCombineMethod == 1) {
          if (iPosWords == 0) {
            this.igPositiveSentiment = 1;
          } else {
            this.igPositiveSentiment = (int) Math.round(
                ((fTotalPos + iPosWords) + 0.45D) / iPosWords);
          }

          if (iNegWords == 0) {
            this.igNegativeSentiment = -1;
          } else {
            this.igNegativeSentiment = (int) Math.round(
                ((fTotalNeg - iNegWords) + 0.55D) / iNegWords);
          }
        } else {
          if (this.options.igEmotionSentenceCombineMethod == 2) {
            this.igPositiveSentiment = Math.round(fTotalPos) + iPosWords;
            this.igNegativeSentiment = Math.round(fTotalNeg) - iNegWords;
          } else {
            this.igPositiveSentiment = Math.round(fMaxPos);
            this.igNegativeSentiment = Math.round(fMaxNeg);
          }
        }

        // Question word and question mark reduce negative emotion
        if (this.options.bgReduceNegativeEmotionInQuestionSentences &&
            this.igNegativeSentiment < -1) {
          for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
            if (this.term[iTerm].isWord()) {
              if (this.resources.questionWords.contains(
                  this.term[iTerm].getTranslatedWord().toLowerCase())) {
                ++this.igNegativeSentiment;
                if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale.append("[+1 negative for question word]");
                }
                break;
              }
            } else if (this.term[iTerm].isPunctuation() &&
                this.term[iTerm].punctuationContains("?")) {
              ++this.igNegativeSentiment;
              if (this.options.bgExplainClassification) {
                this.sgClassificationRationale.append("[+1 negative for question mark ?]");
              }
              break;
            }
          }
        }

        // Why check here? Isn't it better to put it in the word list?
        if (this.igPositiveSentiment == 1 && this.options.bgMissCountsAsPlus2) {
          for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
            if (this.term[iTerm].isWord() &&
                this.term[iTerm].getTranslatedWord().toLowerCase().compareTo("miss") == 0) {
              this.igPositiveSentiment = 2;
              if (this.options.bgExplainClassification) {
                this.sgClassificationRationale.append("[pos = 2 for term 'miss']");
              }
              break;
            }
          }
        }

        // ! influence sentiment
        if (bSentencePunctuationBoost) {
          if (this.igPositiveSentiment < -this.igNegativeSentiment) {
            --this.igNegativeSentiment;
            if (this.options.bgExplainClassification) {
              this.sgClassificationRationale.append("[-1 punctuation emphasis] ");
            }
          } else if (this.igPositiveSentiment > -this.igNegativeSentiment) {
            ++this.igPositiveSentiment;
            if (this.options.bgExplainClassification) {
              this.sgClassificationRationale.append("[+1 punctuation emphasis] ");
            }
          } else if (this.options.igMoodToInterpretNeutralEmphasis > 0) {
            ++this.igPositiveSentiment;
            if (this.options.bgExplainClassification) {
              this.sgClassificationRationale.append("[+1 punctuation mood emphasis] ");
            }
          } else if (this.options.igMoodToInterpretNeutralEmphasis < 0) {
            --this.igNegativeSentiment;
            if (this.options.bgExplainClassification) {
              this.sgClassificationRationale.append("[-1 punctuation mood emphasis] ");
            }
          }
        }

        // ! in neutral sentence count as plus 2
        if (this.igPositiveSentiment == 1 && this.igNegativeSentiment == -1 &&
            this.options.bgExclamationInNeutralSentenceCountsAsPlus2) {
          for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
            if (this.term[iTerm].isPunctuation() && this.term[iTerm].punctuationContains("!")) {
              this.igPositiveSentiment = 2;
              if (this.options.bgExplainClassification) {
                this.sgClassificationRationale.append("[pos = 2 for !]");
              }
              break;
            }
          }
        }

        // you/your/whats in neutral sentence count as plus 2
        if (this.igPositiveSentiment == 1 && this.igNegativeSentiment == -1 &&
            this.options.bgYouOrYourIsPlus2UnlessSentenceNegative) {
          for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
            if (this.term[iTerm].isWord()) {
              String sTranslatedWord = this.term[iTerm].getTranslatedWord().toLowerCase();
              if (sTranslatedWord.compareTo("you") == 0 || sTranslatedWord.compareTo("your") == 0 ||
                  sTranslatedWord.compareTo("whats") == 0) {
                this.igPositiveSentiment = 2;
                if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale.append("[pos = 2 for you/your/whats]");
                }
                break;
              }
            }
          }
        }

        this.adjustSentimentForIrony();
        if (this.options.igEmotionSentenceCombineMethod != 2) {
          if (this.igPositiveSentiment > 5) {
            this.igPositiveSentiment = 5;
          }

          if (this.igNegativeSentiment < -5) {
            this.igNegativeSentiment = -5;
          }
        }

        if (this.options.bgExplainClassification) {
          this.sgClassificationRationale.append("[sentence: ").append(this.igPositiveSentiment)
              .append(",").append(this.igNegativeSentiment).append("]");
        }

      }
    }
  }

  /**
   * Private method to adjust the sentiment score of a sentence based on the presence of irony.
   * If the positive sentiment score of the sentence is above a certain threshold, this method checks
   * for the presence of irony in the sentence by looking at various factors, such as the use of quotes,
   * exclamation points, and certain terms known to be associated with irony.
   * If irony is detected, this method adjusts the sentiment score of the sentence accordingly.
   * Note: This method assumes that the sentence object has already been initialized with the necessary
   * data, including the text of the sentence, the positive and negative sentiment scores, and the options
   * for detecting irony.
   */
  private void adjustSentimentForIrony() {
    int iTerm;
    if (this.igPositiveSentiment >= this.options.igMinSentencePosForQuotesIrony) {
      for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
        if (this.term[iTerm].isPunctuation() && this.term[iTerm].getText().indexOf(34) >= 0) {
          if (this.igNegativeSentiment > -this.igPositiveSentiment) {
            this.igNegativeSentiment = 1 - this.igPositiveSentiment;
          }

          this.igPositiveSentiment = 1;
          this.sgClassificationRationale.append("[Irony change: pos = 1, neg = ")
              .append(this.igNegativeSentiment).append("]");
          return;
        }
      }
    }

    if (this.igPositiveSentiment >= this.options.igMinSentencePosForPunctuationIrony) {
      for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
        if (this.term[iTerm].isPunctuation() && this.term[iTerm].punctuationContains("!") &&
            this.term[iTerm].getPunctuationEmphasisLength() > 0) {
          if (this.igNegativeSentiment > -this.igPositiveSentiment) {
            this.igNegativeSentiment = 1 - this.igPositiveSentiment;
          }

          this.igPositiveSentiment = 1;
          this.sgClassificationRationale.append("[Irony change: pos = 1, neg = ")
              .append(this.igNegativeSentiment).append("]");
          return;
        }
      }
    }

    if (this.igPositiveSentiment >= this.options.igMinSentencePosForTermsIrony) {
      for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
        if (this.resources.ironyList.contains(this.term[iTerm].getText())) {
          if (this.igNegativeSentiment > -this.igPositiveSentiment) {
            this.igNegativeSentiment = 1 - this.igPositiveSentiment;
          }

          this.igPositiveSentiment = 1;
          this.sgClassificationRationale.append("[Irony change: pos = 1, neg = ")
              .append(this.igNegativeSentiment).append("]");
          return;
        }
      }
    }

  }

  /**
   * Overrides the strength of terms with the strength of corresponding object evaluations.
   *
   * @param recalculateIfAlreadyDone If set to true, reapply the object evaluations even if they were already applied.
   */
  public void overrideTermStrengthsWithObjectEvaluationStrengths(boolean recalculateIfAlreadyDone) {
    boolean bMatchingObject;
    boolean bMatchingEvaluation;
    if (!this.bgObjectEvaluationsApplied || recalculateIfAlreadyDone) {
      for (int iObject = 1; iObject < this.resources.evaluativeTerms.igObjectEvaluationCount;
           ++iObject) {
        bMatchingObject = false;
        bMatchingEvaluation = false;

        int iTerm;
        for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
          if (this.term[iTerm].isWord() && this.term[iTerm].matchesStringWithWildcard(
              this.resources.evaluativeTerms.sgObject[iObject], true)) {
            bMatchingObject = true;
            break;
          }
        }

        if (bMatchingObject) {
          for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
            if (this.term[iTerm].isWord() && this.term[iTerm].matchesStringWithWildcard(
                this.resources.evaluativeTerms.sgObjectEvaluation[iObject], true)) {
              bMatchingEvaluation = true;
              break;
            }
          }
        }

        if (bMatchingEvaluation) {
          if (this.options.bgExplainClassification) {
            this.sgClassificationRationale.append("[term weight changed by object/evaluation]");
          }

          this.term[iTerm].setSentimentOverrideValue(
              this.resources.evaluativeTerms.igObjectEvaluationStrength[iObject]);
        }
      }

      this.bgObjectEvaluationsApplied = true;
    }

  }

  /**
   * This method overrides the sentiment strength of individual terms with the sentiment strength of the idiom
   * that they appear in.
   * If a term is part of multiple idioms, its sentiment strength will be overridden with the highest sentiment
   * strength of all idioms it appears in.
   *
   * @param recalculateIfAlreadyDone if true, recalculate the override even if it has already been done
   */
  public void overrideTermStrengthsWithIdiomStrengths(boolean recalculateIfAlreadyDone) {
    // Only apply the override if it hasn't already been done or if recalculateIfAlreadyDone is true
    if (!this.bgIdiomsApplied || recalculateIfAlreadyDone) {
      for (int iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
        // Only apply the override to actual words, not punctuation or emoticons
        if (this.term[iTerm].isWord()) {
          for (int iIdiom = 1; iIdiom <= this.resources.idiomList.igIdiomCount; ++iIdiom) {
            // Check if the current term plus the length of the idiom is within the bounds of the sentence
            if (iTerm + this.resources.idiomList.igIdiomWordCount[iIdiom] - 1 <= this.igTermCount) {
              boolean bMatchingIdiom = true;

              int iIdiomTerm;
              for (iIdiomTerm = 0; iIdiomTerm < this.resources.idiomList.igIdiomWordCount[iIdiom];
                   ++iIdiomTerm) {
                if (!this.term[iTerm + iIdiomTerm].matchesStringWithWildcard(
                    this.resources.idiomList.sgIdiomWords[iIdiom][iIdiomTerm], true)) {
                  bMatchingIdiom = false;
                  break;
                }
              }

              // If the term and subsequent terms match the idiom, override their sentiment strength
              if (bMatchingIdiom) {
                if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale.append("[term weight(s) changed by idiom ")
                      .append(this.resources.idiomList.getIdiom(iIdiom)).append("]");
                }

                this.term[iTerm].setSentimentOverrideValue(
                    this.resources.idiomList.igIdiomStrength[iIdiom]);

                for (iIdiomTerm = 1; iIdiomTerm < this.resources.idiomList.igIdiomWordCount[iIdiom];
                     ++iIdiomTerm) {
                  this.term[iTerm + iIdiomTerm].setSentimentOverrideValue(0);
                }
              }
            }
          }
        }
      }

      this.bgIdiomsApplied = true;
    }

  }
}
