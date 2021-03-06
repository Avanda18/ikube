package ikube.action;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.ClusterManagerMock;
import ikube.toolkit.FILE;
import mockit.Deencapsulation;
import mockit.Mockit;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 16-01-2012
 */
public class CleanTest extends AbstractTest {

    @Before
    public void before() {
        when(indexContext.getIndexDirectoryPath()).thenReturn("./" + this.getClass().getSimpleName());
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        Mockit.setUpMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class, ClusterManagerMock.class);
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
    }

    /**
     * In this test we create one index. Then run the clean and nothing should happen, i.e. the index
     * should still remain, nothing should be deleted. Then we delete one of the index files, i.e. making
     * the index corrupt and run the clean again, this time the clean should delete the corrupt index.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void execute() throws Exception {
        File latestIndexDirectory = createIndexFileSystem(indexContext, "some words to index");

        // Running the clean the locked index directory should be un-locked
        Clean clean = new Clean();
        Deencapsulation.setField(clean, clusterManager);
        clean.execute(indexContext);

        Directory directory = FSDirectory.open(latestIndexDirectory);
        try {
            assertTrue(DirectoryReader.indexExists(directory));
        } finally {
            directory.close();
        }

        List<File> files = FILE.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
        for (File file : files) {
            FILE.deleteFile(file, 1);
        }

        clean.execute(indexContext);
        assertFalse("The index directory should have been deleted because it is corrupt : ", latestIndexDirectory.exists());

        latestIndexDirectory = createIndexFileSystem(indexContext, "some words to index");
        Lock lock = getLock(FSDirectory.open(latestIndexDirectory), latestIndexDirectory);
        boolean isLocked = lock.isLocked();
        assertTrue("We should be able to get the lock from the index directory : ", isLocked);

        clean.execute(indexContext);

        directory = FSDirectory.open(latestIndexDirectory);
        try {
            assertTrue("This index exists, but is locked : ", IndexWriter.isLocked(directory));
            assertTrue("This index exists, but is locked : ", DirectoryReader.indexExists(directory));
        } finally {
            directory.close();
        }
        clean.execute(indexContext);
        lock.close();
    }

}