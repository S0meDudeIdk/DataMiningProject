import weka.core.Instances;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

public class Evaluator {
    private Evaluation evaluation;
    private Instances data;
    private Classifier classifier;

    public Evaluator(Classifier classifier, Instances data) throws Exception {
        this.classifier = classifier;
        this.data = data;
        this.evaluation = new Evaluation(data);
    }

    public void crossValidate(int folds) throws Exception {
        Random rand = new Random(1);
        evaluation.crossValidateModel(classifier, data, folds, rand);
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

        evaluation.evaluateModel(classifier, data);
        
        // Print results
        System.out.println(evaluation.toSummaryString("=== Evaluation Results ===", false));
        System.out.println("Confusion Matrix:");
        double[][] cmatrix = evaluation.confusionMatrix();
        for (double[] row : cmatrix) {
            for (double val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    public void saveResultsToFile(String outputPath) throws Exception {
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
            writer.println(evaluation.toMatrixString().trim());
            writer.println(evaluation.toClassDetailsString().trim());
            System.out.println("Evaluation results saved to: " + outputPath);
        }
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }
}