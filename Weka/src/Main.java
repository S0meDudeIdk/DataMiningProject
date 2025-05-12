import weka.classifiers.Evaluation;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            String filePath = "Weka/dataset/iris.arff";

            if (args.length > 0) {
                filePath = args[0];
            }

            ClassifierModel model = new ClassifierModel(filePath, "j48");
            model.trainModel();

            Evaluation eval = model.evaluateModel(10);

            model.printResults(eval);

            String datasetName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.lastIndexOf("."));

            String resultPath = "Weka/results/" + datasetName + "_j48_results.txt";
            model.saveResultsToFile(eval, resultPath);

            System.out.println("Process completed successfully!");

        } catch (Exception e) {
            System.err.println("Error in data mining process: " + e.getMessage());
            e.printStackTrace();
        }
    }
}