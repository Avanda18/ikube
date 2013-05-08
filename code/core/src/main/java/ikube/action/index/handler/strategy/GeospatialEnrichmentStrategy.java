package ikube.action.index.handler.strategy;

import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.action.index.handler.enrich.geocode.Coordinate;
import ikube.action.index.handler.enrich.geocode.IGeocoder;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableColumn;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.spatial.tier.projections.CartesianTierPlotter;
import org.apache.lucene.spatial.tier.projections.IProjector;
import org.apache.lucene.spatial.tier.projections.SinusoidalProjector;
import org.apache.lucene.util.NumericUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * This strategy will intercept typically databases that have co-ordinate data, geo co-ordinates. It will then add ge-hash fields to the documents in the Lucene
 * index that can then be queried and the results can be sorted by distance. Another application of course is that the query can limit all results from a
 * specific point, i.e. only places that have 'Julia Roberts' but limited to Hollywood Boulevard.
 * 
 * @author Michael Couck
 * @since 20.01.2012
 * @version 01.00
 */
@SuppressWarnings("deprecation")
public final class GeospatialEnrichmentStrategy extends AStrategy {

	@Value("${start.tier}")
	private transient int startTierParam = 10;
	@Value("${end.tier}")
	private transient int endTierParam = 20;

	private transient int startTier;
	private transient int endTier;

	/** No idea what this does :) */
	private transient IProjector sinusodialProjector;
	/** The geocoder to get the co-ordinates for the indexable. */
	@Autowired
	private IGeocoder geocoder;

	public GeospatialEnrichmentStrategy() {
		this(null);
	}

	public GeospatialEnrichmentStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		boolean mustProceed = Boolean.TRUE;
		// The parameters can be either the columns and values from a csv file
		// or the columns from a table filled in with the values. All the logic from the Enrichment class can
		// be used in here to keep all the enrichment logic in the same place
		Coordinate coordinate = getCoordinate(indexable);
		if (coordinate != null) {
			addSpatialLocationFields(coordinate, document);
		}
		return mustProceed && super.aroundProcess(indexContext, indexable, document, resource);
	}

	/**
	 * This method will look through the children of the indexable, and try to find the latitude and longitude to generate a co-ordinate. If there is no
	 * c-ordinate data in the indexable hierarchy, then this method will potentially go to a reverse geo-coding web service to get the co-ordinate based on the
	 * name, as a last resort.
	 * 
	 * @param indexable the indexable to try to find ge-spatial data in
	 * @return the co-ordinate generated from the latitude and longitude in the indexable, or null if there is no geo-data in the indexable, and the reverse
	 *         geo-coder didn't match anything either
	 */
	final Coordinate getCoordinate(final Indexable<?> indexable) {
		Double latitude = null;
		Double longitude = null;
		for (final Indexable<?> child : indexable.getChildren()) {
			if (IndexableColumn.class.isAssignableFrom(child.getClass())) {
				IndexableColumn indexableColumn = (IndexableColumn) child;
				Object content = indexableColumn.getContent();
				if (indexableColumn.getFieldName() == null) {
					continue;
				}
				if (indexableColumn.getFieldName().toLowerCase().contains(IConstants.LATITUDE.toLowerCase())) {
					latitude = Double.parseDouble(content.toString());
				} else if (indexableColumn.getFieldName().toLowerCase().contains(IConstants.LONGITUDE.toLowerCase())) {
					longitude = Double.parseDouble(content.toString());
				}
			}
		}
		if (latitude == null || longitude == null) {
			String address = buildAddress(indexable, new StringBuilder()).toString();
			// The GeoCoder is a last resort in fact
			Coordinate coordinate = geocoder.getCoordinate(address);
			if (coordinate != null) {
				return coordinate;
			}
			logger.warn("Lat and/or long are null, have you configured the columns and address properties correctly : ");
			return null;
		}
		return new Coordinate(latitude, longitude);
	}

	final void addSpatialLocationFields(final Coordinate coordinate, final Document document) {
		document.add(new Field(IConstants.LAT, NumericUtils.doubleToPrefixCoded(coordinate.getLat()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		document.add(new Field(IConstants.LNG, NumericUtils.doubleToPrefixCoded(coordinate.getLon()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addCartesianTiers(coordinate, document);
	}

	final void addCartesianTiers(final Coordinate coordinate, final Document document) {
		for (int tier = startTier; tier <= endTier; tier++) {
			CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(tier, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
			final double boxId = cartesianTierPlotter.getTierBoxId(coordinate.getLat(), coordinate.getLon());
			document.add(new Field(cartesianTierPlotter.getTierFieldName(), NumericUtils.doubleToPrefixCoded(boxId), Field.Store.YES,
					Field.Index.NOT_ANALYZED_NO_NORMS));
		}
	}

	final StringBuilder buildAddress(final Indexable<?> indexable, final StringBuilder builder) {
		if (indexable.isAddress()) {
			if (indexable instanceof IndexableColumn) {
				IndexableColumn indexableColumn = (IndexableColumn) indexable;
				if (builder.length() > 0) {
					builder.append(" ");
				}
				builder.append(indexableColumn.getContent());
			}
		}
		if (indexable.getChildren() != null) {
			for (Indexable<?> child : indexable.getChildren()) {
				buildAddress(child, builder);
			}
		}
		return builder;
	}

	public void initialize() {
		sinusodialProjector = new SinusoidalProjector();
		CartesianTierPlotter cartesianTierPlotter = new CartesianTierPlotter(0, sinusodialProjector, CartesianTierPlotter.DEFALT_FIELD_PREFIX);
		startTier = cartesianTierPlotter.bestFit(startTierParam);
		endTier = cartesianTierPlotter.bestFit(endTierParam);
	}

}