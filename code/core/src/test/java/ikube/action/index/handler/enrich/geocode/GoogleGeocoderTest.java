package ikube.action.index.handler.enrich.geocode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ikube.AbstractTest;
import ikube.action.index.handler.strategy.geocode.GoogleGeocoder;
import ikube.mock.FILEMock;
import ikube.mock.URLMock;
import ikube.model.Coordinate;
import ikube.toolkit.FILE;

import java.io.ByteArrayOutputStream;
import java.io.File;

import mockit.Mockit;

import org.junit.Test;

/**
 * @author Michael Couck
 * @since 06.03.11
 * @version 01.00
 */
public class GoogleGeocoderTest extends AbstractTest {

	@Test
	public void getCoordinate() throws Exception {
		try {
			File file = FILE.findFileRecursively(new File("."), "address.xml");
			ByteArrayOutputStream contents = FILE.getContents(file, Integer.MAX_VALUE);
			FILEMock.setContents(contents);
			URLMock.setContents(contents);
			Mockit.setUpMocks(URLMock.class, FILEMock.class);

			GoogleGeocoder geocoder = new GoogleGeocoder();
			geocoder.setSearchUrl("http://maps.googleapis.com/maps/api/geocode/xml");
			Coordinate coordinate = geocoder.getCoordinate("9 avenue road, cape town, south africa");
			assertNotNull("The coordinate can not be null as this is a real address from Google : ", coordinate);
			double lat = coordinate.getLatitude();
			double lon = coordinate.getLongitude();
			assertEquals("We know that this address exists and where it is : ", -33.9693580, lat, 1.0);
			assertEquals("We know that this address exists and where it is : ", 18.4622110, lon, 1.0);
		} finally {
			Mockit.tearDownMocks(URLMock.class, FILEMock.class);
		}
	}

}
