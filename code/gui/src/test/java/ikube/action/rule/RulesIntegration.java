package ikube.action.rule;

import ikube.Integration;
import ikube.action.IAction;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This test can be used ad-hoc to see if the rules are configured property.
 * 
 * @author Michael Couck
 * @since 20.03.11
 * @version 01.00
 */
@Ignore
public class RulesIntegration extends Integration {

	private Logger logger = Logger.getLogger(this.getClass());

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void evaluate() {
		Map<String, IAction> actions = ApplicationContextManager.getBeans(IAction.class);
		for (IAction action : actions.values()) {
			logger.info("Action : " + action);
			Object rules = action.getRules();
			logger.info("Rules : " + rules);
			if (List.class.isAssignableFrom(rules.getClass())) {
				for (IRule<IndexContext> rule : (List<IRule<IndexContext>>) rules) {
					for (final IndexContext indexContext : ApplicationContextManager.getBeans(IndexContext.class).values()) {
						boolean result = rule.evaluate(indexContext);
						logger.info("Rule : " + rule + ", result : " + result);
					}
				}
			}
		}
	}

}