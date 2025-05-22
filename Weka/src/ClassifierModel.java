// import weka.core.Instances;
// import weka.core.converters.ConverterUtils.DataSource;
// import weka.core.converters.CSVLoader;
// import java.io.File;
// import java.io.IOException;
// import weka.classifiers.Classifier;
// import weka.classifiers.trees.J48;
// import weka.classifiers.bayes.NaiveBayes;
// import weka.classifiers.functions.SMO;
// import weka.filters.Filter;
// import weka.filters.unsupervised.attribute.Normalize;
// import weka.filters.unsupervised.attribute.ReplaceMissingValues;
// import weka.filters.unsupervised.attribute.StringToWordVector;
// import weka.filters.supervised.attribute.AttributeSelection;
// import weka.attributeSelection.InfoGainAttributeEval;
// import weka.attributeSelection.Ranker;
// import weka.attributeSelection.CfsSubsetEval;
// import weka.attributeSelection.BestFirst;
// import weka.filters.unsupervised.attribute.NumericToNominal;
// import weka.filters.unsupervised.attribute.Discretize;

// public class ClassifierModel {
//     private Instances data;
//     private Classifier classifier;
//     private String classifierType;

//     public ClassifierModel(String dataPath, String classifierType, Instances improvedData) throws Exception {
//         this.classifierType = classifierType.toLowerCase();

//         if (improvedData != null) {
//             this.data = new Instances(improvedData);
//             handleMissingValues();
//             preprocessNumericData();

//             if (data.classAttribute().isNumeric() &&
//                     (classifierType.equals("j48") || classifierType.equals("naivebayes")
//                             || classifierType.equals("svm"))) {
//                 discretizeClassAttribute();
//             }

//             System.out.println("Loaded dataset: " + dataPath);
//             System.out.println("Number of instances: " + data.numInstances());
//             System.out.println("Number of attributes: " + data.numAttributes());
//             System.out.println("Class attribute: " + data.classAttribute().name());
//         } else {
//             loadData(dataPath);
//         }

//         initializeClassifier();
//     }

//     private void loadData(String dataPath) throws Exception {
//         try {
//             File f = new File(dataPath);
//             if (!f.exists()) {
//                 throw new IOException("File does not exist: " + f.getAbsolutePath());
//             }

//             if (dataPath.toLowerCase().endsWith(".csv")) {
//                 System.out.println("Converting CSV to ARFF format...");
//                 CSVLoader loader = new CSVLoader();
//                 loader.setSource(new File(dataPath));
//                 data = loader.getDataSet();
//             } else {
//                 DataSource source = new DataSource(dataPath);
//                 data = source.getDataSet();
//             }

//             if (data.classIndex() == -1) {
//                 data.setClassIndex(data.numAttributes() - 1);
//                 System.out.println("No class attribute specified. Using last attribute as class.");
//             }

//             handleMissingValues();
//             preprocessNumericData();

//             if (data.classAttribute().isNumeric() &&
//                     (classifierType.equals("j48") || classifierType.equals("naivebayes")
//                             || classifierType.equals("svm"))) {
//                 discretizeClassAttribute();
//             }

//             System.out.println("Loaded dataset: " + dataPath);
//             System.out.println("Number of instances: " + data.numInstances());
//             System.out.println("Number of attributes: " + data.numAttributes());
//             System.out.println("Class attribute: " + data.classAttribute().name());
//         } catch (Exception e) {
//             System.err.println("Error loading dataset: " + e.getMessage());
//             e.printStackTrace();
//             throw e;
//         }
//     }

//     private void handleMissingValues() throws Exception {
//         int missingCount = countMissingValues();
//         if (missingCount > 0) {
//             System.out.println("Detected " + missingCount + " missing values. Applying ReplaceMissingValues filter.");
//             ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
//             replaceMissing.setInputFormat(data);
//             data = Filter.useFilter(data, replaceMissing);
//         }
//     }

//     private int countMissingValues() {
//         int missingCount = 0;
//         for (int i = 0; i < data.numInstances(); i++) {
//             for (int j = 0; j < data.numAttributes(); j++) {
//                 if (data.instance(i).isMissing(j)) {
//                     missingCount++;
//                 }
//             }
//         }
//         return missingCount;
//     }

