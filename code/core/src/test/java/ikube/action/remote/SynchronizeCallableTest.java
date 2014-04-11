package ikube.action.remote;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05-04-2014
 */
public class SynchronizeCallableTest extends AbstractTest {

    @After
    public void after() {
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void call() throws Exception {
        String[] strings = {"create an", "index with", "something in it"};
        File indexDirectory = createIndexFileSystem(indexContext, System.currentTimeMillis(), ip, strings);

        final List<String> filePaths = new ArrayList<>();
        Files.walkFileTree(indexDirectory.toPath(), new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(final Path path, BasicFileAttributes attrs) {
                File file = path.toFile();
                if (file.isFile()) {
                    filePaths.add(file.getAbsolutePath());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        for (final String indexFile : filePaths) {
            logger.info("Reading file : " + indexFile);
            int offset = 0;
            long length = 0;
            do {
                SynchronizeCallable synchronizeCallable = new SynchronizeCallable(indexFile, offset, 1024);
                byte[] chunk = synchronizeCallable.call();
                logger.info("Chunk : " + chunk.length);
                offset = chunk.length;
                length += chunk.length;
            } while (offset > 0);
            assertEquals("The chunks read must be the same length as the original file : ", new File(indexFile).length(), length);
        }
    }

}