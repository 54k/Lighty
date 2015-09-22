package io.lighty.concurrent;

@FunctionalInterface
public interface FutureListener<V extends Future<?>> {

    void onComplete(V future);
}
