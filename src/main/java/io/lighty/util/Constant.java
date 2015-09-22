package io.lighty.util;

public interface Constant<T extends Constant<T>> extends Comparable<Constant<T>> {

    int id();

    String name();
}
