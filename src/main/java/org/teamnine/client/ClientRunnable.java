package org.teamnine.client;

public interface ClientRunnable<E extends Exception> extends Runnable {
    @Override
    default void run() throws RuntimeException {
        try {
            runThrowable();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    void runThrowable() throws E;
}
