package ikube.web.service;

import ikube.cluster.IClusterManager;
import ikube.cluster.IMonitorService;
import ikube.search.ISearcherService;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.SerializationUtilities;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * This is the base class for all web services, common logic and properties.
 * 
 * @author Michael couck
 * @since 20.11.12
 * @version 01.00
 */
public abstract class Resource {

	/** Constants for the paths to the web services. */
	public static final String REQUEST = "request";

	public static final String SEPARATOR = ",;:|";

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	protected Gson gson;
	@Autowired
	protected IMonitorService monitorService;
	@Autowired
	protected ISearcherService searcherService;
	@Autowired
	protected IClusterManager clusterManager;

	{
		gson = new Gson();
	}

	/**
	 * This method will create the response builder, then convert the results to Json and add them the the response payload, then build the response object from
	 * the builder.
	 * 
	 * @param result the data to convert to Json
	 * @return the Json response object to send to the caller/client
	 */
	protected Response buildJsonResponse(final Object result) {
		if (result == null) {
			return buildResponse().build();
		}
		String jsonString = gson.toJson(result);
		return buildResponse().entity(jsonString).build();
	}

	/**
	 * This method will create the response builder, then convert the results to xml and add them the the response payload, then build the response object from
	 * the builder.
	 * 
	 * @param result the data to convert to xml
	 * @return the xml response object to send to the caller/client
	 */
	protected Response buildXmlResponse(final Object result) {
		if (result == null) {
			return buildResponse().build();
		}
		return buildResponse().entity(SerializationUtilities.serialize(result)).build();
	}

	/**
	 * This method will just create the response builder and add some headers for cross site scripting.
	 * 
	 * @return the response builder for the category
	 */
	protected ResponseBuilder buildResponse() {
		return Response.status(200).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
	}

	<T> T unmarshall(final Class<T> clazz, final HttpServletRequest request) {
		String json = null;
		try {
			json = FileUtilities.getContents(request.getInputStream(), Integer.MAX_VALUE).toString();
			T t = gson.fromJson(json, clazz);
			if (t == null) {
				t = newInstance(clazz, json);
			}
			return t;
		} catch (IOException e) {
			return newInstance(clazz, json);
		}
	}

	private <T> T newInstance(final Class<T> clazz, final String json) {
		try {
			// If we don't have the class in the input stream then create one for the caller
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Couldn't unmarshall : " + json + ", to : " + clazz, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Couldn't unmarshall : " + json + ", to : " + clazz, e);
		}
	}

}