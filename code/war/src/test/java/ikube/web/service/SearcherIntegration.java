package ikube.web.service;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.model.Search;
import ikube.toolkit.FILE;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;

import static ikube.toolkit.REST.doPost;
import static ikube.toolkit.OBJECT.populateFields;
import static org.junit.Assert.assertNotNull;

public abstract class SearcherIntegration extends AbstractTest {

    protected abstract String getSearchUrl(String path) throws Exception;

    @Test
    public void searchSingle() throws Exception {
        String url = getSearchUrl(SearcherXml.SIMPLE);

        String[] names = { //
                IConstants.INDEX_NAME, //
                IConstants.SEARCH_STRINGS, //
                IConstants.SEARCH_FIELDS, //
                IConstants.FRAGMENT,//
                IConstants.FIRST_RESULT, //
                IConstants.MAX_RESULTS};
        String[] values = {//
                IConstants.GEOSPATIAL,//
                "cape AND town AND university",//
                IConstants.NAME, //
                Boolean.TRUE.toString(),//
                "0", //
                "10"};
        verify(url, names, values);
    }

    @Test
    public void searchMulti() throws Exception {
        String url = getSearchUrl(SearcherXml.SIMPLE);

        String[] names = { //
                IConstants.INDEX_NAME, //
                IConstants.SEARCH_STRINGS, //
                IConstants.SEARCH_FIELDS, //
                IConstants.FRAGMENT,//
                IConstants.FIRST_RESULT, //
                IConstants.MAX_RESULTS};
        String[] values = { //
                IConstants.GEOSPATIAL, //
                "cape AND town AND university;south africa", //
                IConstants.NAME + ";" + IConstants.COUNTRY,//
                Boolean.TRUE.toString(), //
                "0", //
                "10"};
        verify(url, names, values);
    }

    @Test
    public void searchMultiSorted() throws Exception {
        String url = getSearchUrl(SearcherXml.SORTED);

        String[] names = { //
                IConstants.INDEX_NAME, //
                IConstants.SEARCH_STRINGS, //
                IConstants.SEARCH_FIELDS, //
                IConstants.SORT_FIELDS,//
                IConstants.FRAGMENT, //
                IConstants.FIRST_RESULT, //
                IConstants.MAX_RESULTS};
        String[] values = { //
                IConstants.GEOSPATIAL, //
                "cape AND town AND university;south africa", //
                IConstants.NAME + ";" + IConstants.COUNTRY,//
                IConstants.NAME + ";" + IConstants.COUNTRY, //
                Boolean.TRUE.toString(), //
                "0", //
                "10"};
        verify(url, names, values);
    }

    @Test
    public void searchMultiSpacial() throws Exception {
        String url = getSearchUrl(SearcherXml.GEOSPATIAL);

        String[] names = { //
                IConstants.INDEX_NAME, //
                IConstants.SEARCH_STRINGS, //
                IConstants.SEARCH_FIELDS, //
                IConstants.TYPE_FIELDS, //
                IConstants.FRAGMENT, //
                IConstants.FIRST_RESULT, //
                IConstants.MAX_RESULTS, //
                IConstants.DISTANCE, //
                IConstants.LATITUDE, //
                IConstants.LONGITUDE};
        String[] values = { //
                IConstants.GEOSPATIAL, //
                "cape town university|berlin", //
                "name|name", //
                "string|string", //
                Boolean.TRUE.toString(), //
                "0", //
                "10", //
                "20", //
                "-33.9693580", //
                "18.4622110"};
        verify(url, names, values);
    }

    @Test
    public void searchComplex() throws Exception {
        String url = getSearchUrl(SearcherXml.SORTED_TYPED);

        String[] names = { //
                IConstants.INDEX_NAME, //
                IConstants.SEARCH_STRINGS, //
                IConstants.SEARCH_FIELDS, //
                IConstants.TYPE_FIELDS, //
                IConstants.SORT_FIELDS, //
                IConstants.FRAGMENT, //
                IConstants.FIRST_RESULT,//
                IConstants.MAX_RESULTS};
        String[] values = { //
                IConstants.GEOSPATIAL,//
                "michael AND couck|123456789", //
                "contents|lastmodified", //
                "string|numeric", //
                "lastmodified", //
                Boolean.TRUE.toString(), //
                "0", //
                "10"};
        verify(url, names, values);
    }

    @Test
    public void search() throws Exception {
        verify(getSearchUrl(""));
    }

    @Test
    public void searchAll() throws Exception {
        verify(getSearchUrl(Searcher.ALL));
    }

    protected void verify(final String url) throws Exception {
        verify(url, MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
    }

    protected void verify(final String url, final String consumes, final String produces) throws Exception {
        Search search = populateFields(new Search(), Boolean.TRUE, 10);
        search.setIndexName(IConstants.GEOSPATIAL);

        search.setSearchStrings(Arrays.asList("Michael Couck"));
        search.setSearchFields(Arrays.asList("contents"));
        search.setTypeFields(Arrays.asList("string"));
        search.setSortFields(Arrays.asList("lastmodified"));

        search.setFirstResult(0);
        search.setMaxResults(10);
        search.setFragment(Boolean.TRUE);

        String response = doPost(url, null, null, search, consumes, produces, null, null, String.class);
        assertNotNull(response);
    }



    protected void verify(final String url, final String[] names, final Object[] values) throws IOException {
        HttpGet getMethod = new HttpGet(url);
        HttpParams httpParams = new BasicHttpParams();
        for (int i = 0; i < names.length; i++) {
            httpParams.setParameter(names[i], values[i]);
        }
        getMethod.setParams(httpParams);

        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response) {
                try {
                    return FILE.getContents(response.getEntity().getContent(), Long.MAX_VALUE).toString();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        String response = HTTP_CLIENT.execute(getMethod, responseHandler);
        assertNotNull(response);
    }

}