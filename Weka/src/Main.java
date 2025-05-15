import weka.classifiers.Evaluation;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            String filePath = "Weka/dataset/wekadataset/iris.arff"; 

            if (args.length > 0) {
                filePath = args[0];
            }
            System.out.println("Choose operation:");
            System.out.println("1. Classification only");
            System.out.println("2. Classification with Clustering (Improved Model)");
            System.out.print("Enter choice (1 or 2): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Choose classifier:");
            System.out.println("j48 | naivebayes | svm");
            System.out.print("Enter classifier type: ");
            String classifierType = scanner.nextLine().trim().toLowerCase();

            int folds = 10;

            if (choice == 1) {
                ClassifierModel model = new ClassifierModel(filePath, classifierType, null);
                model.trainModel();
                // Evaluation eval = model.evaluateModel(10);
                // model.printResults(eval);
                
                Evaluator evaluator = new Evaluator(model.getClassifier(), model.getData());
                evaluator.crossValidate(10);
                evaluator.printResults();

                String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results.txt";
                evaluator.saveResultsToFile(resultPath);

            } else if (choice == 2) {
                System.out.println("Choose clustering method:");
                System.out.println("kmeans | em");
                System.out.print("Enter clustering method: ");
                String clusterMethod = scanner.nextLine().trim().toLowerCase();

                ImprovedModel improvedModel = new ImprovedModel();
                improvedModel.runWithClassifier(filePath, clusterMethod, classifierType, folds);
            } else {
                System.out.println("Invalid choice. Please select 1 or 2.");
            }
            System.out.println("Process completed successfully!");

        } catch (Exception e) {
            System.err.println("Error in data mining process: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}