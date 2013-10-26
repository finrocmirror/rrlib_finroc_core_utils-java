//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.container;

import org.rrlib.finroc_core_utils.jc.ArrayWrapper;
import org.rrlib.finroc_core_utils.jc.MutexLockOrder;

/**
 * @author Max Reichardt
 *
 * Implementation of class below.
 */
abstract class SafeConcurrentlyIterableListBase<T> {

    /** Mutex for list - Since we call garbage collector lock for list needs to be before in order */
    public final MutexLockOrder objMutex;

    /** Current list backend */
    protected volatile ArrayWrapper<T> currentBackend;

    /** optimization variable: There are no free entries before this index */
    private int firstFreeFromHere = 0;

    /**
     * @param initialSize Initial size of backend
     * @param resizeFactor Factor by which list is enlarged, when backend is too small (dummy in C++, template parameter specifies it here)
     * @param deleteElemsWithList Delete elements when List is deleted? (relevant for C++ only)
     */
    @SuppressWarnings("unchecked")
    public SafeConcurrentlyIterableListBase(int initialSize) {
        objMutex = new MutexLockOrder(Integer.MAX_VALUE - 20);
        currentBackend = initialSize > 0 ? new ArrayWrapper<T>(0, initialSize) : ArrayWrapper.getEmpty();
    }

    /**
     * Add element
     *
     * @param element element
     * @param appendToBack Append new element to back O(1)? (or rather search for hole => O(n), but possibly smaller list and faster iteration)
     * @return Array index at which element was inserted
     */
    public synchronized int add(T element, boolean appendToBack) {
        ArrayWrapper<T> backend = currentBackend; // acquire non-volatile pointer
        if (!appendToBack) {
            for (int i = firstFreeFromHere, n = backend.size(); i < n; i++) {
                if (backend.get(i) == getNullElement()) {
                    backend.set(i, element);
                    firstFreeFromHere = i + 1;
                    return i;
                }
            }
            firstFreeFromHere = backend.size() + 1;
        }
        if (backend.freeCapacity()) {
            backend.add(element);
        } else {
            ArrayWrapper<T> old = currentBackend;
            ArrayWrapper<T> newBackend = new ArrayWrapper<T>(0, Math.max(1, backend.getCapacity()) * getResizeFactor());
            newBackend.copyAllFrom(backend);
            newBackend.add(element);
            currentBackend = newBackend;

            if (old.size() > 0) { // we don't want to delete empty backend from ArrayWrapper class
                deleteBackend(old);
            }
        }
        return currentBackend.size() - 1;
    }

    private void deleteBackend(ArrayWrapper<T> b) {
    }

    /**
     * @return Null/empty element (marks free slots in array)
     */
    private T getNullElement() {
        return (T)null;
    }

    /**
     * @return Factor by which list is enlarged, when backend is too small
     */
    protected abstract int getResizeFactor();

    /**
     * @return Delete elements when List is deleted? (relevant for C++ only)
     */
    protected abstract boolean deleteElemsWithList();

    /**
     * Ensure/Reserve specified capacity
     *
     * @param cap Capacity
     */
    public synchronized void ensureCapacity(int cap) {
        ArrayWrapper<T> backend = currentBackend; // acquire non-volatile pointer
        if (backend.getCapacity() < cap) {
            ArrayWrapper<T> old = currentBackend;
            ArrayWrapper<T> newBackend = new ArrayWrapper<T>(0, cap * getResizeFactor());
            newBackend.copyAllFrom(backend);
            currentBackend = newBackend;

            deleteBackend(old);
        }
    }

    /**
     * @return Safe View for iterating over list
     */
    public ArrayWrapper<T> getIterable() {
        return currentBackend;
    }

    /**
     * Remove element
     *
     * @param element Element to remove
     */
    public synchronized void remove(T element) {
        ArrayWrapper<T> iterable = getIterable();
        for (int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) == element) {

                iterable.set(i, getNullElement());
                firstFreeFromHere = Math.min(firstFreeFromHere, i);

                // Shrink list when last elements are deleted
                if (i == size() - 1) {
                    iterable.setSize(size() - 1);

                    while (size() > 0 && iterable.get(size() - 1) == getNullElement()) {
                        iterable.setSize(size() - 1);
                    }
                    firstFreeFromHere = Math.min(firstFreeFromHere, size());
                }

                break;
            }
        }
    }

    /**
     * @return Returns size of list
     */
    public int size() {
        return getIterable().size();
    }

    /**
     * @return Returns number of elements currently in list (in parallel - so can already be invalid due to concurrent modifications)
     */
    public int countElements() {
        ArrayWrapper<T> iterable = getIterable();
        int count = 0;
        for (int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) != getNullElement()) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return Clear list
     */
    public synchronized void clear() {
        getIterable().clear();
        firstFreeFromHere = 0;
    }

    /**
     * Are any non-null elements in current backend?
     */
    public boolean isEmpty() {
        ArrayWrapper<T> iterable = getIterable();
        for (int i = 0, n = iterable.size(); i < n; i++) {
            if (iterable.get(i) != getNullElement()) {
                return false;
            }
        }
        return true;
    }
}

/**
 * @author Max Reichardt
 *
 * This list is thread-safe. It can be iterated over (concurrently to modifications)
 * by many threads without blocking. Iterations are very efficient (both C++ and Java).
 * The list may contain null entries after deleting (so check!).
 *
 * To efficiently iterate over the list, this code should be used:
 *
 *      Java:
 *       ArrayWrapper<T> iterable = xyz.getIterable();
 *       for (int i = 0, n = iterable.size(); i < n; i++) {
 *          T x = iterable.get(i);
 *          if (x != null) {
 *              // do something
 *          }
 *       }
 *
 *      C++:
 *       ArrayWrapper<T>* iterable = xyz.getIterable();
 *       for (int i = 0, n = iterable->size(); i < n; i++) {
 *          T* x = iterable->get(i);
 *          if (x != NULL {
 *              // do something
 *          }
 *       }
 *
 * Idea: Unlike ArrayList (Java) or std::vector (C++), old Array backends are deleted deferred so that
 * threads still iterating over this area can always complete this.
 * TODO: If iterations can be particularly delayed, use a delay-iterator in C++.
 *
 *
 * (C++) This list may only contain pointers (no shared pointers etc. because of thread-safety)...
 *       because only this allows atomic deletion by overwriting with null
 */
public class SafeConcurrentlyIterableList<T> extends SafeConcurrentlyIterableListBase<T> {

    /** Empty Dummy List */
    @SuppressWarnings("rawtypes")
    private static final SafeConcurrentlyIterableList EMPTY = new SafeConcurrentlyIterableList(0, 0);

    /** Factor by which list is enlarged, when backend is too small */
    private final int resizeFactor;

    /**
     * @return Empty Dummy List
     */
    @SuppressWarnings("rawtypes")
    public static SafeConcurrentlyIterableList getEmptyInstance() {
        return EMPTY;
    }

    /**
     * @param initialSize Initial size of backend
     * @param resizeFactor Factor by which list is enlarged, when backend is too small (dummy in C++, template parameter specifies it here)
     */
    public SafeConcurrentlyIterableList(int initialSize, int resizeFactor_) {
        super(initialSize);
        resizeFactor = resizeFactor_;
    }

    @Override
    protected boolean deleteElemsWithList() {
        return false;
    }

    @Override
    protected int getResizeFactor() {
        return resizeFactor;
    }
}
