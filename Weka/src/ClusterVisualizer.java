import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.core.Instance;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Method;

public class ClusterVisualizer {
    
    /**
     * Visualizes KMeans clustering results
     * @param clusterer The trained KMeans clusterer
     * @param data The dataset with clustering results
     * @param datasetName Name of the dataset (for window title)
     * @throws Exception If visualization fails
     */
    public static void visualizeCluster(SimpleKMeans clusterer, Instances data, String datasetName) throws Exception {
        // Create a frame for visualization
        JFrame frame = new JFrame("Cluster Visualization for " + datasetName);
        frame.setSize(new Dimension(800, 600));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Create a text-based visualization of clusters
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
        
        // Build the cluster information text
        StringBuilder clusterInfo = new StringBuilder();
        clusterInfo.append("=== KMeans Clustering for " + datasetName + " ===\n\n");
        
        // Get cluster centroids and sizes
        Instances centroids = clusterer.getClusterCentroids();
        int numClusters = clusterer.getNumClusters();
        
        // Get cluster assignments - handle both possible return types
        int[] clusterSizes = new int[numClusters];
        
        try {
            // Try to get assignments using reflection to handle different return types
            Method getAssignmentsMethod = clusterer.getClass().getMethod("getAssignments");
            Object assignments = getAssignmentsMethod.invoke(clusterer);
            
            if (assignments instanceof int[]) {
                int[] intAssignments = (int[]) assignments;
                for (int i = 0; i < intAssignments.length; i++) {
                    clusterSizes[intAssignments[i]]++;
                }
            } else if (assignments instanceof double[]) {
                double[] doubleAssignments = (double[]) assignments;
                for (int i = 0; i < doubleAssignments.length; i++) {
                    clusterSizes[(int) doubleAssignments[i]]++;
                }
            }
        } catch (Exception e) {
            // If reflection fails, just note that cluster sizes couldn't be determined
            System.out.println("Could not determine cluster sizes: " + e.getMessage());
            for (int i = 0; i < numClusters; i++) {
                clusterSizes[i] = -1; // Unknown size
            }
        }
        
        // Display cluster information
        clusterInfo.append("Number of clusters: " + numClusters + "\n\n");
        
        for (int i = 0; i < numClusters; i++) {
            clusterInfo.append("Cluster " + i);
            if (clusterSizes[i] >= 0) {
                clusterInfo.append(" (" + clusterSizes[i] + " instances)");
            }
            clusterInfo.append("\n");
            clusterInfo.append("------------------------------\n");
            
            // Show centroid values
            Instance centroid = centroids.instance(i);
            clusterInfo.append("Centroid values:\n");
            
            for (int j = 0; j < centroid.numAttributes(); j++) {
                if (j != data.classIndex()) {
                    clusterInfo.append("  " + data.attribute(j).name() + ": ");
                    
                    if (data.attribute(j).isNominal()) {
                        int valueIndex = (int) centroid.value(j);
                        if (valueIndex >= 0 && valueIndex < data.attribute(j).numValues()) {
                            clusterInfo.append(data.attribute(j).value(valueIndex));
                        } else {
                            clusterInfo.append("Unknown (" + valueIndex + ")");
                        }
                    } else {
                        clusterInfo.append(String.format("%.4f", centroid.value(j)));
                    }
                    clusterInfo.append("\n");
                }
            }
            clusterInfo.append("\n");
        }
        
        // Set the text in the text area
        textArea.setText(clusterInfo.toString());
        
        // Add text area to a scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // Display the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Save cluster visualization as DOT file
        saveClusterToDotFile(clusterer, data, datasetName, clusterSizes);
    }
    
    /**
     * Saves cluster visualization to a DOT file
     * @param clusterer The trained KMeans clusterer
     * @param data The dataset with clustering results
     * @param datasetName Name of the dataset (for filenames)
     * @param clusterSizes Array containing the size of each cluster
     * @throws Exception If saving fails
     */
    private static void saveClusterToDotFile(SimpleKMeans clusterer, Instances data, 
            String datasetName, int[] clusterSizes) throws Exception {
        // Create DOT file representation of clusters
        String dotFilePath = "Weka/results/" + datasetName + "_kmeans_clusters.dot";
        
        // Ensure directory exists
        File outputFile = new File(dotFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        // Create DOT file content
        StringBuilder dotContent = new StringBuilder();
        dotContent.append("digraph KMeansClusters {\n");
        dotContent.append("  rankdir=TB;\n");
        dotContent.append("  node [shape=box, style=filled, color=lightblue];\n");
        dotContent.append("  edge [color=black];\n\n");
        
        // Add dataset node
        dotContent.append("  dataset [label=\"").append(datasetName).append("\", shape=ellipse, fillcolor=gold];\n\n");
        
        // Get cluster centers
        Instances centers = clusterer.getClusterCentroids();
        int numClusters = clusterer.getNumClusters();
        
        // Add cluster nodes
        for (int i = 0; i < numClusters; i++) {
            dotContent.append("  cluster").append(i).append(" [label=\"Cluster ").append(i);
            if (clusterSizes[i] >= 0) {
                dotContent.append("\\n").append(clusterSizes[i]).append(" instances");
            }
            dotContent.append("\", fillcolor=lightsalmon];\n");
            dotContent.append("  dataset -> cluster").append(i).append(";\n");
            
            // Add attributes for each cluster
            Instance center = centers.instance(i);
            for (int j = 0; j < center.numAttributes(); j++) {
                if (j != data.classIndex() && data.attribute(j).isNumeric()) {
                    String attrName = data.attribute(j).name().replaceAll("\\W+", "_");
                    dotContent.append("  cluster").append(i).append("_").append(attrName)
                             .append(" [label=\"").append(data.attribute(j).name()).append("\\n")
                             .append(String.format("%.4f", center.value(j))).append("\", fillcolor=lightgreen];\n");
                    dotContent.append("  cluster").append(i).append(" -> cluster").append(i).append("_").append(attrName).append(";\n");
                }
            }
            dotContent.append("\n");
        }
        
        dotContent.append("}\n");
        
        // Write DOT file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write(dotContent.toString());
        }
        
        System.out.println("Cluster visualization saved to: " + dotFilePath);
        System.out.println("To view the DOT file, you can use tools like Graphviz or online DOT viewers.");
    }
}
