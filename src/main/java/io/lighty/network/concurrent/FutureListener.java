package io.lighty.network.concurrent;

@FunctionalInterface
public interface FutureListener<V extends Future<?>> {

    void onComplete(V future);
}
