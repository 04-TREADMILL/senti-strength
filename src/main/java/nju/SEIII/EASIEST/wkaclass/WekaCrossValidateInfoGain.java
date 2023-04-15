// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   WekaCrossValidateInfoGain.java

package nju.SEIII.EASIEST.wkaclass;

import java.io.*;
import java.util.Date;
import java.util.Random;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.*;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.ClassificationViaRegression;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.instance.Randomize;

// Referenced classes of package nju.SEIII.EASIEST.wkaclass:
//            Utilities

public class WekaCrossValidateInfoGain
{

    private static final String sgJarFolder = System.getProperty("user.dir") + "libs/";

    public WekaCrossValidateInfoGain()
    {
    }

    public static void main(String[] args)
        throws Exception
    {
        boolean[] bArgumentRecognised = new boolean[args.length];
        boolean lessFeaturesRequestedThanData = true;
        String classifierName = ",,all,";
        String classifierExclude = "";
        String addToClasspath = "";
        String instructionFilename = "-";
        String inputArffFilename = "-";
        String resultsFileName = "-";
        String summaryResultsFileName = "-";
        int minFeatures = 100;
        int maxFeatures = 1000;
        int stepFeatures = 100;
        int iterations = 30;
        Random random = new Random();
        for(int i = 0; i < args.length; i++)
            bArgumentRecognised[i] = false;

        for(int i = 0; i < args.length; i++)
        {
            if(args[i].equals("infogain"))
                bArgumentRecognised[i] = true;
            if(i < args.length - 1)
            {
                if(args[i].equals("min"))
                {
                    minFeatures = Integer.parseInt(args[i + 1].trim());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("max"))
                {
                    maxFeatures = Integer.parseInt(args[i + 1].trim());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("step"))
                {
                    stepFeatures = Integer.parseInt(args[i + 1].trim());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("iterations"))
                {
                    iterations = Integer.parseInt(args[i + 1].trim());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("classifier"))
                {
                    classifierName = ",," + args[i + 1].toLowerCase() + ",";
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("exclude"))
                {
                    classifierExclude = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("instructions"))
                {
                    instructionFilename = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("input"))
                {
                    inputArffFilename = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("results"))
                {
                    resultsFileName = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("summary"))
                {
                    summaryResultsFileName = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
                if(args[i].equals("addToClasspath"))
                {
                    addToClasspath = args[i + 1];
                    Utilities.addToClassPath(addToClasspath);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }
            }
        }

        for(int i = 0; i < args.length; i++)
            if(!bArgumentRecognised[i])
                System.out.println("Unrecognised command - wrong spelling or case?: " + args[i]);

        reportParameters(args, minFeatures, maxFeatures, stepFeatures, iterations, classifierName, classifierExclude, addToClasspath, instructionFilename, inputArffFilename, resultsFileName, summaryResultsFileName);
        if(instructionFilename.equals("-"))
        {
            if(inputArffFilename.equals("-") || resultsFileName.equals("-") || summaryResultsFileName.equals("-"))
            {
                System.out.println("Must specify instructions file or input, results and summary files. Giving up.");
                return;
            }
            System.out.println("started processing " + inputArffFilename);
            for(int features = minFeatures; features <= maxFeatures; features += stepFeatures)
            {
                System.out.println("Starting " + features + " features for " + inputArffFilename);
                for(int i = 1; i <= iterations; i++)
                {
                    int randomSeed = random.nextInt();
                    lessFeaturesRequestedThanData = classifyArff(inputArffFilename, classifierName, classifierExclude, randomSeed, features, resultsFileName, summaryResultsFileName);
                    System.out.println(i + "/" + iterations + " iterations done");
                }

                if(!lessFeaturesRequestedThanData)
                {
                    System.out.println("More features requested than in data last time!");
                    return;
                }
            }

            System.out.println("For correlations, use Windows SentiStrength, Data Mining|Summarise Average... menu item");
        } else
        {
            File f = new File(instructionFilename);
            if(f.exists())
            {
                BufferedReader reader;
                for(reader = new BufferedReader(new FileReader(instructionFilename)); reader.ready();)
                {
                    inputArffFilename = reader.readLine();
                    resultsFileName = reader.readLine();
                    summaryResultsFileName = reader.readLine();
                    System.out.println("started processing " + inputArffFilename);
                    for(int features = minFeatures; features <= maxFeatures; features += stepFeatures)
                    {
                        System.out.println("Starting " + features + " features for " + inputArffFilename);
                        for(int i = 1; i <= iterations + 1; i++)
                        {
                            int randomSeed = random.nextInt();
                            lessFeaturesRequestedThanData = classifyArff(inputArffFilename, classifierName, classifierExclude, randomSeed, features, resultsFileName, summaryResultsFileName);
                            System.out.println(i + "/" + iterations + " 10-fold iterations with " + features + " features done");
                        }

                        if(!lessFeaturesRequestedThanData)
                        {
                            System.out.println("More features requested than in data last time!");
                            reader.close();
                            return;
                        }
                    }

                }

                reader.close();
            } else
            {
                System.out.println(instructionFilename + " not found - should contain data file, results file, summary file.");
                System.out.println("Program terminating without running analysis.");
            }
        }
    }

    public static void reportParameters(String[] args, int minFeatures, int maxFeatures, int stepFeatures, int iterations, String classifierName, String classifierExclude, String addToClasspath,
                                        String instructionFilename, String inputFilename, String resultsFileName, String summaryResultsFileName)
    {
        System.out.println("InfoGain method: default options or values set by command line:");
        System.out.print(" " + minFeatures + " [min] features, ");
        System.out.print(maxFeatures + " [max] features, ");
        System.out.println(stepFeatures + " [step] features");
        System.out.println(" " + iterations + " [iterations]");
        System.out.println(" " + classifierName + " [classifier] LibSVM/LibLin/ALL = SMO/SLOG/BAYES/ADA/SMOreg/JRIP/DEC/J48");
        System.out.println(" " + classifierExclude + " [classifierExclude] SMO/SLOG/BAYES/ADA/SMOreg/JRIP/DEC/J48");
        System.out.println(" " + instructionFilename + " [instructions] file (train-eval-results triples)");
        System.out.println(" " + inputFilename + " [input] ARFF file");
        System.out.println(" " + resultsFileName + " [results] file");
        System.out.println(" " + summaryResultsFileName + " [summary] results file (just accuracy)");
        System.out.println(" " + addToClasspath + " [addToClasspath] file to add to classpath");
        System.out.println();
    }

    public static boolean classifyArff(String arffFileName, String classifierName, String classifierExclude, int randomSeed, int features, String allResultsFilename, String summaryResultsFilename)
        throws Exception
    {
        boolean lessFeaturesRequestedThanData = true;
        Date start = Utilities.getNow();
        System.out.print("Loading data ... ");
        BufferedReader reader = new BufferedReader(new FileReader(arffFileName));
        Instances data = new Instances(reader);
        reader.close();
        data.setClassIndex(data.numAttributes() - 1);
        Randomize randomize = new Randomize();
        randomize.setRandomSeed(randomSeed);
        randomize.setInputFormat(data);
        data = Filter.useFilter(data, randomize);
        Ranker ranker = new Ranker();
        if(features < data.numAttributes())
        {
            ranker.setNumToSelect(features);
        } else
        {
            ranker.setNumToSelect(data.numAttributes());
            lessFeaturesRequestedThanData = false;
        }
        ranker.setGenerateRanking(true);
        ranker.setThreshold(1.7976931348623157E+308D);
        InfoGainAttributeEval infoGainAttributeEval = new InfoGainAttributeEval();
        AttributeSelection attributeSelection = new AttributeSelection();
        attributeSelection.setEvaluator(infoGainAttributeEval);
        attributeSelection.setSearch(ranker);
        attributeSelection.setInputFormat(data);
        data = Filter.useFilter(data, attributeSelection);
        String options = null;
        System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
        if(classifierName.indexOf(",liblin,") > 0)
            try
            {
                Utilities.printNameAndWarning("LibLINEAR");
                start = Utilities.getNow();
                Utilities.addToClassPath(sgJarFolder + "LibLINEAR.jar");
                Evaluation eval = new Evaluation(data);
                LibLINEAR schemeLibLINEAR = new LibLINEAR();
                options = " -i -o \u2013t";
                schemeLibLINEAR.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeLibLINEAR, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "LibLin", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with LibLINEAR on " + arffFileName + " " + e.getMessage());
                System.out.println("Must have jar file in Jar folder " + sgJarFolder + " or classpath. Here is the current Java classpath");
                Utilities.printClasspath();
            }
        if(classifierName.indexOf(",libsvm,") > 0)
            try
            {
                Utilities.printNameAndWarning("LibSVM");
                start = Utilities.getNow();
                Utilities.addToClassPath(sgJarFolder + "LibSVM.jar");
                Evaluation eval = new Evaluation(data);
                LibSVM schemeLibSVM = new LibSVM();
                options = "-s 0";
                schemeLibSVM.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeLibSVM, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "LibSVM", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with LibSVM on " + arffFileName + " " + e.getMessage());
                System.out.println("Must have jar file in Jar folder " + sgJarFolder + " or classpath. Here is the current Java classpath");
                Utilities.printClasspath();
            }
        if((classifierName.indexOf(",smo,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SMO"))
            try
            {
                Utilities.printNameAndWarning("SMO   ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                SMO schemeSMO = new SMO();
                options = "-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
                schemeSMO.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeSMO, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "SMO", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SMO on " + arffFileName + " " + e.getMessage());
                System.out.println("Must have jar file in classpath (not dir!). Here is the current Java classpath");
                Utilities.printClasspath();
            }
        if((classifierName.indexOf(",slog,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SLOG"))
            try
            {
                Utilities.printNameAndWarning("SLOG  ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                SimpleLogistic schemeSLOG = new SimpleLogistic();
                options = "-I 0 -M 500 -H 50 -W 0.0";
                schemeSLOG.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeSLOG, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "SLOG", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SLOG on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",bayes,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("BAYES"))
            try
            {
                Utilities.printNameAndWarning("BAYES ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                NaiveBayes schemeBayes = new NaiveBayes();
                eval.crossValidateModel(schemeBayes, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "BAYES", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with BAYES on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",ada,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("ADA"))
            try
            {
                Utilities.printNameAndWarning("ADA   ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                AdaBoostM1 schemeAda = new AdaBoostM1();
                options = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
                schemeAda.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeAda, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "ADA", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with ADA on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",smoreg,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SMOreg"))
            try
            {
                Utilities.printNameAndWarning("SMOreg");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                ClassificationViaRegression schemeSMOreg = new ClassificationViaRegression();
                options = "-W weka.classifiers.functions.SMOreg -- -C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.0010 -W 1 -P 1.0E-12 -T 0.0010 -V\" -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
                schemeSMOreg.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeSMOreg, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "SMOreg", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SMOreg on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",jrip,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("JRIP"))
            try
            {
                Utilities.printNameAndWarning("JRIP  ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                JRip schemeJrip = new JRip();
                options = "-F 3 -N 2.0 -O 2 -S 1";
                schemeJrip.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeJrip, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "JRIP", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with JRIP on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",dec,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("DEC"))
            try
            {
                Utilities.printNameAndWarning("DEC   ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                DecisionTable schemeDec = new DecisionTable();
                options = "-X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\"";
                schemeDec.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeDec, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "DEC", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with DEC on " + arffFileName + " " + e.getMessage());
            }
        if((classifierName.indexOf(",j48,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("J48"))
            try
            {
                Utilities.printNameAndWarning("J48   ");
                start = Utilities.getNow();
                Evaluation eval = new Evaluation(data);
                J48 schemeJ48 = new J48();
                options = "-C 0.25 -M 2";
                schemeJ48.setOptions(Utils.splitOptions(options));
                eval.crossValidateModel(schemeJ48, data, 10, new Random(randomSeed), new Object[0]);
                printClassificationResults(eval, arffFileName, "J48", randomSeed, features, options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with J48 on " + arffFileName + " " + e.getMessage());
            }
        return lessFeaturesRequestedThanData;
    }

    public static void printClassificationResults(Evaluation eval, String arffFilename, String classifierName, int randomSeed, int features, String options, String allResultsFilename, String summaryResultsFilename)
        throws Exception
    {
        FileOutputStream fout = new FileOutputStream(allResultsFilename, true);
        PrintStream allResultsPrintStream = new PrintStream(fout);
        allResultsPrintStream.println();
        allResultsPrintStream.println(arffFilename);
        allResultsPrintStream.println("=== Evaluation result ===");
        allResultsPrintStream.println("Scheme: " + classifierName);
        allResultsPrintStream.println("Options: " + options);
        allResultsPrintStream.println("Relation: .Randomize-S" + randomSeed + "Features-N" + features);
        allResultsPrintStream.println(eval.toSummaryString());
        allResultsPrintStream.println(eval.toClassDetailsString());
        allResultsPrintStream.println(eval.toMatrixString());
        fout.close();
        fout = new FileOutputStream(summaryResultsFilename, true);
        PrintStream summaryResultsPrintStream = new PrintStream(fout);
        summaryResultsPrintStream.println(classifierName + "\t" + randomSeed + "\t" + features + "\t" + eval.pctCorrect() + "%\t" + options + "\t" + arffFilename);
        fout.close();
    }

}
