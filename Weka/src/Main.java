// import weka.classifiers.Evaluation;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try {
            String filePath = "Weka/dataset/data_ready/Students-Social-Media-Addiction.arff";
            if (args.length > 0) {
                // filePath = args[0];
            }

            System.out.println("Choose file path:");
            System.out.println("1. Default file path");
            System.out.println("2. Optional file path");
            System.out.print("Enter choice (1 or 2): ");
            int option = Integer.parseInt(scanner.nextLine().trim());
            if(option == 2){
                System.out.print("Enter file path: ");
                String alternatePath = scanner.nextLine().trim();
                filePath = alternatePath;
            }
            
            System.out.println("Choose operation:");
            System.out.println("1. Classification only");
            System.out.println("2. Classification with Clustering (Improved Model)");
            System.out.print("Enter choice (1 or 2): ");
            int choice = Integer.parseInt(scanner.nextLine().trim());

            System.out.println("Choose classifier:");
            System.out.println("j48 | naivebayes | oner");
            System.out.print("Enter classifier type: ");
            String classifierType = scanner.nextLine().trim().toLowerCase();

            int folds = 10;

            if (choice == 1) {
                long startTimeModel = System.currentTimeMillis();
                ClassifierModel model = new ClassifierModel(filePath, classifierType, null, null);

                model.trainModelSingleData();
                long endTimeModel = System.currentTimeMillis();

                long startTimeEval = System.currentTimeMillis();
                Evaluator evaluator = new Evaluator(model.getClassifier(), model.getData(), null);
                evaluator.crossValidate(10);
                evaluator.printResults();
                long endTimeEval = System.currentTimeMillis();

                long modelTime = endTimeModel - startTimeModel;
                long evalTime = endTimeEval - startTimeEval;
                
                String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                String resultPath = "Weka/results/" + datasetName + "_" + classifierType + "_results.txt";
                evaluator.saveResultsToFile(resultPath, null, modelTime, evalTime, 0);
                
                // Add tree visualization option for J48 only
                if (classifierType.equals("j48")) {
                    System.out.println("Would you like to visualize the J48 decision tree? (y/n)");
                    String visualizeChoice = scanner.nextLine().trim().toLowerCase();
                    if (visualizeChoice.equals("y") || visualizeChoice.equals("yes")) {
                        TreeVisualizer.visualizeClassifier(model.getClassifier(), datasetName);
                    }
                }

            } else if (choice == 2) {
                System.out.println("Choose clustering method:");
                System.out.println("kmeans");
                System.out.print("Enter clustering method: ");
                String clusterMethod = scanner.nextLine().trim().toLowerCase();

                ImprovedModel improvedModel = new ImprovedModel();
                improvedModel.runWithClassifier(filePath, clusterMethod, classifierType, folds);
                
                // Visualization options based on algorithm
                if (classifierType.equals("j48")) {
                    System.out.println("Would you like to visualize the J48 decision tree? (y/n)");
                    String visualizeChoice = scanner.nextLine().trim().toLowerCase();
                    if (visualizeChoice.equals("y") || visualizeChoice.equals("yes")) {
                        String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                        if (improvedModel.getClassifier() != null) {
                            TreeVisualizer.visualizeClassifier(improvedModel.getClassifier(), datasetName);
                        } else {
                            System.out.println("No classifier available for visualization.");
                        }
                    }
                }
                
                // Add cluster visualization option for KMeans
                if (clusterMethod.equals("kmeans")) {
                    System.out.println("Would you like to visualize the KMeans clusters? (y/n)");
                    String visualizeChoice = scanner.nextLine().trim().toLowerCase();
                    if (visualizeChoice.equals("y") || visualizeChoice.equals("yes")) {
                        String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));
                        if (improvedModel.getClusterer() instanceof weka.clusterers.SimpleKMeans) {
                            ClusterVisualizer.visualizeCluster(
                                (weka.clusterers.SimpleKMeans)improvedModel.getClusterer(),
                                improvedModel.getImprovedTrainData(),
                                datasetName
                            );
                        } else {
                            System.out.println("Only KMeans clustering can be visualized currently.");
                        }
                    }
                }
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