package ikube.action;

import ikube.ATest;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 17.04.11
 * @version 01.00
 */
public class ValidatorTest extends ATest {

	public ValidatorTest() {
		super(ValidatorTest.class);
	}

	@Before
	public void before() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@After
	public void after() {
		FileUtilities.deleteFile(new File(INDEX_CONTEXT.getIndexDirectoryPath()), 1);
	}

	@Test
	public void validate() throws Exception {
		Validator validator = new Validator();
		validator.execute(INDEX_CONTEXT);
		// There should be one mail sent because there are no indexes created

		File latestIndexDirectory = createIndex(INDEX_CONTEXT, "a little sentence");
		File serverIndexDirectory = new File(latestIndexDirectory, IP);
		validator.execute(INDEX_CONTEXT);
		// There should be no mail sent because there is an index generated

		Directory directory = FSDirectory.open(serverIndexDirectory);
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		lock.obtain(1000);
		validator.execute(INDEX_CONTEXT);
		// There should be no mail sent because the index is locked, i.e. being generated
		lock.release();
		directory.clearLock(IndexWriter.WRITE_LOCK_NAME);

		// Delete one file in the index and there should be an exception
		List<File> files = FileUtilities.findFilesRecursively(latestIndexDirectory, new ArrayList<File>(), "segments");
		for (File file : files) {
			FileUtilities.deleteFile(file, 1);
		}
		validator.execute(INDEX_CONTEXT);
		// There should be a mail sent because the index is corrupt

		FileUtilities.deleteFile(latestIndexDirectory, 1);
	}
}