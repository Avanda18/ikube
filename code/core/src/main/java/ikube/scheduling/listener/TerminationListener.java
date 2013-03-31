package ikube.scheduling.listener;

import ikube.scheduling.Schedule;
import ikube.toolkit.ThreadUtilities;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This listener is to terminate the executer service, essentially aborting any actions that may be submitted, like indexing for example.
 * 
 * @author Michael Couck
 * @since 24.12.11
 * @version 01.00
 */
public class TerminationListener extends Schedule {

	@Autowired
	private ThreadUtilities threadUtilities;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		threadUtilities.destroy();
	}

}
