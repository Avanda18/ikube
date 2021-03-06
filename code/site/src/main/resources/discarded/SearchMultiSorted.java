package ikube.search;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

/**
 * Searches for multiple fields in an index, and sorts the results.
 * 
 * @see Search
 * @author Michael Couck
 * @since 02.09.08
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public class SearchMultiSorted extends SearchMulti {

	public SearchMultiSorted(final Searcher searcher) {
		this(searcher, ANALYZER);
	}

	public SearchMultiSorted(final Searcher searcher, final Analyzer analyzer) {
		super(searcher, analyzer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TopDocs search(final Query query) throws IOException {
		Sort sort = getSort(query);
		Filter filter = new QueryWrapperFilter(query);
		return searcher.search(query, filter, firstResult + maxResults, sort);
	}

}
