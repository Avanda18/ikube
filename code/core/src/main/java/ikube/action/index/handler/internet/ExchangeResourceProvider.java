package ikube.action.index.handler.internet;

import ikube.action.index.handler.IResourceProvider;
import ikube.model.IndexableExchange;
import ikube.model.Url;

import java.util.List;

/**
 * @author David Turley
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings("FieldCanBeLocal")
public class ExchangeResourceProvider implements IResourceProvider<Url> {

    private final IndexableExchange indexableExchange;

    public ExchangeResourceProvider(final IndexableExchange indexableExchange) {
        this.indexableExchange = indexableExchange;
        // TODO: Start 'crawling' Exchange server here, starting at the point left off the last time
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Url getResource() {
        // TODO: Return a crawled resource to the caller
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setResources(final List<Url> resources) {
        // TODO: Add the resource crawled to the collection available in this object
    }

}