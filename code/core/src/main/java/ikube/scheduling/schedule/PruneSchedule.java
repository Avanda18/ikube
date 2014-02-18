package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.database.IDataBase;
import ikube.scheduling.Schedule;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * This schedule will remove objects from the database that are not used to keep the volume of data restricted.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 22.07.12
 */
public class PruneSchedule extends Schedule {

    @Autowired
    private IDataBase dataBase;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void run() {
        String[] fieldsToSortOn = new String[]{IConstants.ID};
        Boolean[] directionOfSort = new Boolean[]{true};
        delete(dataBase, ikube.model.Action.class, fieldsToSortOn, directionOfSort, IConstants.MAX_ACTIONS);
        delete(dataBase, ikube.model.Snapshot.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SNAPSHOTS);
        delete(dataBase, ikube.model.Server.class, fieldsToSortOn, directionOfSort, IConstants.MAX_SERVERS);
    }

    @SuppressWarnings("unchecked")
    protected void delete(final IDataBase dataBase,
                          final Class<?> klass,
                          final String[] fieldsToSortOn,
                          final Boolean[] directionOfSort,
                          final long toRemain) {
        int batchSize = (int) toRemain / 4;
        int count = dataBase.count(klass).intValue();
        while (count > toRemain) {
            logger.info("Count : " + count + ", to remain : " + toRemain + ", batch size : " + batchSize);
            List<?> entities = dataBase.find(klass, fieldsToSortOn, directionOfSort, 0, batchSize);
            for (final Object entity : entities) {
                if (ikube.model.Action.class.isAssignableFrom(entity.getClass())) {
                    // We only delete the actions that are complete
                    if (((ikube.model.Action) entity).getEndTime() != null) {
                        dataBase.remove(entity);
                    }
                } else {
                    dataBase.remove(entity);
                }
            }
            count = dataBase.count(klass).intValue();
        }
    }

}