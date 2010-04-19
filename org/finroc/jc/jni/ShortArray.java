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
package org.finroc.jc.jni;

import java.nio.ShortBuffer;

import org.finroc.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * Array with doubles in C++ (short *)
 *
 * Lightweight implementation used for custom JNI implementations.
 *
 * In auto-generated JNI stuff, the DoubleArrayWrapper is the class to use.
 */
@JavaOnly

public class ShortArray extends SharedBuffer {

    public ShortBuffer dbuffer;

    public ShortArray(short[] doubles) {
        super(doubles.length * 2);
        dbuffer = buffer.asShortBuffer();
        dbuffer.put(doubles);
    }

    /** Create array with specified size */
    public ShortArray(int size) {
        super(size * 2);
        dbuffer = buffer.asShortBuffer();
    }

    /** Wrap existing C++ array */
    public ShortArray(long pointer) {
        super(pointer);
        dbuffer = buffer.asShortBuffer();
    }

    /**
     * Only for buffers received from C++
     *
     * @param size Size information (in #doubles)
     */
    public void setSize(int size) {
        getCSideBuffer(size * 2);
        dbuffer = buffer.asShortBuffer();
    }
}
