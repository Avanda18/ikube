package ikube.index.handler.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;
import ikube.model.IndexableTable;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.DatabaseUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

public class IndexableTableHandlerTest extends BaseTest {

	private IndexableTable faqIndexableTable;
	private IndexableColumn faqIdIndexableColumn;
	private IndexableTable attachmentIndexableTable;
	private List<Indexable<?>> faqIndexableColumns;
	private IndexableTableHandler indexableTableHandler;
	private Connection connection;

	@Before
	public void before() throws Exception {
		indexableTableHandler = ApplicationContextManager.getBean(IndexableTableHandler.class);

		faqIndexableTable = ApplicationContextManager.getBean("tableOne");
		attachmentIndexableTable = ApplicationContextManager.getBean("tableTwo");

		faqIndexableColumns = faqIndexableTable.getChildren();
		faqIdIndexableColumn = indexableTableHandler.getIdColumn(faqIndexableColumns);

		connection = ApplicationContextManager.getBean(DataSource.class).getConnection();
	}

	@Test
	public void buildSql() throws Exception {
		String expectedSql = "select db2admin.faq.faqId, db2admin.faq.creationtimestamp, db2admin.faq.modifiedtimestamp, db2admin.faq.creator, "
				+ "db2admin.faq.modifier, db2admin.faq.question, db2admin.faq.answer from db2admin.faq where faq.faqid > 0 and db2admin.faq.faqId > 0 "
				+ "and db2admin.faq.faqId <= 10";
		// IndexContext, IndexableTable, long
		long nextIdNumber = 0;
		String sql = indexableTableHandler.buildSql(indexContext, faqIndexableTable, nextIdNumber);
		logger.debug("Sql : " + sql);
		assertEquals(expectedSql, sql);
	}

	@Test
	public void getIdFunction() throws Exception {
		// IndexableTable, Connection, String
		long minId = indexableTableHandler.getIdFunction(faqIndexableTable, connection, "min");
		logger.debug("Min id : " + minId);
		assertEquals(1, minId);
		long maxId = indexableTableHandler.getIdFunction(faqIndexableTable, connection, "max");
		logger.debug("Max id : " + maxId);
		assertEquals(100, maxId);
	}

	@Test
	public void getIdColumn() throws Exception {
		// List<Indexable<?>>
		assertNotNull(faqIdIndexableColumn);
		assertEquals("faqId", faqIdIndexableColumn.getName());
	}

	@Test
	public void setParameters() throws Exception {
		// IndexableTable, PreparedStatement
		faqIdIndexableColumn.setObject(1);
		String sql = "select * from db2admin.attachment where faqId = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(sql);
		indexableTableHandler.setParameters(attachmentIndexableTable, preparedStatement);
		// Execute this statement just for shits and giggles
		ResultSet resultSet = preparedStatement.executeQuery();
		assertNotNull(resultSet);

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(preparedStatement);
	}

	@Test
	public void getResultSet() throws Exception {
		// IndexContext, IndexableTable, Connection
		faqIdIndexableColumn.setObject(1);
		ResultSet resultSet = indexableTableHandler.getResultSet(indexContext, attachmentIndexableTable, connection);
		assertNotNull(resultSet);

		DatabaseUtilities.close(resultSet);
	}

	@Test
	public void handleColumn() throws Exception {
		// IndexableColumn, Document
		IndexableColumn faqIdIndexableColumn = indexableTableHandler.getIdColumn(faqIndexableColumns);
		faqIdIndexableColumn.setObject("Hello World!");
		faqIdIndexableColumn.setColumnType(Types.VARCHAR);
		Document document = new Document();
		indexableTableHandler.handleColumn(faqIdIndexableColumn, document);
		// This must just succeed as the sub components are tested separately
		assertTrue(Boolean.TRUE);
	}

	@Test
	public void setIdField() throws Exception {
		// List<Indexable<?>>, IndexableTable, Document, ResultSet
		Document document = new Document();
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from db2admin.faq");
		resultSet.next();
		indexableTableHandler.setIdField(faqIndexableColumns, faqIndexableTable, document, resultSet);
		logger.debug("Document : " + document);
		String idFieldValue = document.get(IConstants.ID);
		logger.debug("Id field : " + idFieldValue);
		assertEquals("db2admin.faq.faqId.1", idFieldValue);

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void setColumnTypes() throws Exception {
		// List<Indexable<?>>, ResultSet
		faqIdIndexableColumn.setColumnType(0);
		Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery("select * from db2admin.faq");
		resultSet.next();
		indexableTableHandler.setColumnTypes(faqIndexableColumns, resultSet);
		logger.debug("Faq id column type : " + faqIdIndexableColumn.getColumnType());
		assertEquals(Types.BIGINT, faqIdIndexableColumn.getColumnType());

		DatabaseUtilities.close(resultSet);
		DatabaseUtilities.close(statement);
	}

	@Test
	public void handleTable() throws Exception {
		// IndexContext, IndexableTable, Connection, Document
		indexContext.setIndexWriter(indexWriter);
		List<Thread> threads = indexableTableHandler.handle(indexContext, faqIndexableTable);
		waitForThreads(threads);
		// We just need to succeed, the integration tests test the
		// data that is indexed and validates it
	}

}
