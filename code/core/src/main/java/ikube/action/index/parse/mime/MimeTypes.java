package ikube.action.index.parse.mime;

import ikube.toolkit.FILE;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;

/**
 * This class is a MimeType repository. It gathers a set of MimeTypes and enables
 * to retrieves a content-type from a specified file extension, or from a magic character
 * sequence (or both).
 *
 * @author Jerome Charron - http://frutch.free.fr/
 * @author Michael Couck - modified by
 */
public final class MimeTypes {

    private static final Logger LOGGER = Logger.getLogger(MimeTypes.class);

    /**
     * The static instance of this class.
     */
    private static MimeTypes INSTANCE;
    /**
     * The default <code>application/octet-stream</code> MimeType
     */
    public final static String DEFAULT = "application/octet-stream";
    /**
     * All the registered MimeTypes
     */
    private static final Map<String, MimeType> TYPES = new HashMap<>();
    /**
     * My registered instances There is one instance associated for each specified file while
     * calling the {@link MimeTypes#getMimeTypes(String)} method. Key is the specified file path in the
     * {@link ikube.action.index.parse.mime.MimeTypes#getMimeTypes(String)} method. Value is the associated
     * MimeType instance.
     */
    private static final Map<Integer, MimeTypes> INSTANCES = new HashMap<>();

    /**
     * MimeTypes indexed on the file extension
     */
    private transient final Map<String, List<MimeType>> extIdx = new HashMap<>();
    /**
     * List of MimeTypes containing a magic char sequence
     */
    private transient final List<MimeType> magicsIdx = new ArrayList<>();
    /**
     * The minimum length of data to provide to check all MimeTypes
     */
    private transient int minLength = 0;

    public static Map<String, MimeType> getTypes() {
        return TYPES;
    }

    public static Collection<String> getTypesCollection() {
        Collection<String> mimeTypes = new ArrayList<>();
        for (final Map.Entry<String, MimeType> mapEntry : TYPES.entrySet()) {
            mimeTypes.add(mapEntry.getValue().getName());
        }
        return mimeTypes;
    }

    @SuppressWarnings("UnnecessaryBoxing")
    public MimeTypes(final String fileName) {
        InputStream inputStream = null;
        try {
            File file = FILE.findFileRecursively(new File("."), fileName);
            inputStream = new FileInputStream(file);
            MimeTypes instance;
            synchronized (INSTANCES) {
                Integer hash = Integer.valueOf(inputStream.hashCode());
                instance = INSTANCES.get(hash);
                if (instance == null) {
                    instance = new MimeTypes(inputStream);
                    INSTANCES.put(hash, instance);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception loading the mapping file for mime types : ", e);
        } finally {
            FILE.close(inputStream);
        }

    }

    /**
     * Returns the MimeType from the name of the mime type, i.e. something like 'text/html' or it can be the name of the file like
     * index.html and all the extensions will be checked.
     *
     * @param type the name of the mime type or the name of the file
     * @return the mime type associated with the name or null if no such mime type can be found
     */
    public static MimeType getMimeTypeFromName(final String type) {
        if (type == null) {
            return null;
        }
        MimeType mimeType = TYPES.get(type);
        if (mimeType == null) {
            for (MimeType m : TYPES.values()) {
                String[] extensions = m.getExtensions();
                for (String extension : extensions) {
                    if (type.endsWith(extension)) {
                        mimeType = m;
                        break;
                    }
                }
            }
        }
        return mimeType;
    }

    /**
     * Find the Mime Content Type of a stream from its content.
     *
     * @param data are the first bytes of data of the content to analyze. Depending on the length of provided data, all known MimeTypes are
     *             checked. If the length of provided data is greater or egals to the value returned by {@link #getMinLength()}, then all known
     *             MimeTypes are checked, otherwise only the MimeTypes that could be analyzed with the length of provided data are analyzed.
     * @return The Mime Content Type found for the specified data, or <code>null</code> if none is found.
     * @see #getMinLength()
     */
    public static MimeType getMimeType(final byte[] data) {
        // Preliminary checks
        if ((data == null) || (data.length < 1)) {
            return null;
        }
        Iterator<MimeType> iter = INSTANCE.magicsIdx.iterator();
        MimeType type;
        // Todo: This is a very naive first approach (scanning all the magic
        // bytes since one is matching. A first improvement could be to use a search path on the magic
        // bytes. Todo: A second improvement could be to search for the most qualified (the longuest) magic
        // sequence (not the first that is matching).
        while (iter.hasNext()) {
            type = iter.next();
            if (type.matches(data)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Find the Mime Content Type of a document from its name and its content.
     *
     * @param name of the document to analyze.
     * @param data are the first bytes of the document's content.
     * @return the Mime Content Type of the specified document, or <code>null</code> if none is found.
     * @see #getMinLength()
     */
    public static MimeType getMimeType(final String name, final byte[] data) {
        // First, try to get the mime-type from the name
        MimeType mimeType = null;
        MimeType[] mimeTypes = INSTANCE.getMimeTypes(name);
        if (mimeTypes == null) {
            // No mime-type found, so trying to analyse the content
            mimeType = getMimeType(data);
        } else if (mimeTypes.length >= 1) {
            // Todo: More than one mime-type found, so trying magic resolution
            // on these mime types mimeType = getMimeType(data, mimeTypes);
            // For now, just get the first one
            mimeType = mimeTypes[0];
        }
        return mimeType;
    }

    /**
     * Should never be instantiated from outside
     */
    private MimeTypes(final InputStream inputStream) {
        INSTANCE = this;
        MimeTypesReader reader = new MimeTypesReader();
        add(reader.read(inputStream));
    }

    /**
     * Return the minimum length of data to provide to analyzing methods based on the document's content in order to check all the known
     * MimeTypes.
     *
     * @return the minimum length of data to provide.
     * @see #getMimeType(byte[])
     * @see #getMimeType(String, byte[])
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Returns an array of matching MimeTypes from the specified name (many MimeTypes can have the same registered extensions).
     */
    private MimeType[] getMimeTypes(final String name) {
        if (name == null) {
            return null;
        }
        List<MimeType> mimeTypes = null;
        int index = name.lastIndexOf('.');
        if ((index != -1) && (index != name.length() - 1)) {
            // There's an extension, so try to find
            // the corresponding mime-types
            String ext = name.substring(index + 1);
            mimeTypes = extIdx.get(ext);
        }
        return (mimeTypes != null) ? mimeTypes.toArray(new MimeType[mimeTypes.size()]) : null;
    }

    /**
     * Add the specified mime-types in the repository.
     *
     * @param types are the mime-types to add.
     */
    void add(final MimeType[] types) {
        if (types == null) {
            return;
        }
        for (final MimeType type : types) {
            add(type);
        }
    }

    /**
     * Add the specified mime-type in the repository.
     *
     * @param type is the mime-type to add.
     */
    void add(final MimeType type) {
        TYPES.put(type.getName(), type);
        // Update minLentgth
        minLength = Math.max(minLength, type.getMinLength());
        // Update the extensions index...
        String[] exts = type.getExtensions();
        for (final String ext : exts) {
            List<MimeType> list = extIdx.get(ext);
            if (list == null) {
                // No type already registered for this extension...
                // So, create a list of types
                list = new ArrayList<>();
                extIdx.put(ext, list);
            }
            list.add(type);
        }
        // Update the magics index...
        if (type.hasMagic()) {
            magicsIdx.add(type);
        }
    }
}