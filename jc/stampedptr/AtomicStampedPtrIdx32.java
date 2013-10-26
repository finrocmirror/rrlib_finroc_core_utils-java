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
package org.rrlib.finroc_core_utils.jc.stampedptr;

import org.rrlib.finroc_core_utils.jc.AtomicInt;
import org.rrlib.finroc_core_utils.jc.container.AbstractReusable;
import org.rrlib.finroc_core_utils.jc.container.AllocationRegister;

/**
 * @author Max Reichardt
 *
 * Stamped Pointer implementation.
 * Pointer is not a direct pointer - but rather an index in the ReusablesRegister.
 *
 * Stamp can be 0-255 ; there can be a maximum of 16.7 million objects in the ReusablesRegister
 */
public class AtomicStampedPtrIdx32<T extends AbstractReusable> extends AbstractAtomicStampedPtr<T> {

    /** wrapped atomic pointer */
    private AtomicInt wrapped = new AtomicInt();

    /** Number of bits for index */
    public static final int INDEX_BITS = 24;

    /** Number of bits for stamp */
    public static final int STAMP_BITS = 8;

    /** Maximum index */
    public static final int MAX_INDEX = (1 << INDEX_BITS) - 1;

    /** Maximum stamp */
    public static final int MAX_STAMP = (1 << STAMP_BITS) - 1;

    //Cpp typedef int32 raw_t;

    public AtomicStampedPtrIdx32() {
        assert(INDEX_BITS + STAMP_BITS == 32);
    }

    @Override
    public boolean compareAndSet(T expectedPointer, int expectedStamp, T setPointer, int setStamp) {
        return compareAndSet(merge(expectedPointer, expectedStamp), merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(int expect, T setPointer, int setStamp) {
        return compareAndSet(expect, merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(T expectedPointer, int expectedStamp, int set) {
        return compareAndSet(merge(expectedPointer, expectedStamp), set);
    }

    /**
     * Raw compare and set variant
     *
     * @param rawExpect Expected value
     * @param rawSet Raw value
     * @return Has value been changed?
     */
    public boolean compareAndSet(int rawExpect, int rawSet) {
        return wrapped.compareAndSet(rawExpect, rawSet);
    }


    @Override
    public void set(T pointer, int stamp) {
        set(merge(pointer, stamp));
    }

    /**
     * Raw set operation
     *
     * @param raw Raw integer value that contains stamp and pointer
     */
    public void set(int raw) {
        wrapped.set(raw);
    }

    /**
     * @return Raw integer value that contains stamp and pointer
     */
    public int getRaw() {
        return wrapped.get();
    }

    /**
     * Convert pointer and stamp to raw integer value conatining both
     *
     * @param pointer Pointer
     * @param stamp Stamp
     * @return Raw integer value that contains stamp and pointer
     */
    public int merge(T pointer, int stamp) {
        assert(pointer.getRegisterIndex() <= MAX_INDEX);
        assert(stamp <= MAX_STAMP);
        return (pointer.getRegisterIndex() << STAMP_BITS) | stamp;
    }

    /**
     * Extract pointer from raw integer
     *
     * @return Pointer
     */
    @SuppressWarnings("unchecked")
    public T getPointer(int raw) {
        return (T)AllocationRegister.getByIndex(raw >>> STAMP_BITS);
    }

    /**
     * Extract stamp from raw integer
     *
     * @return Stamp
     */
    public static int getStamp(int raw) {
        return raw & MAX_STAMP;
    }

    @Override
    public T getPointer() {
        return getPointer(wrapped.get());
    }

    @Override
    public int getStamp() {
        return getStamp(wrapped.get());
    }

}
