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
    private weka.classifiers.Classifier classifier;

    public Instances[] runClustering(String dataPath, String method, double trainRatio) throws Exception {
        DataSource source = new DataSource(dataPath);
        originalData = source.getDataSet();
        int classIndex = originalData.numAttributes() - 1;
        originalData.setClassIndex(classIndex);

        originalData.randomize(new Random(42));

        int trainSize = (int) Math.round(originalData.numInstances() * trainRatio);
        int testSize = originalData.numInstances() - trainSize;

        originalTrainData = new Instances(originalData, 0, trainSize);
        originalTestData = new Instances(originalData, trainSize, testSize);

        originalTrainData.setClassIndex(-1);
        originalTestData.setClassIndex(-1);

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

        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(originalTrainData);
        Instances newTrainData = Filter.useFilter(originalTrainData, filter);
        Instances newTestData = Filter.useFilter(originalTestData, filter);

        clusterer.buildClusterer(newTrainData);

        ClusterEvaluation clusterEvaluation = new ClusterEvaluation();
        clusterEvaluation.setClusterer(clusterer);
        clusterEvaluation.evaluateClusterer(newTestData);
        clusterInfo = clusterEvaluation.clusterResultsToString();
        System.out.println(clusterer);

        originalTrainData.setClassIndex(classIndex);
        originalTestData.setClassIndex(classIndex);

        improvedTrainData = new Instances(originalTrainData);
        improvedTestData = new Instances(originalTestData);

        int numClusters = (clusterer instanceof EM)
                ? ((EM) clusterer).numberOfClusters()
                : ((SimpleKMeans) clusterer).getNumClusters();

        ArrayList<String> clusterNames = new ArrayList<>();
        for (int i = 0; i < numClusters; i++) {
            clusterNames.add("cluster" + i);
        }
        Attribute clusterAttr = new Attribute("cluster", clusterNames);

        improvedTrainData.insertAttributeAt(clusterAttr, improvedTrainData.classIndex());
        improvedTestData.insertAttributeAt(clusterAttr, improvedTestData.classIndex());

        for (int i = 0; i < improvedTrainData.numInstances(); i++) {
            int clusterIndex = clusterer.clusterInstance(newTrainData.instance(i));
            improvedTrainData.instance(i).setValue(improvedTrainData.classIndex() - 1, "cluster" + clusterIndex);
        }

        for (int i = 0; i < improvedTestData.numInstances(); i++) {
            int clusterIndex = clusterer.clusterInstance(newTestData.instance(i));
            improvedTestData.instance(i).setValue(improvedTestData.classIndex() - 1, "cluster" + clusterIndex);
        }

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
        ClassifierModel classifierModel = new ClassifierModel(null, classifierType, improved[0], improved[1]);
        classifierModel.trainModel();
        // Store the classifier for later access
        this.classifier = classifierModel.getClassifier();
        long endTimeModel = System.currentTimeMillis();
        
        long startTimeEval = System.currentTimeMillis();
        Evaluator evaluator = new Evaluator(classifierModel.getClassifier(), classifierModel.getTrainData(),
                classifierModel.getTestData());
        evaluator.evaluateOnTestData();
        evaluator.printResults();
        long endTimeEval = System.currentTimeMillis();

        String datasetName = dataPath.substring(dataPath.lastIndexOf("/") + 1, dataPath.lastIndexOf("."));
        String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results_improved_by_"
                + clusterMethod + ".txt";
        evaluator.saveResultsToFile(resultPath, clusterInfo, endTimeModel - startTimeModel, endTimeEval - startTimeEval, endClustering - startClustering);
    }

    public weka.classifiers.Classifier getClassifier() {
        return classifier;
    }
    
    public weka.clusterers.Clusterer getClusterer() {
        return clusterer;
    }

    public Instances getImprovedTrainData() {
        return improvedTrainData;
    }

    public Instances getImprovedTestData() {
        return improvedTestData;
    }

    public String getClusterInfo() {
        return clusterInfo;
    }
}

