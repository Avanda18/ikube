package ikube.index.content;

import ikube.model.IndexableColumn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class ColumnContentProvider implements IContentProvider<IndexableColumn> {

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * TODO - we need to parse the data here and return a file input stream if it is too large.
	 */
	public Object getContent(IndexableColumn indexable) {
		Object object = indexable.getObject();
		int columnType = indexable.getColumnType();
		if (object == null) {
			return "";
		}

		// BugFix for Oracle and Db2 it seems: Oracle seems to think that a Blob is a NULL
		// type so we'll just reset the type to Blob in this case
		if (Blob.class.isAssignableFrom(object.getClass()) && columnType != Types.BLOB) {
			columnType = Types.BLOB;
		} else if (Clob.class.isAssignableFrom(object.getClass()) && columnType != Types.CLOB) {
			columnType = Types.CLOB;
		}

		Object result = "";
		InputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		Reader clobReader = null;
		try {
			// Get the data for the object according to the class type
			switch (columnType) {
			case Types.BOOLEAN:
				result = object.toString();
				break;

			case Types.BIT:
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
			case Types.BIGINT:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE:
			case Types.NUMERIC:
			case Types.DECIMAL:
				result = object.toString();
				break;

			case Types.CHAR:
			case Types.VARCHAR:
			case Types.LONGVARCHAR:
				result = object.toString();
				break;

			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP:
				// So for Oracle that has to be special the timestamp object doesn't implement the Java timestamp
				// so we have to guess the method to call to get a representation of the object, so we will just look for a
				// method that returns a Timestamp from the class without any parameters and hope for the best
				Method method = findMethod(object, Timestamp.class);
				Timestamp timestamp = null;
				if (Timestamp.class.isAssignableFrom(object.getClass())) {
					timestamp = (Timestamp) object;
				} else {
					if (method != null) {
						method.setAccessible(true);
						timestamp = (Timestamp) method.invoke(object, (Object[]) null);
					}
				}
				if (timestamp != null) {
					result = Long.toString(timestamp.getTime());
				}
				break;

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				result = new ByteArrayInputStream((byte[]) object);
				break;

			case Types.OTHER:
				break;

			case Types.JAVA_OBJECT:
			case Types.DISTINCT:
			case Types.STRUCT:
			case Types.ARRAY:
				break;

			case Types.NULL:
				result = "null";
				break;

			case Types.BLOB:
				// Get an input stream method, as this can be different for each driver blob or clob
				// for both Oracle or DB2 the input stream are both implemented. If at any stage another
				// database is used then this has to be re-tested
				if (Blob.class.isAssignableFrom(object.getClass())) {
					inputStream = ((Blob) object).getBinaryStream();
				} else {
					method = findMethod(object, InputStream.class);
					if (method != null) {
						method.setAccessible(true);
						// Get the input stream and read the data from the column
						inputStream = (InputStream) method.invoke(object, (Object[]) null);
					}
				}
				int read = 0;
				byte[] bytes = new byte[1024 * 256];
				// File file = File.createTempFile(Long.toString(System.nanoTime()), IConstants.READER_FILE_SUFFIX);
				// logger.debug("Temp file : " + file.getAbsolutePath());
				// OutputStream outputStream = new FileOutputStream(file);
				outputStream = new ByteArrayOutputStream();
				while (inputStream != null && (read = inputStream.read(bytes)) > 0) {
					outputStream.write(bytes, 0, read);
				}
				result = new ByteArrayInputStream(outputStream.toByteArray());
				break;
			case Types.CLOB:
				// Get an input stream method, as this can be different for each driver blob or clob
				// for both Oracle or DB2 the input stream are both implemented. If at any stage another
				// database is used then this has to be re-tested
				if (Clob.class.isAssignableFrom(object.getClass())) {
					clobReader = ((Clob) object).getCharacterStream();
				} else {
					method = findMethod(object, Reader.class);
					if (method != null) {
						method.setAccessible(true);
						// Get the input stream and read the data from the column
						clobReader = (Reader) method.invoke(object, (Object[]) null);
					}
				}
				read = 0;
				char[] chars = new char[1024 * 256];
				// file = File.createTempFile(Long.toString(System.nanoTime()), IConstants.READER_FILE_SUFFIX);
				// logger.debug("Temp file : " + file.getAbsolutePath());
				// outputStream = new FileOutputStream(file);
				outputStream = new ByteArrayOutputStream();
				while (clobReader != null && (read = clobReader.read(chars)) > 0) {
					outputStream.write(String.valueOf(chars).getBytes(), 0, read);
				}
				result = new ByteArrayInputStream(outputStream.toByteArray());
				break;
			}
		} catch (Exception e) {
			logger.error("Exception accessing data from column.", e);
			@SuppressWarnings("unused")
			String identifier = indexable.getName();
			// We need to log this in the database too via an event
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
			if (clobReader != null) {
				try {
					clobReader.close();
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		return result;
	}

	/**
	 * Finds a method in an object that has as return type the class of the second parameter. For example in the Blob object for Oracle
	 * there is a method getBinaryStream that returns an input stream to the blob.
	 * 
	 * In DB2 it's getInputStream. We, due to the inability of these database vendors to implement the java.sq.Blob interface, have to guess
	 * what the method is. We don't care if it is a string stream or an ASCII stream the final data will be read in bytes and converted to
	 * string anyway.
	 * 
	 * @param object
	 *            the object that we need to find a method on
	 * @param klass
	 *            the return type of the method that we are looking for
	 * @return a method on the object that has as return type the class parameter
	 */
	protected Method findMethod(Object object, Class<?> klass) {
		if (object == null) {
			return null;
		}
		Method[] methods = object.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			// Looking for a method that takes no parameters and returns the type of class
			// specified in the parameter list
			if (methods[i].getParameterTypes().length == 0 && klass.isAssignableFrom(methods[i].getReturnType())) {
				return methods[i];
			}
		}
		return null;
	}

}