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


/**
 * @author Max Reichardt
 *
 * This class maintains a pointer together with an integer "stamp".
 * _Both_ can be set in an atomic operation.
 * Such stamped pointers are required for several non-blocking algorithms and data-structures
 * (for instance to avoid ABA problem)
 *
 * There are various implementations for such stamped pointers (since they are critical for overall performance)
 * They differ in counter bits and implementation (raw pointers vs. array index).
 * Some only work in C++.
 */
public abstract class AbstractAtomicStampedPtr<T> {

    /**
     * Set pointer and stamp to the following values in one atomic operation
     *
     * @param pointer Pointer
     * @param stamp Stamp
     */
    public abstract void set(T pointer, int stamp);

    /**
     * Compare expected pointer and stamp to current values. If they are equal, set current value to new values.
     *
     * @param expectedPointer Expected Pointer
     * @param expectedStamp Expected Stamp
     * @param setPointer new Pointer
     * @param setStamp new Stamp
     * @return Did values match - and have new values been set?
     */
    public abstract boolean compareAndSet(T expectedPointer, int expectedStamp, T setPointer, int setStamp);

    /**
     * @return Pointer
     */
    public abstract T getPointer();

    /**
     * @return Stamp
     */
    public abstract int getStamp();
}
