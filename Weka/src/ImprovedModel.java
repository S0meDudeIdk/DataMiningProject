// import java.util.ArrayList;

// import weka.classifiers.Evaluation;
// import weka.clusterers.Clusterer;
// import weka.clusterers.SimpleKMeans;
// import weka.clusterers.EM;
// import weka.core.Instances;
// import weka.core.Attribute;
// import weka.core.converters.ConverterUtils.DataSource;
// import weka.filters.Filter;
// import weka.filters.unsupervised.attribute.StringToWordVector;

// public class ImprovedModel {
//     private Instances originalData;
//     private Instances improvedData;

//     public Instances runClustering(String dataPath, String method) throws Exception {
//     DataSource source = new DataSource(dataPath);
//     originalData = source.getDataSet();
//     int classIndex = originalData.numAttributes() - 1;
//     originalData.setClassIndex(classIndex);

//     // Temporarily remove class attribute for clustering
//     originalData.setClassIndex(-1);

//     Clusterer clusterer;
//     switch (method.toLowerCase()){
//         case "em":
//             EM em = new EM();
//             em.setNumClusters(-1);
//             clusterer = em;
//             System.out.println("Using EM clustering");
//             break;
//         case "kmeans":
//         default:
//             SimpleKMeans kMeans = new SimpleKMeans();
//             kMeans.setNumClusters(2); // consider tuning this
//             kMeans.setSeed(10);
//             kMeans.setPreserveInstancesOrder(true);
//             clusterer = kMeans;
//             System.out.println("Using KMeans clustering");
//             break;
//     }
//     StringToWordVector filter = new StringToWordVector();
//     filter.setInputFormat(originalData);
//     Instances newData = Filter.useFilter(originalData, filter);
//     clusterer.buildClusterer(newData);

//     // Restore class attribute before copying
//     originalData.setClassIndex(classIndex);

//     improvedData = new Instances(originalData);

//     int numClusters;
//     if(clusterer instanceof EM) {
//         numClusters = ((EM) clusterer).numberOfClusters();
//     } else if (clusterer instanceof SimpleKMeans) {
//         numClusters = ((SimpleKMeans) clusterer).getNumClusters();
//     } else {
//         numClusters = -1;
//     }

//     ArrayList<String> clusterNames = new ArrayList<>();
//     for(int i=0; i<numClusters; i++){
//         clusterNames.add("cluster" + i);
//     }
//     Attribute clusterAttr = new Attribute("cluster", clusterNames);

//     // Insert cluster attribute before class attribute to keep class index stable
//     improvedData.insertAttributeAt(clusterAttr, improvedData.classIndex());

//     for(int i=0; i<improvedData.numInstances(); i++){
//         int clusterIndex = clusterer.clusterInstance(newData.instance(i));
//         improvedData.instance(i).setValue(improvedData.classIndex() - 1, "cluster" + clusterIndex);
//     }

//     // Set class index again (should be unchanged)
//     improvedData.setClassIndex(classIndex);

//     System.out.println("Clustering (" + method + ") complete. Cluster info added.");

//     return improvedData;
// }

//     public void runWithClassifier(String dataPath, String clusterMethod, String classifiertype, int folds) throws Exception {
//         Instances improved = runClustering(dataPath, clusterMethod);
//         ClassifierModel classifier = new ClassifierModel(null, classifiertype, improved);
//         classifier.trainModel();
//         Evaluator evaluator = new Evaluator(classifier.getClassifier(), classifier.getData());
//         evaluator.crossValidate(10);
//         evaluator.printResults();
//         String datasetName = dataPath.substring(dataPath.lastIndexOf("/")+1, dataPath.lastIndexOf("."));
//         String resultPath = "Weka/results/" + datasetName + "_" + classifiertype +"_results_improved_by_"+ clusterMethod +".txt";
//         evaluator.saveResultsToFile(resultPath);
//     }

// }

// import java.util.ArrayList;

