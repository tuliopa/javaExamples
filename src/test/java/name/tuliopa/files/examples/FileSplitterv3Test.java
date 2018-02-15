package name.tuliopa.files.examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import name.tuliopa.files.examples.FileSplitterv3;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FileSplitterv3Test {

	private static String dir = "/tmp/";
	private static String smallFile = dir + "smallTestFile.txt";
	private static String sampleFile = dir + "sampleFile.txt";
	private static String gigaFile = dir + "GigaFile.txt";
	
	@BeforeClass
	public static void createFiles() throws InterruptedException {
		String generator = "src/main/resources/text_generator.sh";
		try {
			Process p = new ProcessBuilder(generator, "5", smallFile).start();
			p.waitFor();
			
			p = new ProcessBuilder(generator, "1500", sampleFile).start();
			p.waitFor();
			
			p = new ProcessBuilder(generator, "300000", gigaFile).start();
			p.waitFor();
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
		
	@Test
	public void testConvertStreamToFile(){
		String fileName = "/tmp/writeStream.txt";
		try {
			List<String> persons = Arrays.asList("Max", "Peter", "Pamela", "Alexandra", "Maria", "David");
			
			FileSplitterv3.convertStreamToFile(persons.stream(), Paths.get(fileName));
		
			File f = new File(fileName);
			assertTrue(f.exists());
			
			List<String> results = Files.readAllLines(Paths.get(fileName));
			assertEquals("Max", results.get(0));
			assertEquals("Peter", results.get(1));
			assertEquals("Pamela", results.get(2));
			assertEquals("Alexandra", results.get(3));
			assertEquals("Maria", results.get(4));
			assertEquals("David", results.get(5));
			
			//delete test file.
			f.delete();
			
			
		} catch ( IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testConvertFileToStream(){
		
		try {
			Stream<String> stream = FileSplitterv3.convertFileToStream(smallFile);
			List<String> lines = stream.collect(Collectors.toList());
			
			assertEquals(321, lines.size());
			assertTrue(lines.get(0).startsWith("0000000"));
			assertTrue(lines.get(1).startsWith("0000010"));
			assertTrue(lines.get(320).startsWith("0001400"));
			
		} catch ( IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSplitFile() throws IOException, InterruptedException {
		System.out.println("Splitter V3 - small file test");
		System.gc();
		
		List<Path> paths = FileSplitterv3.splitFile(sampleFile, 1);

		assertEquals(6, paths.size());
		assertEquals(1048576, Files.size(paths.get(0)));
		assertEquals(1048576, Files.size(paths.get(1)));
		assertEquals(1048576, Files.size(paths.get(2)));
		assertEquals(1048576, Files.size(paths.get(3)));
		assertEquals(1048576, Files.size(paths.get(4)));
		assertEquals( 133128, Files.size(paths.get(5)));
		
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
	
	@Test(expected=IOException.class)
	public void testSplitFileNotFound() throws IOException {
		FileSplitterv3.splitFile("/tmp/wrongName.txt", 1);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSplitFileIllegalArgument() throws IOException, IllegalArgumentException {
		FileSplitterv3.splitFile(sampleFile, -1);
	}
	
	@Test
	public void testSplitHugeFile() throws IOException, InterruptedException {
		System.out.println("Splitter V3 - huge file test");
		System.gc();
		
		List<Path> paths = FileSplitterv3.splitFile(gigaFile, 50);

		assertEquals(21, paths.size());
		assertEquals(52428800, Files.size(paths.get(0)));
		assertEquals(52428800, Files.size(paths.get(4)));
		assertEquals(52428800, Files.size(paths.get(9)));
		assertEquals(52428800, Files.size(paths.get(14)));
		assertEquals(52428800, Files.size(paths.get(19)));
		assertEquals(29046793, Files.size(paths.get(20)));
		
		Files.delete(Paths.get(gigaFile));
		paths.forEach(a -> a.toFile().delete());
		
		printMemoryUsage();
	}
	
	@AfterClass
	public static void deleteFiles() {
		File f = new File(smallFile);
		f.delete();
		f = new File(sampleFile);
		f.delete();
	}
	
	private void printMemoryUsage() {
		Runtime r = Runtime.getRuntime();
		System.out.println("Memory  Used: " + (r.totalMemory() - r.freeMemory()));
	}

}
