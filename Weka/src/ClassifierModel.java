import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;

public class ClassifierModel {
    private Instances data;
    private Classifier classifier;
    private String classifierType;

    public ClassifierModel(String dataPath, String classifierType, Instances improvedData) throws Exception {
        if (improvedData != null) {
            this.data = new Instances(improvedData);
        } else {
            loadData(dataPath);
        }
        this.classifierType = classifierType.toLowerCase();
        initializeClassifier();
    }

    private void loadData(String dataPath) throws Exception {
        DataSource source = new DataSource(dataPath);
        data = source.getDataSet();

        if (data.classIndex() == -1) {
            data.setClassIndex(data.numAttributes() - 1);
        }

        // Preprocess numeric data
        preprocessNumericData();

        System.out.println("Loaded dataset: " + dataPath);
        System.out.println("Number of instances: " + data.numInstances());
        System.out.println("Number of attributes: " + data.numAttributes());
        System.out.println("Class attribute: " + data.classAttribute().name());
    }

    private void preprocessNumericData() throws Exception {
        // Count numeric attributes
        int numericCount = 0;
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i != data.classIndex() && data.attribute(i).isNumeric()) {
                numericCount++;
            }
        }

        // Apply normalization if we have numeric attributes
        if (numericCount > 0) {
            System.out.println("Detected " + numericCount + " numeric attributes. Applying normalization.");
            Normalize normalize = new Normalize();
            normalize.setInputFormat(data);
            data = Filter.useFilter(data, normalize);

            // If dataset is high-dimensional, perform attribute selection
            if (data.numAttributes() > 10) {
                AttributeSelection attSelection = new AttributeSelection();
                InfoGainAttributeEval eval = new InfoGainAttributeEval();
                Ranker search = new Ranker();
                search.setThreshold(0.0);
                attSelection.setEvaluator(eval);
                attSelection.setSearch(search);
                attSelection.setInputFormat(data);
                data = Filter.useFilter(data, attSelection);
                System.out.println("Applied attribute selection. Reduced to " + data.numAttributes() + " attributes.");
            }
        }
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

    public Classifier getClassifier() {
        return classifier;
    }

    public Instances getData() {
        return data;
    }
}