// import weka.classifiers.Evaluation;
// import weka.clusterers.Clusterer;
// import weka.clusterers.SimpleKMeans;
// import weka.clusterers.EM;
// import weka.core.Instances;
// import weka.core.Attribute;
// import weka.core.converters.ConverterUtils.DataSource;
// import weka.filters.Filter;
// import weka.filters.unsupervised.attribute.StringToWordVector;

// public class ImprovedModel {
//     private Instances originalTrainData;
//     private Instances originalTestData;
//     private Instances improvedTrainData;
//     private Instances improvedTestData;
//     private Clusterer clusterer;

//     public Instances[] runClustering(String trainPath, String testPath, String method) throws Exception {
//         // Load training data
//         DataSource trainSource = new DataSource(trainPath);
//         originalTrainData = trainSource.getDataSet();
//         int classIndex = originalTrainData.numAttributes() - 1;
//         originalTrainData.setClassIndex(classIndex);

//         // Load test data
//         DataSource testSource = new DataSource(testPath);
//         originalTestData = testSource.getDataSet();
//         originalTestData.setClassIndex(classIndex);

//         // Temporarily remove class attribute for clustering
//         originalTrainData.setClassIndex(-1);
//         originalTestData.setClassIndex(-1);

//         // Initialize clusterer
//         switch (method.toLowerCase()) {
//             case "em":
//                 EM em = new EM();
//                 em.setNumClusters(-1);
//                 clusterer = em;
//                 System.out.println("Using EM clustering");
//                 break;
//             case "kmeans":
//             default:
//                 SimpleKMeans kMeans = new SimpleKMeans();
//                 kMeans.setNumClusters(2); // Consider tuning this
//                 kMeans.setSeed(10);
//                 kMeans.setPreserveInstancesOrder(true);
//                 clusterer = kMeans;
//                 System.out.println("Using KMeans clustering");
//                 break;
//         }

//         // Apply StringToWordVector filter
//         StringToWordVector filter = new StringToWordVector();
//         filter.setInputFormat(originalTrainData);
//         Instances newTrainData = Filter.useFilter(originalTrainData, filter);
//         Instances newTestData = Filter.useFilter(originalTestData, filter);

//         // Build clusterer on training data
//         clusterer.buildClusterer(newTrainData);

//         // Restore class attribute
//         originalTrainData.setClassIndex(classIndex);
//         originalTestData.setClassIndex(classIndex);

//         // Initialize improved datasets
//         improvedTrainData = new Instances(originalTrainData);
//         improvedTestData = new Instances(originalTestData);

//         // Get number of clusters
//         int numClusters;
//         if (clusterer instanceof EM) {
//             numClusters = ((EM) clusterer).numberOfClusters();
//         } else if (clusterer instanceof SimpleKMeans) {
//             numClusters = ((SimpleKMeans) clusterer).getNumClusters();
//         } else {
//             numClusters = -1;
//         }

//         // Create cluster attribute
//         ArrayList<String> clusterNames = new ArrayList<>();
//         for (int i = 0; i < numClusters; i++) {
//             clusterNames.add("cluster" + i);
//         }
//         Attribute clusterAttr = new Attribute("cluster", clusterNames);

//         // Insert cluster attribute before class attribute
//         improvedTrainData.insertAttributeAt(clusterAttr, improvedTrainData.classIndex());
//         improvedTestData.insertAttributeAt(clusterAttr, improvedTestData.classIndex());

//         // Assign clusters to training data
//         for (int i = 0; i < improvedTrainData.numInstances(); i++) {
//             int clusterIndex = clusterer.clusterInstance(newTrainData.instance(i));
//             improvedTrainData.instance(i).setValue(improvedTrainData.classIndex() - 1, "cluster" + clusterIndex);
//         }

//         // Assign clusters to test data
//         for (int i = 0; i < improvedTestData.numInstances(); i++) {
//             int clusterIndex = clusterer.clusterInstance(newTestData.instance(i));
//             improvedTestData.instance(i).setValue(improvedTestData.classIndex() - 1, "cluster" + clusterIndex);
//         }

//         // Set class index
//         improvedTrainData.setClassIndex(classIndex);
//         improvedTestData.setClassIndex(classIndex);

