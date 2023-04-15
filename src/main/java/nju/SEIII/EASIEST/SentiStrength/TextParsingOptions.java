// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   TextParsingOptions.java

package nju.SEIII.EASIEST.SentiStrength;


/**
 * TextParsingOptions class provides settings for text parsing options.
 *
 * @UC <p><ul>
 * <li>UC-16 Process stdin and send to stdout</li>
 * <li>UC-17 Location of linguistic data folder</li>
 * <li>UC-18 Location of sentiment term weights</li>
 * <li>UC-19 Location of output folder</li>
 * <li>UC-20 File name extension for output</li>
 * </ul></p>
 */
public class TextParsingOptions {

  public boolean bgIncludePunctuation;
  public int igNgramSize;
  public boolean bgUseTranslations;
  public boolean bgAddEmphasisCode;

  public TextParsingOptions() {
    bgIncludePunctuation = true;
    igNgramSize = 1;
    bgUseTranslations = true;
    bgAddEmphasisCode = false;
  }
}
