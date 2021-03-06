package ikube.web.service;

import ikube.model.Search;

import javax.ws.rs.core.Response;

/**
 * This is the base class for all searcher web services, common logic and properties. Also all the
 * methods that are exposed to clients for xml and Json responses are defined here. This class could be
 * seen as the API that is exposed to iKube clients.
 *
 * @author Michael couck
 * @version 01.00
 * @see ikube.search.ISearcherService
 * @since 20-11-2012
 */
public abstract class Searcher extends Resource {

    public static final String SEARCH = "/search";

    public static final String ALL = "/all";
    public static final String SIMPLE = "/simple";
    public static final String SORTED = "/sorted";
    public static final String GEOSPATIAL = "/geospatial";
    public static final String SORTED_TYPED = "/sorted/typed";

    /**
     * Basic
     */
    public abstract Response search(
            final String indexName,
            final String searchStrings,
            final String searchFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Sorted
     */
    public abstract Response search(
            final String indexName,
            final String searchStrings,
            final String searchFields,
            final String sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Sorted and typed
     */
    public abstract Response search(
            final String indexName,
            final String searchStrings,
            final String searchFields,
            final String typeFields,
            final String sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults);

    /**
     * Geospatial
     */
    public abstract Response search(
            final String indexName,
            final String searchStrings,
            final String searchFields,
            final String typeFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults,
            final int distance,
            final double latitude,
            final double longitude);

    /**
     * Search json
     */
    public abstract Response search(final Search search);

    /**
     * Search Json, all fields in all indexes, just for convenience.
     */
    @SuppressWarnings("UnusedDeclaration")
    public abstract Response searchAll(final Search search);

}