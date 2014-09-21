package ikube.analytics.weka;

import ikube.toolkit.CsvFileTools;
import ikube.toolkit.Timer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.*;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

import static ikube.toolkit.FileUtilities.getOrCreateFile;
import static ikube.toolkit.MatrixUtilities.objectVectorToDoubleVector;
import static ikube.toolkit.MatrixUtilities.objectVectorToStringVector;

/**
 * This class contains general methods for manipulating the Weka data, and for writing
 * the models to the file system.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public final class WekaToolkit {

    private static final Logger LOGGER = LoggerFactory.getLogger(WekaToolkit.class);

    /**
     * Writes the instances to a file that can be loaded again and used to train algorithms.
     *
     * @param instances the instances data to write to the file
     * @param filePath  the absolute path to the output file
     */
    public static void writeToArff(final Instances instances, final String filePath) {
        try {
            ArffSaver arffSaver = new ArffSaver();
            arffSaver.setInstances(instances);
            File file = getOrCreateFile(filePath);
            arffSaver.setFile(file);
            arffSaver.writeBatch();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method converts and instances object to a sparse instance, which will then
     * be more memory efficient.
     *
     * @param instances the instances to convert to a sparse instances model
     * @return the sparse instances, more memory efficient
     */
    public static Instances nonSparseToSparse(final Instances instances) {
        try {
            NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
            nonSparseToSparseInstance.setInputFormat(instances);
            return Filter.useFilter(instances, nonSparseToSparseInstance);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is a convenience method to load a pure csv file and create an instances object that Weka
     * can use. This would typically be used in an environment where there are missing values in the input
     * data and the Weka loader doesn't like that.
     *
     * @param filePath   the path to the file to load the data from
     * @param classIndex the class index that is missing, can be -1 if there is no known missing attribute,
     *                   and the missing attribute will be set to 0, i.e. the first one, which is the default
     * @return the instances object created from the input, data, with the same number of attributed labeled as
     * the input data has vector lengths
     */
    public static Instances csvFileToInstances(final String filePath, final int classIndex, final Class<?> type) {
        Object[][] matrix;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(filePath));
            matrix = new CsvFileTools().getCsvData(inputStream);
            return matrixToInstances(matrix, classIndex, type);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * This method will convert a matrix to a 'flat' {@link weka.core.Instances} object. Note that the
     * matrix will be converted where necessary to a double vector to comply with the input requirements
     * of the Weka algorithms, or string depending on the type in the parameter list.
     *
     * @param matrix     the matrix to convert into an instances object, or data set for Weka
     * @param classIndex the class index or index o the attribute that is 'missing' or to be predicted, if this is
     *                   set to Integer.MAX_VALUE then the last attribute will be used as the class index
     * @param type       the type of attributes to use in the instances, either double or string
     * @return the instances object created from the matrix, with all the attributes doubles or strings, ready for processing
     */
    public static Instances matrixToInstances(final Object[][] matrix, final int classIndex, final Class<?> type) {
        // Create the instances from the matrix data
        FastVector attributes = new FastVector();
        // Add the attributes to the data set
        for (int i = 0; i < matrix[0].length; i++) {
            attributes.addElement(getAttribute(i, type));
        }
        // Create the instances data set from the data and the attributes
        Instances instances = new Instances("instances", attributes, 0);
        instances.setClass(instances.attribute(classIndex));
        // Populate the instances
        for (final Object[] vector : matrix) {
            instances.add(getInstance(instances, vector, type));
        }
        return instances;
    }

    private static Attribute getAttribute(final int index, final Class<?> type) {
        if (Double.class.isAssignableFrom(type)) {
            return new Attribute(Integer.toString(index));
        } else if (String.class.isAssignableFrom(type)) {
            return new Attribute(Integer.toString(index), (FastVector) null);
        } else {
            throw new RuntimeException("Attribute type not supported : " + type);
        }
    }

    private static Instance getInstance(final Instances instances, final Object[] vector, final Class<?> type) {
        if (Double.class.isAssignableFrom(type)) {
            double[] doubleVector = objectVectorToDoubleVector(vector);
            return new Instance(1.0, doubleVector);
        } else if (String.class.isAssignableFrom(type)) {
            String[] stringVector = objectVectorToStringVector(vector);
            Instance instance = new Instance(stringVector.length);
            for (int i = 0; i < instances.numAttributes(); i++) {
                Attribute attribute = instances.attribute(i);
                String value = stringVector[i];
                instance.setValue(attribute, value);
            }
            return instance;
        } else {
            throw new RuntimeException("Attribute type not supported : " + type);
        }
    }

    /**
     * This method applies multiple filters on the {@link weka.core.Instances} object, hopefully
     * replacing missing values, converting to numbers and so on.
     *
     * @param instances the instances data set to apply the filters on, converting and returning a new instances object potentially
     * @param filters   the filters to be applied to the data in the instances object
     * @return a filtered instances, potentially a new instance
     * @throws Exception
     */
    public static synchronized Instances filter(final Instances instances, final Filter... filters) throws Exception {
        if (filters == null || filters.length == 0) {
            return instances;
        }
        Instances filteredInstances = instances;
        for (final Filter filter : filters) {
            if (filter != null) {
                filter.setInputFormat(filteredInstances);
                filteredInstances = Filter.useFilter(filteredInstances, filter);
            }
        }
        return filteredInstances;
    }

    /**
     * This method will cross train a classifier using the number of folds, and then evaluate the model. Note
     * that cross validation is expensive, and with a million vectors can take several hours on a single thread.
     *
     * @param classifier the classifier to cross validate
     * @param instances  the instances to be used for the cross validation
     * @param folds      the number of folds to cross validate the model
     * @return the error rate of the cross validation
     * @throws Exception
     */
    public static double crossValidate(final Classifier classifier, final Instances instances, final int folds) throws Exception {
        final Evaluation evaluation = new Evaluation(instances);
        final StringBuffer predictionsOutput = new StringBuffer();
        double duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    evaluation.crossValidateModel(classifier, instances, folds, new Random(), predictionsOutput, new Range(), true);
                } catch (final Exception e) {
                    LOGGER.error("Exception cross validating the classifier : ", e);
                }
            }
        });
        LOGGER.warn("Duration for cross validation : " + duration);

        duration = Timer.execute(new Timer.Timed() {
            @Override
            public void execute() {
                try {
                    evaluation.evaluateModel(classifier, instances);
                } catch (final Exception e) {
                    LOGGER.error("Exception evaluating the classifier : ", e);
                }
            }
        });
        LOGGER.warn("Duration for model evaluation : " + duration);

        return evaluation.relativeAbsoluteError();
    }

    private WekaToolkit() {
    }

}
