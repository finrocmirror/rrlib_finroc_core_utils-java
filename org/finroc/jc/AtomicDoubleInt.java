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

import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Two unsigned integer numbers with together max. 31 bit that can be set atomically at the same time.
 *
 * Convention with all set operations: If numbers are too large - bits are simply cut off
 */
@NoCpp @Inline
public class AtomicDoubleInt {

    /** Wrapped AtomicInt 32 */
    private AtomicInt wrapped = new AtomicInt();

    /** Bit mask for first and second number; Number of bits to shift first number */
    private final @SizeT int mask1, mask2, shift1;

    /**
     * @param num1Bits Number of bits for first number
     * @param num2Bits Number of bits for second number
     */
    public AtomicDoubleInt(@SizeT int num1Bits, @SizeT int num2Bits) {
        this(num1Bits, num2Bits, 0, 0);
    }

    /**
     * @param num1Bits Number of bits for first number
     * @param num2Bits Number of bits for second number
     * @param num1 Initial value for first number
     * @param num2 Initial value for second number
     */
    public AtomicDoubleInt(@SizeT int num1Bits, @SizeT int num2Bits, @SizeT int num1, @SizeT int num2) {
        assert(num1Bits + num2Bits <= 31) : "Max 31 bits for both numbers";
        mask1 = (1 << num1Bits) - 1;
        mask2 = (1 << num2Bits) - 1;
        shift1 = num2Bits;
        set(num1, num2);
    }

    /**
     * @param num1 Value for first number
     * @param num2 Value for second number
     */
    public void set(@SizeT int num1, @SizeT int num2) {
        wrapped.set(combine(num1, num2));
    }

    /**
     * Combine two numbers to one integer
     *
     * @param num1 Value for first number
     * @param num2 Value for second number
     * @return Combined int32 value
     */
    public @SizeT int combine(@SizeT int num1, @SizeT int num2) {
        return ((num1 & mask1) << shift1) | (num2 & mask2);
    }

    /**
     * Set value to new value if it matches expectation
     *
     * @param rawExpect Expected value (raw combined integer)
     * @param rawSet Value to set (raw combined integer)
     * @return Did old value match expection? Was new value set?
     */
    public boolean compareAndSet(@SizeT int rawExpect, @SizeT int rawSet) {
        return wrapped.compareAndSet(rawExpect, rawSet);
    }

    /**
     * Set value to new value if it matches expectation
     *
     * @param expect1 Expected first value
     * @param expect2 Expected second value
     * @param set1 New Value for first value
     * @param set2 New Value for second value
     * @return Did old value match expectation? Was new value set?
     */
    public boolean compareAndSet(@SizeT int expect1, @SizeT int expect2, @SizeT int set1, @SizeT int set2) {
        return wrapped.compareAndSet(combine(expect1, expect2), combine(set1, set2));
    }

    /**
     * Set value to new value if it matches expectation
     *
     * @param rawExpect Expected value (raw combined integer)
     * @param set1 New Value for first value
     * @param set2 New Value for second value
     * @return Did old value match expectation? Was new value set?
     */
    public boolean compareAndSet(@SizeT int expectRaw, @SizeT int set1, @SizeT int set2) {
        return wrapped.compareAndSet(expectRaw, combine(set1, set2));
    }

    /**
     * @return Raw integer value that contains both values
     */
    public @SizeT int getRaw() {
        return wrapped.get();
    }

    /**
     * @param raw "Raw" Integer value that contains both values
     * @return value 1
     */
    public @SizeT int getVal1(@SizeT int raw) {
        return raw >> shift1;
    }

    /**
     * @param raw "Raw" Integer value that contains both values
     * @return value 2
     */
    public @SizeT int getVal2(@SizeT int raw) {
        return raw & mask2;
    }

    /**
     * @return value 1
     */
    public @SizeT int getVal1() {
        return getVal1(getRaw());
    }

    /**
     * @return value 2
     */
    public @SizeT int getVal2() {
        return getVal2(getRaw());
    }

}
