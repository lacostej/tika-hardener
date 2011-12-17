package no.finntech.tika.harderner;

import no.finntech.io.utils.BitFlipperInputStream;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * A set of test that flips bits in various files to detect unexpected issues in the various parsers.
 */
public class TikaIndexerHardenerTest {

    private Tika tika = new Tika();

    @Test
    public void originalFileIndexesProperly() throws Exception {
        URL url = getFileUrl("testing.doc");
        assertEquals(IndexResult.OK, flipBitAndIndexContent(url, -1));
    }

    @Test
    public void invalidPoiSectionSizeShouldntCauseUnhandledExceptions() throws Exception {
        URL url = getFileUrl("testing.doc");
        assertEquals(IndexResult.OK, flipBitAndIndexContent(url, 2295 * 8 + 2));
    }

    // these tests take longer time but allows to find various kind of problems
    @Test
    public void flipOneBitAtATimeSimpleDoc() throws Exception {
        flipOneBitAtATime("testing.doc");
    }

    @Test
    public void flipOneBitAtATimeSimpleDocx() throws Exception {
        flipOneBitAtATime("testing.docx");
    }

    @Test
    public void flipOneBitAtATimeSimplePdf() throws Exception {
        flipOneBitAtATime("testing.pdf");
    }

    private void flipOneBitAtATime(String fileName) throws IOException {
        URL url = getFileUrl(fileName);
        File f = new File(url.getFile());
        long nbBits = f.length() * 8;
        System.out.println("Testing... " + url + " with " + nbBits + " bits to flip");
        long before = System.nanoTime();
        List<Integer> unhandled = new ArrayList<Integer>();
        for (int indexOfBitToFlip = 0; indexOfBitToFlip < nbBits; indexOfBitToFlip++) {
            IndexResult handlingResult = flipBitAndIndexContent(url, indexOfBitToFlip);
            if (handlingResult == IndexResult.UNHANDLED)
                unhandled.add(indexOfBitToFlip);
        }
        long after = System.nanoTime();
        System.out.println("Total indexing took: " + (after - before) / 1000000 + " ms.");
        assertEquals("The following flipped bit indexes caused unhandled exceptions in file: " + fileName + " : " + unhandled, 0, unhandled.size());
    }

    static enum IndexResult {
        OK, HANDLED, UNHANDLED
    }

    private IndexResult flipBitAndIndexContent(URL url, long indexOfBitToFlip) throws IOException {
        InputStream inputStream = url.openStream();
        inputStream = new BitFlipperInputStream(inputStream, indexOfBitToFlip);
        IndexResult result = parseContent(inputStream);
        if (result == IndexResult.UNHANDLED)
            System.err.println("[" + result + "] bit #" + indexOfBitToFlip + " caused unexpected exception.");
        return result;
    }

    private IndexResult parseContent(InputStream inputStream) throws IOException {
        IndexResult result = IndexResult.OK;
        try {
            tika.parseToString(inputStream);
        } catch (TikaException ignored) {
            result = IndexResult.HANDLED;
        } catch (IOException ignored) {
            result = IndexResult.HANDLED;
        } catch (Throwable unhandled) {
            result = IndexResult.UNHANDLED;
            unhandled.printStackTrace(System.err);
        } finally {
            inputStream.close();
        }
        return result;
    }

    private URL getFileUrl(String fileName) throws MalformedURLException {
        return new URL(this.getClass().getResource("/" + fileName).toString());
    }

}
