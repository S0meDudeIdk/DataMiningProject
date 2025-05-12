import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.Evaluation;
import java.util.Random;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ClassifierModel {
    private Instances data;
    private Classifier classifier;
    private String classifierType;

    public ClassifierModel(String dataPath, String classifierType) throws Exception {
        loadData(dataPath);
        this.classifierType = classifierType.toLowerCase();
        initializeClassifier();
    }

    private void loadData(String dataPath) throws Exception {
        DataSource source = new DataSource(dataPath);
        data = source.getDataSet();

        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        System.out.println("Loaded dataset: " + dataPath);
        System.out.println("Number of instances: " + data.numInstances());
        System.out.println("Number of attributes: " + data.numAttributes());
        System.out.println("Class attribute: " + data.classAttribute().name());
    }

    private void initializeClassifier() throws Exception {
        switch (classifierType) {
            case "j48":
                J48 j48 = new J48();
                j48.setConfidenceFactor(0.25f);
                j48.setMinNumObj(2);
                classifier = j48;
                System.out.println("Using J48 decision tree classifier");
                break;
            case "naivebayes":
                classifier = new NaiveBayes();
                System.out.println("Using Naive Bayes classifier");
                break;
            case "svm":
                classifier = new SMO();
                System.out.println("Using SVM classifier (SMO implementation)");
                break;
            default:
                classifier = new J48();
                classifierType = "j48";
                System.out.println("Unknown classifier type. Defaulting to J48 decision tree.");
        }
    }

    public void trainModel() throws Exception {
        if (data == null) {
            throw new Exception("No data loaded. Load data before training.");
        }

        System.out.println("Training " + classifierType + " classifier...");
        classifier.buildClassifier(data);
        System.out.println("Training complete.");
    }

    public Evaluation evaluateModel(int folds) throws Exception {
        if (data == null) {
            throw new Exception("No data loaded. Load data before evaluation.");
        }

        System.out.println("Evaluating model using " + folds + "-fold cross-validation...");
        Evaluation eval = new Evaluation(data);
        Random rand = new Random(1);
        eval.crossValidateModel(classifier, data, folds, rand);

        return eval;
    }

    public void printResults(Evaluation eval) throws Exception {
        System.out.println("\n=== Model Information ===");
        System.out.println(classifier.toString());

        System.out.println("\n=== Evaluation Results ===");
        System.out.println("Accuracy: " + (eval.pctCorrect()) + "%");
        System.out.println("Kappa statistic: " + eval.kappa());
        System.out.println("Mean absolute error: " + eval.meanAbsoluteError());
        System.out.println("Root mean squared error: " + eval.rootMeanSquaredError());

        System.out.println("\n=== Confusion Matrix ===");
        System.out.println(eval.toMatrixString());

        System.out.println("\n=== Detailed Accuracy By Class ===");
        System.out.println(eval.toClassDetailsString());
    }

    public void saveModel(String modelPath) throws Exception {
        weka.core.SerializationHelper.write(modelPath, classifier);
        System.out.println("Model saved to: " + modelPath);
    }

    public void saveResultsToFile(Evaluation eval, String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
            System.out.println("Created directory: " + parentDir.getAbsolutePath());
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputPath))) {
            writer.println("=== Model Information ===");
            writer.println(classifier.toString());

            writer.println("\n=== Evaluation Results ===");
            writer.println("Accuracy: " + (eval.pctCorrect()) + "%");
            writer.println("Kappa statistic: " + eval.kappa());
            writer.println("Mean absolute error: " + eval.meanAbsoluteError());
            writer.println("Root mean squared error: " + eval.rootMeanSquaredError());

            writer.println();
            writer.println(eval.toMatrixString());

            writer.println();
            writer.println(eval.toClassDetailsString());

            System.out.println("Evaluation results saved to: " + outputPath);
        }
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public Instances getData() {
        return data;
    }
}