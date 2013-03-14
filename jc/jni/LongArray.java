//Generated from DoubleArray.java
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
package org.rrlib.finroc_core_utils.jc.jni;

import java.nio.LongBuffer;

/**
 * @author Max Reichardt
 *
 * Array with doubles in C++ (long *)
 *
 * Lightweight implementation used for custom JNI implementations.
 *
 * In auto-generated JNI stuff, the DoubleArrayWrapper is the class to use.
 */
public class LongArray extends SharedBuffer {

    public LongBuffer dbuffer;

    public LongArray(long[] doubles) {
        super(doubles.length * 8);
        dbuffer = buffer.asLongBuffer();
        dbuffer.put(doubles);
    }

    /** Create array with specified size */
    public LongArray(int size) {
        super(size * 8);
        dbuffer = buffer.asLongBuffer();
    }

    /** Wrap existing C++ array */
    public LongArray(long pointer) {
        super(pointer);
        dbuffer = buffer.asLongBuffer();
    }

    /**
     * Only for buffers received from C++
     *
     * @param size Size information (in #doubles)
     */
    public void setSize(int size) {
        getCSideBuffer(size * 8);
        dbuffer = buffer.asLongBuffer();
    }
}
