package ikube.cluster;

import ikube.IConstants;

import org.apache.log4j.Logger;
import org.jgroups.JChannel;

public abstract class Service {

	protected static final long SLEEP = 5000;
	protected static final String CONFIGURATION = "META-INF/cluster/mping.xml";

	protected Logger logger = Logger.getLogger(this.getClass());

	protected static JChannel CHANNEL;
	static {
		try {
			CHANNEL = new JChannel(CONFIGURATION);
			CHANNEL.connect(IConstants.IKUBE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
