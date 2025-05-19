import java.io.*;
import java.util.*;

public class DataCleaner {

    public static void main(String[] args){
        String inputARFF = "Weka/dataset/computed_insight_success_of_active_sellers.arff";
        String outputARFF = "Weka/dataset/data_ready/computed_insight_success_of_active_sellers_cleaned.arff";
            // summer-products-with-rating-and-performance_2020-08
            // computed_insight_success_of_active_sellers
            // unique-categories
            // unique-categories.sorted-by-count
        try{
            cleanARFF(inputARFF, outputARFF);
        } catch (IOException e){
            System.err.println("Error processing ARFF file: " + e.getMessage());
        }
    }

    public static void cleanARFF(String inputPath, String outputPath) throws IOException{
        List<String> header = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        Set<String> uniqueRows = new HashSet<>();
        boolean inDataSection = false;

        // Step 1: Read the ARFF file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))){
            String line;
            while((line = reader.readLine()) != null) {
                if(line.trim().isEmpty()) continue;

                if(line.trim().toLowerCase().startsWith("@data")){
                    inDataSection = true;
                    header.add(line); 
                    continue;
                }

                if(!inDataSection){
                    header.add(line); // ARFF header
                } 
                else{
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("%") && trimmed.contains(",")){
                        String[] fields = trimmed.split(",");
                        if (fields.length > 1 && uniqueRows.add(trimmed)){
                            data.add(fields);
                        }
                    }
                }
            }
        }

        // Step 2: Calculate mode for each column
        int numCols = data.get(0).length;
        String[] modes = new String[numCols];
        for (int col = 0; col < numCols; col++){
            Map<String, Integer> freq = new HashMap<>();
            for (String[] row : data){
                String value = row[col].trim();
                if (!value.isEmpty() && !value.equals("?")){
                    freq.put(value, freq.getOrDefault(value, 0) + 1);
                }
            }
            modes[col] = freq.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("?");
        }
        // Step 3: Replace missing values with mode
        for (String[] row : data) {
            for (int col = 0; col < numCols; col++){
                if (row[col].trim().equals("?") || row[col].trim().isEmpty()){
                    row[col] = modes[col];
                }
            }
        }
        // Step 4: Write cleaned ARFF
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))){
            for (String h : header){
                writer.write(h + "\n");
            }
            for (String[] row : data){
                writer.write(String.join(",", row) + "\n");
            }
            System.out.println("Cleaned ARFF file saved to: " + outputPath);
        }
    }
}
