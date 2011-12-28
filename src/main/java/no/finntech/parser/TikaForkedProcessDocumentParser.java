package no.finntech.parser;

import org.apache.tika.exception.TikaException;
import org.apache.tika.fork.ForkParser;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class just delegates the parsing to Tika forked parser. For a discussion on tika ForkParser, see Tika's web site.
 * <p>
 * Clients that are finished using this document parser should {@link #close()} all resources.
 * <p> 
 * This allows to give more stability to the parsing process.  
 * 
 * @see org.apache.tika.Tika
 * @see org.apache.tika.fork.ForkParser
 * @see org.apache.tika.parser.AutoDetectParser the parser used in the forked process
 */
public class TikaForkedProcessDocumentParser implements DocumentParser {

    private final ForkTika tika = new ForkTika();

    /**
     * Override the default fork parser poolsize (5)
     * @param poolSize
     * @see ForkParser#setPoolSize(int) 
     */
    public void setPoolSize(int poolSize) {
        tika.parser.setPoolSize(poolSize);
    }

    /**
     * Override the default fork parser java command (java -Xmx32m)
     * @param javaCommand
     * @see ForkParser#setJavaCommand(String)
     */
    public void setJavaCommand(String javaCommand) {
        tika.parser.setJavaCommand(javaCommand);
    }

    /**
     * Tika.parseToString doesn't support ForkParser
     *
     * @see <a href="https://issues.apache.org/jira/browse/TIKA-830">TIKA-830</a>
     */
    private static class ForkTika {
        private ClassLoader classLoader = ForkParser.class.getClassLoader();
        private AutoDetectParser underlyingParser = new AutoDetectParser();
        private ForkParser parser = new ForkParser(classLoader, underlyingParser);

        /**
         * Maximum length of the strings returned by the parseToString methods.
         * Used to prevent out of memory problems with huge input documents.
         * The default setting is 100k characters.
         */
        private int maxStringLength = 100 * 1000;

        private String parseToString(InputStream stream) throws IOException, TikaException {
            return parseToString(stream, new Metadata());
        }

        // copy of Tika.parseToString() adapted to work with ForkParser. The only change is that we pass
        // the proper parser instance in the context
        private String parseToString(InputStream stream, Metadata metadata) throws IOException, TikaException {
            WriteOutContentHandler handler =
                    new WriteOutContentHandler(maxStringLength);

            try {
                ParseContext context = new ParseContext();
                context.set(Parser.class, underlyingParser);
                parser.parse(stream, new BodyContentHandler(handler), metadata, context);
            } catch (SAXException e) {
                if (!handler.isWriteLimitReached(e)) {
                    // This should never happen with BodyContentHandler...
                    throw new TikaException("Unexpected SAX processing failure", e);
                }
            } finally {
                stream.close();
            }
            return handler.toString();

        }
    }

    public String parseToString(InputStream is) throws DocumentParserException, IOException {
        try {
            return tika.parseToString(is);
        } catch (TikaException e) {
            throw new DocumentParserException(e);
        }
    }

    public void close() {
        tika.parser.close();
    }
}
