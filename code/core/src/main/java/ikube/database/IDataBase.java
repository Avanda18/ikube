package ikube.database;

import java.util.List;
import java.util.Map;

/**
 * Interface for database objects. Database objects support access to the persistent store. Implementations of this class then act as the
 * generic data access object for all entities, avoiding boiler place specific code for entities which is so popular now and essentially
 * just a pre-determined place to put the database access.
 * 
 * @author Michael Couck
 * @since 20.04.09
 * @version 01.00
 */
public interface IDataBase {

	/**
	 * This method removed the specified entity from the database.
	 * 
	 * @param <T> the type of object that will be removed
	 * @param object the object that must be removed
	 * @return the removed object, i.e. a refresh
	 */
	<T> T remove(T object);

	/**
	 * Executed a delete statement in the entity manager and returns the number of rows that were affected by the statement, the number of
	 * records that were deleted.
	 * 
	 * @param sql the update/delete statement to execute on the database
	 * @return the number of rows that were affected
	 */
	int remove(String sql);

	/**
	 * Removes the object defined by the type and the id.
	 * 
	 * @param <T> the type of object that will be removed
	 * @param klass the class of object to remove
	 * @param objectId the id of the object to remove
	 * @return the removed object, or null if no such object is found to remove
	 */
	<T> T remove(Class<T> klass, Long objectId);

	/**
	 * Removes the batch of objects in the parameter list. Depending on the underlying implementation, this method should be executed in a
	 * transaction. As such the commit of the transaction will then flush the removes to the database. So the statements will be batched up
	 * and sent in one shot to the database.
	 * 
	 * @param <T> the type of object to batch remove
	 * @param batch the list of objects to batch remove
	 */
	<T> void removeBatch(List<T> batch);

	/**
	 * Persists an object in the database.
	 * 
	 * @param object the object to be persisted
	 * @return the refreshed object from the database, typically this object will have the unique id filled in by the database
	 */
	<T> T persist(T object);

	/**
	 * Persists a batch of objects, i.e. the list passed in the parameter list. As with the {@link IDataBase#removeBatch(List)} method this
	 * will run in a transaction, and the commit of the transaction will send the statements to the database, depending on the underlying
	 * Jpa implementation.
	 * 
	 * @param <T> the type of entities to batch persist
	 * @param list the list of entities to batch persist
	 */
	<T> void persistBatch(List<T> list);

	/**
	 * Merges the object parameter with the object from the database. In the case where a primitive field is changed in the object and this
	 * change needs to be persisted the merge will update the database with the new value.
	 * 
	 * @param object the object to be merged or updated in the database
	 * @return the refreshed object from the database, typically this will be exactly the same as the object to be merged
	 */
	<T> T merge(T object);

	/**
	 * Merges the entities in a batch. Running in a transaction, the underlying implementation will batch up the statements and send them to
	 * the database when the transaction is comitted.
	 * 
	 * @param <T> the type of entities to batch merge
	 * @param batch the list of entities to batch merge
	 */
	<T> void mergeBatch(List<T> batch);

	/**
	 * Finds an object by the if field only. This method will be very expensive as each object in the database is iterated through and the
	 * id field found by reflection and compared to the parameter. Also only the first object will be returned. This method assumes that
	 * every object in the database has a database scope unique id.
	 * 
	 * @param <T> the type of object to find
	 * @param objectId the id of the object
	 * @return the first object with the or or null if no such object can be found
	 */
	<T> T find(Long objectId);

	/**
	 * Access the object of a particular class with the id.
	 * 
	 * @param <T> the type of object
	 * @param klass the class of the object
	 * @param objectId the id of the object
	 * @return the object or null if there is no such object or a runtime exception if there is more than one result, meaning of course that
	 *         there is a constraint violation, like unique indexes etc.
	 */
	<T> T find(Class<T> klass, Long objectId);

