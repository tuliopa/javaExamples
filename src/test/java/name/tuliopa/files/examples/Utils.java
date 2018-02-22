package name.tuliopa.files.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public interface Utils {
    class TestFile {
        final String name;
        final Long size;

        TestFile(String name, Long size) {
            this.name = name;
            this.size = size;
        }
    }

    static void createTestFiles(TestFile... testFiles) throws InterruptedException {
        String generator = "src/main/resources/text_generator.sh";
        for (TestFile testFile: testFiles) {
            try {
                Process p = new ProcessBuilder(generator,
                        testFile.size.toString(), testFile.name).start();

                p.waitFor();
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    static void deleteTestFiles(TestFile... testFiles) {
        for (TestFile testFile: testFiles) {
            File f = new File(testFile.name);
            f.delete();
        }
    }

    static void assertSplitFilesIntegrity(String originalFile, List<String> paths) throws IOException, InterruptedException {
        // concatenate files using unix command to validate integrity.
        String result = "/tmp/resultConcat.txt";
        File resultFile = new File(result);

        List<String> args = new ArrayList<>();
        args.add("cat");
        args.addAll(paths);

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.redirectOutput(resultFile);

        Process concat = pb.start();
        concat.waitFor(3, TimeUnit.SECONDS);

        // Check if files are equal.
        Process p = new ProcessBuilder("diff", originalFile, result).start();
        p.waitFor(3, TimeUnit.SECONDS);
        int exitValue = p.exitValue();
        assertEquals(0, exitValue);

        resultFile.delete();
    }

    static void printMemoryUsage() {
        Runtime r = Runtime.getRuntime();
        System.out.println("Memory  Used: " + (r.totalMemory() - r.freeMemory()));
    }
}
