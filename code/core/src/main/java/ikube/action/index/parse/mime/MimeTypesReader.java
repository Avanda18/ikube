package ikube.action.index.parse.mime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reader for the mime-types DTD compliant XML files.
 * 
 * @author Jerome Charron - http://frutch.free.fr/
 */
final class MimeTypesReader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MimeTypesReader.class);

	protected MimeType[] read(final InputStream inputStream) {
		MimeType[] types = null;
		try {
			SAXReader reader = new SAXReader();
			Document doc = reader.read(inputStream);
			types = visit(doc);
		} catch (Exception e) {
			LOGGER.error(null, e);
			// throw new RuntimeException(e);
		}
		return types;
	}

	/** Scan through the document. */
	private MimeType[] visit(final Document document) {
		MimeType[] types = null;
		Element element = document.getRootElement();
		if ((element != null) && element.getName().equals("mime-types")) {
			types = readMimeTypes(element);
		}
		return (types == null) ? (new MimeType[0]) : types;
	}

	/** Read Element named mime-types. */
	private MimeType[] readMimeTypes(final Element rootElement) {
		ArrayList<MimeType> types = new ArrayList<MimeType>();
		// All the mime-type elements
		List<?> allElements = rootElement.elements();
		for (int i = 0; i < allElements.size(); i++) {
			Element mimeType = (Element) allElements.get(i);
			if (mimeType.getName().equals("mime-type")) {
				MimeType type = readMimeType(mimeType);
				if (type != null) {
					types.add(type);
				}
			}
		}
		return types.toArray(new MimeType[types.size()]);
	}

	/** Read Element named mime-type. */
	private MimeType readMimeType(final Element element) {
		String name = null;
		String description = null;
		MimeType type = null;

		Attribute nameAttribute = element.attribute("name");
		if (nameAttribute != null) {
			name = nameAttribute.getValue();
		}
		Attribute descriptionAttribute = element.attribute("description");
		if (descriptionAttribute != null) {
			description = descriptionAttribute.getValue();
		}
		if ((name == null) || (name.trim().equals(""))) {
			return null;
		}
		try {
			type = new MimeType(name);
		} catch (MimeTypeException mte) {
			mte.printStackTrace();
			// Mime Type not valid... just ignore it
			return null;
		}
		type.setDescription(description);

		List<?> children = element.elements();
		for (int i = 0; i < children.size(); i++) {
			Element child = (Element) children.get(i);
			// System.out.println("MimeTypesReader:" + child.getName());
			if (child.getName().equals("ext")) {
				readExt(child, type);
			} else if (child.getName().equals("magic")) {
				readMagic(child, type);
			}
		}
		return type;
	}

	/** Read Element named ext. */
	private void readExt(final Element element, final MimeType type) {
		String value = element.getText();
		type.addExtension(value);
	}

	/** Read Element named magic. */
	private void readMagic(final Element element, final MimeType mimeType) {
		String offset = null;
		String content = null;
		String type = null;

		Attribute offsetAttribute = element.attribute("offset");
		if (offsetAttribute != null) {
			offset = offsetAttribute.getValue();
		}

		Attribute typeAttribute = element.attribute("type");
		if (typeAttribute != null) {
			type = typeAttribute.getValue();
		}

		Attribute valueAttribute = element.attribute("value");
		if (valueAttribute != null) {
			content = valueAttribute.getValue();
		}

		if ((offset != null) && (content != null)) {
			mimeType.addMagic(Integer.parseInt(offset), type, content);
		}
	}
}
