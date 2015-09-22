package io.lighty.concurrent;

@FunctionalInterface
public interface FutureGroupListener<V> extends FutureListener<FutureGroup<V>> {
}
