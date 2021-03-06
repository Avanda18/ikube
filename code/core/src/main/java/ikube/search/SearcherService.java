package ikube.search;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.database.IDataBase;
import ikube.model.Coordinate;
import ikube.model.IndexContext;
import ikube.model.Search;
import ikube.search.Search.TypeField;
import ikube.toolkit.HASH;
import ikube.toolkit.SERIALIZATION;
import ikube.toolkit.STRING;
import ikube.toolkit.THREAD;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static ikube.toolkit.STRING.stripToAlphaNumeric;
import static org.apache.commons.lang.StringUtils.stripToEmpty;

/**
 * @author Michael Couck
 * @version 02.00
 * @see ISearcherService
 * @since 21-11-2010
 */
@Component
public class SearcherService implements ISearcherService {

    static final Logger LOGGER = LoggerFactory.getLogger(SearcherService.class);

    private AtomicInteger exceptionCounter = new AtomicInteger();

    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IDataBase dataBase;
    /**
     * The service to distribute the searches in the cluster, and to put the searches in the grid.
     */
    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;
    /**
     * The service to get system contexts and other bric-a-brac.
     */
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private IMonitorService monitorService;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults) {
        Search search = new Search();
        search.setIndexName(indexName);
        search.setDistributed(Boolean.TRUE);

        String[] typeFields = new String[searchFields.length];
        Arrays.fill(typeFields, TypeField.STRING.fieldType());

        search.setSearchStrings(Arrays.asList(searchStrings));
        search.setSearchFields(Arrays.asList(searchFields));
        search.setTypeFields(Arrays.asList(typeFields));
        search.setSortFields(Collections.EMPTY_LIST);

        search.setFragment(fragment);
        search.setFirstResult(firstResult);
        search.setMaxResults(maxResults);
        search(search);
        return search.getSearchResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults) {
        Search search = new Search();
        search.setIndexName(indexName);
        search.setDistributed(Boolean.TRUE);

        String[] typeFields = new String[searchFields.length];
        Arrays.fill(typeFields, TypeField.STRING.fieldType());

        search.setSearchStrings(Arrays.asList(searchStrings));
        search.setSearchFields(Arrays.asList(searchFields));
        search.setTypeFields(Arrays.asList(typeFields));
        search.setSortFields(Arrays.asList(sortFields));

        search.setFragment(fragment);
        search.setFirstResult(firstResult);
        search.setMaxResults(maxResults);
        search(search);
        return search.getSearchResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] typeFields,
            final String[] sortFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults) {
        Search search = new Search();
        search.setIndexName(indexName);
        search.setDistributed(Boolean.TRUE);

        search.setSearchStrings(Arrays.asList(searchStrings));
        search.setSearchFields(Arrays.asList(searchFields));
        search.setTypeFields(Arrays.asList(typeFields));
        search.setSortFields(Arrays.asList(sortFields));

        search.setFragment(fragment);
        search.setFirstResult(firstResult);
        search.setMaxResults(maxResults);
        search(search);
        return search.getSearchResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> search(
            final String indexName,
            final String[] searchStrings,
            final String[] searchFields,
            final String[] typeFields,
            final boolean fragment,
            final int firstResult,
            final int maxResults,
            final int distance,
            final double latitude,
            final double longitude) {
        Search search = new Search();
        search.setIndexName(indexName);
        search.setDistributed(Boolean.TRUE);

        search.setSearchStrings(Arrays.asList(searchStrings));
        search.setSearchFields(Arrays.asList(searchFields));
        search.setTypeFields(Arrays.asList(typeFields));
        search.setSortFields(Collections.EMPTY_LIST);

        search.setDistance(distance);
        search.setCoordinate(new Coordinate(latitude, longitude));

        search.setFragment(fragment);
        search.setFirstResult(firstResult);
        search.setMaxResults(maxResults);
        search(search);
        return search.getSearchResults();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Search search(final Search search) {
        String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        for (int i = 0; i < searchStrings.length; i++) {
            String searchString = searchStrings[i];
            searchString = stripToAlphaNumeric(searchString);
            searchString = stripToEmpty(searchString);
            searchStrings[i] = searchString.toLowerCase();
        }
        search.setSearchStrings(Arrays.asList(searchStrings));

        if (search.isDistributed() && clusterManager.getServers().size() > 1) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Distributing search : " + search);
            }
            // Set the flag so we don't get infinite recursion
            search.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on the nodes
            Searcher searcher = new Searcher(search);
            Future<?> future = clusterManager.sendTask(searcher);
            try {
                return (Search) future.get(60, TimeUnit.SECONDS);
            } catch (final Exception e) {
                LOGGER.info("Exception doing remote search : " + search);
                LOGGER.info(null, e);
            }
            // If we have a remote exception then try to do the search locally
            return doSearch(search);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Not distributing search : " + search);
            }
            return doSearch(search);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Search doSearch(final Search search) {
        if (search == null) {
            LOGGER.warn("Search null : ");
            //noinspection ConstantConditions
            return search;
        }
        ikube.search.Search searchAction;
        try {
            if (search.getCoordinate() != null && search.getDistance() != 0) {
                searchAction = getSearch(SearchSpatial.class, search.getIndexName());
            } else if (search.getSortFields() == null || search.getSortFields().size() == 0) {
                searchAction = getSearch(SearchComplex.class, search.getIndexName());
            } else {
                searchAction = getSearch(SearchComplexSorted.class, search.getIndexName());
            }

            if (searchAction == null) {
                LOGGER.warn("Search action null : " + search.getIndexName());
                return search;
            }

            if (SearchSpatial.class.isAssignableFrom(searchAction.getClass())) {
                ((SearchSpatial) searchAction).setDistance(search.getDistance());
                ((SearchSpatial) searchAction).setCoordinate(search.getCoordinate());
            }

            String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
            if (search.isStripToAlphaNumeric()) {
                // Strip unwanted characters from the input string
                for (int i = 0; i < searchStrings.length; i++) {
                    searchStrings[i] = STRING.stripToAlphaNumeric(searchStrings[i], '*', '~');
                }
            }
            String[] searchFields = search.getSearchFields().toArray(new String[search.getSearchFields().size()]);

            String[] typeFields;
            if (search.getTypeFields() == null || search.getTypeFields().size() < searchStrings.length) {
                typeFields = new String[searchStrings.length];
                Arrays.fill(typeFields, TypeField.STRING.fieldType());
            } else {
                typeFields = search.getTypeFields().toArray(new String[search.getTypeFields().size()]);
            }
            String[] sortFields;
            if (search.getSortFields() == null) {
                sortFields = new String[searchStrings.length];
            } else {
                sortFields = search.getSortFields().toArray(new String[search.getSortFields().size()]);
            }
            String[] sortDirections;
            if (search.getSortDirections() == null) {
                sortDirections = new String[searchStrings.length];
            } else {
                sortDirections = search.getSortDirections().toArray(new String[search.getSortDirections().size()]);
            }
            String[] occurrenceFields;
            if (search.getOccurrenceFields() == null) {
                occurrenceFields = new String[searchStrings.length];
                Arrays.fill(occurrenceFields, BooleanClause.Occur.SHOULD.name());
            } else {
                occurrenceFields = search.getOccurrenceFields().toArray(new String[search.getOccurrenceFields().size()]);
            }
            float[] boosts = null;
            if (search.getBoosts() != null) {
                int i = 0;
                boosts = new float[search.getBoosts().size()];
                for (final String boost : search.getBoosts()) {
                    float f = 0.0f;
                    if (STRING.isNumeric(boost)) {
                        f = Float.parseFloat(boost);
                    }
                    boosts[i++] = f;
                }
            }

            searchAction.setFirstResult(search.getFirstResult());
            searchAction.setFragment(search.isFragment());
            searchAction.setMaxResults(search.getMaxResults());
            searchAction.setSearchStrings(searchStrings);
            searchAction.setSearchFields(searchFields);
            searchAction.setTypeFields(typeFields);
            searchAction.setSortFields(sortFields);
            searchAction.setSortDirections(sortDirections);
            searchAction.setOccurrenceFields(occurrenceFields);
            searchAction.setBoosts(boosts);

            ArrayList<HashMap<String, String>> results = searchAction.execute();
            String[] searchStringsCorrected = searchAction.getCorrections(searchStrings);
            if (searchStringsCorrected != null && searchStringsCorrected.length > 0) {
                search.setCorrections(Boolean.TRUE);
                search.setCorrectedSearchStrings(Arrays.asList(searchStringsCorrected));
            } else {
                search.setCorrections(Boolean.FALSE);
            }
            search.setSearchResults(results);
            persistSearch(search);
        } catch (final Exception e) {
            handleException(search, e);
        }
        return search;
    }

    /**
     * This method is particularly expensive, it will do a search on every index in the system.
     */
    @SuppressWarnings("unchecked")
    public Search searchAll(final Search search) {
        LOGGER.debug("Search all");
        try {
            String[] indexNames = monitorService.getIndexNames();
            List<Future<Object>> futures = new ArrayList<>();
            List<Search> searches = new ArrayList<>();
            for (final String indexName : indexNames) {
                final Search clonedSearch = cloneSearch(search, indexName);
                if (clonedSearch == null) {
                    continue;
                }
                searches.add(clonedSearch);
                Runnable searchRunnable = new Runnable() {
                    public void run() {
                        try {
                            // Search each index separately
                            search(clonedSearch);
                        } catch (final Exception e) {
                            LOGGER.error(null, e);
                        }
                    }
                };
                String name = Integer.toString(clonedSearch.hashCode());
                Future<Object> future = (Future<Object>) THREAD.submit(name, searchRunnable);
                futures.add(future);
            }
            THREAD.waitForFutures(futures, 60);
            for (final Search clonedSearch : searches) {
                THREAD.destroy(Integer.toString(clonedSearch.hashCode()));
            }
            aggregateResults(search, searches);
        } catch (final Exception e) {
            handleException(search, e);
        }
        return search;
    }

    private void aggregateResults(final Search search, final List<Search> searches) {
        long totalHits = 0;
        float highScore = 0;
        long duration = 0;

        ArrayList<HashMap<String, String>> searchResults = new ArrayList<>();

        for (final Search clonedSearch : searches) {
            // Consolidate the results, i.e. merge them
            List<HashMap<String, String>> searchSubResults = clonedSearch.getSearchResults();
            if (searchSubResults != null && searchSubResults.size() > 1) {
                HashMap<String, String> statistics = searchSubResults.remove(searchSubResults.size() - 1);
                totalHits += Long.parseLong(statistics.get(IConstants.TOTAL));
                highScore += Float.parseFloat(statistics.get(IConstants.SCORE));
                long subSearchDuration = Long.parseLong(statistics.get(IConstants.DURATION));
                duration = Math.max(duration, subSearchDuration);
                searchResults.addAll(searchSubResults);
            }
        }

        // HashMap<String, String> statistics = new HashMap<>();
        // statistics.put(IConstants.TOTAL, Long.toString(totalHits));
        // statistics.put(IConstants.DURATION, Long.toString(duration));
        // statistics.put(IConstants.SCORE, Float.toString(highScore));
        // Sort all the results according to the score
        Collections.sort(searchResults, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(final HashMap<String, String> o1, final HashMap<String, String> o2) {
                String scoreOneString = o1.get(IConstants.SCORE);
                String scoreTwoString = o2.get(IConstants.SCORE);
                Double scoreOne = Double.parseDouble(scoreOneString);
                Double scoreTwo = Double.parseDouble(scoreTwoString);
                return scoreOne.compareTo(scoreTwo);
            }
        });
        ArrayList<HashMap<String, String>> topResults = new ArrayList<>();
        topResults.addAll(searchResults.subList(0, Math.min(search.getMaxResults(), searchResults.size())));

        String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        SearchComplex searchSingle = new SearchComplex(null);
        searchSingle.setSearchStrings(searchStrings);
        searchSingle.addStatistics(searchStrings, topResults, totalHits, highScore, duration, null);

        search.setSearchResults(topResults);
    }

    @SuppressWarnings("unchecked")
    private Search cloneSearch(final Search search, final String indexName) {
        final Search clonedSearch = (Search) SERIALIZATION.clone(search);
        clonedSearch.setIndexName(indexName);

        String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        String[] searchFields = monitorService.getIndexFieldNames(indexName);
        String[] typeFields = new String[0];
        if (searchFields == null || searchFields.length == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Index : " + indexName + ", search fields : " + Arrays.deepToString(searchFields));
            }
            return null;
        }

        searchStrings = fillArray(searchFields, searchStrings);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Searching index : " + indexName + ", " + //
                    Arrays.deepToString(searchStrings) + ", " + //
                    Arrays.deepToString(searchFields) + ", " + //
                    Arrays.deepToString(typeFields));
        }

        clonedSearch.setSearchStrings(Arrays.asList(searchStrings));
        clonedSearch.setSearchFields(Arrays.asList(searchFields));
        clonedSearch.setTypeFields(Arrays.asList(typeFields));
        clonedSearch.setSortFields(Collections.EMPTY_LIST);

        return clonedSearch;
    }

    private String[] fillArray(final String[] lengthOfThisArray, final String[] originalStrings) {
        String[] newStrings = new String[lengthOfThisArray.length];
        int minLength = Math.min(originalStrings.length, newStrings.length);
        System.arraycopy(originalStrings, 0, newStrings, 0, minLength);
        String fillerString = originalStrings.length > 0 ? originalStrings[0] : "";
        Arrays.fill(newStrings, minLength, newStrings.length, fillerString);
        return newStrings;
    }

    private Search handleException(final Search search, final Exception e) {
        LOGGER.error("Exception doing search on : " + search.getIndexName() + ", " + e.getLocalizedMessage(), e);

        ArrayList<HashMap<String, String>> results = search.getSearchResults();
        if (results == null) {
            results = new ArrayList<>();
            search.setSearchResults(results);
        }
        HashMap<String, String> statistics;
        if (results.size() == 0) {
            statistics = new HashMap<>();
            results.add(statistics);
        } else {
            statistics = results.get(results.size() - 1);
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(outputStream));

        statistics.put(IConstants.EXCEPTION_MESSAGE, e.getMessage());
        statistics.put(IConstants.EXCEPTION_STACK, outputStream.toString());
        return search;
    }

    /**
     * This method will return an instance of the search class, based on the class in the parameter list and the index context name. For each search there is an
     * instance created for the searcher classes to avoid thread overlap. The instance is created using reflection :( but is there a more elegant way?
     *
     * @param <T>       the type of class that is expected as a category
     * @param klass     the class of the searcher
     * @param indexName the name of the index
     * @return the searcher with the searchable injected
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSearch(final Class<?> klass, final String indexName) throws Exception {
        T search;
        IndexContext indexContext = monitorService.getIndexContext(indexName);
        if (indexContext == null || indexContext.getMultiSearcher() == null) {
            LOGGER.warn("No index context : " + indexName);
            return null;
        }
        if (indexContext.getAnalyzer() != null) {
            Constructor<?> constructor = klass.getConstructor(IndexSearcher.class, Analyzer.class);
            search = (T) constructor.newInstance(indexContext.getMultiSearcher(), indexContext.getAnalyzer());
        } else {
            Constructor<?> constructor = klass.getConstructor(IndexSearcher.class);
            search = (T) constructor.newInstance(indexContext.getMultiSearcher());
        }
        return search;
    }

    /**
     * This method will persist the search, using the write behind facility of the
     * grid. The search object will also be distributed over the servers so that all the
     * statistics are available for all the servers. The grid will eventually persist
     * the search object, updating the count using the {@link ikube.database.IDataBase}
     * JPA persistence manager.
     */
    void persistSearch(final Search search) {
        final String indexName = search.getIndexName();
        final String[] searchStrings = search.getSearchStrings().toArray(new String[search.getSearchStrings().size()]);
        final ArrayList<HashMap<String, String>> results = search.getSearchResults();
        // Don't persist the auto complete searches
        if (IConstants.AUTOCOMPLETE.equals(indexName) || results == null) {
            return;
        }
        if (searchStrings.length == 0) {
            return;
        }
        Map<String, String> statistics = results.get(results.size() - 1);
        // Add the index name to the statistics here, not elegant, I know
        statistics.put(IConstants.INDEX_NAME, indexName);

        String string;
        List<String> cleanedSearchStrings = new ArrayList<>(Arrays.asList(searchStrings));
        Iterator<String> iterator = cleanedSearchStrings.iterator();
        do {
            string = iterator.next();
            if (StringUtils.isEmpty(string)) {
                iterator.remove();
            }
        } while (iterator.hasNext());
        if (cleanedSearchStrings.size() == 0) {
            return;
        }
        Long hash = HASH.hash(cleanedSearchStrings.toString().toLowerCase());
        try {
            Search cacheSearch = clusterManager.get(IConstants.SEARCH, hash);
            if (cacheSearch == null) {
                cacheSearch = search;
                cacheSearch.setHash(hash);
            }
            cacheSearch.setCount(cacheSearch.getCount() + 1);
            clusterManager.put(IConstants.SEARCH, hash, cacheSearch);

            if (LOGGER.isDebugEnabled()) {
                cacheSearch = clusterManager.get(IConstants.SEARCH, hash);
                Search dbSearch = dataBase.find(Search.class, new String[]{IConstants.HASH}, new Object[]{hash});
                LOGGER.debug("Cache search : " + cacheSearch);
                LOGGER.debug("Database search : " + dbSearch);
            }
        } catch (final Exception e) {
            if (exceptionCounter.getAndIncrement() % 100 == 0) {
                LOGGER.error("Exception setting search in database : ", e);
            } else {
                LOGGER.debug("Exception setting search in database : ", e);
            }
        }
    }

}