import java.util.ArrayList;

import weka.classifiers.Evaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.clusterers.EM;
import weka.core.Instances;
import weka.core.Attribute;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ImprovedModel {
    private Instances originalData;
    private Instances improvedData;

    public Instances runClustering(String dataPath, String method) throws Exception {
    DataSource source = new DataSource(dataPath);
    originalData = source.getDataSet();
    int classIndex = originalData.numAttributes() - 1;
    originalData.setClassIndex(classIndex);

    // Temporarily remove class attribute for clustering
    originalData.setClassIndex(-1);

    Clusterer clusterer;
    switch (method.toLowerCase()){
        case "em":
            EM em = new EM();
            em.setNumClusters(-1);
            clusterer = em;
            System.out.println("Using EM clustering");
            break;
        case "kmeans":
        default:
            SimpleKMeans kMeans = new SimpleKMeans();
            kMeans.setNumClusters(2); // consider tuning this
            kMeans.setSeed(10);
            kMeans.setPreserveInstancesOrder(true);
            clusterer = kMeans;
            System.out.println("Using KMeans clustering");
            break;
    }
    StringToWordVector filter = new StringToWordVector();
    filter.setInputFormat(originalData);
    Instances newData = Filter.useFilter(originalData, filter);
    clusterer.buildClusterer(newData);

    // Restore class attribute before copying
    originalData.setClassIndex(classIndex);

    improvedData = new Instances(originalData);

    int numClusters;
    if(clusterer instanceof EM) {
        numClusters = ((EM) clusterer).numberOfClusters();
    } else if (clusterer instanceof SimpleKMeans) {
        numClusters = ((SimpleKMeans) clusterer).getNumClusters();
    } else {
        numClusters = -1;
    }

    ArrayList<String> clusterNames = new ArrayList<>();
    for(int i=0; i<numClusters; i++){
        clusterNames.add("cluster" + i);
    }
    Attribute clusterAttr = new Attribute("cluster", clusterNames);
    
    // Insert cluster attribute before class attribute to keep class index stable
    improvedData.insertAttributeAt(clusterAttr, improvedData.classIndex());

    for(int i=0; i<improvedData.numInstances(); i++){
        int clusterIndex = clusterer.clusterInstance(newData.instance(i));
        improvedData.instance(i).setValue(improvedData.classIndex() - 1, "cluster" + clusterIndex);
    }

    // Set class index again (should be unchanged)
    improvedData.setClassIndex(classIndex);

    System.out.println("Clustering (" + method + ") complete. Cluster info added.");

    return improvedData;
}


    public void runWithClassifier(String dataPath, String clusterMethod, String classifiertype, int folds) throws Exception {
        Instances improved = runClustering(dataPath, clusterMethod);
        ClassifierModel classifier = new ClassifierModel(null, classifiertype, improved);
        classifier.trainModel();
        Evaluator evaluator = new Evaluator(classifier.getClassifier(), classifier.getData());
        evaluator.crossValidate(10);
        evaluator.printResults();
        String datasetName = dataPath.substring(dataPath.lastIndexOf("/")+1, dataPath.lastIndexOf("."));
        String resultPath = "Weka/results/" + datasetName + "_" + classifiertype +"_results_improved_by_"+ clusterMethod +".txt";
        evaluator.saveResultsToFile(resultPath);
    }

}
