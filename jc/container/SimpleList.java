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

import java.util.ArrayList;
import java.util.Collection;


/**
 * @author Max Reichardt
 *
 * Simple List wrapper with array-based list backend
 * (Java: ArrayList, C++: std::vector)
 * Not thread safe.
 *
 * Traverse like array wrapper...
 *
 *      Java:
 *       SimpleList<T> l = ...;
 *       for (int i = 0, n = l.getSize(); i < n; i++) {
 *           ... l.get(i)
 *       }
 *
 *      C++:
 *       SimpleList<T> l = ...;
 *       for (int i = 0, n = l.getSize(); i < n; i++) {
 *          ... l.get(i)
 *
 */
public class SimpleList<T> {

    /** UID */
    private static final long serialVersionUID = 5075486406986507019L;

    /** Wrapped array list */
    private final ArrayList<T> backend;

    public SimpleList() {
        backend = new ArrayList<T>();
    }

    /**
     * @param initialSize Initial List Size
     */
    public SimpleList(int initialSize) {
        backend = new ArrayList<T>(initialSize);
    }

    /**
     * @param index Index
     * @return Element at Index
     */
    public T get(int index) {
        return backend.get(index);
    }

    /**
     * @param element Element to add to list
     */
    public void add(T element) {
        backend.add(element);
    }

    /**
     * Insert element at specified position
     *
     * @param index position
     * @param elem Element to insert
     */
    public void insert(int i, T elem) {
        backend.add(i, elem);
    }

    /**
     * @return Size of list
     */
    public int size() {
        return backend.size();
    }

    /**
     * Remove element
     *
     * @param element Element to remove
     */
    public void removeElem(T element) {
        backend.remove(element);
    }

    /**
     * Remove element
     *
     * @param i Index of element to remove
     */
    public T remove(int i) {
        return backend.remove(i);
    }

    /**
     * Add all elements from other list to this list
     *
     * @param other Other list
     */
    public void addAll(SimpleList<T> other) {
        backend.addAll(other.backend);
    }

    /**
     * Remove all elements
     */
    public void clear() {
        backend.clear();
    }

    /**
     * @return Is list empty?
     */
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Does list contain specified element?
     *
     * @param element Element
     * @return Answer
     */
    public boolean contains(T element) {
        return backend.contains(element);
    }

    /**
     * @param element Element
     * @return (First) Index of element in list; -1 if element could not be found (note, that return type of this method is int)
     */
    public int indexOf(T element) {
        return backend.indexOf(element);
    }

    /**
     * Sets object at specified position
     *
     * @param index Position
     * @param object object to set
     */
    public void set(int index, T object) {
        backend.set(index, object);
    }

    /**
     * Add all these elements to list
     *
     * @param elements elements to add
     */
    public void addAll(T[] elements) {
        for (int i = 0; i < elements.length; i++) {
            add(elements[i]);
        }
    }

    /**
     * @return List backend
     */
    public ArrayList<T> getBackend() {
        return backend;
    }

    /**
     * Add all these elements to list
     *
     * @param other elements to add
     */
    public void addAll(Collection<T> other) {
        backend.addAll(other);
    }
}
