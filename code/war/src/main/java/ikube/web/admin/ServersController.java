package ikube.web.admin;

import ikube.IConstants;
import ikube.cluster.IClusterManager;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.IMonitoringService;
import ikube.toolkit.ApplicationContextManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * This controller will get all the servers and put them in the response to be made available to the front end.
 * 
 * @author Michael Couck
 * @since 15.05.2011
 * @version 01.00
 */
public class ServersController extends BaseController {

	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String viewUrl = getViewUri(request);
		ModelAndView modelAndView = new ModelAndView(viewUrl);
		IClusterManager clusterManager = ApplicationContextManager.getBean(IClusterManager.class);
		// Get the servers and sort them
		List<Server> servers = clusterManager.getServers();
		Collections.sort(servers, new Comparator<Server>() {
			@Override
			public int compare(Server o1, Server o2) {
				return o1.getAddress().compareTo(o2.getAddress());
			}
		});
		modelAndView.addObject(IConstants.SERVERS, servers);

		// Put the server and other related stuff in the response
		Server server = clusterManager.getServer();
		modelAndView.addObject(IConstants.SERVER, server);
		modelAndView.addObject(IConstants.ACTIONS, server.getActions());
		modelAndView.addObject(IConstants.WEB_SERVICE_URLS, server.getWebServiceUrls());
		modelAndView.addObject(IConstants.INDEXING_EXECUTIONS, server.getIndexingExecutions());
		modelAndView.addObject(IConstants.SEARCHING_EXECUTIONS, server.getSearchingExecutions());

		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		modelAndView.addObject(IConstants.INDEX_CONTEXTS, indexContexts.values());

		IMonitoringService monitoringService = ApplicationContextManager.getBean(IMonitoringService.class);
		String[] indexNames = monitoringService.getIndexNames();
		modelAndView.addObject(IConstants.INDEX_NAMES, indexNames);

		for (String indexName : indexNames) {
			Map<String, Object> indexProperties = new HashMap<String, Object>();
			long indexSize = monitoringService.getIndexSize(indexName);
			int numDocs = monitoringService.getIndexDocuments(indexName);
			indexProperties.put(IConstants.INDEX_SIZE, indexSize);
			indexProperties.put(IConstants.INDEX_DOCUMENTS, numDocs);
			modelAndView.addObject(indexName, indexProperties);
		}

		return modelAndView;
	}

}