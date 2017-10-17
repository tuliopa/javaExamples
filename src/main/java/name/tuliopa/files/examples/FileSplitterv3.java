package name.tuliopa.files.examples;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileSplitterv3 {
    
    public static Stream<String> convertFileToStream(String location) throws IOException {
        return Files.lines(Paths.get(location));
    }
    
    public static void convertStreamToFile(Stream<String> data, Path path) throws IOException {
        Files.write(path, (Iterable<String>) data::iterator);
    }
    
    /**
     * Split a file into multiples files.
     * @param fileName Name of file to be splited.
     * @param mBperSplit maximum number of MB per file.
     * @throws IOException
     */
    public static List<Path> splitFile(final String fileName, final int mBperSplit) throws IOException {
        
    	if(mBperSplit <= 0) {
    		throw new IllegalArgumentException("mBperSplit must be more than zero");
    	}
    	
        String dir = "/tmp/";
        String suffix = ".mulePart";
        List<Path> tempFiles = new ArrayList<>();
        
        long sourceSize = Files.size(Paths.get(fileName));

        long bytesPerSplit = 1024l * 1024l * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;

        long remainingBytes = sourceSize % bytesPerSplit;
        int i=0;
        
        RandomAccessFile fromFile = new RandomAccessFile(fileName, "r");
        FileChannel      fromChannel = fromFile.getChannel();

        
        for( ; i < numSplits; i++){
            //write multipart files.
            Path tempName = Paths.get(dir + UUID.randomUUID() + suffix);            
            RandomAccessFile toFile = new RandomAccessFile(tempName.toFile(), "rw");
            FileChannel      toChannel = toFile.getChannel();

            fromChannel.position(i * bytesPerSplit);
            toChannel.transferFrom(fromChannel, 0, bytesPerSplit);
            toChannel.close();
            toFile.close();
            tempFiles.add(tempName);
        }

        if ( remainingBytes > 0 ){
            Path tempName = Paths.get(dir + UUID.randomUUID() + suffix);
            
            RandomAccessFile toFile = new RandomAccessFile(tempName.toFile(), "rw");
            FileChannel      toChannel = toFile.getChannel();

            fromChannel.position(i * bytesPerSplit);
            toChannel.transferFrom(fromChannel, 0, remainingBytes);
            toChannel.close();
            toFile.close();
            tempFiles.add(tempName);
        }
        
        fromFile.close();
        fromChannel.close();
        return tempFiles;
 	}
	
}
