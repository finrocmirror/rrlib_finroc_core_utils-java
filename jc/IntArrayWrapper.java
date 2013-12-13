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
package org.rrlib.finroc_core_utils.jc;

import org.rrlib.serialization.BinaryInputStream;
import org.rrlib.serialization.BinaryOutputStream;
import org.rrlib.serialization.BinarySerializable;

/**
 * Wraps an array
 */
public class IntArrayWrapper implements BinarySerializable {

    /** size of array... may be smaller than backend capacity */
    private int size;

    /** backend */
    private final int[] backend;

    /** Universal Empty Array Wrapper */
    private static final IntArrayWrapper EMPTY = new IntArrayWrapper(0);

    /**
     * @return Empty Array Wrapper
     */
    public static IntArrayWrapper getEmpty() {
        return EMPTY;
    }

    /**
     * @param size size of array... may be smaller than backend capacity
     */
    public IntArrayWrapper(int size) {
        this.size = size;
        this.backend = new int[size];
    }

    public int get(int index) {
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
    public void add(int element) {
        backend[size] = element;
        size++;
    }

    public void set(int index, int value) {
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
    public void copyAllFrom(IntArrayWrapper from) {
        int newSize = Math.min(from.size(), getCapacity());
        //Arrays.fill(backend, newSize, size, 0);
        size = newSize;
        System.arraycopy(from.backend, 0, backend, 0, size);
    }

    /**
     * Remove Last element in array and return it
     */
    public int removeLast() {
        assert(size > 0);
        size--;
        return backend[size];
    }

    public void setSize(int i) {
        size = 0;
    }

    @Override
    public void serialize(BinaryOutputStream os) {
        os.writeInt(size());
        for (int i = 0, n = size(); i < n; i++) {
            os.writeInt(get(i));
        }
    }

    @Override
    public void deserialize(BinaryInputStream is) {
        setSize(0);
        int size = is.readInt();
        for (int i = 0; i < size; i++) {
            add(is.readInt());
        }
    }
}
