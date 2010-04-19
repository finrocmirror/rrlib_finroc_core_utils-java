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

import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.Ptr;

/**
 * @author max
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
@Inline @NoCpp @JavaOnly
public abstract class AbstractAtomicStampedPtr<T> {

    /**
     * Set pointer and stamp to the following values in one atomic operation
     *
     * @param pointer Pointer
     * @param stamp Stamp
     */
    @NonVirtual
    public abstract void set(@Ptr T pointer, int stamp);

    /**
     * Compare expected pointer and stamp to current values. If they are equal, set current value to new values.
     *
     * @param expectedPointer Expected Pointer
     * @param expectedStamp Expected Stamp
     * @param setPointer new Pointer
     * @param setStamp new Stamp
     * @return Did values match - and have new values been set?
     */
    @NonVirtual
    public abstract boolean compareAndSet(@Ptr T expectedPointer, int expectedStamp, @Ptr T setPointer, int setStamp);

    /**
     * @return Pointer
     */
    @NonVirtual
    public abstract @Ptr T getPointer();

    /**
     * @return Stamp
     */
    @NonVirtual
    public abstract int getStamp();
}
