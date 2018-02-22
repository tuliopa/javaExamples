package name.tuliopa.files.examples;

/**
 * @author tuliopa
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSplitterv2 {

    private static final String dir = "/tmp/";
    private static final String suffix = ".splitPart";

    /**
     *
     * @param fileName name of file to be splited.
     * @param mBperSplit number of MB per file.
     * @return Return a list of files.
     * @throws IOException
     */
    public static List splitFile(String fileName, int mBperSplit) throws IOException {

        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List partFiles = new ArrayList();

        final long sourceSize = new File(fileName).length();

        final long bytesPerSplit = 1024L * 1024L * mBperSplit;
        final long numSplits = sourceSize / bytesPerSplit;
        long remainingBytes = sourceSize % bytesPerSplit;

        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        int maxReadBufferSize = 8 * 1024; //8KB
        int partNum = 0;
        for (; partNum < numSplits; partNum++) {
            BufferedOutputStream bw = newWriteBuffer(partNum, partFiles);
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if (remainingBytes > 0) {
            BufferedOutputStream bw = newWriteBuffer(partNum, partFiles);
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();

        return partFiles;
    }

    private static BufferedOutputStream newWriteBuffer(int partNum, List partFiles) throws FileNotFoundException {
        String partFileName = dir + "part" + partNum + suffix;
        partFiles.add(partFileName);
        return new BufferedOutputStream(new FileOutputStream(partFileName));
    }

    private static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }

}
