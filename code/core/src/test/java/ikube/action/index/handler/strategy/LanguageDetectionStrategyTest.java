package ikube.action.index.handler.strategy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Indexable;
import ikube.toolkit.PerformanceTester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Couck
 * @since 05.04.13
 * @version 01.00
 */
public class LanguageDetectionStrategyTest extends AbstractTest {

	private static LanguageDetectionStrategy languageDetectionStrategy;

	@Before
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void before() {
		languageDetectionStrategy = new LanguageDetectionStrategy(null);
		languageDetectionStrategy.initialize();
		when(indexableColumn.getContent()).thenReturn("some english text");
		List<Indexable<?>> children = new ArrayList(Arrays.asList(indexableColumn));
		when(indexableTable.getChildren()).thenReturn(children);
	}

	@Test
	public void aroundProcess() throws Exception {
		try {
			Document document = new Document();
			languageDetectionStrategy.aroundProcess(indexContext, indexableTable, document, null);
			String language = document.get(IConstants.LANGUAGE);
			assertEquals("We expect English for this one : ", "en", language);

			when(indexableColumn.getContent()).thenReturn("soms een andere taal");
			document = new Document();
			languageDetectionStrategy.aroundProcess(indexContext, indexableTable, document, null);
			language = document.get(IConstants.LANGUAGE);
			assertTrue("We expect Afrikaans for this one : ", "af".equals(language) || "nl".equals(language));
		} catch (Throwable t) {
			logger.error(null, t);
		}
	}

	@Test
	public void aroundProcessPerformance() {
		int iterations = 1000;
		final Document document = new Document();
		double perSecond = PerformanceTester.execute(new PerformanceTester.APerform() {
			public void execute() throws Throwable {
				languageDetectionStrategy.aroundProcess(indexContext, indexableTable, document, null);
			}
		}, "Language detection strategy : ", iterations, Boolean.TRUE);
		assertTrue(perSecond > 100);
		logger.info("Detection per second : " + perSecond);
	}

}