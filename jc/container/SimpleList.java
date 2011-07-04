/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2007-2010 Max Reichardt,
 *   Robotics Research Lab, University of Kaiserslautern
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.rrlib.finroc_core_utils.jc.container;

import java.util.ArrayList;
import java.util.Collection;

import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Managed;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
//import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.SizeT;

/**
 * @author max
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
@Include("<vector>")
@Inline @SizeT @NoCpp
public class SimpleList<T> {

    /** UID */
    @JavaOnly
    private static final long serialVersionUID = 5075486406986507019L;

    /** Wrapped array list */
    @InCpp("std::vector<T> backend;")
    private final ArrayList<T> backend;

    @Init("backend()")
    public SimpleList() {
        backend = new ArrayList<T>();
    }

    /**
     * @param initialSize Initial List Size
     */
    @Init("backend()")
    @InCpp("backend._reserve(initialSize);")
    public SimpleList(@SizeT int initialSize) {
        backend = new ArrayList<T>(initialSize);
    }

    /**
     * @param index Index
     * @return Element at Index
     */
    @ConstMethod
    @InCpp("return backend[index];")
    public T get(int index) {
        return backend.get(index);
    }

    /**
     * @param element Element to add to list
     */
    @InCpp("backend.push_back(element);")
    public void add(@Const @Managed T element) {
        backend.add(element);
    }

    /**
     * Insert element at specified position
     *
     * @param index position
     * @param elem Element to insert
     */
    @InCpp("backend._insert(backend._begin() + i, elem);")
    public void insert(@SizeT int i, @Const T elem) {
        backend.add(i, elem);
    }

    /**
     * @return Size of list
     */
    @ConstMethod
    @InCpp("return backend._size();")
    public int size() {
        return backend.size();
    }

    /**
     * Remove element
     *
     * @param element Element to remove
     */
    @InCpp( {"for (typename std::vector<T>::iterator it = backend._begin(); it != backend._end(); ++it) {",
             "    if (*(it) == element) {",
             "        backend._erase(it);",
             "        return;",
             "    }",
             "}"
            })
    public void removeElem(@Const T element) {
        backend.remove(element);
    }

    /**
     * Remove element
     *
     * @param i Index of element to remove
     */
    @InCpp( {"const T r = backend._at(i);",
             "backend._erase(backend._begin() + i);",
             "return r;"
            })
    public /*@PassByValue @Const*/ T remove(int i) {
        return backend.remove(i);
    }

    /**
     * Add all elements from other list to this list
     *
     * @param other Other list
     */
    @InCpp("addAll(other.backend);")
    public void addAll(@Const @Ref SimpleList<T> other) {
        backend.addAll(other.backend);
    }

    /*Cpp
    // Returns vector which is backend of this list
    std::vector<T>& getBackend() {
        return backend;
    }

    // Add all elements from other stl::container to this list
    inline void addAll(const std::vector<T>& other) {
        backend._insert(backend._end(), other._begin(), other._end());
    }
    */

    /**
     * Remove all elements
     */
    @InCpp("backend._clear();")
    public void clear() {
        backend.clear();
    }

    /**
     * @return Is list empty?
     */
    @ConstMethod
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Does list contain specified element?
     *
     * @param element Element
     * @return Answer
     */
    @InCpp( {"for (typename std::vector<T>::const_iterator it = backend._begin(); it != backend._end(); ++it) {",
             "    if (*(it) == element) {",
             "        return true;",
             "    }",
             "}",
             "return false;"
            })
    @ConstMethod public boolean contains(T element) {
        return backend.contains(element);
    }

    /**
     * @param element Element
     * @return (First) Index of element in list; -1 if element could not be found (note, that return type of this method is int)
     */
    @InCpp( {"for (size_t i = 0; i < size(); i++) {",
             "    if (get(i) == element) {",
             "        return i;",
             "    }",
             "}",
             "return -1;"
            })
    @ConstMethod @CppType("int") public int indexOf(T element) {
        return backend.indexOf(element);
    }

    /**
     * Sets object at specified position
     *
     * @param index Position
     * @param object object to set
     */
    @InCpp("backend[index] = object;")
    public void set(int index, @Const T object) {
        backend.set(index, object);
    }

    /**
     * Add all these elements to list
     *
     * @param elements elements to add
     */
    public void addAll(@Const @Ref T[] elements) {
        for (@SizeT int i = 0; i < elements.length; i++) {
            add(elements[i]);
        }
    }

    /**
     * @return List backend
     */
    @JavaOnly
    public ArrayList<T> getBackend() {
        return backend;
    }

    /**
     * Add all these elements to list
     *
     * @param other elements to add
     */
    @JavaOnly
    public void addAll(Collection<T> other) {
        backend.addAll(other);
    }
}
