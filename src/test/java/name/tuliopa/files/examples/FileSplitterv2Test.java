package name.tuliopa.files.examples;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static name.tuliopa.files.examples.Utils.TestFile;
import static name.tuliopa.files.examples.Utils.assertSplitFilesIntegrity;
import static name.tuliopa.files.examples.Utils.createTestFiles;
import static name.tuliopa.files.examples.Utils.deleteTestFiles;
import static name.tuliopa.files.examples.Utils.printMemoryUsage;
import static org.junit.Assert.assertEquals;

public class FileSplitterv2Test {

    private static String dir = "/tmp/";
    private static String sampleFile = dir + "sampleFile.txt";
    private static String gigaFile = dir + "GigaFile.txt";
    private static TestFile[] testFiles = {new TestFile(sampleFile, 1500L),
            new TestFile(gigaFile, 300000L)};

    @BeforeClass
    public static void createFiles() throws InterruptedException {
        createTestFiles(testFiles);
    }

    @Test
    public void testSplitFile() throws IOException, InterruptedException {
        System.out.println("Version 2 - Read small parts of file - small file test");
        System.gc();

        List<String> paths = FileSplitterv2.splitFile(sampleFile, 1);

        assertEquals(6, paths.size());
        assertEquals(1048576, Files.size(Paths.get(paths.get(0))));
        assertEquals(1048576, Files.size(Paths.get(paths.get(1))));
        assertEquals(1048576, Files.size(Paths.get(paths.get(2))));
        assertEquals(1048576, Files.size(Paths.get(paths.get(3))));
        assertEquals(1048576, Files.size(Paths.get(paths.get(4))));
        assertEquals(133128, Files.size(Paths.get(paths.get(5))));

        assertSplitFilesIntegrity(sampleFile, paths);

        paths.forEach(a -> new File(a).delete());
        printMemoryUsage();
    }

    @Test
    public void testSplitHugeFile() throws IOException {
        System.out.println("Version 2 - Read small parts of file - Huge file test");
        System.gc();
        printMemoryUsage();

        List<String> paths = FileSplitterv2.splitFile(gigaFile, 50);

        assertEquals(21, paths.size());
        assertEquals(52428800, Files.size(Paths.get(paths.get(0))));
        assertEquals(52428800, Files.size(Paths.get(paths.get(4))));
        assertEquals(52428800, Files.size(Paths.get(paths.get(9))));
        assertEquals(52428800, Files.size(Paths.get(paths.get(14))));
        assertEquals(52428800, Files.size(Paths.get(paths.get(19))));
        assertEquals(29046793, Files.size(Paths.get(paths.get(20))));

        paths.forEach(a -> new File(a).delete());
        printMemoryUsage();
    }

    @AfterClass
    public static void deleteFiles() {
        deleteTestFiles(testFiles);
    }

}
