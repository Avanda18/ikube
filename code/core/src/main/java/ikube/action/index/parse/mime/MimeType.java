package ikube.action.index.parse.mime;

// JDK imports
import java.util.ArrayList;

/**
 * Defines a Mime Content Type.
 * 
 * @author Jerome Charron - http://frutch.free.fr/
 * @author Hari Kodungallur
 */
public final class MimeType {

	/** The primary and sub types separator */
	private final static String SEPARATOR = "/";
	/** The parameters separator */
	private final static String PARAMS_SEP = ";";
	/** Special characters not allowed in content types. */
	private final static String SPECIALS = "()<>@,;:\\\"/[]?=";
	/** The Mime-Type full name */
	private transient String name = null;
	/** The Mime-Type primary type */
	private transient String primary = null;
	/** The Mime-Type sub type */
	private transient String sub = null;
	/** The Mime-Type description */
	private transient String description = null;
	/** The Mime-Type associated extensions */
	private transient ArrayList<String> extensions = null;
	/** The magic bytes associated to this Mime-Type */
	private transient ArrayList<Magic> magics = null;
	/** The minimum length of data to provides for magic analyzis */
	private transient int minLength = 0;

	/**
	 * Creates a MimeType from a String.
	 * 
	 * @param name
	 *            the MIME content type String.
	 */
	public MimeType(final String name) throws MimeTypeException {
		if (name == null || name.length() <= 0) {
			throw new MimeTypeException("The type can not be null or empty");
		}
		// Split the two parts of the Mime Content Type
		String[] parts = name.split(SEPARATOR, 2);
		// Checks validity of the parts
		if (parts.length != 2) {
			throw new MimeTypeException("Invalid Content Type " + name);
		}
		init(parts[0], parts[1]);
	}

	/**
	 * Creates a MimeType with the given primary type and sub type.
	 * 
	 * @param primary
	 *            the content type primary type.
	 * @param sub
	 *            the content type sub type.
	 */
	public MimeType(final String primary, final String sub) throws MimeTypeException {
		init(primary, sub);
	}

	/** Init method used by constructors. */
	private void init(final String primary, final String sub) throws MimeTypeException {
		// Preliminary checks...
		if ((primary == null) || (primary.length() <= 0) || (!isValid(primary))) {
			throw new MimeTypeException("Invalid Primary Type " + primary);
		}
		// Remove optional parameters from the sub type
		String clearedSub = null;
		if (sub != null) {
			clearedSub = sub.split(PARAMS_SEP)[0];
		}
		if ((clearedSub == null) || (clearedSub.length() <= 0) || (!isValid(clearedSub))) {
			throw new MimeTypeException("Invalid Sub Type " + clearedSub);
		}
		// All is ok, assign values
		this.name = primary + SEPARATOR + sub;
		this.primary = primary;
		this.sub = clearedSub;
		this.extensions = new ArrayList<String>();
		this.magics = new ArrayList<MimeType.Magic>();
	}

	/**
	 * Return the name of this mime-type.
	 * 
	 * @return the name of this mime-type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the primary type of this mime-type.
	 * 
	 * @return the primary type of this mime-type.
	 */
	public String getPrimaryType() {
		return primary;
	}

	/**
	 * Return the sub type of this mime-type.
	 * 
	 * @return the sub type of this mime-type.
	 */
	public String getSubType() {
		return sub;
	}

