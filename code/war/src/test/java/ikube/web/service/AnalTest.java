package ikube.web.service;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import ikube.BaseTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.search.ISearcherService;
import ikube.toolkit.SerializationUtilities;
import ikube.web.service.Anal.TwitterSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Michael couck
 * @since 17.12.13
 * @version 01.00
 */
public class AnalTest extends BaseTest {

	@MockClass(realClass = SerializationUtilities.class)
	public static class SerializationUtilitiesMock {
		@Mock
		@SuppressWarnings("unchecked")
		public static <T> T clone(final Class<T> klass, T t) {
			return (T) search;
		}
	}

	private static TwitterSearch search;

	/** Class under test */
	private Anal anal;
	private Gson gson;
	private ISearcherService searcherService;

	@Before
	@SuppressWarnings("unchecked")
	public void before() throws Exception {
		gson = new GsonBuilder().disableHtmlEscaping().create();

		HashMap<String, String> statistics = new HashMap<>();
		statistics.put(IConstants.TOTAL, "100");
		ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();

		search = new TwitterSearch();
		search.setSearchStrings(new ArrayList<>(Arrays.asList("1386765181186-1387369981186")));
		search.setSearchFields(new ArrayList<>(Arrays.asList(Anal.CREATED_AT)));
		search.setOccurrenceFields(new ArrayList<>(Arrays.asList(Anal.OCCURRENCE)));
		search.setTypeFields(new ArrayList<>(Arrays.asList("range")));
		searchResults.add(statistics);
		search.setSearchResults(searchResults);

		anal = mock(Anal.class);
		searcherService = mock(ISearcherService.class);

		when(anal.buildResponse()).thenCallRealMethod();
		when(anal.invertMatrix(any(Object[][].class))).thenCallRealMethod();
		when(anal.count(any(Search.class), anyInt(), anyInt(), anyString())).thenCallRealMethod();
		when(anal.buildJsonResponse(any(Object.class))).thenCallRealMethod();
		when(anal.twitter(any(HttpServletRequest.class), any(UriInfo.class))).thenCallRealMethod();
		when(anal.unmarshall(any(Class.class), any(HttpServletRequest.class))).thenReturn(search);
		when(searcherService.search(any(Search.class))).thenReturn(search);

		Deencapsulation.setField(anal, gson);
		Deencapsulation.setField(anal, logger);
		Deencapsulation.setField(anal, searcherService);

		Mockit.setUpMock(SerializationUtilitiesMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks(SerializationUtilitiesMock.class);
	}

	@Test
	public void analyze() {
		Response response = anal.twitter((HttpServletRequest) null, (UriInfo) null);
		String string = (String) response.getEntity();
		logger.info("Results : " + string);
		TwitterSearch twitterSearch = (TwitterSearch) gson.fromJson(string, TwitterSearch.class);
		logger.info("Twitter results : " + ToStringBuilder.reflectionToString(twitterSearch));
		// Mockito.verify(anal, Mockito.atLeastOnce()).buildJsonResponse(any());
	}

	@Test
	public void count() {
		search.getSearchStrings().add(IConstants.POSITIVE);
		search.getSearchFields().add(IConstants.CLASSIFICATION);
		search.getOccurrenceFields().add(Anal.OCCURRENCE);
		search.getTypeFields().add(String.class.getSimpleName().toLowerCase());

		long startTime = 0;
		long endTime = 10;
		String classification = IConstants.POSITIVE;
		anal.count(search, startTime, endTime, classification);
		assertEquals(2, search.getSearchStrings().size());
		assertEquals(2, search.getSearchFields().size());
		assertEquals(2, search.getOccurrenceFields().size());
		assertEquals(2, search.getTypeFields().size());

		search.getSearchStrings().remove(1);
		search.getSearchFields().remove(1);
		search.getOccurrenceFields().remove(1);
		search.getTypeFields().remove(1);

		anal.count(search, startTime, endTime, classification);
		assertEquals(2, search.getSearchStrings().size());
		assertEquals(2, search.getSearchFields().size());
		assertEquals(2, search.getOccurrenceFields().size());
		assertEquals(2, search.getTypeFields().size());
	}

}