	/**
	 * Selects all the objects in the database that conform to the class type, limited by the max results parameter.
	 * 
	 * @param klass the class of object to select
	 * @param startIndex the first result in the result set, used for paging
	 * @param endIndex the last object in the results, i.e. the index of the last result
	 * @return the list of objects from the database specified by the class type
	 */
	<T> List<T> find(Class<T> klass, int startIndex, int endIndex);

	/**
	 * This method will find the class type specified in the database, and then sort by multiple fields, in the order that they were
	 * specified. the direction of sort is boolean for ascending and false for descending.
	 * 
	 * @param <T> the type to expect
	 * @param klass the class to select from the database
	 * @param fieldsToSortOn the fields to sort on, in the order of the sort requirements, i.e. first on start time then on end time for
	 *            example
	 * @param directionOfSort the direction of the sort for each field. For example sort acesnding on the start time and descending on the
	 *            end time
	 * @param firstResult the first result in the set, i.e. skip the first n results
	 * @param maxResults and the size of the result set
	 * @return the list of sorted entities from the database
	 */
	<T> List<T> find(Class<T> klass, String[] fieldsToSortOn, boolean[] directionOfSort, int firstResult, int maxResults);

	/**
	 * Selects a single object based on the sql and the parameters. The combination of parameters should result in a single unique entity
	 * being returned from the database, otherwise a non unique exception should be thrown.
	 * 
	 * @param <T> the type of entity expected from the query
	 * @param klass the class of entity expected from the query
	 * @param sql the sql statement to select the entity
	 * @param parameters the parameters for the query, must result in a unique result
	 * @return the entity that returned from the query and parameters. This could also result in an entity not found exception and a non
	 *         unique result exception
	 */
	<T> T find(Class<T> klass, String sql, Map<String, Object> parameters);

	/**
	 * Selects a list of entities from the database based on the sql and the parameters. The results will be limited by the starting
	 * position and the maximum results parameters. This method is therefore suited to paging results. Note that the sql should have an
	 * order by clause as the underlying implementation will not guarantee the order of the results, the side effect of this is uncertain.
	 * 
	 * @param <T> the type of entity expected
	 * @param klass the class of entity expected
	 * @param sql the sql to select the entities from the database, should contain an order by clause
	 * @param parameters the parameters to narrow the results
	 * @param startPosition the first result position
	 * @param maxResults the maximum results to return
	 * @return the list of results determined by the sql, parameters and the limiting parameters
	 */
	<T> List<T> find(Class<T> klass, String sql, Map<String, Object> parameters, int startPosition, int maxResults);

	/**
	 * Executed the passed sql on the database, returning a single entity. Note that the query should result in exactly one entity otherwise
	 * either a no result exception or a non unique exception will be thrown.
	 * 
	 * @param <T> the type of entity expected
	 * @param klass the class of entity expected
	 * @param sql the sql to select a single entity from the database, i.e. must be unique and exist for the query
	 * @return the single entity resulting from the query
	 */
	<T> T execute(Class<T> klass, String sql);

	/**
	 * Executes the query against the database using the sql and parameters dynamically set in the query. As with the above this query
	 * should result in a unique entity being selected.
	 * 
	 * @param <T> the type of entity expected
	 * @param klass the class of entity expected
	 * @param sql the sql to execute to select the entity
	 * @param parameters the parameters to narrow the results to one entity
	 * @return the single entity resulting from the combination of the query and the parameters
	 */
	<T> T execute(Class<T> klass, String sql, Map<String, Object> parameters);

	/**
	 * TODO Document me!
	 * 
	 * @param <T>
	 * @param klass
	 * @param sql
	 * @param names
	 * @param values
	 * @return
	 */
	<T> T find(Class<T> klass, String sql, String[] names, Object[] values);

	/**
	 * TODO Document me!
	 * 
	 * @param <T>
	 * @param klass
	 * @param sql
	 * @param names
	 * @param values
	 * @param startPosition
	 * @param maxResults
	 * @return
	 */
	<T> List<T> find(Class<T> klass, String sql, String[] names, Object[] values, int startPosition, int maxResults);

}