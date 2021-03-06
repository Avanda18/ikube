package ikube.jms;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Spy;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 15-01-2015
 */
@Ignore
public class BrowserIntegration extends AbstractIntegration {

    @Spy
    private Browser browser;

    @Test
    public void browseRemoteWas() throws Exception {
        for (final String queue : queues) {
            browser.browse(
                    userid, // username
                    password, // password
                    url, // url
                    connectionFactory, // connection factory
                    destinationPrefix + queue, // destination name
                    queue + queueSuffix, // queue name
                    connectorType);
        }
    }

}