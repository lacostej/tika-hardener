package no.finntech.io.utils;

import no.finntech.io.utils.BitFlipperInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;

public class BitFlipperInputStreamTest {
    private InputStream wrapped;
    private BitFlipperInputStream corrupt;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void bitPosition() throws IOException {
        wrap(new byte[] {0x00, 0x00, 0x00, 0x00});
        corrupt.setBitIndexToCorrupt(0);
        assertEquals(0, corrupt.bitIndexFromCurrentPosition());
        corrupt.setBitIndexToCorrupt(10);
        assertEquals(10, corrupt.bitIndexFromCurrentPosition());

        corrupt.setBitIndexToCorrupt(20);
        assertEquals(20, corrupt.bitIndexFromCurrentPosition());

        corrupt.read();

        assertEquals(12, corrupt.bitIndexFromCurrentPosition());
    }

    @Test
    public void emptyFile() throws IOException {
        wrap(new byte[]{});
        assertEquals(-1, corrupt.getBitIndexToCorrupt());
        assertEquals(0, corrupt.getPosition());
        assertEquals(-1, corrupt.read());
        assertEquals(0, corrupt.getPosition());
    }

    @Test
    public void corruptNoBit() throws IOException {
        wrap(new byte[]{ 0x00});

        assertEquals(-1, corrupt.getBitIndexToCorrupt());
        assertEquals(0, corrupt.getPosition());
        assertEquals(0x00, corrupt.read());
        assertEquals(1, corrupt.getPosition());
    }

    @Test
    public void corruptBitOne() throws IOException {
        wrap(new byte[]{ 0x00});
        corrupt.setBitIndexToCorrupt(0);

        int read = corrupt.read();
        assertEquals(0x80, read);
    }

    @Test
    public void corruptBitTwo() throws IOException {
        wrap(new byte[]{ 0x00});
        corrupt.setBitIndexToCorrupt(1);

        assertEquals(0x40, corrupt.read());
    }

    @Test
    public void corruptBitFifteen() throws IOException {
        wrap(new byte[]{ 0x00, 0x00});
        corrupt.setBitIndexToCorrupt(14);
        assertEquals(0x00, corrupt.read());
        assertEquals(0x02, corrupt.read());
    }

    private void wrap(byte[] buf) {
        wrapped = new ByteArrayInputStream(buf);
        corrupt = new BitFlipperInputStream(wrapped);
    }
}
