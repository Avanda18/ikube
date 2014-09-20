package ikube.search;

import ikube.AbstractTest;
import ikube.model.IndexContext;
import ikube.toolkit.FileUtilities;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-02-2014
 */
public class SearchToolkitTest extends AbstractTest {

    private IndexContext indexContext;

    @Before
    public void before() {
        indexContext = mock(IndexContext.class);
        when(indexContext.getIndexDirectoryPath()).thenReturn("/tmp");
        when(indexContext.getName()).thenReturn("index");
        when(indexContext.getIndexName()).thenReturn("index");
        when(indexContext.getBufferedDocs()).thenReturn(10);
        when(indexContext.getBufferSize()).thenReturn(10d);
        when(indexContext.getMaxFieldLength()).thenReturn(10);
        when(indexContext.getMaxReadLength()).thenReturn(10000l);
        when(indexContext.getMergeFactor()).thenReturn(10);
        when(indexContext.getMaxAge()).thenReturn(60l);
    }

    @Test
    public void main() {
        String text = "and some data for hello world search";
        File indexDirectory = createIndexFileSystem(indexContext, System.currentTimeMillis(), "127.0.1.1", text);
        String indexDirectoryPath = FileUtilities.cleanFilePath(indexDirectory.getAbsolutePath());
        String[] args = {indexDirectoryPath, "contents", "hello world"};
        SearchToolkit.main(args);
    }

}