//     private void preprocessNumericData() throws Exception {
//         int numericCount = countNumericAttributes();
//         if (numericCount > 0) {
//             System.out.println("Detected " + numericCount + " numeric attributes. Applying normalization.");
//             Normalize normalize = new Normalize();
//             normalize.setInputFormat(data);
//             data = Filter.useFilter(data, normalize);

//             if (data.numAttributes() > 10) {
//                 applyAttributeSelection();
//             }
//         }
//     }

//     private int countNumericAttributes() {
//         int numericCount = 0;
//         for (int i = 0; i < data.numAttributes(); i++) {
//             if (i != data.classIndex() && data.attribute(i).isNumeric()) {
//                 numericCount++;
//             }
//         }
//         return numericCount;
//     }

//     private void applyAttributeSelection() throws Exception {
//         if (data.classAttribute().isNumeric()) {
//             System.out.println("Numeric class detected. Skipping InfoGain attribute selection.");
//             AttributeSelection attSelection = new AttributeSelection();
//             CfsSubsetEval eval = new CfsSubsetEval();
//             BestFirst search = new BestFirst();
//             attSelection.setEvaluator(eval);
//             attSelection.setSearch(search);
//             attSelection.setInputFormat(data);
//             data = Filter.useFilter(data, attSelection);
//             System.out.println("Applied CFS attribute selection. Reduced to " + data.numAttributes() + " attributes.");
//         } else {
//             AttributeSelection attSelection = new AttributeSelection();
//             InfoGainAttributeEval eval = new InfoGainAttributeEval();
//             Ranker search = new Ranker();
//             search.setThreshold(0.0);
//             attSelection.setEvaluator(eval);
//             attSelection.setSearch(search);
//             attSelection.setInputFormat(data);
//             data = Filter.useFilter(data, attSelection);
//             System.out.println(
//                     "Applied InfoGain attribute selection. Reduced to " + data.numAttributes() + " attributes.");
//         }
//     }

//     private void discretizeClassAttribute() throws Exception {
//         System.out.println("Converting numeric class to nominal for " + classifierType + " classifier...");
//         int classIdx = data.classIndex();

//         NumericToNominal convert = new NumericToNominal();
//         convert.setAttributeIndices("" + (classIdx + 1));
//         convert.setInputFormat(data);
//         data = Filter.useFilter(data, convert);

//         if (data.classAttribute().numValues() <= 1) {
//             System.out.println("Direct conversion failed. Trying discretization with bins...");
//             data.setClassIndex(-1);

//             Discretize discretize = new Discretize();
//             discretize.setAttributeIndices("" + (classIdx + 1));
//             discretize.setUseEqualFrequency(true);
//             discretize.setBins(5);
//             discretize.setInputFormat(data);
//             data = Filter.useFilter(data, discretize);
//             data.setClassIndex(classIdx);
//         }

//         System.out
//                 .println("Class attribute converted to nominal with " + data.classAttribute().numValues() + " values");
//     }

//     private void initializeClassifier() throws Exception {
//         switch (classifierType) {
//             case "j48":
//                 J48 j48 = new J48();
//                 j48.setConfidenceFactor(0.25f);
//                 j48.setMinNumObj(2);
//                 classifier = j48;
//                 System.out.println("Using J48 decision tree classifier");
//                 break;
//             case "naivebayes":
//                 classifier = new NaiveBayes();
//                 System.out.println("Using Naive Bayes classifier");
//                 break;
//             case "svm":
//                 classifier = new SMO();
//                 System.out.println("Using SVM classifier (SMO implementation)");
//                 break;
//             default:
//                 classifier = new J48();
//                 classifierType = "j48";
//                 System.out.println("Unknown classifier type. Defaulting to J48 decision tree.");
//         }
//     }

//     public void trainModel() throws Exception {
//         if (data == null) {
//             throw new Exception("No data loaded. Load data before training.");
//         }
//         System.out.println("Training " + classifierType + " classifier...");
//         StringToWordVector filter = new StringToWordVector();
//         filter.setInputFormat(data);
//         data = Filter.useFilter(data, filter);
//         classifier.buildClassifier(data);
//         System.out.println("Training complete.");
//     }

//     public Classifier getClassifier() {
//         return classifier;
//     }

//     public Instances getData() {
//         return data;
//     }
// }



