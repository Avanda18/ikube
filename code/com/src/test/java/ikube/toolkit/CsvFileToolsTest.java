package ikube.toolkit;

import ikube.AbstractTest;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-08-2014
 */
public class CsvFileToolsTest extends AbstractTest {

    private String inputFilePath;
    @Spy
    private CsvFileTools csvFileTools;

    @Before
    public void before() {
        File inputFile = FileUtilities.findFileRecursively(new File("."), "csv-file-tools.csv");
        inputFilePath = FileUtilities.cleanFilePath(inputFile.getAbsolutePath());
        Deencapsulation.setField(csvFileTools, "inputFile", inputFilePath);
    }

    @Test
    public void getCsvData() throws Exception {
        Object[][] csvData = csvFileTools.getCsvData(inputFilePath);
        assertNotNull(csvData);
        assertTrue(csvData.length == 2);
        assertTrue(csvData[0].length == 4);
    }

    @Test
    public void getCsvDataStreamExcluded() throws Exception {
        String input = "1,2,3,4,5,6,7,8,9,10\n1,2,3,4,5,6,7,8,9,10\n1,2,3,4,5,6,7,8,9,10";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Object[][] csvData = csvFileTools.getCsvData(inputStream, 2, 8, 9);
        assertEquals(3, csvData.length);
        assertEquals(7, csvData[0].length);
    }

}