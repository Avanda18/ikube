package ikube.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
@Entity()
public class IndexContext extends Persistable implements Comparable<IndexContext> {

	/** The name of the index. This muse be unique in the configuration. */
	private String indexName;
	/** The maximum age of the index defined in minutes. */
	private long maxAge;
	/** Is this used anymore? */
	private long queueTimeout;
	/** The delay between documente being indexed, slows the indexing down. */
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

	private List<Indexable<?>> indexables;

	@Transient
	private Index index;

	public IndexContext() {
		super();
		index = new Index();
	}

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
		this.maxAge = maxAge * 60 * 1000;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	@Override
	public int compareTo(final IndexContext other) {
		return getIndexName().compareTo(other.getIndexName());
	}

}