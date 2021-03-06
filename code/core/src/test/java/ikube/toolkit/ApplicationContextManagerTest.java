package ikube.toolkit;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.analytics.weka.WekaClusterer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 01-12-2012
 */
public class ApplicationContextManagerTest extends AbstractTest {

    @Before
    public void before() {
        File externalConfig = FILE.findDirectoryRecursively(new File("."), new String[] {"external"});
        File springConfig = FILE.findFileRecursively(externalConfig, "spring\\.xml");
        String springConfigPath = FILE.cleanFilePath(springConfig.getParentFile().getAbsolutePath());
        System.setProperty(IConstants.IKUBE_CONFIGURATION, springConfigPath);
    }

    @After
    public void after() {
        System.getProperties().remove(IConstants.IKUBE_CONFIGURATION);
    }

    @Test
    public void getApplicationContext() {
        ApplicationContext applicationContext = ApplicationContextManager.getApplicationContext();
        assertNotNull(applicationContext);
    }

    @Test
    public void setBean() {
        String name = "weka-analyzer";
        ApplicationContextManager.setBean(name, WekaClusterer.class.getName());
        Object wekaAnalyzer = ApplicationContextManager.getBean(name);
        assertNotNull(wekaAnalyzer);
    }

}
