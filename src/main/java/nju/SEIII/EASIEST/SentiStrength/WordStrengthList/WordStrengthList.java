package nju.SEIII.EASIEST.SentiStrength.WordStrengthList;

import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;

public abstract class WordStrengthList {
  public abstract boolean initialise(String sFilename, ClassificationOptions options);

  public abstract int getStrength(String sword);
}
