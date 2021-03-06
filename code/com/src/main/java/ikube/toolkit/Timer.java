package ikube.toolkit;

import ikube.Constants;

/**
 * This is just a utility that will return t he time taken for a particular piece of logic.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-03-2013
 */
public final class Timer {

    /**
     * Implement this interface and put the logic to be timed in the execute method and feed it to the
     * {@link Timer#execute(Timed)} method, the return value will be the time taken in milliseconds for the
     * logic to be executed.
     *
     * @author Michael Couck
     * @version 01.00
     * @since 10-03-2013
     */
    public interface Timed {
        void execute();
    }

    /**
     * This method takes a timed object and executes the logic to be times, returning the
     * time taken for the execution to finish in milliseconds.
     *
     * @param timed the wrapper for the logic that is to be timed
     * @return the time taken for the logic to execute in milliseconds
     */
    public static double execute(final Timed timed) {
        long start = System.nanoTime();
        timed.execute();
        double duration = System.nanoTime() - start;
        return duration / Constants.MILLION;
    }

}
