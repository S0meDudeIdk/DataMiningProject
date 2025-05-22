import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class Evaluator {
    private Evaluation evaluation;
    private Classifier classifier;
    private Instances trainData;
    private Instances testData;

    public Evaluator(Classifier classifier, Instances trainData, Instances testData) throws Exception {
        this.classifier = classifier;
        this.trainData = trainData;
        this.testData = testData;
        this.evaluation = new Evaluation(trainData);
    }
    
    public void crossValidate(int folds) throws Exception {
        Random rand = new Random(1);
        evaluation.crossValidateModel(classifier, trainData, folds, rand);
    }

    public void evaluateOnTestData() throws Exception {
        evaluation.evaluateModel(classifier, testData);
    }

    public void printResults() throws Exception {
        System.out.println("=== Model Information ===");
        System.out.println(classifier.toString().trim());
        System.out.println("=== Evaluation Results ===");
        System.out.println("Accuracy: " + evaluation.pctCorrect() + "%");
        System.out.println("Kappa statistic: " + evaluation.kappa());
        System.out.println("Mean absolute error: " + evaluation.meanAbsoluteError());
        System.out.println("Root mean squared error: " + evaluation.rootMeanSquaredError());
        System.out.println(evaluation.toMatrixString().trim());
        System.out.println(evaluation.toClassDetailsString().trim());
    }

    public void saveResultsToFile(String outputPath, String outputCluster, long modelTime, long evalTime, long clusterTime) throws Exception {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("Created directory: " + parentDir.getAbsolutePath());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("=== Model Information ===");
            writer.println(classifier.toString().trim());
            writer.println("=== Evaluation Results ===");
            writer.println("Accuracy: " + evaluation.pctCorrect() + "%");
            writer.println("Kappa statistic: " + evaluation.kappa());
            writer.println("Mean absolute error: " + evaluation.meanAbsoluteError());
            writer.println("Root mean squared error: " + evaluation.rootMeanSquaredError());

            double sumPrecision = 0, sumRecall = 0, sumFMeasure = 0;
            int numClasses = (testData != null) ? testData.numClasses() : trainData.numClasses();
            int validClassCount = 0;
            for (int i = 0; i < numClasses; i++) {
                double p = evaluation.precision(i);
                double r = evaluation.recall(i);
                double f = evaluation.fMeasure(i);
                if (!Double.isNaN(p) && !Double.isNaN(r) && !Double.isNaN(f)) {
                    sumPrecision += p;
                    sumRecall += r;
                    sumFMeasure += f;
                    validClassCount++;
                }
            }
            double macroPrecision = validClassCount > 0 ? sumPrecision / validClassCount : Double.NaN;
            double macroRecall = validClassCount > 0 ? sumRecall / validClassCount : Double.NaN;
            double macroFMeasure = validClassCount > 0 ? sumFMeasure / validClassCount : Double.NaN;
            writer.println("Macro Precision: " + macroPrecision);
            writer.println("Macro Recall: " + macroRecall);
            writer.println("Macro F-Measure: " + macroFMeasure);

            writer.println(evaluation.toMatrixString().trim());
            writer.println(evaluation.toClassDetailsString().trim());
            if(outputCluster!=null){
                writer.println(outputCluster);
            }
            writer.println("Train model time: " + modelTime);
            writer.println("Evaluation time: " + evalTime);
            writer.println("Clustering time: " + clusterTime);
            long totalTime = modelTime + evalTime + clusterTime;
            writer.println("Total time: " + totalTime);
            
            System.out.println("Evaluation results saved to: " + outputPath);
        }
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }
}
