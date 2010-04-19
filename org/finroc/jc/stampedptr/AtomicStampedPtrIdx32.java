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
package org.finroc.jc.stampedptr;

import org.finroc.jc.AtomicInt;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.container.AbstractReusable;
import org.finroc.jc.container.ReusablesRegister;

/**
 * @author max
 *
 * Stamped Pointer implementation.
 * Pointer is not a direct pointer - but rather an index in the ReusablesRegister.
 *
 * Stamp can be 0-255 ; there can be a maximum of 16.7 million objects in the ReusablesRegister
 */
@Inline
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
    public boolean compareAndSet(@Ptr T expectedPointer, int expectedStamp, @Ptr T setPointer, int setStamp) {
        return compareAndSet(merge(expectedPointer, expectedStamp), merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(int expect, @Ptr T setPointer, int setStamp) {
        return compareAndSet(expect, merge(setPointer, setStamp));
    }

    /** Convenience wrapper */
    public boolean compareAndSet(@Ptr T expectedPointer, int expectedStamp, int set) {
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
    public void set(@Ptr T pointer, int stamp) {
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
    public int merge(@Ptr T pointer, int stamp) {
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
    public @Ptr T getPointer(int raw) {
        return (T)ReusablesRegister.get(raw >>> STAMP_BITS);
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
    public @Ptr T getPointer() {
        return getPointer(wrapped.get());
    }

    @Override
    public int getStamp() {
        return getStamp(wrapped.get());
    }

}
