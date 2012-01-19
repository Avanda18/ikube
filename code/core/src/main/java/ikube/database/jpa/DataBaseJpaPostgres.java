package ikube.database.jpa;

import javax.persistence.EntityManager;

/**
 * @author Michael Couck
 * @since 19.11.11
 * @version 01.00
 */
public class DataBaseJpaPostgres extends ADataBaseJpa {

	// @PersistenceContext(type = PersistenceContextType.TRANSACTION, unitName = IConstants.PERSISTENCE_UNIT_POSTGRES)
	protected EntityManager entityManager;

	@Override
	protected EntityManager getEntityManager() {
		return entityManager;
	}

}