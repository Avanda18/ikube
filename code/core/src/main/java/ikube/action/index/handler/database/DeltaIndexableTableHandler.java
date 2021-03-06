package ikube.action.index.handler.database;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.IndexableTable;
import ikube.model.SavePoint;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.concurrent.ForkJoinTask;

/**
 * This class extends the table handler to add a save point, to start indexing the delta or added
 * rows in the table.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
public class DeltaIndexableTableHandler extends IndexableTableHandler {

    @Autowired
    @Qualifier(value = "ikube.cluster.IClusterManager")
    private IClusterManager clusterManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public ForkJoinTask<?> handleIndexableForked(final IndexContext indexContext, final IndexableTable indexableTable) throws Exception {
        try {
            if (indexContext.isDelta()) {
                SavePoint savePoint = clusterManager.get(IConstants.SAVE_POINT, indexableTable.getName());
                if (savePoint != null) {
                    logger.debug("Save point start : " + ToStringBuilder.reflectionToString(savePoint));
                    indexableTable.setMinimumId(savePoint.getIdentifier());
                }
            }
            return super.handleIndexableForked(indexContext, indexableTable);
        } finally {
            if (indexContext.isDelta()) {
                SavePoint savePoint = clusterManager.get(IConstants.SAVE_POINT, indexableTable.getName());
                if (savePoint == null) {
                    savePoint = new SavePoint();
                    savePoint.setIdentifier(0l);
                }
                logger.debug("From to point : " + ToStringBuilder.reflectionToString(savePoint));
                logger.debug("Min id : " + indexableTable.getMaximumId() + ", diff : " + (indexableTable.getMaximumId() - savePoint.getIdentifier()));
                savePoint.setIndexable(indexableTable.getName());
                savePoint.setIndexContext(indexContext.getName());
                savePoint.setIdentifier(indexableTable.getMaximumId());
                logger.debug("Save point end : " + ToStringBuilder.reflectionToString(savePoint));
                clusterManager.put(IConstants.SAVE_POINT, indexableTable.getName(), savePoint);
            }
        }
    }

}