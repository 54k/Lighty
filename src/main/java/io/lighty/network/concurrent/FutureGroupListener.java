package io.lighty.network.concurrent;

@FunctionalInterface
public interface FutureGroupListener<V> extends FutureListener<FutureGroup<V>> {
}