import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.converters.CSVLoader;
import java.io.File;
import java.io.IOException;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.rules.OneR;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
// import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.BestFirst;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.Discretize;

public class ClassifierModel {
    private Instances trainData;
    private Instances testData;
    private Instances data;
    private Classifier classifier;
    private String classifierType;

    public ClassifierModel(String dataPath, String classifierType, Instances improvedTrainData, Instances improvedTestData) throws Exception {
        this.classifierType = classifierType.toLowerCase();

        if (improvedTrainData != null && improvedTestData != null) {
            this.trainData = new Instances(improvedTrainData);
            this.testData = new Instances(improvedTestData);
            processData();
        } else {
            loadData(dataPath);
        }

        initializeClassifier();
    }

    private void loadData(String dataPath) throws Exception {
        try {
            File f = new File(dataPath);
            if (!f.exists()) {
                throw new IOException("File does not exist: " + f.getAbsolutePath());
            }

            if (dataPath.toLowerCase().endsWith(".csv")) {
                System.out.println("Converting CSV to ARFF format...");
                CSVLoader loader = new CSVLoader();
                loader.setSource(new File(dataPath));
                data = loader.getDataSet();
            } else {
                DataSource source = new DataSource(dataPath);
                data = source.getDataSet();
            }

            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
                System.out.println("No class attribute specified. Using last attribute as class.");
            }

            handleMissingValuesSingleData();
            preprocessNumericDataSingleData();

            if (data.classAttribute().isNumeric() &&
                    (classifierType.equals("j48") || classifierType.equals("naivebayes"))) {
                discretizeClassAttributeSingleData();
            }

            System.out.println("Loaded dataset: " + dataPath);
            System.out.println("Number of instances: " + data.numInstances());
            System.out.println("Number of attributes: " + data.numAttributes());
            System.out.println("Class attribute: " + data.classAttribute().name());
        } catch (Exception e) {
            System.err.println("Error loading dataset: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    

    private void processData() throws Exception {
        handleMissingValues();
        preprocessNumericData();
        if (trainData.classIndex() == -1) {
            trainData.setClassIndex(trainData.numAttributes() - 1);
        }
        if (trainData.classAttribute().isNumeric() &&
                (classifierType.equals("j48") || classifierType.equals("naivebayes"))) {
            discretizeClassAttribute();
        }
    }

    private void handleMissingValues() throws Exception {
        int trainMissingCount = countMissingValues(trainData);
        if (trainMissingCount > 0) {
            System.out.println("Detected " + trainMissingCount + " missing values in training data. Applying ReplaceMissingValues filter.");
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(trainData);
            trainData = Filter.useFilter(trainData, replaceMissing);
        }

        // Process test data using the same filter settings
        int testMissingCount = countMissingValues(testData);
        if (testMissingCount > 0) {
            System.out.println("Detected " + testMissingCount + " missing values in test data. Applying ReplaceMissingValues filter.");
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(trainData);
            testData = Filter.useFilter(testData, replaceMissing);
        }
    }

    private void handleMissingValuesSingleData() throws Exception {
        int missingCount = countMissingValues(data);
        if (missingCount > 0) {
            System.out.println("Detected " + missingCount + " missing values. Applying ReplaceMissingValues filter.");
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(data);
            data = Filter.useFilter(data, replaceMissing);
        }
    }

    private int countMissingValues(Instances data) {
        int missingCount = 0;
        for (int i = 0; i < data.numInstances(); i++) {
            for (int j = 0; j < data.numAttributes(); j++) {
                if (data.instance(i).isMissing(j)) {
                    missingCount++;
                }
            }
        }
        return missingCount;
    }

    private void preprocessNumericData() throws Exception {
        int trainNumericCount = countNumericAttributes(trainData);
        if (trainNumericCount > 0) {
            System.out.println("Detected " + trainNumericCount + " numeric attributes in training data. Applying normalization.");
            Normalize normalize = new Normalize();
            normalize.setInputFormat(trainData);
            trainData = Filter.useFilter(trainData, normalize);

            testData = Filter.useFilter(testData, normalize);

            if (trainData.numAttributes() > 10) {
                applyAttributeSelection();
            }
        }
    }

    private void preprocessNumericDataSingleData() throws Exception {
        int numericCount = countNumericAttributes(data);
        if (numericCount > 0) {
            System.out.println("Detected " + numericCount + " numeric attributes. Applying normalization.");
            Normalize normalize = new Normalize();
            normalize.setInputFormat(data);
            data = Filter.useFilter(data, normalize);

            if (data.numAttributes() > 10) {
                applyAttributeSelectionSingleData();
            }
        }
    }

    private int countNumericAttributes(Instances data) {
        int numericCount = 0;
        for (int i = 0; i < data.numAttributes(); i++) {
            if (i != data.classIndex() && data.attribute(i).isNumeric()) {
                numericCount++;
            }
        }
        return numericCount;
    }

    private void applyAttributeSelection() throws Exception {
        if (trainData.classAttribute().isNumeric()) {
            System.out.println("Numeric class detected. Applying CFS attribute selection.");
            AttributeSelection attSelection = new AttributeSelection();
            CfsSubsetEval eval = new CfsSubsetEval();
            BestFirst search = new BestFirst();
            attSelection.setEvaluator(eval);
            attSelection.setSearch(search);
            attSelection.setInputFormat(trainData);
            trainData = Filter.useFilter(trainData, attSelection);
            testData = Filter.useFilter(testData, attSelection);
            System.out.println("Applied CFS attribute selection. Reduced to " + trainData.numAttributes() + " attributes.");
        } else {
            System.out.println("Applying InfoGain attribute selection.");
            AttributeSelection attSelection = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            search.setThreshold(0.0);
            attSelection.setEvaluator(eval);
            attSelection.setSearch(search);
            attSelection.setInputFormat(trainData);
            trainData = Filter.useFilter(trainData, attSelection);
            testData = Filter.useFilter(testData, attSelection);
            System.out.println("Applied InfoGain attribute selection. Reduced to " + trainData.numAttributes() + " attributes.");
        }
    }

    private void applyAttributeSelectionSingleData() throws Exception {
        if (data.classAttribute().isNumeric()) {
            System.out.println("Numeric class detected. Skipping InfoGain attribute selection.");
            AttributeSelection attSelection = new AttributeSelection();
            CfsSubsetEval eval = new CfsSubsetEval();
            BestFirst search = new BestFirst();
            attSelection.setEvaluator(eval);
            attSelection.setSearch(search);
            attSelection.setInputFormat(data);
            data = Filter.useFilter(data, attSelection);
            System.out.println("Applied CFS attribute selection. Reduced to " + data.numAttributes() + " attributes.");
        } else {
            AttributeSelection attSelection = new AttributeSelection();
            InfoGainAttributeEval eval = new InfoGainAttributeEval();
            Ranker search = new Ranker();
            search.setThreshold(0.0);
            attSelection.setEvaluator(eval);
            attSelection.setSearch(search);
            attSelection.setInputFormat(data);
            data = Filter.useFilter(data, attSelection);
            System.out.println(
                    "Applied InfoGain attribute selection. Reduced to " + data.numAttributes() + " attributes.");
        }
    }

    private void discretizeClassAttribute() throws Exception {
        System.out.println("Converting numeric class to nominal for " + classifierType + " classifier...");
        int classIdx = trainData.classIndex();

        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("" + (classIdx + 1));
        convert.setInputFormat(trainData);
        trainData = Filter.useFilter(trainData, convert);
        testData = Filter.useFilter(testData, convert);

        if (trainData.classAttribute().numValues() <= 1) {
            System.out.println("Direct conversion failed. Trying discretization with bins...");
            trainData.setClassIndex(-1);
            testData.setClassIndex(-1);

            Discretize discretize = new Discretize();
            discretize.setAttributeIndices("" + (classIdx + 1));
            discretize.setUseEqualFrequency(true);
            discretize.setBins(5);
            discretize.setInputFormat(trainData);
            trainData = Filter.useFilter(trainData, discretize);
            testData = Filter.useFilter(testData, discretize);

            trainData.setClassIndex(classIdx);
            testData.setClassIndex(classIdx);
        }

        System.out.println("Class attribute converted to nominal with " + trainData.classAttribute().numValues() + " values");
    }

    private void discretizeClassAttributeSingleData() throws Exception {
        System.out.println("Converting numeric class to nominal for " + classifierType + " classifier...");
        int classIdx = data.classIndex();

        NumericToNominal convert = new NumericToNominal();
        convert.setAttributeIndices("" + (classIdx + 1));
        convert.setInputFormat(data);
        data = Filter.useFilter(data, convert);

        if (data.classAttribute().numValues() <= 1) {
            System.out.println("Direct conversion failed. Trying discretization with bins...");
            data.setClassIndex(-1);

            Discretize discretize = new Discretize();
            discretize.setAttributeIndices("" + (classIdx + 1));
            discretize.setUseEqualFrequency(true);
            discretize.setBins(5);
            discretize.setInputFormat(data);
            data = Filter.useFilter(data, discretize);
            data.setClassIndex(classIdx);
        }

        System.out
                .println("Class attribute converted to nominal with " + data.classAttribute().numValues() + " values");
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
            case "oner":
                classifier = new OneR();
                System.out.println("Using OneR classifier (simple one-rule learner)");
                break;
            default:
                classifier = new J48();
                classifierType = "j48";
                System.out.println("Unknown classifier type. Defaulting to J48 decision tree.");
        }
    }

    public void trainModel() throws Exception {
        if (trainData == null) {
            throw new Exception("No training data loaded. Load data before training.");
        }
        System.out.println("Training " + classifierType + " classifier...");
        // StringToWordVector filter = new StringToWordVector();
        // filter.setInputFormat(trainData);
        // trainData = Filter.useFilter(trainData, filter);
        // testData = Filter.useFilter(testData, filter);

        classifier.buildClassifier(trainData);
        System.out.println("Training complete.");
    }

    public void trainModelSingleData() throws Exception {
        if (data == null) {
            throw new Exception("No data loaded. Load data before training.");
        }
        System.out.println("Training " + classifierType + " classifier...");
        // StringToWordVector filter = new StringToWordVector();
        // filter.setInputFormat(data);
        // data = Filter.useFilter(data, filter);
        // if (data.classIndex() == -1) {
        //     data.setClassIndex(data.numAttributes() - 1);
        //     discretizeClassAttributeSingleData();
        // }
        discretizeClassAttributeSingleData();
        classifier.buildClassifier(data);
        System.out.println("Training complete.");
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public Instances getTrainData() {
        return trainData;
    }

    public Instances getTestData() {
        return testData;
    }

    public Instances getData(){
        return data;
    }
}




///////////////////////////////////////////////////////////////////////////
// import weka.core.Instances;
// import weka.core.converters.ConverterUtils.DataSource;
// import weka.core.converters.CSVLoader;
// import java.io.File;
// import java.io.IOException;
// import weka.classifiers.Classifier;
// import weka.classifiers.trees.J48;
// import weka.classifiers.bayes.NaiveBayes;
// import weka.classifiers.functions.SMO;
// import weka.filters.Filter;
// import weka.filters.unsupervised.attribute.Normalize;
// import weka.filters.unsupervised.attribute.ReplaceMissingValues;
// import weka.filters.unsupervised.attribute.StringToWordVector;
// import weka.filters.supervised.attribute.AttributeSelection;
// import weka.attributeSelection.InfoGainAttributeEval;
// import weka.attributeSelection.Ranker;
// import weka.attributeSelection.CfsSubsetEval;
// import weka.attributeSelection.BestFirst;
// import weka.filters.unsupervised.attribute.NumericToNominal;
// import weka.filters.unsupervised.attribute.Discretize;

// public class ClassifierModel {
//     private Instances trainData;
//     private Instances testData;
//     private Classifier classifier;
//     private String classifierType;

//     public ClassifierModel(String trainPath, String testPath, String classifierType, Instances improvedTrainData, Instances improvedTestData) throws Exception {
//         this.classifierType = classifierType.toLowerCase();

//         if (improvedTrainData != null && improvedTestData != null) {
//             this.trainData = new Instances(improvedTrainData);
//             this.testData = new Instances(improvedTestData);
//             processData();
//         } else {
//             loadData(trainPath, testPath);
//         }

//         initializeClassifier();
//     }

//     private void loadData(String trainPath, String testPath) throws Exception {
//         try {
//             // Load training data
//             File trainFile = new File(trainPath);
//             if (!trainFile.exists()) {
//                 throw new IOException("Training file does not exist: " + trainFile.getAbsolutePath());
//             }

//             if (trainPath.toLowerCase().endsWith(".csv")) {
//                 System.out.println("Converting training CSV to ARFF format...");
//                 CSVLoader loader = new CSVLoader();
//                 loader.setSource(new File(trainPath));
//                 trainData = loader.getDataSet();
//             } else {
//                 DataSource source = new DataSource(trainPath);
//                 trainData = source.getDataSet();
//             }

//             // Load test data
//             File testFile = new File(testPath);
//             if (!testFile.exists()) {
//                 throw new IOException("Test file does not exist: " + testFile.getAbsolutePath());
//             }

//             if (testPath.toLowerCase().endsWith(".csv")) {
//                 System.out.println("Converting test CSV to ARFF format...");
//                 CSVLoader loader = new CSVLoader();
//                 loader.setSource(new File(testPath));
//                 testData = loader.getDataSet();
//             } else {
//                 DataSource source = new DataSource(testPath);
//                 testData = source.getDataSet();
//             }

//             if (trainData.classIndex() == -1) {
//                 trainData.setClassIndex(trainData.numAttributes() - 1);
//                 System.out.println("No class attribute specified in training data. Using last attribute as class.");
//             }
//             if (testData.classIndex() == -1) {
//                 testData.setClassIndex(testData.numAttributes() - 1);
//                 System.out.println("No class attribute specified in test data. Using last attribute as class.");
//             }

//             processData();

//             System.out.println("Loaded training dataset: " + trainPath);
//             System.out.println("Training instances: " + trainData.numInstances());
//             System.out.println("Training attributes: " + trainData.numAttributes());
//             System.out.println("Training class attribute: " + trainData.classAttribute().name());
//             System.out.println("Loaded test dataset: " + testPath);
//             System.out.println("Test instances: " + testData.numInstances());
//             System.out.println("Test attributes: " + testData.numAttributes());
//             System.out.println("Test class attribute: " + testData.classAttribute().name());
//         } catch (Exception e) {
//             System.err.println("Error loading datasets: " + e.getMessage());
//             e.printStackTrace();
//             throw e;
//         }
//     }

//     private void processData() throws Exception {
//         handleMissingValues();
//         preprocessNumericData();

//         if (trainData.classAttribute().isNumeric() &&
//                 (classifierType.equals("j48") || classifierType.equals("naivebayes") || classifierType.equals("svm"))) {
//             discretizeClassAttribute();
//         }
//     }

//     private void handleMissingValues() throws Exception {
//         // Process training data
//         int trainMissingCount = countMissingValues(trainData);
//         if (trainMissingCount > 0) {
//             System.out.println("Detected " + trainMissingCount + " missing values in training data. Applying ReplaceMissingValues filter.");
//             ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
//             replaceMissing.setInputFormat(trainData);
//             trainData = Filter.useFilter(trainData, replaceMissing);
//         }

//         // Process test data using the same filter settings
//         int testMissingCount = countMissingValues(testData);
//         if (testMissingCount > 0) {
//             System.out.println("Detected " + testMissingCount + " missing values in test data. Applying ReplaceMissingValues filter.");
//             ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
//             replaceMissing.setInputFormat(trainData); // Use trainData format to ensure consistency
//             testData = Filter.useFilter(testData, replaceMissing);
//         }
//     }

//     private int countMissingValues(Instances data) {
//         int missingCount = 0;
//         for (int i = 0; i < data.numInstances(); i++) {
//             for (int j = 0; j < data.numAttributes(); j++) {
//                 if (data.instance(i).isMissing(j)) {
//                     missingCount++;
//                 }
//             }
//         }
//         return missingCount;
//     }

//     private void preprocessNumericData() throws Exception {
//         // Process training data
//         int trainNumericCount = countNumericAttributes(trainData);
//         if (trainNumericCount > 0) {
//             System.out.println("Detected " + trainNumericCount + " numeric attributes in training data. Applying normalization.");
//             Normalize normalize = new Normalize();
//             normalize.setInputFormat(trainData);
//             trainData = Filter.useFilter(trainData, normalize);

//             // Apply same normalization to test data
//             testData = Filter.useFilter(testData, normalize);

//             if (trainData.numAttributes() > 10) {
//                 applyAttributeSelection();
//             }
//         }
//     }

//     private int countNumericAttributes(Instances data) {
//         int numericCount = 0;
//         for (int i = 0; i < data.numAttributes(); i++) {
//             if (i != data.classIndex() && data.attribute(i).isNumeric()) {
//                 numericCount++;
//             }
//         }
//         return numericCount;
//     }

//     private void applyAttributeSelection() throws Exception {
//         if (trainData.classAttribute().isNumeric()) {
//             System.out.println("Numeric class detected. Applying CFS attribute selection.");
//             AttributeSelection attSelection = new AttributeSelection();
//             CfsSubsetEval eval = new CfsSubsetEval();
//             BestFirst search = new BestFirst();
//             attSelection.setEvaluator(eval);
//             attSelection.setSearch(search);
//             attSelection.setInputFormat(trainData);
//             trainData = Filter.useFilter(trainData, attSelection);
//             testData = Filter.useFilter(testData, attSelection);
//             System.out.println("Applied CFS attribute selection. Reduced to " + trainData.numAttributes() + " attributes.");
//         } else {
//             System.out.println("Applying InfoGain attribute selection.");
//             AttributeSelection attSelection = new AttributeSelection();
//             InfoGainAttributeEval eval = new InfoGainAttributeEval();
//             Ranker search = new Ranker();
//             search.setThreshold(0.0);
//             attSelection.setEvaluator(eval);
//             attSelection.setSearch(search);
//             attSelection.setInputFormat(trainData);
//             trainData = Filter.useFilter(trainData, attSelection);
//             testData = Filter.useFilter(testData, attSelection);
//             System.out.println("Applied InfoGain attribute selection. Reduced to " + trainData.numAttributes() + " attributes.");
//         }
//     }

//     private void discretizeClassAttribute() throws Exception {
//         // Process training data
//         System.out.println("Converting numeric class to nominal for " + classifierType + " classifier...");
//         int classIdx = trainData.classIndex();

//         NumericToNominal convert = new NumericToNominal();
//         convert.setAttributeIndices("" + (classIdx + 1));
//         convert.setInputFormat(trainData);
//         trainData = Filter.useFilter(trainData, convert);
//         testData = Filter.useFilter(testData, convert);

//         if (trainData.classAttribute().numValues() <= 1) {
//             System.out.println("Direct conversion failed. Trying discretization with bins...");
//             trainData.setClassIndex(-1);
//             testData.setClassIndex(-1);

//             Discretize discretize = new Discretize();
//             discretize.setAttributeIndices("" + (classIdx + 1));
//             discretize.setUseEqualFrequency(true);
//             discretize.setBins(5);
//             discretize.setInputFormat(trainData);
//             trainData = Filter.useFilter(trainData, discretize);
//             testData = Filter.useFilter(testData, discretize);

//             trainData.setClassIndex(classIdx);
//             testData.setClassIndex(classIdx);
//         }

//         System.out.println("Class attribute converted to nominal with " + trainData.classAttribute().numValues() + " values");
//     }

//     private void initializeClassifier() throws Exception {
//         switch (classifierType) {
//             case "j48":
//                 J48 j48 = new J48();
//                 j48.setConfidenceFactor(0.25f);
//                 j48.setMinNumObj(2);
//                 classifier = j48;
//                 System.out.println("Using J48 decision tree classifier");
//                 break;
//             case "naivebayes":
//                 classifier = new NaiveBayes();
//                 System.out.println("Using Naive Bayes classifier");
//                 break;
//             case "svm":
//                 classifier = new SMO();
//                 System.out.println("Using SVM classifier (SMO implementation)");
//                 break;
//             default:
//                 classifier = new J48();
//                 classifierType = "j48";
//                 System.out.println("Unknown classifier type. Defaulting to J48 decision tree.");
//         }
//     }

//     public void trainModel() throws Exception {
//         if (trainData == null) {
//             throw new Exception("No training data loaded. Load data before training.");
//         }
//         System.out.println("Training " + classifierType + " classifier...");
//         StringToWordVector filter = new StringToWordVector();
//         filter.setInputFormat(trainData);
//         trainData = Filter.useFilter(trainData, filter);
//         testData = Filter.useFilter(testData, filter);
//         classifier.buildClassifier(trainData);
//         System.out.println("Training complete.");
//     }

//     public Classifier getClassifier() {
//         return classifier;
//     }

//     public Instances getTrainData() {
//         return trainData;
//     }

//     public Instances getTestData() {
//         return testData;
//     }
// }


