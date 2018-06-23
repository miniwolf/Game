package mini.app;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ApplicationTask<V> implements Future<V> {
    private final ReentrantLock stateLock = new ReentrantLock();
    private final Condition finishedCondition = stateLock.newCondition();
    private final Callable<V> callable;
    private boolean cancelled, finished;
    private V result;
    private ExecutionException exception;

    public ApplicationTask(Callable<V> callable) {
        this.callable = callable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        stateLock.lock();
        try {
            if (result != null) {
                return false;
            }
            cancelled = true;
            finishedCondition.signalAll();
            return true;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isCancelled() {
        stateLock.lock();
        try {
            return cancelled;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public boolean isDone() {
        stateLock.lock();
        try {
            return finished || cancelled || (exception != null);
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        stateLock.lock();
        try {
            while (!isDone()) {
                finishedCondition.await();
            }
            if (exception != null) {
                throw exception;
            }
            return result;
        } finally {
            stateLock.unlock();
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        stateLock.lock();
        try {
            while (!isDone()) {
                finishedCondition.await(timeout, unit);
            }
            if (exception != null) {
                throw exception;
            }
            if (result == null) {
                throw new TimeoutException("Object not returned within time");
            }
            return result;
        } finally {
            stateLock.unlock();
        }
    }

    public void invoke() {
        try {
            V tempResult = callable.call();
            stateLock.lock();
            try {
                result = tempResult;
                finished = true;
                finishedCondition.signalAll();
            } finally {
                stateLock.unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
            stateLock.lock();
            try {
                exception = new ExecutionException(e);
                finishedCondition.signalAll();
            } finally {
                stateLock.unlock();
            }
        }
    }
}
