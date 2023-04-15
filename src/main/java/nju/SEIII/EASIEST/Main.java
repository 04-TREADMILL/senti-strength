package nju.SEIII.EASIEST;

import nju.SEIII.EASIEST.SentiStrength.SentiStrength;

public class Main {
    public static void main(String[] args) {
        SentiStrength classifier = new SentiStrength();
        classifier.initialiseAndRun(args);
    }
}