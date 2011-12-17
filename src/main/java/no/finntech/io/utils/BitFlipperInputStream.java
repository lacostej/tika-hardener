package no.finntech.io.utils;

import java.io.IOException;
import java.io.InputStream;

/**
 * A variation on input stream that flips the specified bit to simulate file corruption.
 */
public class BitFlipperInputStream extends InputStream {
    private long position = 0;
    private long bitIndexToCorrupt;
    private InputStream in;
    
    public BitFlipperInputStream(InputStream in) {
        this(in, -1);
    }

    public BitFlipperInputStream(InputStream in, long indexOfBitToCorrupt) {
        this.bitIndexToCorrupt = indexOfBitToCorrupt;
        this.in = in;
    }

    void setPosition(long position) {
        this.position = position;
    }

    public long getBitIndexToCorrupt() {
        return bitIndexToCorrupt;
    }

    public void setBitIndexToCorrupt(long bitIndexToCorrupt) {
        this.bitIndexToCorrupt = bitIndexToCorrupt;
    }

    @Override
    public int read() throws IOException {
        int read = in.read();
        if (read != -1) {
            read = corruptRead(read);
            position++;
        }

        return read;
    }

    private int corruptRead(final int read) {
        int res = read;
        long corruptBitIndexInPosition = bitIndexFromCurrentPosition();
        if (corruptBitIndexInPosition >= 0 && corruptBitIndexInPosition < 8) {
            res = flipBit(res, corruptBitIndexInPosition);
        }
        return res;
    }

    private static int flipBit(int res, long indexOfBitToFlip) {
        res ^= 1 << (7 - indexOfBitToFlip);
        return res;
    }

    /**
     * Flip a bit in the specified byte array
     * @param data
     * @param indexOfBitToFlip
     */
    private static void flipBit(byte[] data, long indexOfBitToFlip) {
        int index = (int) indexOfBitToFlip / 8;
        int indexInByte = (int) (indexOfBitToFlip % 8);
        if (index >= 0 && index < data.length)
            data[index] ^= 1 << (7 - indexInByte);
    }


    long bitIndexFromCurrentPosition() {
        return bitIndexToCorrupt - position * 8;
    }

    public long getPosition() {
        return position;
    }
}
