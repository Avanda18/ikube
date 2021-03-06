package ikube.action.index.handler.internet;

import ikube.AbstractTest;
import ikube.mock.TwitterTemplateMock;
import ikube.model.IndexableTweets;
import ikube.toolkit.PERFORMANCE;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.impl.TwitterTemplate;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2013
 */
public class TwitterResourceProviderTest extends AbstractTest {

    @Mock
    private Tweet tweet;
    @Mock
    private IndexableTweets indexableTweets;
    private TwitterResourceProvider twitterResourceProvider;

    @Before
    public void before() throws IOException {
        Mockit.setUpMocks(TwitterTemplateMock.class);
        when(indexableTweets.getParent()).thenReturn(indexContext);
        twitterResourceProvider = new TwitterResourceProvider(indexableTweets);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(TwitterTemplate.class);
    }

    @Test
    public void getResource() {
        twitterResourceProvider.setResources(Arrays.asList(tweet));
        Tweet returnTweet = twitterResourceProvider.getResource();
        assertNotNull(returnTweet);
        // Now we'll deplete the stream and see that we always get a tweet
        PERFORMANCE.execute(new PERFORMANCE.APerform() {
            public void execute() {
                twitterResourceProvider.setResources(Arrays.asList(tweet));
                Tweet tweet = twitterResourceProvider.getResource();
                assertNotNull(tweet);
            }
        }, "Depletion of the tweets ", 1000, true);
    }

}