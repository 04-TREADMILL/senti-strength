package nju.SEIII.EASIEST;

import nju.SEIII.EASIEST.sentistrength.SentiStrength;

public class Main {
    public static void main(String[] args) {
        SentiStrength classifier = new SentiStrength();
        classifier.initialiseAndRun(args);
    }
}