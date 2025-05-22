import weka.classifiers.Evaluation;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            // For testing the classifier
            // String filePath = "emotions_train.arff"; 
            String filePath = "Weka/dataset/test2.arff";
            // summer-products-with-rating-and-performance_2020-08
            // computed_insight_success_of_active_sellers
            // unique-categories
            // unique-categories.sorted-by-count
            if (args.length > 0) {
                // filePath = args[0];
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
                ClassifierModel model = new ClassifierModel(filePath, classifierType, null, null);
                // ClassifierModel model = new ClassifierModel(trainFilePath, testFilePath, classifierType, null, null);
                model.trainModelSingleData();
                // Evaluation eval = model.evaluateModel(10);
                // model.printResults(eval);
                
                Evaluator evaluator = new Evaluator(model.getClassifier(), model.getData(), null);
                evaluator.crossValidate(10);
                evaluator.printResults();

                String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results.txt";
                evaluator.saveResultsToFile(resultPath, null);

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