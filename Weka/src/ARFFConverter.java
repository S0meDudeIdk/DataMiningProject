import java.io.File;

import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.util.Scanner;

public class ARFFConverter {

    public static void csvToArff(String filePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(filePath));
        Instances dataset = loader.getDataSet();

        ArffSaver saver = new ArffSaver();
        saver.setInstances(dataset);
        saver.setFile(new File("Weka/dataset/arff/Students-Social-Media-Addiction.arff"));
        saver.writeBatch();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            
            String csvFilePath = "Weka/dataset/csv/Students-Social-Media-Addiction.csv";
            System.out.println("Choose file path:");
            System.out.println("1. Default file path");
            System.out.println("2. Optional file path");
            System.out.print("Enter choice (1 or 2): ");
            int option = Integer.parseInt(scanner.nextLine().trim());
            if(option == 2){
                System.out.print("Enter file path: ");
                String alternatePath = scanner.nextLine().trim();
                csvFilePath = alternatePath;
            }
            csvToArff(csvFilePath);
            System.out.println("Conversion successful.");
        } catch (Exception e) {
            System.err.println("Error during conversion: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
