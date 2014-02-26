package ikube.analytics;

import ikube.cluster.IClusterManager;
import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import ikube.toolkit.Timer;
import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * This class is implemented as a state pattern. The user specifies the type of analyzer, and the
 * service 'connects' to the correct implementation and executes the analysis logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
public class AnalyticsService<I, O, C> implements IAnalyticsService<I, O, C>, BeanPostProcessor {

    static {
        // We register a converter for the Bean utils so it
        // doesn't complain when the value is null
        ConvertUtils.register(new Converter() {
            @Override
            public Object convert(final Class type, final Object value) {
                return value;
            }
        }, Timestamp.class);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsService.class);

    @Autowired
    private IClusterManager clusterManager;
    private Map<String, Context> contexts = new HashMap<>();
    private Map<String, IAnalyzer> analyzers = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> create(final Context context) {
        List<Future<?>> futures = clusterManager.sendTaskToAll(new Callable() {
            @Override
            public Object call() throws Exception {
                Map<String, Context> contexts = getContexts();
                Map<String, IAnalyzer> analyzers = getAnalyzers();

                // Instantiate the classifier, the algorithm and the filter
                Object algorithmName = context.getAlgorithm();
                Object analyzerName = context.getAnalyzer();
                Object filterName = context.getFilter();
                context.setAlgorithm(Class.forName(String.valueOf(algorithmName)).newInstance());
                context.setAnalyzer(Class.forName(String.valueOf(analyzerName)).newInstance());
                if (filterName != null && !StringUtils.isEmpty(String.valueOf(filterName))) {
                    context.setFilter(Class.forName(String.valueOf(filterName)).newInstance());
                }

                IAnalyzer<I, O, C> analyzer = (IAnalyzer<I, O, C>) AnalyzerManager.buildAnalyzer(context);
                contexts.put(context.getName(), context);
                analyzers.put(context.getName(), analyzer);
                return analyzer;
            }
        });
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(context.getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> train(final Analysis<I, O> analysis) {
        List<Future<?>> futures = clusterManager.sendTaskToAll(new Callable() {
            @Override
            public Object call() throws Exception {
                IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
                analyzer.train((I) analysis);
                return analyzer;
            }
        });
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> build(final Analysis<I, O> analysis) {
        List<Future<?>> futures = clusterManager.sendTaskToAll(new Callable() {
            @Override
            public Object call() throws Exception {
                Context context = getContext(analysis.getAnalyzer());
                IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
                analyzer.build(context);
                return analyzer;
            }
        });
        ThreadUtilities.waitForFutures(futures, 15);
        return getAnalyzer(analysis.getAnalyzer());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Analysis<I, O> analyze(final Analysis<I, O> analysis) {
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on the nodes
            Callable callable = new Callable<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public Object call() throws Exception {
                    IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
                    return analyzer.analyze((I) analysis);
                }
            };
            Future<?> future = clusterManager.sendTask(callable);
            ThreadUtilities.waitForFuture(future, 60);
            try {
                Analysis<I, O> result = (Analysis<I, O>) future.get();
                BeanUtilsBean2.getInstance().copyProperties(analysis, result);
            } catch (final Exception e) {
                LOGGER.error("Exception getting the result from the distributed task : ", e);
            }
        } else {
            Timer.Timed timed = new Timer.Timed() {
                @Override
                @SuppressWarnings("unchecked")
                public void execute() {
                    IAnalyzer<I, O, C> analyzer = getAnalyzer(analysis.getAnalyzer());
                    try {
                        analyzer.analyze((I) analysis);
                    } catch (final Exception e) {
                        analysis.setException(e);
                        throw new RuntimeException("Exception analyzing data : " + analyzer, e);
                    }
                }
            };
            execute(timed, analysis);
        }
        return analysis;
    }

    @SuppressWarnings("unchecked")
    public Analysis classesOrClusters(final Analysis<I, O> analysis) {
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on the remote node
            Callable callable = new Callable<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public Object call() throws Exception {
                    IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
                    Object[] classesOrClusters = analyzer.classesOrClusters();
                    analysis.setClassesOrClusters(classesOrClusters);
                    return analysis;
                }
            };
            Future<?> future = clusterManager.sendTask(callable);
            ThreadUtilities.waitForFuture(future, 60);
            try {
                Analysis<I, O> result = (Analysis<I, O>) future.get();
                BeanUtilsBean2.getInstance().copyProperties(analysis, result);
            } catch (final Exception e) {
                LOGGER.error("Exception getting the result from the distributed task : ", e);
            }
        } else {
            Timer.Timed timed = new Timer.Timed() {
                @Override
                @SuppressWarnings("unchecked")
                public void execute() {
                    IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
                    try {
                        Object[] classesOrClusters = analyzer.classesOrClusters();
                        analysis.setClassesOrClusters(classesOrClusters);
                    } catch (final Exception e) {
                        throw new RuntimeException("Exception getting the class of cluster for analysis : ", e);
                    }
                }
            };
            execute(timed, analysis);
        }
        return analysis;
    }

