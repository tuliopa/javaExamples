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

public class FileSplitterv2 {
    
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
    	
        String dir = "/tmp/";
        String suffix = ".splitPart";
        List tempFiles = new ArrayList();
        
        final long sourceSize = new File(fileName).length();

        long bytesPerSplit = 1024l * 1024l * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;
        long remainingBytes = sourceSize % bytesPerSplit;
        
        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        int maxReadBufferSize = 8 * 1024; //8MB
        for(int destIx=1; destIx <= numSplits; destIx++) {
            String tempName = dir + "part"+ destIx + suffix;
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(tempName));
            if(bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit/maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for(int i=0; i<numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if(numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            }else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
            tempFiles.add(tempName);
        }
        if(remainingBytes > 0) {
            String tempName = dir + "part"+ (numSplits+1) + suffix;
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(tempName));
            readWrite(raf, bw, remainingBytes);
            bw.close();
            tempFiles.add(tempName);
        }
        raf.close();
        
        return tempFiles;
    }
    

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if(val != -1) {
            bw.write(buf);
        }
    }

}
