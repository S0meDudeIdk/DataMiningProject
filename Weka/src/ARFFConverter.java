import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
// import java.util.stream.Stream;

public class ARFFConverter {

    public static void main(String[] args) {
        String csvFolderPath = "Weka/dataset/csv";
        String arffFolderPath = "Weka/dataset";
        
        // Create directories if they don't exist
        createDirectoryIfNotExists(csvFolderPath);
        createDirectoryIfNotExists(arffFolderPath);
        
        try {
            // Process all CSV files in the folder
            File csvFolder = new File(csvFolderPath);
            File[] csvFiles = csvFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            
            if (csvFiles == null || csvFiles.length == 0) {
                System.out.println("No CSV files found in " + csvFolderPath);
                return;
            }
            
            for (File csvFile : csvFiles) {
                String csvFilePath = csvFile.getAbsolutePath();
                String fileName = csvFile.getName().replace(".csv", ".arff");
                String arffFilePath = arffFolderPath + "/" + fileName;
                
                try {
                    convertCsvToArff(csvFilePath, arffFilePath);
                    System.out.println("Successfully converted: " + csvFile.getName() + " to " + fileName);
                } catch (Exception e) {
                    System.err.println("Error converting " + csvFile.getName() + ": " + e.getMessage());
                }
            }
            
            System.out.println("All conversions completed!");
            
        } catch (Exception e) {
            System.err.println("Error processing CSV files: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void convertUsingManualParsing(String csvFilePath, String arffFilePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            // Read header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Empty CSV file");
            }
            
            // Parse header to get attribute names
            List<String> attributeNames = parseCSVLine(headerLine);
                
            // Create ARFF file manually
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(arffFilePath))) {
                // Write ARFF header
                writer.write("@relation 'converted_data'\n\n");
                
                // Write attribute declarations - all as string type to avoid nominal value issues
                for (int i = 0; i < attributeNames.size(); i++) {
                    String attrName = attributeNames.get(i).trim();
                    // Clean attribute name for ARFF format
                    attrName = attrName.replace("'", "");
                    attrName = attrName.replace("\"", "");
                    if (attrName.isEmpty()) attrName = "unnamed_attribute_" + i;
                    
                    // All attributes as string to avoid nominal value enumeration
                    writer.write("@attribute '" + attrName + "' string\n");
                }
                writer.write("\n@data\n");
                
                // Write data rows
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> values = parseCSVLine(line);
                    
                    // Format as standard ARFF data row (not sparse)
                    for (int i = 0; i < attributeNames.size(); i++) {
                        // Use value if available, otherwise use missing value marker
                        String value = (i < values.size()) ? values.get(i).trim() : "?";
                        
                        // Handle empty values
                        if (value.isEmpty()) {
                            value = "?";
                        }
                        
                        // Determine if value needs quotes (non-numeric values need quotes)
                        boolean needsQuotes = !isNumeric(value) && !value.equals("?");
                        
                        // Write the value with appropriate formatting
                        if (needsQuotes) {
                            // Escape any double quotes in the value
                            value = value.replace("\"", "\\\"");
                            writer.write("\"" + value + "\"");
                        } else {
                            writer.write(value);
                        }
                        
                        // Add comma if not the last value
                        if (i < attributeNames.size() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }
                
                System.out.println("Manual conversion successful using standard ARFF format!");
            }
        }
    }

    // Helper method to check if a string is numeric
    private static boolean isNumeric(String str) {
        if (str == null || str.equals("?")) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static List<String> parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        
        if (line == null || line.isEmpty()) {
            return fields;
        }
        
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Double quotes inside quoted field - add a single quote
                    field.append('"');
                    i++; // Skip the next quote
                } else {
                    // Toggle quote state
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }
        
        // Add the last field
        fields.add(field.toString());
        
        return fields;
    }
    
    private static String escapeArffString(String s) {
        // Escape single quotes with backslash for ARFF format
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }

    public static void convertCsvToArff(String csvFilePath, String arffFilePath) throws Exception {
        // Skip Weka's CSVLoader entirely for problematic files
        if (csvFilePath.contains("unique-categories") || 
            csvFilePath.contains("summer-products")) {
            System.out.println("Using manual conversion for problematic file: " + csvFilePath);
            convertUsingManualParsing(csvFilePath, arffFilePath);
            return;
        }
    
        // For other files, proceed with standard conversion
        // Create a temporary file with properly escaped values
        File tempFile = File.createTempFile("preprocessed_", ".csv");
        try {
            // Preprocess CSV to properly escape problematic characters
            preprocessCsv(csvFilePath, tempFile.getAbsolutePath());
            
            // Try to use CSVLoader for standard files
            try {
                CSVLoader loader = new CSVLoader();
                loader.setNoHeaderRowPresent(false);
                loader.setFieldSeparator(",");
                loader.setEnclosureCharacters("\"");
                loader.setMissingValue("?");
                loader.setSource(tempFile);
                
                Instances data = loader.getDataSet();
                
                ArffSaver saver = new ArffSaver();
                saver.setInstances(data);
                saver.setFile(new File(arffFilePath));
                saver.writeBatch();
            } catch (Exception e) {
                System.out.println("CSVLoader failed, trying manual conversion: " + e.getMessage());
                convertUsingManualParsing(tempFile.getAbsolutePath(), arffFilePath);
            }
        } finally {
            if (!tempFile.getName().contains("debug")) {
                tempFile.delete();
            }
        }
    }
    
    private static void preprocessCsv(String inputCsvPath, String outputCsvPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputCsvPath));
            java.io.PrintWriter writer = new java.io.PrintWriter(outputCsvPath)) {
            
            String line;
            boolean firstLine = true;
            int expectedColumns = 0;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip file path comments if present
                if (line.trim().startsWith("//")) {
                    continue;
                }
                
                // Special handling for the header line
                if (firstLine) {
                    writer.println(line);
                    expectedColumns = line.split(",").length;
                    System.out.println("Header has " + expectedColumns + " columns");
                    firstLine = false;
                    continue;
                }
                
                try {
                    // Process line character by character for proper handling of quotes
                    List<String> fields = new ArrayList<>();
                    StringBuilder currentField = new StringBuilder();
                    boolean inQuotes = false;
                    
                    for (int i = 0; i < line.length(); i++) {
                        char c = line.charAt(i);
                        
                        if (c == '"') {
                            // Check for escaped quotes (double quotes)
                            if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                                // This is an escaped quote, add a single quote and skip the next character
                                currentField.append('"');
                                i++;  // Skip the next quote
                            } else {
                                // Toggle the quote state
                                inQuotes = !inQuotes;
                            }
                        } else if (c == ',' && !inQuotes) {
                            // This is a field separator
                            fields.add(currentField.toString());
                            currentField = new StringBuilder();
                        } else {
                            // Regular character, add to current field
                            currentField.append(c);
                        }
                    }
                    
                    // Add the last field
                    fields.add(currentField.toString());
                    
                    // Debug output for problematic lines
                    if (lineNumber == 29) {
                        System.out.println("DEBUG - Line " + lineNumber + " has " + fields.size() + " fields (expected " + expectedColumns + ")");
                    }
                    
                    // Make sure we have the right number of fields by adding ? for missing fields
                    while (fields.size() < expectedColumns) {
                        fields.add("?");
                    }
                    
                    // Ensure we don't exceed the expected number of columns
                    if (fields.size() > expectedColumns) {
                        System.out.println("Truncating line " + lineNumber + " from " + fields.size() + " to " + expectedColumns + " fields");
                        fields = new ArrayList<>(fields.subList(0, expectedColumns));
                    }
                    
                    // Write normalized fields to the output file
                    for (int i = 0; i < fields.size(); i++) {
                        String field = fields.get(i).trim();
                        
                        // Handle empty fields
                        if (field.isEmpty()) {
                            field = "?";
                        }
                        
                        // Properly quote fields that contain commas, quotes, or other special characters
                        if (field.contains(",") || field.contains("\"") || field.contains("'")) {
                            // Replace quotes with escaped quotes
                            field = field.replace("\"", "\"\"");
                            field = "\"" + field + "\"";
                        }
                        
                        writer.print(field);
                        if (i < fields.size() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                    
                } catch (Exception e) {
                    System.err.println("Error processing line " + lineNumber + ": " + e.getMessage());
                    e.printStackTrace();
                    
                    // Write a placeholder line with missing values
                    for (int i = 0; i < expectedColumns; i++) {
                        writer.print(i > 0 ? ",?" : "?");
                    }
                    writer.println();
                }
            }
        }
    }
    
    /**
     * Create a directory if it doesn't exist
     * 
     * @param directoryPath Path to the directory
     */
    private static void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                System.out.println("Created directory: " + directoryPath);
            } else {
                System.err.println("Failed to create directory: " + directoryPath);
            }
        }
    }
}