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


    private static byte[] convertFileToBytes(String location) throws IOException {
        RandomAccessFile f = new RandomAccessFile(location, "r");
        byte[] b = new byte[(int)f.length()];
        f.readFully(b);
        return b;
    }
    
    private static void writeBytes(String name, byte[] buffer) throws IOException {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(name));
        bw.write(buffer);
        bw.close();
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
    	
        String dir = "/tmp/";
        String suffix = ".splitPart";
        List tempFiles = new ArrayList();
        
        final long sourceSize = new File(fileName).length();

        int bytesPerSplit = 1024 * 1024 * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;
        long remainingBytes = sourceSize % bytesPerSplit;

      /// Copy arrays
        byte[] originalbytes = convertFileToBytes(fileName);
        int i=0;
        for( ; i < numSplits; i++){
            //write stream to a file.
            String tempName = dir + "part" + i + suffix;
            byte[] b = new byte[bytesPerSplit];
            System.arraycopy(originalbytes, (i * bytesPerSplit), b, 0, bytesPerSplit);
            writeBytes(tempName, b);
            tempFiles.add(tempName);
        }

        if ( remainingBytes > 0 ){
            String tempName = dir + "part" + i + suffix;
            byte[] b = new byte[(int)remainingBytes];
            System.arraycopy(originalbytes, (i * bytesPerSplit), b, 0, (int)remainingBytes);
            writeBytes(tempName, b);
            tempFiles.add(tempName);
        }

        return tempFiles;
    }
}
