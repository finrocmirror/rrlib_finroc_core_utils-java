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

import org.rrlib.finroc_core_utils.jc.AtomicInt64;
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
public class AtomicStampedPtrIdx64<T extends AbstractReusable> extends AbstractAtomicStampedPtr<T> {

    /** wrapped atomic pointer */
    private AtomicInt64 wrapped = new AtomicInt64();

    /** Number of bits for index */
    public static final int INDEX_BITS = 32;

    /** Number of bits for stamp */
    public static final int STAMP_BITS = 32;

    /** Maximum index */
    public static final long MAX_INDEX = (1L << INDEX_BITS) - 1L;

    /** Maximum stamp */
    public static final long MAX_STAMP = (1L << STAMP_BITS) - 1L;

    //Cpp typedef int64 raw_t;

    public AtomicStampedPtrIdx64() {
        assert(INDEX_BITS + STAMP_BITS == 64);
    }

    @Override
    public boolean compareAndSet(T expectedPointer, int expectedStamp, T setPointer, int setStamp) {
        return compareAndSet(merge(expectedPointer, expectedStamp), merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(long expect, T setPointer, int setStamp) {
        return compareAndSet(expect, merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(T expectedPointer, int expectedStamp, long set) {
        return compareAndSet(merge(expectedPointer, expectedStamp), set);
    }

    /**
     * Raw compare and set variant
     *
     * @param rawExpect Expected value
     * @param rawSet Raw value
     * @return Has value been changed?
     */
    public boolean compareAndSet(long rawExpect, long rawSet) {
        return wrapped.compareAndSet(rawExpect, rawSet);
    }


    @Override
    public void set(T pointer, int stamp) {
        wrapped.set(merge(pointer, stamp));
    }

    /**
     * Raw set operation
     *
     * @param raw Raw integer value that contains stamp and pointer
     */
    public void set(long raw) {
        wrapped.set(raw);
    }

    /**
     * @return Raw integer value that contains stamp and pointer
     */
    public long getRaw() {
        return wrapped.get();
    }

    /**
     * Convert pointer and stamp to raw integer value containing both
     *
     * @param pointer Pointer
     * @param stamp Stamp
     * @return Raw integer value that contains stamp and pointer
     */
    public long merge(T pointer, int stamp) {
        //JavaOnlyBlock
        assert(pointer.getRegisterIndex() <= MAX_INDEX);
        assert(stamp <= MAX_STAMP);

        //return (((long)pointer.getRegisterIndex()) << STAMP_BITS) | stamp;
        return ((long)stamp << STAMP_BITS) | pointer.getRegisterIndex();
    }

    /**
     * Extract pointer from raw integer
     *
     * @return Pointer
     */
    @SuppressWarnings("unchecked")
    public T getPointer(long raw) {
        //return (T)ReusablesRegister.get((int)(raw >>> STAMP_BITS));
        return (T)AllocationRegister.getByIndex((int)(raw & MAX_INDEX));
    }

    /**
     * Extract stamp from raw integer
     *
     * @return Stamp
     */
    public int getStamp(long raw) {
        return (int)(raw >>> STAMP_BITS);
    }

    @Override
    public T getPointer() {
        return getPointer(wrapped.get());
    }

    @Override
    public int getStamp() {
        return getStamp(wrapped.get());
    }

    public String toString() {
        T ptr = getPointer();
        return "Stamp: " + getStamp() + " Object: " + (ptr != null ? ptr.toString() : "null");
    }

    public static void main(String[] args) {

        // self-test
        for (int stamp = Integer.MIN_VALUE; stamp < Integer.MAX_VALUE; stamp += 10000) {
            for (int pointer = 1; pointer < Integer.MAX_VALUE; pointer += 10000) {
                long raw = ((long)stamp << STAMP_BITS) | pointer;
                int newStamp = (int)(raw >>> STAMP_BITS);
                boolean stampOk = (newStamp == stamp);
                int newPtr = (int)(raw & MAX_INDEX);
                boolean ptrOk = (newPtr == pointer);
                if (!stampOk || !ptrOk) {
                    throw new RuntimeException("3523466");
                }
            }
        }
    }
}
