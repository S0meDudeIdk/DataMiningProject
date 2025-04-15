import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Main {
    public static void main(String[] args) throws Exception {
        String filePath = "Weka/dataset/iris.arff";
        DataSource source = new DataSource(filePath);
        Instances data = source.getDataSet();

        if (data == null) {
            System.out.println("Failed to load dataset.");
            return;
        }

        System.out.println("Loaded dataset with " + data.numInstances() + " instances.");
        System.out.println("=== Full Dataset ===");

        for (int i = 0; i < data.numInstances(); i++) {
            System.out.println(data.instance(i));
        }
    }
}