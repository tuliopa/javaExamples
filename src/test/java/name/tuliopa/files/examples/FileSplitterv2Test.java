package name.tuliopa.files.examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

public class FileSplitterv2Test {

	private static String dir = "/tmp/";
	private static String sampleFile = dir + "sampleFile.txt";
	private static String gigaFile = dir + "GigaFile.txt";
	
	@BeforeClass
	public static void createFiles() throws InterruptedException {
		String generator = "src/main/resources/text_generator.sh";
		try {
			Process p = new ProcessBuilder(generator, "1500", sampleFile).start();
			p.waitFor();
			
			p = new ProcessBuilder(generator, "300000", gigaFile).start();
			p.waitFor();
		} catch (IOException e) {
			fail(e.getMessage());
		}
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
		assertEquals( 133128, Files.size(Paths.get(paths.get(5))));
		
		// concatenate files using unix command to validate integrity.
		String result = "/tmp/resultConcat.txt";
		File resultFile = new File(result);
		
		List<String> args = new ArrayList<>();
		args.add("cat");
		paths.forEach(a -> args.add(a.toString()));
		
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectOutput(resultFile);
		
		Process concat = pb.start();
		concat.waitFor(3, TimeUnit.SECONDS);
		
		// Check if files are equal.
		Process p = new ProcessBuilder("diff", sampleFile, result).start();
		p.waitFor(3, TimeUnit.SECONDS);
		int exitValue = p.exitValue();
		assertEquals(0, exitValue);
		
		// Delete multipart files.
		paths.forEach(a -> new File(a.toString()).delete() );
		resultFile.delete();
		
		printMemoryUsage();
	}
	
	@Test
	public void testSplitHugeFile() throws IOException, InterruptedException {
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

		paths.forEach(a -> Paths.get(a).toFile().delete());
		printMemoryUsage();
	}
	
	@AfterClass
	public static void deleteFiles() {
		File f = new File(sampleFile);
		f.delete();
		f = new File(gigaFile);
		f.delete();
	}
	
	private void printMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		System.out.println("Memory  Used: " + (r.totalMemory() - r.freeMemory()));
	}

}
