package ikube.model.geospatial;

import ikube.model.Persistable;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NamedQueries(value = {
		@NamedQuery(name = GeoName.SELECT_FROM_GEONAME_BY_ID_GREATER_AND_SMALLER, query = GeoName.SELECT_FROM_GEONAME_BY_ID_GREATER_AND_SMALLER),
		@NamedQuery(name = GeoName.SELECT_FROM_GEONAME_BY_FEATURECLASS_FEATURECODE_COUNTRYCODE, query = GeoName.SELECT_FROM_GEONAME_BY_FEATURECLASS_FEATURECODE_COUNTRYCODE) })
public class GeoName extends Persistable {

	public static final String SELECT_FROM_GEONAME_BY_ID_GREATER_AND_SMALLER = "select g from GeoName as g " + //
			"where " + //
			"(g.city is null and " + //
			" g.country is null) " + //
			"and g.id > :id " + //
			"order by g.id";
	public static final String SELECT_FROM_GEONAME_BY_FEATURECLASS_FEATURECODE_COUNTRYCODE = "select g from GeoName as g " + //
			"where " + //
			"g.featureClass = :featureclass and " + //
			"g.featureCode = :featurecode and " + //
			"g.countryCode = :countrycode";

	public static final String GEONAME_NAME = "geoname_name";
	public static final String GEONAME_ASCIINAME = "geoname_asciiname";
	public static final String GEONAME_CITY = "geoname_city";
	public static final String GEONAME_COUNTRY = "geoname_country";

	public static final String[] CREATE_INDEX = { //
	"create index geoname_name on geoname (name)", //
			"create index geoname_asciiname on geoname (asciiname)", //
			"create index geoname_city Oon geoname (city)",//
			"create index geoname_country on geoname (country)"//
	};

	private Integer geonameid; // : integer id of record in geonames database
	@Column(length = 200)
	// @Index(name = GeoName.GEONAME_NAME)
	private String name; // : name of geographical point (utf8) varchar(200)
	@Column(length = 200)
	// @Index(name = GeoName.GEONAME_CITY)
	private String city;
	@Column(length = 200)
	// @Index(name = GeoName.GEONAME_COUNTRY)
	private String country;
	@Column(length = 200)
	// @Index(name = GeoName.GEONAME_ASCIINAME)
	private String asciiname; // : name of geographical point in plain ascii characters, varchar(200)
	@Lob
	@Column(length = 5000)
	@Basic(fetch = FetchType.EAGER)
	private String alternatenames; // : alternate names, comma separated varchar(5000)
	private Double latitude; // : latitude in decimal degrees (wgs84)
	private Double longitude; // : longitude in decimal degrees (wgs84)
	// @Index
	@Column(length = 1)
	private String featureClass; // : see http://www.geonames.org/export/codes.html, char(1)
	// @Index
	@Column(length = 10)
	private String featureCode; // : see http://www.geonames.org/export/codes.html, varchar(10)
	@Column(length = 2)
	private String countryCode; // : ISO-3166 2-letter country code, 2 characters
	@Column(length = 60)
	private String cc2; // : alternate country codes, comma separated, ISO-3166 2-letter country code, 60 characters
	@Column(length = 20)
	private String admin1Code; // : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display
								// names of this code; varchar(20)
	@Column(length = 80)
	private String admin2Code; // : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80)
	@Column(length = 20)
	private String admin3Code; // : code for third level administrative division, varchar(20)
	@Column(length = 20)
	private String admin4Code; // : code for fourth level administrative division, varchar(20)
	private Integer population; // : bigint (8 byte int)
	private Integer elevation; // : in meters, integer
	private Integer gtopo30; // : average elevation of 30'x30' (ca 900mx900m) area in meters, integer
	@Column(length = 48)
	private String timezone; // : the timezone id (see file timeZone.txt)
	@Temporal(value = TemporalType.DATE)
	private Date modification; // date : date of last modification in yyyy-MM-dd format

	public Integer getGeonameid() {
		return geonameid;
	}

	public void setGeonameid(Integer geonameid) {
		this.geonameid = geonameid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getAsciiname() {
		return asciiname;
	}

	public void setAsciiname(String asciiname) {
		this.asciiname = asciiname;
	}

	public String getAlternatenames() {
		return alternatenames;
	}

	public void setAlternatenames(String alternatenames) {
		this.alternatenames = alternatenames;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public String getFeatureClass() {
		return featureClass;
	}

	public void setFeatureClass(String featureClass) {
		this.featureClass = featureClass;
	}

	public String getFeatureCode() {
		return featureCode;
	}

	public void setFeatureCode(String featureCode) {
		this.featureCode = featureCode;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getCc2() {
		return cc2;
	}

	public void setCc2(String cc2) {
		this.cc2 = cc2;
	}

	public String getAdmin1Code() {
		return admin1Code;
	}

	public void setAdmin1Code(String admin1Code) {
		this.admin1Code = admin1Code;
	}

	public String getAdmin2Code() {
		return admin2Code;
	}

	public void setAdmin2Code(String admin2Code) {
		this.admin2Code = admin2Code;
	}

	public String getAdmin3Code() {
		return admin3Code;
	}

	public void setAdmin3Code(String admin3Code) {
		this.admin3Code = admin3Code;
	}

	public String getAdmin4Code() {
		return admin4Code;
	}

	public void setAdmin4Code(String admin4Code) {
		this.admin4Code = admin4Code;
	}

	public Integer getPopulation() {
		return population;
	}

	public void setPopulation(Integer population) {
		this.population = population;
	}

	public Integer getElevation() {
		return elevation;
	}

	public void setElevation(Integer elevation) {
		this.elevation = elevation;
	}

	public Integer getGtopo30() {
		return gtopo30;
	}

	public void setGtopo30(Integer gtopo30) {
		this.gtopo30 = gtopo30;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String zone) {
		this.timezone = zone;
	}

	public Date getModification() {
		return modification;
	}

	public void setModification(Date modification) {
		this.modification = modification;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
	}

}
