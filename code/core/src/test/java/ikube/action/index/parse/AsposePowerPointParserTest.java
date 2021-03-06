package ikube.action.index.parse;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FILE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class AsposePowerPointParserTest extends AbstractTest {

	@Test
	public void parse() throws Exception {
		// final InputStream inputStream, final OutputStream outputStream
		AsposePowerPointParser asposePowerPointParser = new AsposePowerPointParser();
		File file = FILE.findFileRecursively(new File("."), "pptx.pptx");
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		asposePowerPointParser.parse(inputStream, outputStream);
		String text = outputStream.toString();
		String fragment = "Complete development in mdot and mobile";
		assertTrue("Document must contain this text : " + fragment, text.contains(fragment));
	}

}
