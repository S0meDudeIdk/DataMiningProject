import java.io.File;

import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class test {

    public static void csvToArff(String filePath) throws Exception {
        // Load csv file
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        Instances dataset = loader.getDataSet();

        // Save as arff format
        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File("Weka/dataset/test2.arff"));
        saver.writeBatch();
    }

    public static void main(String[] args) {
        try {
            // Provide the path to your CSV file here
            String csvFilePath = "Weka/dataset/csv/Students Social Media Addiction.csv";
            csvToArff(csvFilePath);
            System.out.println("Conversion successful.");
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
