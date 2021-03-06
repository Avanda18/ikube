package ikube.action.index.parse;

import static org.junit.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.toolkit.FILE;
import ikube.toolkit.PERFORMANCE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 12.10.2010
 * @version 01.00
 */
public class ParserPerformanceTest extends AbstractTest {

	private int			iterations	= 10;
	private String[]	files		= { "pdf.pdf", "xml.xml", "html.html", "xls.xls", "doc.doc", "txt.txt", "pot.pot", "docx.docx", "xlsx.xlsx",
			"rtf.rtf" /* "pptx.pptx" */};

	@Test
	public void parserPerformance() throws Exception {
		for (String fileName : files) {
			final File file = FILE.findFileRecursively(new File("."), fileName);
			final IParser parser = ParserProvider.getParser(file.getName(), new byte[0]);
			PERFORMANCE.execute(new PERFORMANCE.APerform() {
                @Override
                public void execute() throws Exception {
                    InputStream inputStream = new FileInputStream(file);
                    parser.parse(inputStream, new ByteArrayOutputStream());
                }
            }, parser.getClass().getSimpleName() + ", file name : " + fileName, iterations, true);
		}
	}

	@Test
	public void patternPerformance() throws Exception {
		File file = FILE.findFileRecursively(new File("."), new String[]{"html.html"});
		byte[] bytes = FILE.getContents(file, Integer.MAX_VALUE).toByteArray();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		final String string = FILE.getContents(inputStream, Integer.MAX_VALUE).toString();
		// (@)?(href=')?(HREF=')?(HREF=\")?(href=\")?
		// This pattern will extract the urls form the log so we can check them
		final Pattern pattern = Pattern.compile("(http://|https://)[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\n\\-=?\\+\\%/\\.\\w]+)?");
		double executionsPerSecond = PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() {
                Matcher matcher = pattern.matcher(string);
                while (matcher.find()) {
                    matcher.group();
                }
            }
        }, "Pattern matcher : ", 100, true);
		assertTrue(executionsPerSecond > 10);
	}

}