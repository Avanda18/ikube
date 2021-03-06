package ikube.action;

import ikube.action.index.IndexManager;
import ikube.action.rule.DirectoryExistsAndIsLocked;
import ikube.model.IndexContext;
import ikube.toolkit.FILE;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * This action deletes the old indexes. There should always be one index, and potentially an
 * index that is being generated. Any other index files should be deleted. This class will also
 * delete the old indexes that are backups.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 31-10-2010
 */
public class Delete extends Action<IndexContext, Boolean> {

    /**
     * {@inheritDoc}
     */
    @Override
    boolean internalExecute(final IndexContext indexContext) {
        String indexDirectoryPath = IndexManager.getIndexDirectoryPath(indexContext);
        String indexDirectoryPathBackup = IndexManager.getIndexDirectoryPathBackup(indexContext);
        boolean deletedOldIndexes = deleteOldIndexes(indexDirectoryPath);
        boolean deletedOldBackupIndexes = deleteOldIndexes(indexDirectoryPathBackup);
        return deletedOldIndexes | deletedOldBackupIndexes;
    }

    private boolean deleteOldIndexes(final String indexDirectoryPath) {
        File baseIndexDirectory = FILE.getFile(indexDirectoryPath, Boolean.TRUE);
        File[] timeIndexDirectories = baseIndexDirectory.listFiles();
        if (timeIndexDirectories == null || timeIndexDirectories.length <= 2) {
            return Boolean.FALSE;
        }
        Arrays.sort(timeIndexDirectories, new Comparator<File>() {
            @Override
            public int compare(final File fileOne, final File fileTwo) {
                return fileOne.getName().compareTo(fileTwo.getName());
            }
        });
        // Check if the last index directory is locked
        boolean latestIndexDirectoryIsLocked = Boolean.FALSE;
        File latestIndexDirectory = IndexManager.getLatestIndexDirectory(indexDirectoryPath);
        File[] serverIndexDirectories = latestIndexDirectory.listFiles();
        if (serverIndexDirectories != null) {
            for (final File serverIndexDirectory : serverIndexDirectories) {
                if (new DirectoryExistsAndIsLocked().evaluate(serverIndexDirectory)) {
                    latestIndexDirectoryIsLocked = Boolean.TRUE;
                    break;
                }
            }
            int indexesToRemain = latestIndexDirectoryIsLocked ? 2 : 1;
            // We delete all the indexes except the last one, i.e. the latest index.
            // In the case that there is an index running then the latest index directory will
            // be locked, in that case we leave the last two indexes, i.e. the the latest index
            // and the index being created
            int indexesToDelete = timeIndexDirectories.length - indexesToRemain;
            if (indexesToDelete == 0) {
                return Boolean.FALSE;
            }
            for (int i = 0; i < indexesToDelete; i++) {
                File indexToDelete = timeIndexDirectories[i];
                logger.info("Deleting index directory : " + indexToDelete.getAbsolutePath());
                boolean deleted = FILE.deleteFile(indexToDelete, 1);
                if (!deleted) {
                    logger.warn("Didn't delete directory : " + indexToDelete.getAbsolutePath());
                }
            }
        }
        return Boolean.TRUE;
    }

}