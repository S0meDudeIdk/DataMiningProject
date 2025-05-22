import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode2;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class TreeVisualizer {
    
    /**
     * Displays and saves a classifier visualization if it's a tree-based classifier
     * @param classifier The trained classifier (only J48 supported)
     * @param datasetName The name of the dataset (for window title and file name)
     * @throws Exception If visualization fails
     */
    public static void visualizeClassifier(weka.classifiers.Classifier classifier, String datasetName) throws Exception {
        if (classifier instanceof J48) {
            displayJ48Tree((J48) classifier, "J48 Decision Tree for " + datasetName);
            saveTreeToDotFile((J48) classifier, "Weka/results/" + datasetName + "_j48_tree.dot");
        } else {
            System.out.println("Visualization is only available for tree-based classifiers like J48.");
        }
    }
    
    /**
     * Displays a J48 decision tree in a window
     * @param classifier The trained J48 classifier
     * @param title The title for the visualization window
     * @throws Exception If visualization fails
     */
    private static void displayJ48Tree(J48 classifier, String title) throws Exception {
        // Get the tree representation as a graph
        String graph = classifier.graph();
        
        // Create a tree visualizer component
        final JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));
        frame.setLocationRelativeTo(null);
        
        // Set up the visualizer
        weka.gui.treevisualizer.TreeVisualizer tv = new weka.gui.treevisualizer.TreeVisualizer(
                null, graph, new PlaceNode2());
        frame.getContentPane().add(tv, BorderLayout.CENTER);
        tv.fitToScreen();
        
        // Display the window
        frame.setVisible(true);
    }
    
    /**
     * Saves a J48 decision tree visualization to a DOT file
     * @param classifier The trained J48 classifier
     * @param outputPath The path where to save the DOT file
     * @throws Exception If saving fails
     */
    private static void saveTreeToDotFile(J48 classifier, String outputPath) throws Exception {
        String graph = classifier.graph();
        
        // Ensure directory exists
        File outputFile = new File(outputPath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));
        writer.write(graph);
        writer.close();
        System.out.println("Tree visualization saved to: " + outputPath);
        System.out.println("To view the DOT file, you can use tools like Graphviz or online DOT viewers.");
    }
}