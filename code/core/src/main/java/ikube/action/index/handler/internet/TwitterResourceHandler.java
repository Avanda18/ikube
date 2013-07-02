package ikube.action.index.handler.internet;

import ikube.IConstants;
import ikube.action.index.IndexManager;
import ikube.action.index.handler.ResourceHandler;
import ikube.model.IndexContext;
import ikube.model.IndexableTweets;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.springframework.social.twitter.api.Tweet;

/**
 * @author Michael Couck
 * @since 19.06.13
 * @version 01.00
 */
public class TwitterResourceHandler extends ResourceHandler<IndexableTweets> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Document handleResource(final IndexContext<?> indexContext, final IndexableTweets indexableTweets, final Document document, final Object resource) {
		Tweet tweet = (Tweet) resource;
		Store store = indexableTweets.isStored() ? Store.YES : Store.NO;
		Index analyzed = indexableTweets.isAnalyzed() ? Index.ANALYZED : Index.NOT_ANALYZED;
		TermVector termVector = indexableTweets.isVectored() ? TermVector.YES : TermVector.NO;

		// This is the unique id of the resource to be able to delete it
		String tweetId = Long.toString(tweet.getId());
		String createdAtField = indexableTweets.getCreatedAtField();
		String fromUserField = indexableTweets.getFromUserField();
		String locationField = indexableTweets.getLocationField();
		String textField = indexableTweets.getTextField();

		// NOTE to self: To be able to delete using the index writer the identifier field must be non analyzed and non tokenized/vectored!
		IndexManager.addStringField(IConstants.ID, tweetId, document, Store.YES, Index.NOT_ANALYZED, TermVector.NO);
		IndexManager.addNumericField(createdAtField, Long.toString(tweet.getCreatedAt().getTime()), document, Store.YES);

		IndexManager.addStringField(fromUserField, tweet.getFromUser(), document, store, analyzed, termVector);
		IndexManager.addStringField(locationField, indexableTweets.getAddressContent(), document, store, analyzed, termVector);
		IndexManager.addStringField(textField, indexableTweets.getContent().toString(), document, store, analyzed, termVector);

		try {
			addDocument(indexContext, indexableTweets, document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return document;
	}

}
