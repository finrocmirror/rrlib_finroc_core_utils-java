//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc;

import java.util.Arrays;

/**
 * Wraps an array.
 * The wrapper keeps track of the array's 'capacity' and the number
 * of elements it currently contains ('size').
 * Usually, 'size' and 'capacity' are identical.
 * In C++, bounds checking is done via asserts (=> can be (de)activated).
 *
 * To efficiently iterate over the array, this code should be used:
 *
 *      Java:
 *       ArrayWrapper<T> iterable = ...;
 *       for (int i = 0, n = iterable.size(); i < n; i++) {
 *          ... iterable.get(i)
 *       }
 *
 *      C++:
 *       ArrayWrapper<T> iterable = ...;
 *       for (size_t i = 0, n = iterable->size(); i < n; i++) {
 *          ... iterable.get(i)  OR  ... iterable[i]
 *       }
 */
public class ArrayWrapper<T> {

    /** size of array... may be smaller than backend capacity/length */
    volatile private int size;

    /** backend */
    private final T[] backend;

    /** Universal Empty Array Wrapper */
    @SuppressWarnings("rawtypes")
    private static final ArrayWrapper EMPTY = new ArrayWrapper(0, 0);

    /**
     * @param backend backend
     * @param size size of array... may be smaller than backend capacity
     */
    public ArrayWrapper(T[] backend, int size) {
        this.backend = backend;
        this.size = size;
    }

    /**
     * @return Empty Array Wrapper
     */
    @SuppressWarnings("rawtypes")
    public static ArrayWrapper getEmpty() {
        return EMPTY;
    }

    /**
     * @param capacity Array capacity and size
     */
    public ArrayWrapper(int capacity) {
        this(capacity, capacity);
    }

    /**
     * @param size size of array... may be smaller than backend capacity
     */
    @SuppressWarnings("unchecked")
    public ArrayWrapper(int size, int capacity) {
        assert size <= capacity;
        this.backend = (T[])new Object[capacity];
    }

    public T get(int index) {
        return backend[index];
    }

    public int size() {
        return size;
    }

    /**
     * @return Array Capacity
     */
    public int getCapacity() {
        return backend.length;
    }

    /**
     * Add element to array (will succeed if capacity
     * is not fully used yet)
     * (not thread safe)
     *
     * @param element Element to add
     */
    public void add(T element) {
        backend[size] = element;
        size++;
    }

    public void set(int index, T value) {
        backend[index] = value;
    }

    /**
     * @return Backend
     */
    public T[] getBackend() {
        return backend;
    }

    /**
     * @return Is there free capacity in the array?
     */
    public boolean freeCapacity() {
        return size < getCapacity();
    }

    /**
     * Fill array (whole capacity) with specified value
     *
     * @param value Value to fill array with
     */
    public void fill(T value) {
        for (int i = 0; i < backend.length; i++) {
            set(i, value);
        }
    }

    /**
     * Clear array contents
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            set(i, null);
        }
        size = 0;
    }

    /**
     * Clear array contents and delete objects in Array
     * (should only be used when using array with raw pointers)
     */
    public void clearAndDelete() {
        clear();
    }

    /**
     * Copy all elements from specified Array
     *
     * @param from Array to copy from
     */
    public void copyAllFrom(ArrayWrapper<T> from) {
        int newSize = Math.min(from.size(), getCapacity());
        Arrays.fill(backend, 0, getCapacity(), null); // clear array
        size = newSize;
        System.arraycopy(from.backend, 0, backend, 0, size);
    }

    /**
     * Set element to new value
     *
     * @param index Index at which to set element
     * @param value New Value
     * @return Old value
     */
    public T setAndGet(int index, T value) {
        T result = backend[index];
        backend[index] = value;
        return result;
    }

    /**
     * Sets (somewhat virtual) size of Array (not capacity)
     * - if size is reduced, elements are not deleted or set to NULL
     * (Attention! Potential Java and C++ memory leaks...)
     */
    public void setSize(int newSize) {
        assert(newSize <= backend.length);
        size = newSize;
    }

    /**
     * Remove Last element in array and return it
     */
    public T removeLast() {
        assert(size > 0);
        size--;
        T ret = backend[size];
        backend[size] = null;
        return ret;
    }
}