//         System.out.println("Clustering (" + method + ") complete. Cluster info added to both datasets.");

//         return new Instances[]{improvedTrainData, improvedTestData};
//     }

//     public void runWithClassifier(String trainPath, String testPath, String clusterMethod, String classifierType, int folds) throws Exception {
//         Instances[] improved = runClustering(trainPath, testPath, clusterMethod);
//         ClassifierModel classifier = new ClassifierModel(null, null, classifierType, improved[0], improved[1]);
//         classifier.trainModel();
//         Evaluator evaluator = new Evaluator(classifier.getClassifier(), classifier.getTrainData(), classifier.getTestData());
//         evaluator.crossValidate(folds);
//         evaluator.evaluateOnTestData();
//         evaluator.printResults(true);
//         String datasetName = trainPath.substring(trainPath.lastIndexOf("/") + 1, trainPath.lastIndexOf("."));
//         String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results_improved_by_" + clusterMethod + ".txt";
//         evaluator.saveResultsToFile(resultPath, true);
//     }
// }

import java.util.ArrayList;
// import java.util.Collections;
import java.util.Random;

// import weka.classifiers.Evaluation;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.core.Attribute;
// import weka.core.Instance;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ImprovedModel {
    private Instances originalData;
    private Instances originalTrainData;
    private Instances originalTestData;
    private Instances improvedTrainData;
    private Instances improvedTestData;
    private Clusterer clusterer;
    private String clusterInfo;

    public Instances[] runClustering(String dataPath, String method, double trainRatio) throws Exception {
        // Load full dataset
        DataSource source = new DataSource(dataPath);
        originalData = source.getDataSet();
        int classIndex = originalData.numAttributes() - 1;
        originalData.setClassIndex(classIndex);

        // Shuffle data
        originalData.randomize(new Random(42));

        // Split dataset
        int trainSize = (int) Math.round(originalData.numInstances() * trainRatio);
        int testSize = originalData.numInstances() - trainSize;

        originalTrainData = new Instances(originalData, 0, trainSize);
        originalTestData = new Instances(originalData, trainSize, testSize);

        // Temporarily remove class attribute for clustering
        originalTrainData.setClassIndex(-1);
        originalTestData.setClassIndex(-1);

        // Initialize clusterer
        switch (method.toLowerCase()) {
            case "kmeans":
            default:
                SimpleKMeans kMeans = new SimpleKMeans();
                kMeans.setNumClusters(3);
                kMeans.setSeed(10);
                kMeans.setPreserveInstancesOrder(true);
                clusterer = kMeans;
                System.out.println("Using KMeans clustering");
                break;
        }

        // Apply StringToWordVector
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(originalTrainData);
        Instances newTrainData = Filter.useFilter(originalTrainData, filter);
        Instances newTestData = Filter.useFilter(originalTestData, filter);

        // Build clusterer
        clusterer.buildClusterer(newTrainData);

        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(newTestData);
        clusterInfo = clusterEvaluation.clusterResultsToString();
        System.out.println(clusterer);

        // Restore class attribute
        originalTrainData.setClassIndex(classIndex);
        originalTestData.setClassIndex(classIndex);

        // Initialize improved datasets
        improvedTrainData = new Instances(originalTrainData);
        improvedTestData = new Instances(originalTestData);

        // Determine number of clusters
        int numClusters = (clusterer instanceof EM)
                ? ((EM) clusterer).numberOfClusters()
                : ((SimpleKMeans) clusterer).getNumClusters();

        // Create nominal cluster attribute
        ArrayList<String> clusterNames = new ArrayList<>();
        for (int i = 0; i < numClusters; i++) {
            clusterNames.add("cluster" + i);
        }
        Attribute clusterAttr = new Attribute("cluster", clusterNames);

        // Insert cluster attribute before class
        improvedTrainData.insertAttributeAt(clusterAttr, improvedTrainData.classIndex());
        improvedTestData.insertAttributeAt(clusterAttr, improvedTestData.classIndex());

        // Assign clusters
        for (int i = 0; i < improvedTrainData.numInstances(); i++) {
            int clusterIndex = clusterer.clusterInstance(newTrainData.instance(i));
            improvedTrainData.instance(i).setValue(improvedTrainData.classIndex() - 1, "cluster" + clusterIndex);
        }

        for (int i = 0; i < improvedTestData.numInstances(); i++) {
            int clusterIndex = clusterer.clusterInstance(newTestData.instance(i));
            improvedTestData.instance(i).setValue(improvedTestData.classIndex() - 1, "cluster" + clusterIndex);
        }

        // Set class index
        // improvedTrainData.setClassIndex(improvedTrainData.numAttributes() - 1);
        // improvedTestData.setClassIndex(improvedTestData.numAttributes() - 1);
        improvedTrainData.setClassIndex(classIndex);
        improvedTestData.setClassIndex(classIndex);

        System.out.println("Clustering (" + method + ") complete. Data split and clusters added.");

        return new Instances[] { improvedTrainData, improvedTestData };
    }
    
    public void runWithClassifier(String dataPath, String clusterMethod, String classifierType, int folds)
            throws Exception {
        
        long startClustering = System.currentTimeMillis();
        Instances[] improved = runClustering(dataPath, clusterMethod, 0.8);
        long endClustering = System.currentTimeMillis();

        long startTimeModel = System.currentTimeMillis();
        ClassifierModel classifier = new ClassifierModel(null, classifierType, improved[0], improved[1]);
        classifier.trainModel();
        long endTimeModel = System.currentTimeMillis();
        
        long startTimeEval = System.currentTimeMillis();
        Evaluator evaluator = new Evaluator(classifier.getClassifier(), classifier.getTrainData(),
                classifier.getTestData());
        evaluator.evaluateOnTestData();
        evaluator.printResults();
        long endTimeEval = System.currentTimeMillis();

        String datasetName = dataPath.substring(dataPath.lastIndexOf("/") + 1, dataPath.lastIndexOf("."));
        String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results_improved_by_"
                + clusterMethod + ".txt";
        evaluator.saveResultsToFile(resultPath, clusterInfo, endTimeModel - startTimeModel, endTimeEval - startTimeEval, endClustering - startClustering);
    }
}


// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.Random;

// import weka.classifiers.Evaluation;
// import weka.clusterers.Clusterer;
// import weka.clusterers.SimpleKMeans;
// import weka.clusterers.EM;
// import weka.core.Instances;
// import weka.core.Attribute;
// import weka.core.Instance;
// import weka.core.converters.ConverterUtils.DataSource;
// import weka.filters.Filter;
// import weka.filters.unsupervised.attribute.StringToWordVector;

// public class ImprovedModel {
//     private Instances originalData;
//     private Instances originalTrainData;
//     private Instances originalTestData;
//     private Instances improvedTrainData;
//     private Instances improvedTestData;
//     private Clusterer clusterer;

//     public Instances[] runClustering(String dataPath, String method, double trainRatio) throws Exception {
//         // Load full dataset
//         DataSource source = new DataSource(dataPath);
//         originalData = source.getDataSet();
        
//         // Set class index (last attribute)
//         int classIndex = originalData.numAttributes() - 1;
//         originalData.setClassIndex(classIndex);
        
//         // Split dataset into training and test sets
//         splitDataset(trainRatio);

//         // Initialize clusterer
//         switch (method.toLowerCase()) {
//             case "em":
//                 EM em = new EM();
//                 em.setNumClusters(-1);
//                 clusterer = em;
//                 System.out.println("Using EM clustering");
//                 break;
//             case "kmeans":
//             default:
//                 SimpleKMeans kMeans = new SimpleKMeans();
//                 kMeans.setNumClusters(2);
//                 kMeans.setSeed(10);
//                 kMeans.setPreserveInstancesOrder(true);
//                 clusterer = kMeans;
//                 System.out.println("Using KMeans clustering");
//                 break;
//         }

//         // Temporarily remove class attribute for clustering
//         originalTrainData.setClassIndex(-1);
//         originalTestData.setClassIndex(-1);

