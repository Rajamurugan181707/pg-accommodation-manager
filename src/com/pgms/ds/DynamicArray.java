package com.pgms.ds;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DS CONCEPT: ARRAY.
 *
 * A growable array written by hand (no ArrayList), used everywhere the app
 * keeps a list of rooms, tenants or payments in memory.
 *
 *   add          O(1) on average  - doubles the backing array when it is full
 *   get / set    O(1)             - direct index into the array
 *   removeAt     O(n)             - shifts the later elements one place left
 *   indexOf      O(n)             - linear search
 *   sort         O(n^2)           - insertion sort (fine for a PG's few hundred rows)
 *   binarySearch O(log n)         - only on an array already sorted by the same key
 */
public class DynamicArray<T> implements Iterable<T> {

    private Object[] data;
    private int size;

    public DynamicArray() {
        this(8);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity < 1) {
            initialCapacity = 1;
        }
        data = new Object[initialCapacity];
    }

    public void add(T item) {
        if (size == data.length) {
            grow();
        }
        data[size++] = item;
    }

    /** Copy every element into an array twice as big. */
    private void grow() {
        Object[] bigger = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            bigger[i] = data[i];
        }
        data = bigger;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkIndex(index);
        return (T) data[index];
    }

    public void set(int index, T item) {
        checkIndex(index);
        data[index] = item;
    }

    /** Remove the element at index and close the gap. */
    public T removeAt(int index) {
        T removed = get(index);
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[--size] = null;
        return removed;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Linear search: index of the first element that matches, or -1. */
    public int indexOf(Matcher<T> matcher) {
        for (int i = 0; i < size; i++) {
            if (matcher.matches(get(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Linear search that keeps every match, in a new array. */
    public DynamicArray<T> filter(Matcher<T> matcher) {
        DynamicArray<T> result = new DynamicArray<T>();
        for (int i = 0; i < size; i++) {
            T item = get(i);
            if (matcher.matches(item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Insertion sort, in place. Stable, so equal items keep their order. */
    public void sort(Comparator<? super T> comparator) {
        for (int i = 1; i < size; i++) {
            T key = get(i);
            int j = i - 1;
            while (j >= 0 && comparator.compare(get(j), key) > 0) {
                data[j + 1] = data[j];
                j--;
            }
            data[j + 1] = key;
        }
    }

    /**
     * Binary search. The array must already be sorted by the same key.
     * probe.compareToKey(element) returns negative if the element is before
     * the key, 0 if it is the key, positive if it is after the key.
     * Returns the index found, or -1.
     */
    public int binarySearch(KeyProbe<T> probe) {
        int low = 0;
        int high = size - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = probe.compareToKey(get(mid));
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int next = 0;

            @Override
            public boolean hasNext() {
                return next < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return get(next++);
            }
        };
    }

    /** Test used by indexOf and filter. */
    public interface Matcher<T> {
        boolean matches(T item);
    }

    /** Comparison used by binarySearch. */
    public interface KeyProbe<T> {
        int compareToKey(T item);
    }
}
