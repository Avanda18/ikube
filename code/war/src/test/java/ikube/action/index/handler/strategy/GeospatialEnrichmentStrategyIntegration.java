package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.filesystem.IndexableFilesystemCsvHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableFileSystemCsv;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import ikube.toolkit.URI;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.util.concurrent.ForkJoinTask;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 20-01-2012
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class GeospatialEnrichmentStrategyIntegration extends IntegrationTest {

    @Autowired
    @Qualifier("geoname-csv")
    private IndexContext indexContext;
    @Autowired
    @Qualifier("geoname-csv-files")
    private IndexableFileSystemCsv indexableFileSystemCsv;
    @Autowired
    private IndexableFilesystemCsvHandler indexableHandlerFilesystemCsvHandler;

    @Test
    public void aroundProcess() throws Exception {
        File file = FILE.findFileRecursively(new File("."), "min-eco.csv");
        indexableFileSystemCsv.setPath(file.getParentFile().getAbsolutePath());
        IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, System.currentTimeMillis(), URI.getIp());
        indexContext.setIndexWriters(indexWriter);
        ForkJoinTask<?> forkJoinTask = indexableHandlerFilesystemCsvHandler.handleIndexableForked(indexContext, indexableFileSystemCsv);

        THREAD.executeForkJoinTasks(indexContext.getName(), indexableFileSystemCsv.getThreads(), forkJoinTask);
        THREAD.sleep(15000);
        THREAD.cancelForkJoinPool(indexContext.getName());

        IndexManager.closeIndexWriter(indexWriter);
        String indexPath = IndexManager.getIndexDirectoryPath(indexContext);
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexPath);
        IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(latestIndexDirectory, URI.getIp())));
        for (int i = 0; i < indexReader.numDocs(); i++) {
            Document document = indexReader.document(i);
            assertNotNull(document.get(IConstants.LAT));
            assertNotNull(document.get(IConstants.LNG));
        }
    }

}