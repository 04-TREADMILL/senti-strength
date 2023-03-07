package nju.SEIII.EASIEST.sentistrength;

import java.util.Random;

import nju.SEIII.EASIEST.utilities.Sort;
import nju.SEIII.EASIEST.utilities.StringIndex;


/**
 *  This class represents a paragraph and contains information about
 *  its sentences, sentiment, and classification options.
 *
 *  @UC
 *  <p><ul>
 *  <li>UC-11 Classify a single text
 *  <li>UC-12 Classify all lines of text in a file for sentiment [includes accuracy evaluations]
 *  <li>UC-13 Classify texts in a column within a file or folder
 *  <li>UC-14 Listen at a port for texts to classify
 *  <li>UC-15 Run interactively from the command line
 *  <li>UC-17 Location of linguistic data folder
 *  <li>UC-19 Location of output folder
 *  <li>UC-20 File name extension for output
 *  <li>UC-22 Use trinary classification (positive-negative-neutral)
 *  <li>UC-23 Use binary classification (positive-negative)
 *  <li>UC-24 Use a single positive-negative scale classification
 *  <li>UC-25 Explain the classification
 *  <li>UC-27 Optimise sentiment strengths of existing sentiment terms
 *  <li>UC-28 Suggest new sentiment terms (from terms in misclassified texts)
 *  <li>UC-29 Machine learning evaluations
 *  </ul></p>
 */
public class Paragraph {
   private Sentence[] sentence;
   private int igSentenceCount = 0;
   private int[] igSentimentIDList;
   private int igSentimentIDListCount = 0;
   private boolean bSentimentIDListMade = false;
   private int igPositiveSentiment = 0;
   private int igNegativeSentiment = 0;
   private int igTrinarySentiment = 0;
   private int igScaleSentiment = 0;
   private ClassificationResources resources;
   private ClassificationOptions options;
   private Random generator = new Random();
   private String sgClassificationRationale = "";

