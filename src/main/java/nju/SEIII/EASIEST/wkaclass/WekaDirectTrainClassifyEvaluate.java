
package nju.SEIII.EASIEST.wkaclass;

import java.io.*;
import java.util.Date;
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
public class WekaDirectTrainClassifyEvaluate
{

    private static String sgJarFolder = "C:/jars/";

    public WekaDirectTrainClassifyEvaluate()
    {
    }

    public static void directClassifyAllArff(String arffTrainFile, String arffEvalFile, String classifierName, String classifierExclude, String allResultsFilename, String summaryResultsFilename)
        throws Exception
    {
        Date start = Utilities.getNow();
        System.out.print("Loading data ... ");
        BufferedReader reader = new BufferedReader(new FileReader(arffTrainFile));
        Instances trainData = new Instances(reader);
        reader.close();
        trainData.setClassIndex(trainData.numAttributes() - 1);
        reader = new BufferedReader(new FileReader(arffEvalFile));
        Instances evalData = new Instances(reader);
        reader.close();
        evalData.setClassIndex(evalData.numAttributes() - 1);
        String options = null;
        Evaluation eval = new Evaluation(evalData);
        System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
        if(classifierName.indexOf(",liblin,") > 0)
            try
            {
                Utilities.printNameAndWarning("LibLINEAR");
                start = Utilities.getNow();
                Utilities.addToClassPath(sgJarFolder + "liblinear-1.51.jar");
                LibLINEAR schemeLibLINEAR = new LibLINEAR();
                options = " -i -o \u2013t";
                schemeLibLINEAR.setOptions(Utils.splitOptions(options));
                schemeLibLINEAR.buildClassifier(trainData);
                eval.evaluateModel(schemeLibLINEAR, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "LibLin", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with LibLINEAR on " + arffTrainFile + " " + e.getMessage());
                System.out.println("Must have jar file in Jar folder " + sgJarFolder + " or classpath. Here is the current Java classpath");
                Utilities.printClasspath();
            }
        if(classifierName.indexOf(",libsvm,") > 0)
            try
            {
                Utilities.printNameAndWarning("LibSVM");
                start = Utilities.getNow();
                Utilities.addToClassPath(sgJarFolder + "libsvm.jar");
                LibSVM schemeLibSVM = new LibSVM();
                options = "-s 0";
                schemeLibSVM.setOptions(Utils.splitOptions(options));
                schemeLibSVM.buildClassifier(trainData);
                eval.evaluateModel(schemeLibSVM, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "LibSVM", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with LibSVM on " + arffTrainFile + " " + e.getMessage());
                System.out.println("Must have jar file in Jar folder " + sgJarFolder + " or classpath. Here is the current Java classpath");
                Utilities.printClasspath();
            }
        if((classifierName.indexOf(",smo,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SMO"))
            try
            {
                Utilities.printNameAndWarning("SMO   ");
                start = Utilities.getNow();
                SMO schemeSMO = new SMO();
                options = "-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
                schemeSMO.setOptions(Utils.splitOptions(options));
                schemeSMO.buildClassifier(trainData);
                eval.evaluateModel(schemeSMO, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "SMO", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SMO on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",slog,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SMO"))
            try
            {
                Utilities.printNameAndWarning("SLOG  ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                SimpleLogistic schemeSLOG = new SimpleLogistic();
                options = "-I 0 -M 500 -H 50 -W 0.0";
                schemeSLOG.setOptions(Utils.splitOptions(options));
                schemeSLOG.buildClassifier(trainData);
                eval.evaluateModel(schemeSLOG, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "SLOG", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SLOG on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",bayes,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("BAYES"))
            try
            {
                Utilities.printNameAndWarning("BAYES ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                NaiveBayes schemeBayes = new NaiveBayes();
                schemeBayes.buildClassifier(trainData);
                eval.evaluateModel(schemeBayes, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "BAYES", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with BAYES on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",ada,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("ADA"))
            try
            {
                Utilities.printNameAndWarning("ADA   ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                AdaBoostM1 schemeAda = new AdaBoostM1();
                options = "-P 100 -S 1 -I 10 -W weka.classifiers.trees.DecisionStump";
                schemeAda.setOptions(Utils.splitOptions(options));
                schemeAda.buildClassifier(trainData);
                eval.evaluateModel(schemeAda, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "ADA", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with ADA on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",smoreg,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("SMOreg"))
            try
            {
                Utilities.printNameAndWarning("SMOreg");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                ClassificationViaRegression schemeSMOreg = new ClassificationViaRegression();
                options = "-W weka.classifiers.functions.SMOreg -- -C 1.0 -N 0 -I \"weka.classifiers.functions.supportVector.RegSMOImproved -L 0.0010 -W 1 -P 1.0E-12 -T 0.0010 -V\" -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\"";
                schemeSMOreg.setOptions(Utils.splitOptions(options));
                schemeSMOreg.buildClassifier(trainData);
                eval.evaluateModel(schemeSMOreg, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "SMOreg", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with SMOreg on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",jrip,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("JRIP"))
            try
            {
                Utilities.printNameAndWarning("JRIP  ");
                start = Utilities.getNow();
                JRip schemeJrip = new JRip();
                options = "-F 3 -N 2.0 -O 2 -S 1";
                schemeJrip.setOptions(Utils.splitOptions(options));
                schemeJrip.buildClassifier(trainData);
                eval.evaluateModel(schemeJrip, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "JRIP", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with JRIP on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",dec,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("DEC"))
            try
            {
                Utilities.printlnNameAndWarning("DEC   ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                DecisionTable schemeDec = new DecisionTable();
                options = "-X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\"";
                schemeDec.setOptions(Utils.splitOptions(options));
                schemeDec.buildClassifier(trainData);
                eval.evaluateModel(schemeDec, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "DEC", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with DEC on " + arffTrainFile + " " + e.getMessage());
            }
        if((classifierName.indexOf(",j48,") > 0 || classifierName.indexOf(",all,") > 0) && !classifierExclude.equals("J48"))
            try
            {
                Utilities.printNameAndWarning("J48   ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                J48 schemeJ48 = new J48();
                options = "-C 0.25 -M 2";
                schemeJ48.setOptions(Utils.splitOptions(options));
                schemeJ48.buildClassifier(trainData);
                eval.evaluateModel(schemeJ48, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "J48", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with J48 on " + arffTrainFile + " " + e.getMessage());
            }
        if(classifierName.indexOf(",mlp,") > 0)
            try
            {
                Utilities.printlnNameAndWarning("MLP   ");
                start = Utilities.getNow();
                eval = new Evaluation(evalData);
                MultilayerPerceptron schemeMLP = new MultilayerPerceptron();
                options = "-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a";
                schemeMLP.setOptions(Utils.splitOptions(options));
                schemeMLP.buildClassifier(trainData);
                eval.evaluateModel(schemeMLP, evalData, new Object[0]);
                printClassificationResultsAllData(eval, arffTrainFile, "MLP", options, allResultsFilename, summaryResultsFilename);
                System.out.println(Utilities.timeGap(start, Utilities.getNow()) + " taken");
            }
            catch(Exception e)
            {
                System.out.println("Error with MLP on " + arffTrainFile + " " + e.getMessage());
            }
    }

    public static void printClassificationResultsAllData(Evaluation eval, String arffFilename, String classifierName, String options, String allResultsFilename, String summaryResultsFilename)
        throws Exception
    {
        FileOutputStream fout = new FileOutputStream(allResultsFilename, true);
        PrintStream allResultsPrintStream = new PrintStream(fout);
        allResultsPrintStream.println();
        allResultsPrintStream.println(arffFilename);
        allResultsPrintStream.println("=== Evaluation result ===");
        allResultsPrintStream.println("Scheme: " + classifierName);
        allResultsPrintStream.println("Options: " + options);
        allResultsPrintStream.println(eval.toSummaryString());
        allResultsPrintStream.println(eval.toClassDetailsString());
        allResultsPrintStream.println(eval.toMatrixString());
        fout.close();
        fout = new FileOutputStream(summaryResultsFilename, true);
        PrintStream summaryResultsPrintStream = new PrintStream(fout);
        summaryResultsPrintStream.println(classifierName + "\t" + eval.pctCorrect() + "%\t" + options + "\t" + arffFilename);
        fout.close();
    }

}
