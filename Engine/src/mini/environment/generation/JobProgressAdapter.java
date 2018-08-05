package mini.environment.generation;

/**
 * Abstract Adapter class that implements optional methods of JobProgressListener. Extends this
 * class instead of implementing a JobProgressListener if you need only a subset of method
 * implemented.
 */
public abstract class JobProgressAdapter<T> implements JobProgressListener<T> {
    @Override
    public void start() {
    }

    @Override
    public void step(String message) {
    }

    @Override
    public void progress(double value) {
    }

    @Override
    public abstract void done(T result);
}
