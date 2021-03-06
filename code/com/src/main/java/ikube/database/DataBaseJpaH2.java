package ikube.database;

import ikube.Constants;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.Transactional;

/**
 * This class is the primary access to the database via Jpa.
 *
 * @author Michael Couck
 * @version 01.00
 * @see IDataBase
 * @since 28-04-2010
 */
@Transactional
public class DataBaseJpaH2 extends ADataBaseJpa {

    @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = Constants.PERSISTENCE_UNIT_H2)
    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}