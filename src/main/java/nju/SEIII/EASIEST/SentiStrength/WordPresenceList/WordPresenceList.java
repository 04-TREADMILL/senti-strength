package nju.SEIII.EASIEST.SentiStrength.WordPresenceList;

import nju.SEIII.EASIEST.SentiStrength.ClassificationOptions;

public abstract class WordPresenceList {
    public abstract boolean initialise(String sFilename, ClassificationOptions options);

    public abstract boolean contains(String sword);
}
