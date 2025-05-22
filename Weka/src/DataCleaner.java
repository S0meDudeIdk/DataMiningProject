import java.io.*;
import java.util.*;

public class DataCleaner {

    public static void main(String[] args) {
        String inputARFF = "Weka/dataset/arff/Students-Social-Media-Addiction.arff";
        String outputARFF = "Weka/dataset/data_ready/Students-Social-Media-Addiction.arff";
        String reportPath = "Weka/dataset/data_ready/Cleaning_Report.txt";
        try {
            cleanARFF(inputARFF, outputARFF, reportPath);
        } catch (IOException e) {
            System.err.println("Error processing ARFF file: " + e.getMessage());
        }
    }

    public static void cleanARFF(String inputPath, String outputPath, String reportPath) throws IOException {
        List<String> header = new ArrayList<>();
        List<String[]> data = new ArrayList<>();
        Set<String> uniqueRows = new HashSet<>();
        boolean inDataSection = false;

        int totalRecordsBefore = 0;
        int numCols = -1;

        // Step 1: Read the ARFF file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                if (line.trim().toLowerCase().startsWith("@data")) {
                    inDataSection = true;
                    header.add(line);
                    continue;
                }

                if (!inDataSection) {
                    header.add(line);
                } else {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("%") && trimmed.contains(",")) {
                        totalRecordsBefore++;
                        if (uniqueRows.add(trimmed)) {
                            String[] fields = trimmed.split(",", -1); // keep empty fields
                            if (numCols == -1) numCols = fields.length;
                            data.add(fields);
                        }
                    }
                }
            }
        }

        int totalRecordsAfter = data.size();

        // Step 2: Calculate mode and count missing
        String[] modes = new String[numCols];
        int[] missingCounts = new int[numCols];
        int[] imputedCounts = new int[numCols];

        for (int col = 0; col < numCols; col++) {
            Map<String, Integer> freq = new HashMap<>();
            int missing = 0;
            for (String[] row : data) {
                String value = row[col].trim();
                if (value.isEmpty() || value.equals("?")) {
                    missing++;
                } else {
                    freq.put(value, freq.getOrDefault(value, 0) + 1);
                }
            }
            missingCounts[col] = missing;

            modes[col] = freq.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                            .thenComparing(Map.Entry.comparingByKey()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse("?");
        }

        // Step 3: Replace missing values with mode
        for (String[] row : data) {
            for (int col = 0; col < numCols; col++) {
                if (row[col].trim().equals("?") || row[col].trim().isEmpty()) {
                    row[col] = modes[col];
                    imputedCounts[col]++;
                }
            }
        }

        // Step 4: Write cleaned ARFF
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String h : header) {
                writer.write(h + "\n");
            }
            for (String[] row : data) {
                writer.write(String.join(",", row) + "\n");
            }
            System.out.println("Cleaned ARFF file saved to: " + outputPath);
        }

        // Step 5: Write report
        try (BufferedWriter reportWriter = new BufferedWriter(new FileWriter(reportPath))) {
            reportWriter.write("=== Data Cleaning Report ===\n\n");

            reportWriter.write("Total Records Before Duplicate Removal: " + totalRecordsBefore + "\n");
            reportWriter.write("Total Records After Duplicate Removal: " + totalRecordsAfter + "\n");
            reportWriter.write("Total Attributes: " + numCols + "\n\n");

            int totalMissing = Arrays.stream(missingCounts).sum();
            int totalImputed = Arrays.stream(imputedCounts).sum();

            reportWriter.write("Total Missing Values Before Cleaning: " + totalMissing + "\n");
            reportWriter.write("Total Values Imputed: " + totalImputed + "\n\n");

            reportWriter.write(String.format("%-25s %-20s %-20s\n", "Attribute (Col Index)", "Missing", "Imputed"));
            for (int i = 0; i < numCols; i++) {
                reportWriter.write(String.format("Column %02d %-15s %-20d %-20d\n", i, "", missingCounts[i], imputedCounts[i]));
            }
        }

        System.out.println("Cleaning report saved to: " + reportPath);
    }
}
