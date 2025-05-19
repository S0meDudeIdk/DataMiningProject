// Weka/dataset/csv/

import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;

public class Test {

    public static void main(String[] args) {
        try {
            // Paths to your CSV files
            String csvFile1 = "Weka/dataset/csv/summer-products-with-rating-and-performance_2020-08.csv";
            String csvFile2 = "Weka/dataset/csv/computed_insight_success_of_active_sellers.csv";
            String csvFile3 = "Weka/dataset/csv/unique-categories.csv";
            String csvFile4 = "Weka/dataset/csv/unique-categories.sorted-by-count.csv";

            // Load each CSV into Instances
            Instances data1 = loadCSV(csvFile1);
            Instances data2 = loadCSV(csvFile2);
            Instances data3 = loadCSV(csvFile3);
            Instances data4 = loadCSV(csvFile4);

            // Print structure (optional)
            System.out.println("Data1 attributes: " + data1.numAttributes());
            System.out.println("Data2 attributes: " + data2.numAttributes());
            System.out.println("Data3 attributes: " + data3.numAttributes());
            System.out.println("Data4 attributes: " + data4.numAttributes());

            // Merge datasets by appending instances - 
            // Note: datasets must have the same attributes for direct appending.
            // If not the same, merging will require additional processing.
            Instances merged = new Instances(data1);

            addInstances(merged, data2);
            addInstances(merged, data3);
            addInstances(merged, data4);

            System.out.println("Merged dataset size: " + merged.numInstances());

            // Save merged data to ARFF
            String arffOutputPath = "merged_dataset.arff";
            saveAsArff(merged, arffOutputPath);
            System.out.println("Merged ARFF saved to: " + arffOutputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to load CSV
    public static Instances loadCSV(String filePath) throws IOException {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        return loader.getDataSet();
    }

    // Helper method to add all instances from src to dest if compatible
    public static void addInstances(Instances dest, Instances src) {
        // Check attributes compatibility by name and type
        if (!dest.equalHeaders(src)) {
            System.err.println("Warning: Dataset headers differ! Attempting to merge anyway...");
            // Depending on your data, you might want to harmonize headers before merging.
        }
        for (int i = 0; i < src.numInstances(); i++) {
            dest.add(src.instance(i));
        }
    }

    // Helper method to save Instances as ARFF
    public static void saveAsArff(Instances data, String outputFile) throws IOException {
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(outputFile));
        saver.writeBatch();
    }
}
