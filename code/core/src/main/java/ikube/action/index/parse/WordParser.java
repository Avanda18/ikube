package ikube.action.index.parse;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * Parser for the Word format.
 * 
 * @author Michael Couck
 * @since 12.05.04
 * @version 01.00<br>
 *          ----------------------------------
 * @version 01.1
 * @since 22.08.08<br>
 *        Changed the access to the text from the classes from Ackly chap to the POI WordExtractor class.
 * @deprecated Switched to the {@link org.apache.tika.Tika} framework for content extraction
 */
@Deprecated
public class WordParser implements IParser {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final OutputStream parse(final InputStream inputStream, final OutputStream outputStream) throws Exception {
		//POIFSFileSystem fileSystem = new POIFSFileSystem(inputStream);
		//WordExtractor extractor = new WordExtractor(fileSystem);
		//outputStream.write(extractor.getText().trim().getBytes());
		//return outputStream;
        return null;
	}
}