    @SuppressWarnings("unchecked")
    public Analysis<I, O> sizesForClassesOrClusters(final Analysis<I, O> analysis) {
        if (analysis.isDistributed()) {
            // Set the flag so we don't get infinite recursion
            analysis.setDistributed(Boolean.FALSE);
            // Create the callable that will be executed on the remote node
            Callable callable = new Callable<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public Object call() throws Exception {
                    String clazz = analysis.getClazz();
                    classesOrClusters(analysis);
                    Object[] classesOrClusters = analysis.getClassesOrClusters();
                    int[] sizesForClassesOrClusters = new int[analysis.getClassesOrClusters().length];
                    IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
                    for (int i = 0; i < classesOrClusters.length; i++) {
                        analysis.setClazz(classesOrClusters[i].toString());
                        int sizeForClass = analyzer.sizeForClassOrCluster(analysis);
                        sizesForClassesOrClusters[i] = sizeForClass;
                    }
                    analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
                    analysis.setClazz(clazz);
                    return analysis;
                }
            };
            Future<?> future = clusterManager.sendTask(callable);
            ThreadUtilities.waitForFuture(future, 60);
            try {
                Analysis<I, O> result = (Analysis<I, O>) future.get();
                BeanUtilsBean2.getInstance().copyProperties(analysis, result);
            } catch (final Exception e) {
                LOGGER.error("Exception getting the result from the distributed task : ", e);
            }
        } else {
            Timer.Timed timed = new Timer.Timed() {
                @Override
                @SuppressWarnings("unchecked")
                public void execute() {
                    try {
                        String clazz = analysis.getClazz();
                        classesOrClusters(analysis);
                        Object[] classesOrClusters = analysis.getClassesOrClusters();
                        int[] sizesForClassesOrClusters = new int[analysis.getClassesOrClusters().length];
                        IAnalyzer analyzer = getAnalyzer(analysis.getAnalyzer());
                        for (int i = 0; i < classesOrClusters.length; i++) {
                            analysis.setClazz(classesOrClusters[i].toString());
                            int sizeForClass = analyzer.sizeForClassOrCluster(analysis);
                            sizesForClassesOrClusters[i] = sizeForClass;
                        }
                        analysis.setSizesForClassesOrClusters(sizesForClassesOrClusters);
                        analysis.setClazz(clazz);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            execute(timed, analysis);
        }
        return analysis;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IAnalyzer<I, O, C> destroy(final Context context) {
        // Create the callable that will be executed on the remote node
        Callable callable =     new Callable<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public Object call() throws Exception {
                Map<String, Context> contexts = getContexts();
                Map<String, IAnalyzer> analyzers = getAnalyzers();

                contexts.remove(context.getName());
                IAnalyzer<I, O, C> analyzer = analyzers.remove(context.getName());
                if (analyzer != null) {
                    try {
                        analyzer.destroy(context);
                    } catch (Exception e) {
                        throw new RuntimeException("Exception destroying analyzer : " + context, e);
                    }
                }
                return analyzer;
            }
        };
        Future<?> future = clusterManager.sendTask(callable);
        ThreadUtilities.waitForFuture(future, 60);
        return getAnalyzer(context.getName());
    }

    private void execute(final Timer.Timed timed, final Analysis analysis) {
        double duration = Timer.execute(timed);
        analysis.setDuration(duration);
        analysis.setTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        // We collect all the analyzers that have been defined in the configuration here
        if (IAnalyzer.class.isAssignableFrom(bean.getClass())) {
            analyzers.put(beanName, (IAnalyzer) bean);
        } else if (Context.class.isAssignableFrom(bean.getClass())) {
            contexts.put(beanName, (Context) bean);
        }
        return bean;
    }

    @SuppressWarnings("unchecked")
    IAnalyzer<I, O, C> getAnalyzer(final String analyzerName) {
        Map<String, IAnalyzer> analyzers = getAnalyzers();
        IAnalyzer<?, ?, ?> analyzer = analyzers.get(analyzerName);
        return (IAnalyzer<I, O, C>) analyzer;
    }

    @Override
    public Context getContext(final String analyzerName) {
        IAnalyzer analyzer = getAnalyzer(analyzerName);
        for (final Map.Entry<String, Context> mapEntry : contexts.entrySet()) {
            if (mapEntry.getValue().getAnalyzer().equals(analyzer)) {
                return mapEntry.getValue();
            }
        }
        throw new RuntimeException("Couldn't find context for analyzer : " + analyzerName + ", " + analyzer);
    }

    @Override
    public Map<String, IAnalyzer> getAnalyzers() {
        return analyzers;
    }

    public Map<String, Context> getContexts() {
        return contexts;
    }
}