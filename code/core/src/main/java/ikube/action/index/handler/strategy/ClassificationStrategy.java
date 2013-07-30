package ikube.action.index.handler.strategy;

import static ikube.IConstants.CLASSIFICATION;
import static ikube.IConstants.CLASSIFICATION_CONFLICT;
import static ikube.action.index.IndexManager.addStringField;
import ikube.IConstants;
import ikube.action.index.handler.IStrategy;
import ikube.analytics.FeatureExtractor;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.toolkit.Timer;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import libsvm.LibSVM;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;

/**
 * @author Michael Couck
 * @since 07.07.13
 * @version 01.00
 */
public class ClassificationStrategy extends AStrategy {

	private int maxTraining = 1000000;

	private LibSVM libSvm;
	private Dataset dataset;
	private FeatureExtractor featureExtractor;
	private Lock lock;

	public ClassificationStrategy() {
		this(null);
	}

	public ClassificationStrategy(final IStrategy nextStrategy) {
		super(nextStrategy);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws IOException
	 */
	@Override
	public void initialize() {
		libSvm = new LibSVM();
		featureExtractor = new FeatureExtractor();
		lock = new ReentrantLock();
		try {
			String content = "The news is broardcast every day";
			double[] featureVector = featureExtractor.extractFeatures(content, content);
			Instance instance = new SparseInstance(featureVector, IConstants.NEUTRAL);
			dataset = new DefaultDataset(Arrays.asList(instance));
			libSvm.buildClassifier(dataset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean aroundProcess(final IndexContext<?> indexContext, final Indexable<?> indexable, final Document document, final Object resource)
			throws Exception {
		// TODO Perhaps detect the subject and the object. Separate the constructs of the sentence for further processing
		String content = indexable.getContent() != null ? indexable.getContent().toString() : resource != null ? resource.toString() : null;
		if (content != null) {
			// If this data is already classified by another strategy then maxTraining the language
			// classifiers on the data. We can then also classify the data and correlate the results
			String previousClassification = document.get(CLASSIFICATION);
			String currentClassification = detectSentiment(content);
			if (StringUtils.isEmpty(previousClassification)) {
				// Not analyzed so add the sentiment that we get
				addStringField(CLASSIFICATION, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
			} else {
				// We only train if we have had this tweet classified already
				train(previousClassification, content);
				if (!previousClassification.contains(currentClassification)) {
					// We don't change the original analysis, do we?
					addStringField(CLASSIFICATION_CONFLICT, currentClassification, document, Store.YES, Index.ANALYZED, TermVector.NO);
				}
			}
		}
		return super.aroundProcess(indexContext, indexable, document, resource);
	}

	public String detectSentiment(final String content) {
		double[] features;
		try {
			features = featureExtractor.extractFeatures(content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Instance instance = new SparseInstance(features);
		return libSvm.classify(instance).toString();
	}

	void train(final String category, final String content) {
		if (maxTraining == 0) {
			maxTraining--;
			// TODO Persist the data sets in the classifier
		} else if (maxTraining > 0) {
			double[] features;
			try {
				features = featureExtractor.extractFeatures(content, content);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			try {
				if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
					Instance instance = new SparseInstance(features, category);
					dataset.add(instance);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}

			if (dataset.size() % 1000 == 0) {
				logger.info("Building classifier : " + dataset.size());
				long duration = Timer.execute(new Timer.Timed() {
					@Override
					public void execute() {
						Dataset newDataset = null;
						try {
							if (lock.tryLock(1000, TimeUnit.MILLISECONDS)) {
								newDataset = dataset.copy();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							lock.unlock();
						}
						if (newDataset != null) {
							LibSVM newLibSvm = new LibSVM();
							newLibSvm.buildClassifier(newDataset);
							libSvm = newLibSvm;
							newLibSvm = null;
						}
					}
				});
				logger.info("Built classifier in : " + duration);
			}

		}
	}

}