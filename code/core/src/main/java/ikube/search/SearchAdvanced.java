package ikube.search;

import ikube.IConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

/**
 * This is the complex search for combinations of queries, like all of these words and none of these. There are four categories/strings. The
 * first is all the words in the string. The second is an exact phrase. The third is any of the following words and the fourth is none of
 * the words in the string. The result of this query would be something like:
 * 
 * <pre>
 * 		Data:
 * 		{ "cape town university", "cape university town caucation", "cape one", "cape town", "cape town university one" }
 * 		
 * 		Query:
 * 		(cape AND town AND university) AND ("cape town") AND (one OR two OR three) NOT (caucasian)
 * </pre>
 * 
 * In a data set where the content is as in the data above, there will be one result. But in fact in the test there are two results,
 * something in Lucene perhaps? Perhaps my logic is not working correctly?
 * 
 * @see Search
 * @author Michael Couck
 * @since 22.08.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchAdvanced extends Search {

	public SearchAdvanced(final Searcher searcher) {
		this(searcher, IConstants.ANALYZER);
	}

	public SearchAdvanced(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		return searcher.search(query, firstResult + maxResults);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Query getQuery() throws ParseException {
		// The first in the array of search strings all of the words
		StringBuilder stringBuilder = new StringBuilder();
		if (searchStrings.length > 0 && !StringUtils.isEmpty(searchStrings[0])) {
			stringBuilder.append("(");
			StringTokenizer stringTokenizer = new StringTokenizer(searchStrings[0], " ");
			while (stringTokenizer.hasMoreTokens()) {
				String word = stringTokenizer.nextToken();
				if (StringUtils.isEmpty(word)) {
					continue;
				}
				stringBuilder.append(word);
				if (stringTokenizer.hasMoreTokens()) {
					stringBuilder.append(" AND ");
				}
			}
			stringBuilder.append(") ");
		}

		if (searchStrings.length >= 2 && !StringUtils.isEmpty(searchStrings[1])) {
			// The second in the array is and exact phrase
			if (!StringUtils.isEmpty(searchStrings[1])) {
				stringBuilder.append("AND (");
				stringBuilder.append("\"");
				stringBuilder.append(searchStrings[1]);
				stringBuilder.append("\") ");
			}
		}

		if (searchStrings.length >= 3 && !StringUtils.isEmpty(searchStrings[2])) {
			// And the third in the array is one of more of these words
			StringTokenizer stringTokenizer = new StringTokenizer(searchStrings[2], " ");
			stringBuilder.append("AND (");
			while (stringTokenizer.hasMoreTokens()) {
				String word = stringTokenizer.nextToken();
				if (StringUtils.isEmpty(word)) {
					continue;
				}
				stringBuilder.append(word);
				if (stringTokenizer.hasMoreTokens()) {
					stringBuilder.append(" OR ");
				}
			}
			stringBuilder.append(") ");
		}

		if (searchStrings.length >= 4 && !StringUtils.isEmpty(searchStrings[3])) {
			// And finally the fourth in the array is the excluded words
			stringBuilder.append("NOT (");
			StringTokenizer stringTokenizer = new StringTokenizer(searchStrings[3], " ");
			while (stringTokenizer.hasMoreTokens()) {
				String word = stringTokenizer.nextToken();
				if (StringUtils.isEmpty(word)) {
					continue;
				}
				stringBuilder.append(word);
				if (stringTokenizer.hasMoreTokens()) {
					stringBuilder.append(" OR ");
				}
			}
			stringBuilder.append(") ");
		}
		// logger.info("Query : " + stringBuilder.toString());
		String[] newSearchStrings = new String[searchFields.length];
		Arrays.fill(newSearchStrings, 0, newSearchStrings.length, stringBuilder.toString());
		return MultiFieldQueryParser.parse(IConstants.VERSION, newSearchStrings, searchFields, IConstants.ANALYZER);
	}

}