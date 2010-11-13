package ikube.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity()
public class Event extends Persistable {

	public static final String TIMER = "timer";
	public static final String PROFILING = "profiling";
	public static final String SEARCHER_OPENED = "searcherOpened";
	public static final String RESULTS = "results";
	public static final String NO_RESULTS = "noResults";
	public static final String SERVICE = "service";
	public static final String DEAD_LOCK = "deadLock";
	public static final String REPORT = "report";
	public static final String CLUSTERING = "clustering";

	private String type;
	private Timestamp timestamp;
	private boolean consumed;

	private transient IndexContext indexContext;

	public String getType() {
		return type;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public boolean isConsumed() {
		return consumed;
	}

	public String toString() {
		final StringBuilder builder = new StringBuilder("[");
		builder.append(getId()).append(",").append(getType()).append(".").append(getTimestamp());
		builder.append("]");
		return builder.toString();
	}

	public void setConsumed(final boolean consumed) {
		this.consumed = consumed;
	}

	@Transient
	public IndexContext getIndexContext() {
		return indexContext;
	}

	public void setIndexContext(final IndexContext indexContext) {
		this.indexContext = indexContext;
	}

}
