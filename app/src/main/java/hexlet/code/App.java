package hexlet.code;
import com.fasterxml.jackson.core.type.TypeReference;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class App {
    @Command(name = "gendiff", mixinStandardHelpOptions = true,
            description = "Compares two configuration files and shows a difference.")
    public static class GenDiff implements Callable<Integer> {
        @Parameters(paramLabel = "filepath1", description = "path to first file")
        public static String filePath1;
        @Parameters(paramLabel = "filepath2", description = "path to second file")
        public static String filePath2;
        @Option(names = {"-f", "--format"}, defaultValue = "stylish", description = "output format [default: stylish]")
        private String format;
        @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
        boolean usageHelpRequested;
        @Option(names = {"-V", "--version"}, versionHelp = true, description = "Print version information and exit.")
        boolean versionInfoRequested;

        @Override public Integer call() throws IOException {

            Path pathToFile1 = Paths.get(GenDiff.filePath1).toAbsolutePath().normalize();
            Path pathToFile2 = Paths.get(GenDiff.filePath2).toAbsolutePath().normalize();
            String content1 = Files.readString(pathToFile1);
            String content2 = Files.readString(pathToFile2);
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> map1 = objectMapper.readValue(content1, new TypeReference<Map<String,Object>>(){});
            Map<String, Object> map2 = objectMapper.readValue(content2, new TypeReference<Map<String,Object>>(){});

            System.out.print("{\n" + differGenerator(map1, map2) + "}\n");
            return 0;
        }
    }
    public static String differGenerator(Map<String, Object> map1, Map<String, Object> map2) {
        ArrayList<Result> results = new ArrayList<>();
        String sortedResultAsString = "";

        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
            if (map2.containsKey(entry1.getKey())) {
                if (entry1.getValue().equals(map2.get(entry1.getKey()))) {
                    results.add(new Result(" ", entry1.getKey(), entry1.getValue(), 1));
                } else {
                    results.add(new Result("-", entry1.getKey(), entry1.getValue(), 1));
                    results.add(new Result("+", entry1.getKey(), map2.get(entry1.getKey()), 2));
                }
            } else
                results.add(new Result("-", entry1.getKey(), entry1.getValue(), 1));

        }
        for (Map.Entry<String, Object> entry2 : map2.entrySet()) {
            if(!map1.containsKey(entry2.getKey())) {
                results.add(new Result("+", entry2.getKey(), entry2.getValue(), 2));
            }
        }

        List<Result> sortedResults = results.stream().sorted(
                        Comparator.comparing(Result::getKey)
                                .thenComparing(Result::getFileMarker))
                .collect(Collectors.toList());
        for(Result r : sortedResults) {
            sortedResultAsString = sortedResultAsString + " " + r.compareValue + " " + r.key + ": " + r.value + "\n";
        }
        return sortedResultAsString;
    }
    public static class Result {
        public String compareValue;
        public String key;
        public Object value;
        public int fileMarker;
        public Result(String compareValue, String key, Object value, int fileMarker) {
            this.compareValue = compareValue;
            this.key = key;
            this.value = value;
            this.fileMarker = fileMarker;
        }
        public String getKey(){
            return key;
        }
        public int getFileMarker() {
            return fileMarker;
        }
    }
    public static void main(String[] args) {
        int exitCode = new CommandLine(new GenDiff()).execute(args);
        System.exit(exitCode);
    }
}
