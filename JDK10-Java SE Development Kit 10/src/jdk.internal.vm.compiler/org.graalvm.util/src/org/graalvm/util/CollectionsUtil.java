/*
 * Copyright (c) 2017, 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */
package org.graalvm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class contains utility methods for commonly used functional patterns for collections.
 */
public class CollectionsUtil {

    /**
     * Concatenates two iterables into a single iterable. The iterator exposed by the returned
     * iterable does not support {@link Iterator#remove()} even if the input iterables do.
     *
     * @throws NullPointerException if {@code a} or {@code b} is {@code null}
     */
    public static <T> Iterable<T> concat(Iterable<T> a, Iterable<T> b) {
        List<Iterable<T>> l = Arrays.asList(a, b);
        return concat(l);
    }

    /**
     * Concatenates multiple iterables into a single iterable. The iterator exposed by the returned
     * iterable does not support {@link Iterator#remove()} even if the input iterables do.
     *
     * @throws NullPointerException if {@code iterables} or any of its elements are {@code null}
     */
    public static <T> Iterable<T> concat(List<Iterable<T>> iterables) {
        for (Iterable<T> iterable : iterables) {
            Objects.requireNonNull(iterable);
        }
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                if (iterables.size() == 0) {
                    return Collections.emptyIterator();
                }
                return new Iterator<T>() {
                    Iterator<Iterable<T>> cursor = iterables.iterator();
                    Iterator<T> currentIterator = cursor.next().iterator();

                    private void advance() {
                        while (!currentIterator.hasNext() && cursor.hasNext()) {
                            currentIterator = cursor.next().iterator();
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        advance();
                        return currentIterator.hasNext();
                    }

                    @Override
                    public T next() {
                        advance();
                        return currentIterator.next();
                    }
                };
            }

        };
    }

    public static <T> boolean allMatch(T[] inputs, Predicate<T> predicate) {
        return allMatch(Arrays.asList(inputs), predicate);
    }

    public static <T> boolean allMatch(Iterable<T> inputs, Predicate<T> predicate) {
        for (T t : inputs) {
            if (!predicate.test(t)) {
                return false;
            }
        }
        return true;
    }

    public static <T> boolean anyMatch(T[] inputs, Predicate<T> predicate) {
        return anyMatch(Arrays.asList(inputs), predicate);
    }

    public static <T> boolean anyMatch(Iterable<T> inputs, Predicate<T> predicate) {
        for (T t : inputs) {
            if (predicate.test(t)) {
                return true;
            }
        }
        return false;
    }

    public static <T> List<T> filterToList(List<T> inputs, Predicate<? super T> predicate) {
        return filterToList(inputs, predicate, ArrayList::new);
    }

    public static <T> List<T> filterToList(List<T> inputs, Predicate<? super T> predicate, Supplier<List<T>> listGenerator) {
        List<T> resultList = listGenerator.get();
        for (T t : inputs) {
            if (predicate.test(t)) {
                resultList.add(t);
            }
        }
        return resultList;
    }

    /**
     * Filters the inputs, maps them given the mapping function and adds them in the array provided
     * by the generator.
     */
    public static <T, R> R[] filterAndMapToArray(T[] inputs, Predicate<? super T> predicate, Function<? super T, ? extends R> mapper, IntFunction<R[]> arrayGenerator) {
        List<R> resultList = new ArrayList<>();
        for (T t : inputs) {
            if (predicate.test(t)) {
                resultList.add(mapper.apply(t));
            }
        }
        return resultList.toArray(arrayGenerator.apply(resultList.size()));
    }

    /**
     * Maps the inputs given the mapping function and adds them in the array provided by the
     * generator.
     */
    public static <T, R> R[] mapToArray(T[] inputs, Function<? super T, ? extends R> mapper, IntFunction<R[]> arrayGenerator) {
        return mapToArray(Arrays.asList(inputs), mapper, arrayGenerator);
    }

    public static <T, R> R[] mapToArray(Collection<T> inputs, Function<? super T, ? extends R> mapper, IntFunction<R[]> arrayGenerator) {
        R[] result = arrayGenerator.apply(inputs.size());
        int idx = 0;
        for (T t : inputs) {
            result[idx++] = mapper.apply(t);
        }
        return result;
    }

    public static <T, R> String mapAndJoin(T[] inputs, Function<? super T, ? extends R> mapper, String delimiter) {
        return mapAndJoin(Arrays.asList(inputs), mapper, delimiter, "", "");
    }

    public static <T, R> String mapAndJoin(T[] inputs, Function<? super T, ? extends R> mapper, String delimiter, String prefix) {
        return mapAndJoin(Arrays.asList(inputs), mapper, delimiter, prefix, "");
    }

    public static <T, R> String mapAndJoin(T[] inputs, Function<? super T, ? extends R> mapper, String delimiter, String prefix, String suffix) {
        return mapAndJoin(Arrays.asList(inputs), mapper, delimiter, prefix, suffix);
    }

    public static <T, R> String mapAndJoin(Iterable<T> inputs, Function<? super T, ? extends R> mapper, String delimiter) {
        return mapAndJoin(inputs, mapper, delimiter, "", "");
    }

    public static <T, R> String mapAndJoin(Iterable<T> inputs, Function<? super T, ? extends R> mapper, String delimiter, String prefix) {
        return mapAndJoin(inputs, mapper, delimiter, prefix, "");
    }

    public static <T, R> String mapAndJoin(Iterable<T> inputs, Function<? super T, ? extends R> mapper, String delimiter, String prefix, String suffix) {
        StringBuilder strb = new StringBuilder();
        String sep = "";
        for (T t : inputs) {
            strb.append(sep).append(prefix).append(mapper.apply(t)).append(suffix);
            sep = delimiter;
        }
        return strb.toString();
    }
}