	/**
	 * Indicates if an object is equal to this mime-type. The specified object is equal to this mime-type if it is not null, and it is an
	 * instance of MimeType and its name is equals to this mime-type.
	 * 
	 * @param object
	 *            the reference object with which to compare.
	 * @return <code>true</code> if this mime-type is equal to the object argument; <code>false</code> otherwise.
	 */
	public boolean equals(final Object object) {
		if (object == null) {
			return Boolean.FALSE;
		}
		if (!MimeType.class.isAssignableFrom(object.getClass())) {
			return Boolean.FALSE;
		}
		MimeType other = (MimeType) object;
		if (other.getName() == null) {
			return Boolean.FALSE;
		}
		try {
			return other.getName().equals(this.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// Inherited Javadoc
	public int hashCode() {
		return name.hashCode();
	}

	/**
	 * Return the description of this mime-type.
	 * 
	 * @return the description of this mime-type.
	 */
	String getDescription() {
		return description;
	}

	/**
	 * Set the description of this mime-type.
	 * 
	 * @param description
	 *            the description of this mime-type.
	 */
	void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Add a supported extension.
	 * 
	 * @param the
	 *            extension to add to the list of extensions associated to this mime-type.
	 */
	void addExtension(final String ext) {
		extensions.add(ext);
	}

	/**
	 * Return the extensions of this mime-type
	 * 
	 * @return the extensions associated to this mime-type.
	 */
	String[] getExtensions() {
		return extensions.toArray(new String[extensions.size()]);
	}

	void addMagic(final int offset, final String type, final String magic) {
		// Some preliminary checks...
		if ((magic == null) || (magic.length() < 1)) {
			return;
		}
		Magic m = new Magic(offset, type, magic);
		magics.add(m);
		minLength = Math.max(minLength, m.size());
	}

	int getMinLength() {
		return minLength;
	}

	boolean hasMagic() {
		return !magics.isEmpty();
	}

	boolean matches(byte[] data) {
		if (!hasMagic()) {
			return false;
		}
		Magic tested = null;
		for (int i = 0; i < magics.size(); i++) {
			tested = magics.get(i);
			if (tested.matches(data)) {
				return true;
			}
		}
		return false;
	}

	/** Checks if the specified primary or sub type is valid. */
	private boolean isValid(final String type) {
		return (type != null) && (type.trim().length() > 0) && !hasCtrlOrSpecials(type);
	}

	/** Checks if the specified string contains some special characters. */
	private boolean hasCtrlOrSpecials(final String type) {
		int len = type.length();
		int i = 0;
		while (i < len) {
			char c = type.charAt(i);
			if (c <= '\032' || SPECIALS.indexOf(c) > 0) {
				return true;
			}
			i++;
		}
		return false;
	}

	private static class Magic {

		private transient int offset;
		private transient byte[] magic = null;

		Magic(int offset, String type, String magic) {
			this.offset = offset;

			if ((type != null) && (type.equals("byte"))) {
				this.magic = readBytes(magic);
			} else {
				this.magic = magic.getBytes();
			}
		}

		int size() {
			return (offset + magic.length);
		}

		boolean matches(byte[] data) {
			if (data == null) {
				return false;
			}
			int idx = offset;
			if ((idx + magic.length) > data.length) {
				return false;
			}
			for (int i = 0; i < magic.length; i++) {
				if (magic[i] != data[idx++]) {
					return false;
				}
			}
			return true;
		}

		private byte[] readBytes(String magic) {
			byte[] data = null;
			if ((magic.length() % 2) == 0) {
				String tmp = magic.toLowerCase();
				data = new byte[tmp.length() / 2];
				int byteValue = 0;
				for (int i = 0; i < tmp.length(); i++) {
					char c = tmp.charAt(i);
					int number;
					if (c >= '0' && c <= '9') {
						number = c - '0';
					} else if (c >= 'a' && c <= 'f') {
						number = 10 + c - 'a';
					} else {
						throw new IllegalArgumentException();
					}
					if ((i % 2) == 0) {
						byteValue = number * 16;
					} else {
						byteValue += number;
						data[i / 2] = (byte) byteValue;
					}
				}
			}
			return data;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append('[');
			buf.append(offset);
			buf.append('/');
			buf.append(new String(magic));
			buf.append(']');
			return buf.toString();
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append('[');
		buffer.append(this.name);
		buffer.append(',');
		buffer.append(this.description);
		buffer.append(']');
		return buffer.toString();
	}
}
