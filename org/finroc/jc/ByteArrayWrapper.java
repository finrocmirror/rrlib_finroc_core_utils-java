//Generated from FloatArrayWrapper.java
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
package org.finroc.jc;

import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.SizeT;

/**
 * Wraps an array
 */
@JavaOnly

public class ByteArrayWrapper {

    /** size of array... may be smaller than backend capacity */
    private int size;

    /** backend */
    private final byte[] backend;

    /** Universal Empty Array Wrapper */
    private static final ByteArrayWrapper EMPTY = new ByteArrayWrapper(0);

    /**
     * @return Empty Array Wrapper
     */
    public static ByteArrayWrapper getEmpty() {
        return EMPTY;
    }

    /**
     * @param size size of array... may be smaller than backend capacity
     */
    public ByteArrayWrapper(int size) {
        this.size = size;
        this.backend = new byte[size];
        // in cpp mit 0 füllen
    }

    public byte get(int index) {
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
    public void add(byte element) {
        backend[size] = element;
        size++;
    }

    public void set(int index, byte value) {
        backend[index] = value;
    }

    /**
     * @return Is there free capacity in the array?
     */
    public boolean freeCapacity() {
        return size < getCapacity();
    }

    /**
     * Clear array contents
     *
     * @param deleteElements Delete elements in List (relevant for C++ only)
     */
    public void clear(boolean deleteElements) {
    }

    /**
     * Copy all elements from specified Array
     *
     * @param from Array to copy from
     */
    public void copyAllFrom(ByteArrayWrapper from) {
        int newSize = Math.min(from.size(), getCapacity());
        //Arrays.fill(backend, newSize, size, 0);
        size = newSize;
        System.arraycopy(from.backend, 0, backend, 0, size);
    }

    /**
     * Remove Last element in array and return it
     */
    public byte removeLast() {
        assert(size > 0);
        size--;
        return backend[size];
    }

    public void setSize(@SizeT int i) {
        size = 0;
    }
}
