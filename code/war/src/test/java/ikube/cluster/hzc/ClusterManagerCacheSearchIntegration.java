package ikube.cluster.hzc;

import ikube.IConstants;
import ikube.IntegrationTest;
import ikube.cluster.IClusterManager;
import ikube.database.IDataBase;
import ikube.model.Search;
import ikube.toolkit.THREAD;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-06-2014
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
public class ClusterManagerCacheSearchIntegration extends IntegrationTest {

    @Autowired
    private IDataBase dataBase;
    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;
    @Autowired
    private ClusterManagerCacheSearch clusterManagerCacheSearch;

    @Before
    public void before() {
        clusterManager.clear(IConstants.SEARCH);
        delete(dataBase, Search.class);
    }

    @Test
    public void store() {
        Search search = new Search();
        search.setHash(System.currentTimeMillis());
        clusterManagerCacheSearch.store(search.getHash(), search);

        int iterations = 1000;
        for (int i = 0; i < iterations; i++) {
            search.setCount(search.getCount() + 1);
            clusterManagerCacheSearch.store(search.getHash(), search);
        }

        long count = dataBase.count(Search.class);
        Search dbSearch = dataBase.find(Search.class, search.getId());
        THREAD.sleep(5000);
        Assert.assertEquals(1, count);
        Assert.assertEquals(iterations + 1, dbSearch.getCount());
    }

}
