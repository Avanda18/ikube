package ikube.action.rule;

import ikube.model.IndexContext;

import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

/**
 * This rule checks to see if there are searchables in the index context. This means that there is an index and that the index has been opened by the
 * application and is ready to search.
 * 
 * @author Michael Couck
 * @since 12.02.2011
 * @version 01.00
 */
public class AreSearchablesInitialised implements IRule<IndexContext<?>> {

	private static final transient Logger LOGGER = Logger.getLogger(AreSearchablesInitialised.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean evaluate(final IndexContext<?> indexContext) {
		IndexSearcher searcher = indexContext.getMultiSearcher();
		if (searcher == null) {
			LOGGER.debug("No searchables open, should try to reopen : ");
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
