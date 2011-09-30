package ikube.web.admin;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;

/**
 * @author Michael Couck
 * @since 17.09.2011
 * @version 01.00
 */
public class IndexController extends SearchController {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String indexName = request.getParameter(IConstants.INDEX_NAME);
		ModelAndView modelAndView = super.handleRequest(request, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Parameters : " + request.getParameterMap());
		}
		@SuppressWarnings("rawtypes")
		Map<String, IndexContext> indexContexts = ApplicationContextManager.getBeans(IndexContext.class);
		for (IndexContext<?> indexContext : indexContexts.values()) {
			if (indexName.equals(indexContext.getName())) {
				modelAndView.addObject(IConstants.INDEX_NAME, indexContext.getName());
				modelAndView.addObject(IConstants.INDEX_CONTEXT, indexContext);
				break;
			}
		}
		return modelAndView;
	}

	protected String[] getIndexNames(HttpServletRequest request) {
		return new String[] { request.getParameter(IConstants.INDEX_NAME) };
	}

}