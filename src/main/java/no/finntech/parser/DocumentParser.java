package no.finntech.parser;

import java.io.IOException;
import java.io.InputStream;

public interface DocumentParser {
    String parseToString(InputStream is) throws DocumentParserException, IOException;

    void close();
}
