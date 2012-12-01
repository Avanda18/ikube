package ikube.toolkit;

import static org.junit.Assert.assertTrue;
import ikube.ATest;
import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Snapshot;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectToolkitTest extends ATest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectToolkitTest.class);

	public ObjectToolkitTest() {
		super(ObjectToolkitTest.class);
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void populateFields() {
		indexContext = ObjectToolkit.populateFields(IndexContext.class, new IndexContext(), true, 0, 3);
		LOGGER.info("Index context : " + ToStringBuilder.reflectionToString(indexContext));
		assertTrue(indexContext.getId() > 0);
		assertTrue(indexContext.getName() != null);

		indexContext = ObjectToolkit.populateFields(IndexContext.class, new IndexContext(), true, 0, 0, IConstants.ID);
		LOGGER.info("Index context : " + ToStringBuilder.reflectionToString(indexContext));
		assertTrue(indexContext.getId() == 0);
		assertTrue(indexContext.getName() != null);
		assertTrue(indexContext.getBufferSize() != 0.0);
		
		logger.info("Snapshot : " + new Snapshot());
	}

}