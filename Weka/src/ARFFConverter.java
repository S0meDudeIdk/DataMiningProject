import java.io.File;

import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

public class ARFFConverter {

    public static void csvToArff(String filePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        Instances dataset = loader.getDataSet();

        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File("Weka/dataset/data_ready/Students-Social-Media-Addiction.arff"));
        saver.writeBatch();
    }

    public static void main(String[] args) {
        try {
            String csvFilePath = "Weka/dataset/csv/Students-Social-Media-Addiction.csv";
            csvToArff(csvFilePath);
            System.out.println("Conversion successful.");
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
