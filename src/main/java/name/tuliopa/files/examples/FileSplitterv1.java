package name.tuliopa.files.examples;


/**
 *
 * @author tuliopa
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSplitterv1 {

	private static final String dir = "/tmp/";
    private static final String suffix = ".splitPart";

    private static byte[] convertFileToBytes(String location) throws IOException {
        RandomAccessFile f = new RandomAccessFile(location, "r");
        byte[] b = new byte[(int)f.length()];
        f.readFully(b);
        f.close();
        return b;
    }
    
    private static void writeBufferToFiles(byte[] buffer, String fileName) throws IOException {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName));
        bw.write(buffer);
        bw.close();
    }
    
    private static void copyBytesToPartFile(byte[] originalBytes, List partFiles, int partNum, int bytesPerSplit, int bufferSize) throws IOException{
        String partFileName = dir + "part" + partNum + suffix;
        byte[] b = new byte[bufferSize];
        System.arraycopy(originalBytes, (partNum * bytesPerSplit), b, 0, bufferSize);
        writeBufferToFiles(b, partFileName);
        partFiles.add(partFileName);
    }

    /**
     * 
     * @param fileName name of file to be splited.
     * @param mBperSplit number of MB per file.
     * @return Return a list of files.
     * @throws IOException
     */
    public static List splitFile(String fileName, int mBperSplit) throws IOException {
        
    	if(mBperSplit <= 0) {
    		throw new IllegalArgumentException("mBperSplit must be more than zero");
    	}

        List partFiles = new ArrayList();
        final long sourceSize = new File(fileName).length();
        int bytesPerSplit = 1024 * 1024 * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;
        int remainingBytes = (int) sourceSize % bytesPerSplit;

      /// Copy arrays
        byte[] originalBytes = convertFileToBytes(fileName);
        int partNum=0;
        while(partNum < numSplits){
            //write bytes to a part file.
            copyBytesToPartFile(originalBytes, partFiles, partNum, bytesPerSplit, bytesPerSplit);
            ++partNum;
        }

        if ( remainingBytes > 0 ){
            copyBytesToPartFile(originalBytes, partFiles, partNum, bytesPerSplit, remainingBytes);
        }

        return partFiles;
    }
}
