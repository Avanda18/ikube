package ikube;

import ikube.action.index.IndexManager;
import ikube.action.index.parse.mime.MimeMapper;
import ikube.action.index.parse.mime.MimeTypes;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.mock.ApplicationContextManagerMock;
import ikube.mock.IndexManagerMock;
import ikube.model.*;
import ikube.search.Search;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.Logging;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.UriUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static ikube.toolkit.ApplicationContextManager.getBean;
import static ikube.toolkit.ObjectToolkit.populateFields;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is the base test for all mocked tests. There are several useful mocks in this class that can be re-used like the index context and the index reader etc.
 * There are also helpful methods for creating Lucene indexes.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21.11.10
 */
@Ignore
@SuppressWarnings("deprecation")
public abstract class AbstractTest {

	static {
		try {
			Logging.configure();
			new MimeTypes(IConstants.MIME_TYPES);
			new MimeMapper(IConstants.MIME_MAPPING);
			ThreadUtilities.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * These are all mocked objects that are used in sub classes.
	 */
	protected String ip;
	protected ScoreDoc[] scoreDocs;
	protected List<Indexable<?>> indexables;
	protected Map<String, Server> servers;

	protected Lock lock;
	protected Server server;
	protected Action action;
	protected TopDocs topDocs;
	protected IDataBase dataBase;
	protected FSDirectory fsDirectory;
	protected IndexWriter indexWriter;
	protected IndexReader indexReader;
	protected TopFieldDocs topFieldDocs;
	protected IndexSearcher multiSearcher;
	protected IndexSearcher indexSearcher;
	protected IndexContext<?> indexContext;
	protected IClusterManager clusterManager;
	protected IMonitorService monitorService;
	protected IndexableTable indexableTable;
	protected IndexableColumn indexableColumn;
	protected Map<String, IndexContext> indexContexts;

	protected String indexDirectoryPath = "./indexes";
	protected String indexDirectoryPathBackup = "./indexes/backup";

	{
		try {
			initialize();
		} catch (Exception e) {
			logger.error(null, e);
		}
	}

	private void initialize() throws Exception {
		lock = mock(Lock.class);
		server = mock(Server.class);
		action = mock(Action.class);
		topDocs = mock(TopDocs.class);
		dataBase = mock(IDataBase.class);
		fsDirectory = mock(FSDirectory.class);
		indexWriter = mock(IndexWriter.class);
		indexReader = mock(IndexReader.class);
		topFieldDocs = mock(TopFieldDocs.class);
		multiSearcher = mock(IndexSearcher.class);
		indexSearcher = mock(IndexSearcher.class);
		indexContext = mock(IndexContext.class);
		clusterManager = mock(IClusterManager.class);
		monitorService = mock(IMonitorService.class);
		indexableTable = mock(IndexableTable.class);
		indexableColumn = mock(IndexableColumn.class);

		scoreDocs = new ScoreDoc[0];
		indexables = new ArrayList<Indexable<?>>();
		servers = new HashMap<String, Server>();
		indexContexts = new HashMap<String, IndexContext>();

		ip = UriUtilities.getIp();

		when(indexSearcher.getIndexReader()).thenReturn(indexReader);
		when(indexSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);

		when(multiSearcher.search(any(Query.class), anyInt())).thenReturn(topDocs);
		when(multiSearcher.search(any(Query.class), any(Filter.class), anyInt(), any(Sort.class))).thenReturn(topFieldDocs);

		topDocs.totalHits = 0;
		topDocs.scoreDocs = scoreDocs;
		topFieldDocs.totalHits = 0;
		topFieldDocs.scoreDocs = scoreDocs;

		when(fsDirectory.makeLock(anyString())).thenReturn(lock);

		when(indexWriter.getDirectory()).thenReturn(fsDirectory);

		when(indexContext.getIndexDirectoryPath()).thenReturn(indexDirectoryPath);
		when(indexContext.getIndexDirectoryPathBackup()).thenReturn(indexDirectoryPathBackup);
		when(indexContext.getIndexName()).thenReturn("index");
		when(indexContext.getChildren()).thenReturn(indexables);

		when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

		when(indexContext.getBufferedDocs()).thenReturn(10);
		when(indexContext.getBufferSize()).thenReturn(10d);
		when(indexContext.getMaxFieldLength()).thenReturn(10);
		when(indexContext.getMaxReadLength()).thenReturn(10000l);
		when(indexContext.getMergeFactor()).thenReturn(10);
		when(indexContext.getMaxAge()).thenReturn((long) (60));
		when(clusterManager.getServer()).thenReturn(server);
		when(clusterManager.getServers()).thenReturn(servers);
		when(clusterManager.lock(anyString())).thenReturn(Boolean.TRUE);

		indexContexts.put(indexContext.getName(), indexContext);
		when(monitorService.getIndexContexts()).thenReturn(indexContexts);

		when(action.getIndexName()).thenReturn("index");

		when(server.isWorking()).thenReturn(Boolean.FALSE);
		when(server.getAddress()).thenReturn(ip);
		when(server.getIp()).thenReturn(ip);
		when(server.getActions()).thenReturn(Arrays.asList(action));
		when(indexContext.getIndexWriters()).thenReturn(new IndexWriter[]{indexWriter});
		when(indexableTable.getName()).thenReturn("indexableTable");
		when(indexableColumn.getContent()).thenReturn("9a avenue road, cape town, south africa");
		when(indexableColumn.isAddress()).thenReturn(Boolean.TRUE);
		when(indexableColumn.getName()).thenReturn("indexableName");

		when(indexableColumn.isAnalyzed()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isStored()).thenReturn(Boolean.TRUE);
		when(indexableColumn.isVectored()).thenReturn(Boolean.TRUE);

		when(ApplicationContextManagerMock.HANDLER.getIndexableClass()).thenReturn(IndexableTable.class);

		indexables.add(indexableTable);
		indexables.add(indexableColumn);
		servers.put(ip, server);
		IndexManagerMock.setIndexWriter(indexWriter);
		ApplicationContextManagerMock.setIndexContext(indexContext);
		ApplicationContextManagerMock.setClusterManager(clusterManager);
	}

	protected static void delete(final IDataBase dataBase, final Class<?>... klasses) {
		for (final Class<?> klass : klasses) {
			List<?> list = dataBase.find(klass, 0, 1000);
			do {
				dataBase.removeBatch(list);
				list = dataBase.find(klass, 0, 1000);
			} while (list.size() > 0);
		}
	}

	protected static <T> void insert(final Class<T> klass, final int entities) {
		IDataBase dataBase = getBean(IDataBase.class);
		List<T> tees = new ArrayList<T>();
		for (int i = 0; i < entities; i++) {
			T tee;
			try {
				tee = populateFields(klass, klass.newInstance(), Boolean.TRUE, 1, "id", "indexContext");
				tees.add(tee);
				if (tees.size() >= 1000) {
					dataBase.persistBatch(tees);
					tees.clear();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		dataBase.persistBatch(tees);
	}

	protected File createIndexFileSystem(final IndexContext<?> indexContext, final String... strings) {
		try {
			return createIndexFileSystem(indexContext, System.currentTimeMillis(), ip, strings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected Directory createIndexRam(final IndexContext<?> indexContext, final String... strings) throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(new StandardAnalyzer(IConstants.LUCENE_VERSION));
		addDocuments(indexWriter, IConstants.CONTENTS, strings);
		return indexWriter.getDirectory();
	}

	protected File createIndexFileSystem(final IndexContext<?> indexContext, final long time, final String ip, final String... strings) throws Exception {
		IndexWriter indexWriter = IndexManager.openIndexWriter(indexContext, time, ip);
		addDocuments(indexWriter, IConstants.CONTENTS, strings);
		File indexDirectory = ((FSDirectory) indexWriter.getDirectory()).getDirectory();
		IndexManager.closeIndexWriter(indexWriter);
		return indexDirectory;
	}

	protected List<File> createIndexesFileSystem(final IndexContext<?> indexContext, final long time, final String[] ips, final String... strings) {
		try {
			List<File> serverIndexDirectories = new ArrayList<File>();
			for (String ip : ips) {
				File serverIndexDirectory = createIndexFileSystem(indexContext, time, ip, strings);
				serverIndexDirectories.add(serverIndexDirectory);
			}
			return serverIndexDirectories;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected <T extends Search> T createIndexRamAndSearch(final Class<T> searchClass, final Analyzer analyzer, final String field, final String... strings)
		throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(analyzer);
		addDocuments(indexWriter, field, strings);
		IndexReader indexReader = IndexReader.open(indexWriter.getDirectory());
		IndexSearcher searcher = new IndexSearcher(indexReader);
		return searchClass.getConstructor(IndexSearcher.class, Analyzer.class).newInstance(searcher, analyzer);
	}

	protected IndexWriter getRamIndexWriter(final Analyzer analyzer) throws Exception {
		IndexWriterConfig conf = new IndexWriterConfig(IConstants.LUCENE_VERSION, analyzer);
		Directory directory = new RAMDirectory();
		return new IndexWriter(directory, conf);
	}

	protected <T extends Search> T createIndexRamAndSearch(final Class<T> searchClass, final Analyzer analyzer, final String[] fields,
														   final String[]... strings) throws Exception {
		IndexWriter indexWriter = getRamIndexWriter(analyzer);
		Indexable<?> indexable = new IndexableColumn();
		for (final String[] row : strings) {
			Document document = new Document();
			for (int i = 0; i < row.length; i++) {
				final String column = row[i];
				String field = fields[i];
				if (StringUtils.isNumeric(column.trim())) {
					IndexManager.addNumericField(field, column.trim(), document, Boolean.TRUE);
				} else {
					IndexManager.addStringField(field, column, indexable, document);
				}
			}
			indexWriter.addDocument(document);
		}

		Directory directory = indexWriter.getDirectory();

		indexWriter.commit();
		indexWriter.maybeMerge();
		indexWriter.forceMerge(5);
		indexWriter.close(Boolean.TRUE);

		IndexReader indexReader = IndexReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		// printIndex(searcher.getIndexReader(), Integer.MAX_VALUE);

		return searchClass.getConstructor(IndexSearcher.class, Analyzer.class).newInstance(searcher, analyzer);
	}

	protected void addDocuments(final IndexWriter indexWriter, final String field, final String... strings) throws Exception {
		for (final String string : strings) {
			String id = Long.toString(System.currentTimeMillis());
			Document document = getDocument(id, string, field);
			indexWriter.addDocument(document);
		}
		try {
			indexWriter.commit();
			indexWriter.maybeMerge();
		} catch (NullPointerException e) {
			logger.error("Null pointer, mock? : " + e.getMessage());
		}
	}

	protected Document getDocument(final String id, final String string, final String field) {
		Document document = new Document();
		Indexable<?> indexable = new Indexable<Object>() {
		};
		IndexManager.addStringField(IConstants.ID, id, indexable, document);
		IndexManager.addStringField(IConstants.NAME, string, indexable, document);
		if (StringUtils.isNumeric(string.trim())) {
			logger.debug("Adding numeric field : " + string);
			IndexManager.addNumericField(field, string.trim(), document, Boolean.TRUE);
		} else {
			IndexManager.addStringField(field, string, indexable, document);
		}
		return document;
	}

	protected Lock getLock(Directory directory, File serverIndexDirectory) throws IOException {
		logger.debug("Is locked : " + IndexWriter.isLocked(directory));
		Lock lock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
		boolean gotLock = lock.obtain(Lock.LOCK_OBTAIN_WAIT_FOREVER);
		logger.debug("Got lock : " + gotLock + ", is locked : " + lock.isLocked());
		if (!gotLock) {
			FileUtilities.getFile(new File(serverIndexDirectory, IndexWriter.WRITE_LOCK_NAME).getAbsolutePath(), Boolean.FALSE);
		} else {
			assertTrue(IndexWriter.isLocked(directory));
		}
		logger.debug("Is now locked : " + IndexWriter.isLocked(directory));
		return lock;
	}

	/**
	 * This method will just print the data in the index reader.L
	 *
	 * @param indexReader the reader to print the documents for
	 * @throws Exception
	 */
	protected void printIndex(final IndexReader indexReader, final int numDocs) throws Exception {
		logger.info("Num docs : " + indexReader.numDocs());
		for (int i = 0; i < numDocs && i < indexReader.numDocs(); i++) {
			Document document = indexReader.document(i);
			logger.info("Document : " + i);
			printDocument(document);
		}
	}

	protected void printDocument(final Document document) {
		List<IndexableField> fields = document.getFields();
		for (IndexableField indexableField : fields) {
			String fieldName = indexableField.name();
			String fieldValue = indexableField.stringValue();
			int fieldLength = fieldValue != null ? fieldValue.length() : 0;
			Number number = indexableField.numericValue();
			logger.info("        : field name : " + fieldName + ", field length : " + fieldLength + ", field value : " +
				fieldValue + ", numeric value : " + number);
		}
	}

	protected Analysis<String, double[]> getAnalysis(final String clazz, final String input) {
		Analysis<String, double[]> analysis = new Analysis<>();
		analysis.setClazz(clazz);
		analysis.setInput(input);
		return analysis;
	}

}