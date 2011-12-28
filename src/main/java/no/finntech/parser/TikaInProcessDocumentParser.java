package no.finntech.parser;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class just delegates the parsing to a Tika in the same process.
 * @see org.apache.tika.Tika
 * @see org.apache.tika.parser.AutoDetectParser
 */
public class TikaInProcessDocumentParser implements DocumentParser {
    private Tika tika = new Tika();

    public String parseToString(InputStream is) throws DocumentParserException, IOException {
        try {
            return tika.parseToString(is);
        } catch (TikaException e) {
            throw new DocumentParserException(e);
        }
    }

    public void close() {
    }
}