//         // Apply StringToWordVector
//         StringToWordVector filter = new StringToWordVector();
//         filter.setInputFormat(originalTrainData);
//         Instances newTrainData = Filter.useFilter(originalTrainData, filter);
//         Instances newTestData = Filter.useFilter(originalTestData, filter);
        
//         // Build clusterer on training data
//         clusterer.buildClusterer(newTrainData);
        
//         // Restore class attribute
//         originalTrainData.setClassIndex(classIndex);
//         originalTestData.setClassIndex(classIndex);
        
//         // Create improved versions of the datasets
//         improvedTrainData = new Instances(originalTrainData);
//         improvedTestData = new Instances(originalTestData);
        
//         // Get number of clusters
//         int numClusters;
//         if (clusterer instanceof EM) {
//             numClusters = ((EM) clusterer).numberOfClusters();
//         } else if (clusterer instanceof SimpleKMeans) {
//             numClusters = ((SimpleKMeans) clusterer).getNumClusters();
//         } else {
//             numClusters = -1;
//         }
        
//         // Create cluster attribute
//         ArrayList<String> clusterNames = new ArrayList<>();
//         for (int i = 0; i < numClusters; i++) {
//             clusterNames.add("cluster" + i);
//         }
//         Attribute clusterAttr = new Attribute("cluster", clusterNames);
        
//         // Insert cluster attribute before class attribute to keep class index stable
//         improvedTrainData.insertAttributeAt(clusterAttr, improvedTrainData.classIndex());
//         improvedTestData.insertAttributeAt(clusterAttr, improvedTestData.classIndex());
        
//         // Assign cluster values to training instances
//         for (int i = 0; i < improvedTrainData.numInstances(); i++) {
//             int clusterIndex = clusterer.clusterInstance(newTrainData.instance(i));
//             improvedTrainData.instance(i).setValue(improvedTrainData.classIndex() - 1, "cluster" + clusterIndex);
//         }
        
//         // Assign cluster values to test instances
//         for (int i = 0; i < improvedTestData.numInstances(); i++) {
//             int clusterIndex = clusterer.clusterInstance(newTestData.instance(i));
//             improvedTestData.instance(i).setValue(improvedTestData.classIndex() - 1, "cluster" + clusterIndex);
//         }
        
//         // Set class index again
//         improvedTrainData.setClassIndex(classIndex);
//         improvedTestData.setClassIndex(classIndex);
        
//         System.out.println("Clustering (" + method + ") complete. Cluster info added.");
        
//         return new Instances[] { improvedTrainData, improvedTestData };
//     }
    
//     private void splitDataset(double trainRatio) {
//         // Copy original data to preserve it
//         Instances data = new Instances(originalData);
        
//         // Randomize the dataset
//         Random rand = new Random(42);  // Fixed seed for reproducibility
//         data.randomize(rand);
        
//         // Calculate sizes
//         int trainSize = (int) Math.round(data.numInstances() * trainRatio);
//         int testSize = data.numInstances() - trainSize;
        
//         // Create train and test datasets
//         originalTrainData = new Instances(data, 0, trainSize);
//         originalTestData = new Instances(data, trainSize, testSize);
//     }

//     public void runWithClassifier(String dataPath, String clusterMethod, String classifierType, int folds) throws Exception {
//         Instances[] improved = runClustering(dataPath, clusterMethod, 0.8);
//         ClassifierModel classifier = new ClassifierModel(null, classifierType, improved[0], improved[1]);
//         classifier.trainModel();
//         Evaluator evaluator = new Evaluator(classifier.getClassifier(), classifier.getTrainData(), classifier.getTestData());
//         evaluator.crossValidate(folds);
//         evaluator.evaluateOnTestData();
//         evaluator.printResults();
//         String datasetName = dataPath.substring(dataPath.lastIndexOf("/") + 1, dataPath.lastIndexOf("."));
//         String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results_improved_by_" + clusterMethod + ".txt";
//         evaluator.saveResultsToFile(resultPath);
//     }
// }