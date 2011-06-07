package ikube.model;

import ikube.IConstants;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This is the context for a single index. It has the properties that define the index like what it is going to index, i.e. the databases,
 * intranets etc., and properties relating to the Lucene index. This object acts a like the command in the 'Command Pattern' as in this
 * context is passed to handlers that will perform certain logic based on the properties of this context.
 * 
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class IndexContext extends Persistable implements Comparable<IndexContext> {

	/** The name of the index. This muse be unique in the configuration. */
	private String indexName;
	/** The maximum age of the index defined in minutes. */
	private long maxAge;
	/** Is this used anymore? */
	private long queueTimeout;
	/** The delay between documents being indexed, slows the indexing down. */
	private long throttle;

	/** Lucene properties. */
	private int mergeFactor;
	private int bufferedDocs;
	private double bufferSize;
	private int maxFieldLength;
	private boolean compoundFile;

	/** Jdbc properties. */
	private int batchSize;
	/** Internet properties. */
	private int internetBatchSize;

	/** The maximum length of a document that can be read. */
	private long maxReadLength;
	/** The path to the index directory, either relative or absolute. */
	private String indexDirectoryPath;
	/** The path to the backup index directory, either relative or absolute. */
	private String indexDirectoryPathBackup;
	/** Whether the index should be in memory. */
	private boolean inMemory;

	/** The indexables contained in this index context. */
	private List<Indexable<?>> indexables;

	@Transient
	private transient Index index;

	@Transient
	private transient String latestIndexTimestamp;

	/**
	 * The constructor instantiates a new {@link Index} object. In this object the Lucene index will be kept and updated.
	 */
	public IndexContext() {
		super();
		index = new Index();
	}

	/** Getters and setters. */

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(final String indexName) {
		this.indexName = indexName;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(final long maxAge) {
		this.maxAge = maxAge;
	}

	public long getQueueTimeout() {
		return queueTimeout;
	}

	public void setQueueTimeout(final long queueTimeout) {
		this.queueTimeout = queueTimeout;
	}

	public long getThrottle() {
		return throttle;
	}

	public void setThrottle(final long throttle) {
		this.throttle = throttle;
	}

	public boolean isCompoundFile() {
		return compoundFile;
	}

	public void setCompoundFile(final boolean compoundFile) {
		this.compoundFile = compoundFile;
	}

	public int getMaxFieldLength() {
		return maxFieldLength;
	}

	public void setMaxFieldLength(final int maxFieldLength) {
		this.maxFieldLength = maxFieldLength;
	}

	public int getBufferedDocs() {
		return bufferedDocs;
	}

	public void setBufferedDocs(final int bufferedDocs) {
		this.bufferedDocs = bufferedDocs;
	}

	public int getMergeFactor() {
		return mergeFactor;
	}

	public void setMergeFactor(final int mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	public double getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(final double bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(final int batchSize) {
		this.batchSize = batchSize;
	}

	public int getInternetBatchSize() {
		return internetBatchSize;
	}

	public void setInternetBatchSize(final int internetBatchSize) {
		this.internetBatchSize = internetBatchSize;
	}

	public long getMaxReadLength() {
		return maxReadLength;
	}

	public void setMaxReadLength(final long maxReadLength) {
		this.maxReadLength = maxReadLength;
	}

	public String getIndexDirectoryPath() {
		return indexDirectoryPath;
	}

	public void setIndexDirectoryPath(final String indexDirectoryPath) {
		this.indexDirectoryPath = indexDirectoryPath;
	}

	public String getIndexDirectoryPathBackup() {
		return indexDirectoryPathBackup;
	}

	public void setIndexDirectoryPathBackup(final String indexDirectoryPathBackup) {
		this.indexDirectoryPathBackup = indexDirectoryPathBackup;
	}

	public Boolean getInMemory() {
		return inMemory;
	}

	public void setInMemory(final Boolean inMemory) {
		this.inMemory = inMemory;
	}

	public List<Indexable<?>> getIndexables() {
		return indexables;
	}

	public void setIndexables(final List<Indexable<?>> indexables) {
		this.indexables = indexables;
	}

	public Index getIndex() {
		return index;
	}

	public void setIndex(final Index index) {
		this.index = index;
	}

	public String getLatestIndexTimestamp() {
		long timestamp = 0;
		File latestIndexDirectory = FileUtilities.getLatestIndexDirectory(indexDirectoryPath);
		if (latestIndexDirectory != null) {
			String name = latestIndexDirectory.getName();
			if (StringUtils.isNumeric(name)) {
				timestamp = Long.parseLong(name);
			}
		}
		this.latestIndexTimestamp = IConstants.HHMMSS_DDMMYYYY.format(new Date(timestamp));
		return this.latestIndexTimestamp;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int compareTo(final IndexContext other) {
		return getIndexName().compareTo(other.getIndexName());
	}

}