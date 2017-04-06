import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Parse a V3 format input file and pull out the distinct values. Results & stats written to /resources/results.txt
 */
public class DistinctDimensionsExtractor {

    /**
     * Set to point to the file you want to run against.
     **/
    private static final String CSV_PATH = "";

    /**
     * Override to specify your output file location
     **/
    private static final Path RESULTS_PATH = Paths.get("");

    public static void main(String[] args) throws Exception {
        long start = System.nanoTime();
        long rowCount = 0;

        try (
                FileReader fr = new FileReader(CSV_PATH);
                CSVReader csvReader = new CSVReader(fr)
        ) {
            String[] headers = csvReader.readNext();
            List<Indices> dimensionIndices = getDimensionIndices(headers);
            Map<String, Set<Dimension>> uniqueDimensions = new HashMap<>();

            String[] row;

            while ((row = csvReader.readNext()) != null) {
                addUniqueDimensions(dimensionIndices, row, uniqueDimensions);
                rowCount++;
            }

            long timeElapsed = System.nanoTime() - start;
            double totalTimeInSeconds = ((double) timeElapsed / 1000000000.0);

            writeOutput(rowCount, totalTimeInSeconds, uniqueDimensions);
            System.out.println("Complete. Results file -> " + RESULTS_PATH.toString());
        }
    }

    private static void addUniqueDimensions(List<Indices> dimensionIndices, String[] row, Map<String, Set<Dimension>> uniqueDimensions) {
        for (Indices i : dimensionIndices) {
            Dimension d = new Dimension(row, i);
            if (uniqueDimensions.containsKey(d.getName())) {
                Set<Dimension> dimensions = uniqueDimensions.get(d.getName());
                dimensions.add(d);
                uniqueDimensions.put(d.getName(), dimensions);
            } else {
                Set<Dimension> dimensions = new HashSet<>();
                dimensions.add(d);
                uniqueDimensions.put(d.getName(), dimensions);
            }
        }
    }

    private static void writeOutput(long rowsProcessed, double timeTaken, Map<String, Set<Dimension>> result) throws
            IOException {
        Files.deleteIfExists(RESULTS_PATH);
        Files.createFile(RESULTS_PATH);

        String format = "Time: {0} seconds, Rows processed: {1}, Dimension types: {2}\n\n";
        Files.write(RESULTS_PATH, prepareLine(format, timeTaken, rowsProcessed, result.keySet().size()), StandardOpenOption.APPEND);


        for (String dimensionName : result.keySet()) {
            String message = "Dimension: {0}, {1} entries\n";
            Set<Dimension> dimensions = result.get(dimensionName);
            Files.write(RESULTS_PATH, prepareLine(message, dimensionName, dimensions.size()), StandardOpenOption.APPEND);

            for (Dimension d : dimensions) {
                Files.write(RESULTS_PATH, prepareLine("\t{0}\n", d), StandardOpenOption.APPEND);
            }
        }
    }

    private static final byte[] prepareLine(String msg, Object... args) {
        return MessageFormat.format(msg, args).getBytes();
    }

    private static List<Indices> getDimensionIndices(String[] headerRow) {
        if (headerRow.length < 6) {
            throw new RuntimeException("File does not contain");
        }

        List<Indices> dimensionIndices = new ArrayList<>();
        for (int i = 3; i < headerRow.length; i += 3) {
            dimensionIndices.add(new Indices(i));
        }
        return dimensionIndices;
    }

    static class Indices {

        private int start;

        public Indices(int start) {
            this.start = start;
        }

        public int hierarchyIndex() {
            return start;
        }

        public int nameIndex() {
            return start + 1;
        }

        public int valueIndex() {
            return start + 2;
        }
    }
}