   /**
    * This method adds all the sentences to the given UnusedTermsClassificationIndex
    * and then adds a new index to the main index with positive and negative values.
    *
    * @param unusedTermsClassificationIndex The UnusedTermsClassificationIndex to which the sentences are added.
    * @param iCorrectPosClass The number of correct positive classes.
    * @param iEstPosClass The estimated number of positive classes.
    * @param iCorrectNegClass The number of correct negative classes.
    * @param iEstNegClass The estimated number of negative classes.
    */
   public void addParagraphToIndexWithPosNegValues(UnusedTermsClassificationIndex unusedTermsClassificationIndex, int iCorrectPosClass, int iEstPosClass, int iCorrectNegClass, int iEstNegClass) {
      for(int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      unusedTermsClassificationIndex.addNewIndexToMainIndexWithPosNegValues(iCorrectPosClass, iEstPosClass, iCorrectNegClass, iEstNegClass);
   }

   /**
    * Adds all sentences in this object to the specified UnusedTermsClassificationIndex object
    * and then adds that index to the main index with the specified scale values.
    *
    * @param unusedTermsClassificationIndex the UnusedTermsClassificationIndex object to add the sentences to and then add to the main index
    * @param iCorrectScaleClass the correct scale class value for the index
    * @param iEstScaleClass the estimated scale class value for the index
    */
   public void addParagraphToIndexWithScaleValues(UnusedTermsClassificationIndex unusedTermsClassificationIndex, int iCorrectScaleClass, int iEstScaleClass) {
      for(int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      unusedTermsClassificationIndex.addNewIndexToMainIndexWithScaleValues(iCorrectScaleClass, iEstScaleClass);
   }

   /**
    * Adds all sentences in this object to the specified UnusedTermsClassificationIndex object
    * and then adds that index to the main index with the specified binary values.
    *
    * @param unusedTermsClassificationIndex the UnusedTermsClassificationIndex object to add the sentences to and then add to the main index
    * @param iCorrectBinaryClass the correct binary class value for the index
    * @param iEstBinaryClass the estimated binary class value for the index
    */
   public void addParagraphToIndexWithBinaryValues(UnusedTermsClassificationIndex unusedTermsClassificationIndex, int iCorrectBinaryClass, int iEstBinaryClass) {
      for(int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      unusedTermsClassificationIndex.addNewIndexToMainIndexWithBinaryValues(iCorrectBinaryClass, iEstBinaryClass);
   }

   /**
    * Adds all sentences in this object to the specified StringIndex object with the specified text parsing options,
    * recording the count and/or adding to the ARFF index if specified, and returns the number of terms checked.
    *
    * @param stringIndex the StringIndex object to add the sentences to
    * @param textParsingOptions the TextParsingOptions object to use for parsing the text
    * @param bRecordCount a boolean indicating whether to record the count or not
    * @param bArffIndex a boolean indicating whether to add to the ARFF index or not
    * @return the number of terms checked
    */
   public int addToStringIndex(StringIndex stringIndex, TextParsingOptions textParsingOptions, boolean bRecordCount, boolean bArffIndex) {
      int iTermsChecked = 0;

      for(int i = 1; i <= this.igSentenceCount; ++i) {
         iTermsChecked += this.sentence[i].addToStringIndex(stringIndex, textParsingOptions, bRecordCount, bArffIndex);
      }

      return iTermsChecked;
   }

   /**
    * Adds all sentences in this object to the specified UnusedTermsClassificationIndex object
    * and then adds that index to the main index with the specified trinary values.
    *
    * @param unusedTermsClassificationIndex the UnusedTermsClassificationIndex object to add the sentences to and then add to the main index
    * @param iCorrectTrinaryClass the correct trinary class value for the index
    * @param iEstTrinaryClass the estimated trinary class value for the index
    */
   public void addParagraphToIndexWithTrinaryValues(UnusedTermsClassificationIndex unusedTermsClassificationIndex, int iCorrectTrinaryClass, int iEstTrinaryClass) {
      for(int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      unusedTermsClassificationIndex.addNewIndexToMainIndexWithTrinaryValues(iCorrectTrinaryClass, iEstTrinaryClass);
   }

   /**
    * Sets the paragraph text and parses it into sentences.
    *
    * @param sParagraph the paragraph text to set
    * @param classResources the classification resources to use for sentence parsing
    * @param newClassificationOptions the classification options to use for sentence parsing
    */
   public void setParagraph(String sParagraph, ClassificationResources classResources, ClassificationOptions newClassificationOptions) {
      this.resources = classResources;
      this.options = newClassificationOptions;
      if (sParagraph.contains("\"")) {
         sParagraph = sParagraph.replace("\"", "'");
      }

      int iSentenceEnds = 2;
      int iPos = 0;

      while(iPos >= 0 && iPos < sParagraph.length()) {
         iPos = sParagraph.indexOf("<br>", iPos);
         if (iPos >= 0) {
            iPos += 3;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while(iPos >= 0 && iPos < sParagraph.length()) {
         iPos = sParagraph.indexOf(".", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while(iPos >= 0 && iPos < sParagraph.length()) {
         iPos = sParagraph.indexOf("!", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while(iPos >= 0 && iPos < sParagraph.length()) {
         iPos = sParagraph.indexOf("?", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      this.sentence = new Sentence[iSentenceEnds];
      this.igSentenceCount = 0;
      int iLastSentenceEnd = -1;
      boolean bPunctuationIndicatesSentenceEnd = false;
      int iNextBr = sParagraph.indexOf("<br>");
      String sNextSentence = "";

      for(iPos = 0; iPos < sParagraph.length(); ++iPos) {
         String sNextChar = sParagraph.substring(iPos, iPos + 1);
         if (iPos == sParagraph.length() - 1) {
            sNextSentence = sParagraph.substring(iLastSentenceEnd + 1);
         } else if (iPos == iNextBr) {
            sNextSentence = sParagraph.substring(iLastSentenceEnd + 1, iPos);
            iLastSentenceEnd = iPos + 3;
            iNextBr = sParagraph.indexOf("<br>", iNextBr + 2);
         } else if (this.b_IsSentenceEndPunctuation(sNextChar)) {
            bPunctuationIndicatesSentenceEnd = true;
         } else if (sNextChar.compareTo(" ") == 0) {
            if (bPunctuationIndicatesSentenceEnd) {
               sNextSentence = sParagraph.substring(iLastSentenceEnd + 1, iPos);
               iLastSentenceEnd = iPos;
            }
         } else if (this.b_IsAlphanumeric(sNextChar) && bPunctuationIndicatesSentenceEnd) {
            sNextSentence = sParagraph.substring(iLastSentenceEnd + 1, iPos);
            iLastSentenceEnd = iPos - 1;
         }

         if (!sNextSentence.equals("")) {
            ++this.igSentenceCount;
            this.sentence[this.igSentenceCount] = new Sentence();
            this.sentence[this.igSentenceCount].setSentence(sNextSentence, this.resources, this.options);
            sNextSentence = "";
            bPunctuationIndicatesSentenceEnd = false;
         }
      }

   }

   /**
    * Returns the sentiment ID list.
    * If the sentiment ID list has not been made yet, it will be made before returning it.
    *
    * @return an array of integers representing the sentiment IDs.
    */
   public int[] getSentimentIDList() {
      if (!this.bSentimentIDListMade) {
         this.makeSentimentIDList();
      }

      return this.igSentimentIDList;
   }

   /**
    * Returns the classification rationale.
    *
    * @return a string representing the classification rationale.
    */
   public String getClassificationRationale() {
      return this.sgClassificationRationale;
   }

   /**
    * Helper method that makes the sentiment ID list.
    * This method iterates through each sentence in the object's sentence array and collects the sentiment ID
    * lists from each sentence. It then removes duplicates and sorts the list in ascending order.
    */
   public void makeSentimentIDList() {
      boolean bIsDuplicate = false;
      this.igSentimentIDListCount = 0;

      int i;
      for(i = 1; i <= this.igSentenceCount; ++i) {
         if (this.sentence[i].getSentimentIDList() != null) {
            this.igSentimentIDListCount += this.sentence[i].getSentimentIDList().length;
         }
      }

      if (this.igSentimentIDListCount > 0) {
         this.igSentimentIDList = new int[this.igSentimentIDListCount + 1];
         this.igSentimentIDListCount = 0;

         for(i = 1; i <= this.igSentenceCount; ++i) {
            int[] sentenceIDList = this.sentence[i].getSentimentIDList();
            if (sentenceIDList != null) {
               for(int j = 1; j < sentenceIDList.length; ++j) {
                  if (sentenceIDList[j] != 0) {
                     bIsDuplicate = false;

                     for(int k = 1; k <= this.igSentimentIDListCount; ++k) {
                        if (sentenceIDList[j] == this.igSentimentIDList[k]) {
                           bIsDuplicate = true;
                           break;
                        }
                     }

                     if (!bIsDuplicate) {
                        this.igSentimentIDList[++this.igSentimentIDListCount] = sentenceIDList[j];
                     }
                  }
               }
            }
         }

         Sort.quickSortInt(this.igSentimentIDList, 1, this.igSentimentIDListCount);
      }

      this.bSentimentIDListMade = true;
   }

   /**
    * Returns the tagged paragraph.
    * This method concatenates the tagged sentences of each sentence in the object's sentence array.
    *
    * @return a string representing the tagged paragraph.
    */
   public String getTaggedParagraph() {
      StringBuilder sTagged = new StringBuilder();

      for(int i = 1; i <= this.igSentenceCount; ++i) {
         sTagged.append(this.sentence[i].getTaggedSentence());
      }

      return sTagged.toString();
   }

   /**
    * Returns the translated paragraph.
    * This method concatenates the translated sentences of each sentence in the object's sentence array.
    *
    * @return a string representing the translated paragraph.
    */
   public String getTranslatedParagraph() {
      StringBuilder sTranslated = new StringBuilder();

      for(int i = 1; i <= this.igSentenceCount; ++i) {
         sTranslated.append(this.sentence[i].getTranslatedSentence());
      }

      return sTranslated.toString();
   }

   /**
    * Recalculates the sentiment scores for each sentence in the paragraph.
    * This method calls the recalculateSentenceSentimentScore() method of each sentence in the object's sentence array
    * and then calls calculateParagraphSentimentScores() to update the sentiment scores for the entire paragraph.
    */
   public void recalculateParagraphSentimentScores() {
      for(int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
         this.sentence[iSentence].recalculateSentenceSentimentScore();
      }

      this.calculateParagraphSentimentScores();
   }

   /**
    * Re-classifies a classified paragraph for sentiment change based on the given sentiment word ID.
    * This method first checks if the paragraph's negative sentiment score has already been calculated, and if not,
    * calls calculateParagraphSentimentScores() to calculate it. If the paragraph has a non-zero sentiment ID list,
    * and the given sentiment word ID is in the list, then the method calls the
    * reClassifyClassifiedSentenceForSentimentChange() method of each sentence in the object's sentence array with the
    * given sentiment word ID, and then calls calculateParagraphSentimentScores() to update the paragraph's sentiment scores.
    *
    * @param iSentimentWordID the ID of the sentiment word to use for re-classification
    */
   public void reClassifyClassifiedParagraphForSentimentChange(int iSentimentWordID) {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      } else {
         if (!this.bSentimentIDListMade) {
            this.makeSentimentIDList();
         }

         if (this.igSentimentIDListCount != 0) {
            if (Sort.i_FindIntPositionInSortedArray(iSentimentWordID, this.igSentimentIDList, 1, this.igSentimentIDListCount) >= 0) {
               for(int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
                  this.sentence[iSentence].reClassifyClassifiedSentenceForSentimentChange(iSentimentWordID);
               }

               this.calculateParagraphSentimentScores();
            }

         }
      }
   }

   /**
    * Returns the number of positive sentiment words in this paragraph.
    * If the positive sentiment score has not been calculated yet, it will be calculated first.
    *
    * @return the number of positive sentiment words in this paragraph.
    */
   public int getParagraphPositiveSentiment() {
      if (this.igPositiveSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igPositiveSentiment;
   }

   /**
    * Returns the number of negative sentiment words in this paragraph.
    * If the negative sentiment score has not been calculated yet, it will be calculated first.
    *
    * @return the number of negative sentiment words in this paragraph.
    */
   public int getParagraphNegativeSentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igNegativeSentiment;
   }

   /**
    * Returns the trinary sentiment score of this paragraph: positive, neutral, or negative.
    * If the sentiment score has not been calculated yet, it will be calculated first.
    *
    * @return the trinary sentiment score of this paragraph.
    */
   public int getParagraphTrinarySentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igTrinarySentiment;
   }

   /**
    * Returns the scale sentiment of the paragraph.
    * If the negative sentiment score has not been calculated yet,
    * the method calls calculateParagraphSentimentScores() to calculate it.
    *
    * @return the scale sentiment of the paragraph
    */
   public int getParagraphScaleSentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igScaleSentiment;
   }

   /**
    * Determines if the given string is a sentence-ending punctuation.
    *
    * @param sChar the string to be checked
    * @return true if the string is "." or "!" or "?", false otherwise
    */
   private boolean b_IsSentenceEndPunctuation(String sChar) {
      return sChar.compareTo(".") == 0 || sChar.compareTo("!") == 0 || sChar.compareTo("?") == 0;
   }

   /**
    * This method determines whether a given string character is alphanumeric or not.
    *
    * @param sChar the string character to be checked
    * @return true if the character is alphanumeric, false otherwise
    */
   private boolean b_IsAlphanumeric(String sChar) {
      return sChar.compareToIgnoreCase("a") >= 0 && sChar.compareToIgnoreCase("z") <= 0 || sChar.compareTo("0") >= 0 && sChar.compareTo("9") <= 0 || sChar.compareTo("$") == 0 || sChar.compareTo("£") == 0 || sChar.compareTo("'") == 0;
   }

   /**
    * This method calculates the sentiment scores of each paragraph.
    * It sets igPositiveSentiment, igNegativeSentiment, igTrinarySentiment.
    * If bgScaleMode is true, igScaleSentiment is also set.
    * If bgExplainClassification is true, sgClassificationRationale is set
    * based on the classification method used.
    * This method is called by the constructor of the Paragraph class.
    */
   private void calculateParagraphSentimentScores() {
      this.igPositiveSentiment = 1;
      this.igNegativeSentiment = -1;
      this.igTrinarySentiment = 0;
      if (this.options.bgExplainClassification && this.sgClassificationRationale.length() > 0) {
         this.sgClassificationRationale = "";
      }

      int iPosTotal = 0;
      int iPosMax = 0;
      int iNegTotal = 0;
      int iNegMax = 0;
      int iPosTemp =0;
      int iNegTemp =0;
      int iSentencesUsed = 0;
      int wordNum =0;
      int sentiNum =0;
      if (this.igSentenceCount != 0) {
         int iNegTot;
         for(iNegTot = 1; iNegTot <= this.igSentenceCount; ++iNegTot) {
            iNegTemp = this.sentence[iNegTot].getSentenceNegativeSentiment();
            iPosTemp = this.sentence[iNegTot].getSentencePositiveSentiment();
            wordNum+=this.sentence[iNegTot].getIgTermCount();
            sentiNum+=this.sentence[iNegTot].getIgSentiCount();
            if (iNegTemp != 0 || iPosTemp != 0) {
               iNegTotal += iNegTemp;
               ++iSentencesUsed;
               if (iNegMax > iNegTemp) {
                  iNegMax = iNegTemp;
               }

               iPosTotal += iPosTemp;
               if (iPosMax < iPosTemp) {
                  iPosMax = iPosTemp;
               }
            }

            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale + this.sentence[iNegTot].getClassificationRationale() + " ";
            }
         }
         
         int var10000;
         if (iNegTotal == 0) {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 != 2) {
               this.igPositiveSentiment = 0;
               this.igNegativeSentiment = 0;
               this.igTrinarySentiment = this.binarySelectionTieBreaker();
               return;
            }
         }

         var10000 = this.options.igEmotionParagraphCombineMethod;
         this.options.getClass();
         if (var10000 == 1) {
            this.igPositiveSentiment = (int)((double)((float)iPosTotal / (float)iSentencesUsed) + 0.5D);
            this.igNegativeSentiment = (int)((double)((float)iNegTotal / (float)iSentencesUsed) - 0.5D);
            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale + "[result = average (" + iPosTotal + " and " + iNegTotal + ") of " + iSentencesUsed + " sentences]";
            }
         } else {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 == 2) {
               this.igPositiveSentiment = iPosTotal;
               this.igNegativeSentiment = iNegTotal;
               if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale = this.sgClassificationRationale + "[result: total positive; total negative]";
               }
            } else {
               this.igPositiveSentiment = iPosMax;
               this.igNegativeSentiment = iNegMax;
               if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale = this.sgClassificationRationale + "[result: max + and - of any sentence]";
               }
            }
         }

         var10000 = this.options.igEmotionParagraphCombineMethod;
         this.options.getClass();
         if (var10000 != 2) {
            if (this.igPositiveSentiment == 0) {
               this.igPositiveSentiment = 1;
            }

            if (this.igNegativeSentiment == 0) {
               this.igNegativeSentiment = -1;
            }
         }

         if (this.options.bgScaleMode) {
            this.igScaleSentiment = this.igPositiveSentiment + this.igNegativeSentiment;
            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale + "[scale result = sum of pos and neg scores]";
            }

         } else {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 == 2) {
               if (this.igPositiveSentiment == 0 && this.igNegativeSentiment == 0) {
                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.options.igDefaultBinaryClassification;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[binary result set to default value]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[trinary result 0 as pos=1, neg=-1]";
                     }
                  }
               } else {
                  if ((float)this.igPositiveSentiment > this.options.fgNegativeSentimentMultiplier * (float)(-this.igNegativeSentiment)) {
                     this.igTrinarySentiment = 1;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[overall result 1 as pos > -neg * " + this.options.fgNegativeSentimentMultiplier + "]";
                     }

                     return;
                  }

                  if ((float)this.igPositiveSentiment < this.options.fgNegativeSentimentMultiplier * (float)(-this.igNegativeSentiment)) {
                     this.igTrinarySentiment = -1;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[overall result -1 as pos < -neg * " + this.options.fgNegativeSentimentMultiplier + "]";
                     }

                     return;
                  }

                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.options.igDefaultBinaryClassification;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[binary result = default value as pos = -neg * " + this.options.fgNegativeSentimentMultiplier + "]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[trinary result = 0 as pos = -neg * " + this.options.fgNegativeSentimentMultiplier + "]";
                     }
                  }
               }
            } else {
               if (this.igPositiveSentiment == 1 && this.igNegativeSentiment == -1) {
                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.binarySelectionTieBreaker();
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[binary result = default value as pos=1 neg=-1]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale + "[trinary result = 0 as pos=1 neg=-1]";
                     }
                  }

                  return;
               }

               if (this.igPositiveSentiment > -this.igNegativeSentiment) {
                  this.igTrinarySentiment = 1;
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale + "[overall result = 1 as pos>-neg]";
                  }

                  return;
               }

               if (this.igPositiveSentiment < -this.igNegativeSentiment) {
                  this.igTrinarySentiment = -1;
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale + "[overall result = -1 as pos<-neg]";
                  }

                  return;
               }

               iNegTot = 0;
               int iPosTot = 0;

               for(int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
                  iNegTot += this.sentence[iSentence].getSentenceNegativeSentiment();
                  iPosTot = this.sentence[iSentence].getSentencePositiveSentiment();
               }

               if (this.options.bgBinaryVersionOfTrinaryMode && iPosTot == -iNegTot) {
                  this.igTrinarySentiment = this.binarySelectionTieBreaker();
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale + "[binary result = default as posSentenceTotal>-negSentenceTotal]";
                  }
               } else {
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale + "[overall result = largest of posSentenceTotal, negSentenceTotal]";
                  }

                  if (iPosTot > -iNegTot) {
                     this.igTrinarySentiment = 1;
                  } else {
                     this.igTrinarySentiment = -1;
                  }
               }
            }

         }
      }
   }

   /**
    * This method serves as a tie-breaker for binary selection. It returns a random number between -1 and 1 if the
    * default binary classification is not provided. If the default binary classification is provided, it returns the
    * default binary classification value.
    *
    * @return An integer value of either 1 or -1 that represents the binary selection result.
    */
   private int binarySelectionTieBreaker() {
      if (this.options.igDefaultBinaryClassification != 1 && this.options.igDefaultBinaryClassification != -1) {
         return this.generator.nextDouble() > 0.5D ? 1 : -1;
      } else {
         return this.options.igDefaultBinaryClassification != 1 && this.options.igDefaultBinaryClassification != -1 ? this.options.igDefaultBinaryClassification : this.options.igDefaultBinaryClassification;
      }
   }
}
