import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataCleaner {

    public static void main(String[] args) {
        String inputARFF = "Weka/dataset/arff/Students-Social-Media-Addiction.arff";
        String outputARFF = "Weka/dataset/data_ready/Students-Social-Media-Addiction.arff";
        String reportPath = "Weka/dataset/data_ready/Cleaning_Report.txt";
        System.out.println("Choose file path:");
        System.out.println("1. Default file path");
        System.out.println("2. Optional file path");
        System.out.print("Enter choice (1 or 2): ");
        Scanner scanner = new Scanner(System.in);
        int option = Integer.parseInt(scanner.nextLine().trim());
        if(option == 2){
            System.out.print("Enter file path: ");
            String alternatePath = scanner.nextLine().trim();
            inputARFF = alternatePath;
        }
        try {
            cleanARFF(inputARFF, outputARFF, reportPath);
        } catch (IOException e) {
            System.err.println("Error processing ARFF file: " + e.getMessage());
        }
        scanner.close();
    }

    public static void cleanARFF(String inputPath, String outputPath, String reportPath) throws IOException {
        List<String> header = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        List<String> attributeTypes = new ArrayList<>();
        List<String> attributeNames = new ArrayList<>();
        Set<String> uniqueRows = new HashSet<>();
        boolean inDataSection = false;

        int totalRecordsBefore = 0;
        int numCols = -1;
        int platformColumnIndex = -1;

        // Step 1: Read the ARFF file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while((line = reader.readLine()) != null){
                if(line.trim().isEmpty()) continue;

                if(line.trim().toLowerCase().startsWith("@data")){
                    inDataSection = true;
                    header.add(line);
                    continue;
                }

                if(!inDataSection){
                    header.add(line);
                    if(line.toLowerCase().startsWith("@attribute")){
                        String[] parts = line.split("\\s+", 3);
                        if(parts.length >= 3){
                            String attributeName = parts[1].trim();
                            String type = parts[2].trim().toLowerCase();
                            attributeNames.add(attributeName);
                            attributeTypes.add(type);
                    
                            if(attributeName.toLowerCase().contains("platform") || 
                               attributeName.toLowerCase().contains("most_used_platform")){
                                platformColumnIndex = attributeNames.size() - 1;
                            }
                        }
                    }
                } 
                else{
                    String trimmed = line.trim();
                    if(!trimmed.isEmpty() && !trimmed.startsWith("%") && trimmed.contains(",")){
                        totalRecordsBefore++;
                        if (uniqueRows.add(trimmed)){
                            String[] fields = trimmed.split(",", -1); 
                            if (numCols == -1) numCols = fields.length;
                            data.add(fields);
                        }
                    }
                }
            }
        }

        int totalRecordsAfter = data.size();

        // Step 2: Compute statistics for imputation
        String[] imputationValues = new String[numCols];
        int[] missingCounts = new int[numCols];
        int[] imputedCounts = new int[numCols];
        String[] methodsUsed = new String[numCols];

        for(int col = 0; col < numCols; col++){
            boolean isNumeric = attributeTypes.get(col).contains("numeric")
                    || attributeTypes.get(col).contains("real")
                    || attributeTypes.get(col).contains("integer");

            List<Double> numericValues = new ArrayList<>();
            Map<String, Integer> freq = new HashMap<>();
            int missing = 0;

            for(String[] row : data){
                String value = row[col].trim();
                if(value.isEmpty() || value.equals("?")){
                    missing++;
                } 
                else{
                    if(isNumeric){
                        try{
                            numericValues.add(Double.parseDouble(value));
                        } catch (NumberFormatException e) {
                            missing++;
                        }
                    } 
                    else{
                        freq.put(value, freq.getOrDefault(value, 0) + 1);
                    }
                }
            }

            missingCounts[col] = missing;

            if(isNumeric){
                if(!numericValues.isEmpty()){
                    Collections.sort(numericValues);
                    double median;
                    int n = numericValues.size();
                    if(n % 2 == 0){
                        median = (numericValues.get(n / 2 - 1) + numericValues.get(n / 2)) / 2.0;
                    } 
                    else{
                        median = numericValues.get(n / 2);
                    }
                    
                    if(median == Math.floor(median)) {
                        imputationValues[col] = String.format("%.0f", median);  
                    } 
                    else{
                        imputationValues[col] = String.format("%.1f", median); 
                    }
                    methodsUsed[col] = "Median";
                } 
                else{
                    imputationValues[col] = "?";
                    methodsUsed[col] = "N/A";
                }
            } 
            else{
                String mode = freq.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                                .thenComparing(Map.Entry.comparingByKey()))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("?");
                imputationValues[col] = mode;
                methodsUsed[col] = "Mode";
            }
        }

        // Step 3: Impute missing values
        for (String[] row : data){
            for(int col = 0; col < numCols; col++){
                if(row[col].trim().equals("?") || row[col].trim().isEmpty()){
                    row[col] = imputationValues[col];
                    imputedCounts[col]++;
                }
            }
        }

        // Step 4: Calculate platform usage distribution after imputation
        Map<String, Integer> platformDistribution = new HashMap<>();
        int totalPlatformEntries = 0;
        
        if(platformColumnIndex != -1){
            for(String[] row : data){
                String platform = row[platformColumnIndex].trim();
                if(!platform.isEmpty() && !platform.equals("?")){
                    platform = standardizePlatformName(platform);
                    platformDistribution.put(platform, platformDistribution.getOrDefault(platform, 0) + 1);
                    totalPlatformEntries++;
                }
            }
        }

        // Step 5: Write cleaned ARFF file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for(String h : header){
                writer.write(h + "\n");
            }
            for(String[] row : data){
                writer.write(String.join(",", row) + "\n");
            }
            System.out.println("Cleaned ARFF file saved to: " + outputPath);
        }

        // Step 6: Write comprehensive cleaning report
        try(BufferedWriter reportWriter = new BufferedWriter(new FileWriter(reportPath))){
            reportWriter.write("=== Data Cleaning Report ===\n\n");
            reportWriter.write("Total Records Before Duplicate Removal: " + totalRecordsBefore + "\n");
            reportWriter.write("Total Records After Duplicate Removal: " + totalRecordsAfter + "\n");
            reportWriter.write("Total Attributes: " + numCols + "\n\n");

            int totalMissing = Arrays.stream(missingCounts).sum();
            int totalImputed = Arrays.stream(imputedCounts).sum();

            reportWriter.write("Total Missing Values Before Cleaning: " + totalMissing + "\n");
            reportWriter.write("Total Values Imputed: " + totalImputed + "\n\n");

            reportWriter.write("=== Imputation Details by Column ===\n");
            reportWriter.write(String.format("%-25s %-10s %-10s %-10s\n", "Attribute", "Missing", "Imputed", "Method"));
            reportWriter.write("-".repeat(60) + "\n");
            for (int i = 0; i < numCols; i++) {
                String attributeName = (i < attributeNames.size()) ? attributeNames.get(i) : "Column_" + String.format("%02d", i);
                reportWriter.write(String.format("%-25s %-10d %-10d %-10s\n", attributeName, missingCounts[i], imputedCounts[i], methodsUsed[i]));
            }

            if(platformColumnIndex != -1 && !platformDistribution.isEmpty()){
                reportWriter.write("\n=== Platform Usage Distribution (Post-Imputation) ===\n");
                reportWriter.write(String.format("%-15s %-10s %-15s\n", "Platform", "Count", "Percentage"));
                reportWriter.write("-".repeat(45) + "\n");
                
                List<Map.Entry<String, Integer>> sortedPlatforms = platformDistribution.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .collect(Collectors.toList());
                
                for(Map.Entry<String, Integer> entry : sortedPlatforms){
                    String platform = entry.getKey();
                    int count = entry.getValue();
                    double percentage = (double) count / totalPlatformEntries * 100;
                    reportWriter.write(String.format("%-15s %-10d %-15s\n", platform, count, String.format("%.1f%%", percentage)));
                }
                
                reportWriter.write("-".repeat(45) + "\n");
                reportWriter.write(String.format("%-15s %-10d %-15s\n", "Total", totalPlatformEntries, "100.0%"));
            }
        }

        System.out.println("Comprehensive cleaning report saved to: " + reportPath);
    }

    private static String standardizePlatformName(String platform) {
        String normalized = platform.toLowerCase().trim();
        
        // Standardize common platform name variations
        if(normalized.contains("instagram") || normalized.equals("ig")){
            return "Instagram";
        } 
        else if(normalized.contains("tiktok") || normalized.contains("tik tok")){
            return "TikTok";
        } 
        else if(normalized.contains("facebook") || normalized.equals("fb")){
            return "Facebook";
        } 
        else if(normalized.contains("youtube") || normalized.equals("yt")){
            return "YouTube";
        } 
        else if(normalized.contains("linkedin")){
            return "LinkedIn";
        } 
        else if(normalized.contains("snapchat") || normalized.contains("snap")){
            return "Snapchat";
        } 
        else if(normalized.contains("twitter") || normalized.equals("x")){
            return "Twitter/X";
        } 
        else if(normalized.contains("whatsapp")){
            return "WhatsApp";
        } 
        else{
            return platform.substring(0, 1).toUpperCase() + platform.substring(1).toLowerCase();
        }
    }
}