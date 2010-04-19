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

import java.nio.DoubleBuffer;

import org.finroc.jc.annotation.AutoVariants;
import org.finroc.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * Array with doubles in C++ (double *)
 *
 * Lightweight implementation used for custom JNI implementations.
 *
 * In auto-generated JNI stuff, the DoubleArrayWrapper is the class to use.
 */
@JavaOnly
@AutoVariants( {
    "DoubleArray;double;8;DoubleBuffer;asDoubleBuffer",
    "FloatArray;float;4;FloatBuffer;asFloatBuffer",
    "LongArray;long;8;LongBuffer;asLongBuffer",
    "IntArray;int;4;IntBuffer;asIntBuffer",
    "ShortArray;short;2;ShortBuffer;asShortBuffer",
    "ByteArray;byte;1;ByteBuffer;duplicate",
    "CharArray;char;1;CharBuffer;asCharBuffer"
})
public class DoubleArray extends SharedBuffer {

    public DoubleBuffer dbuffer;

    public DoubleArray(double[] doubles) {
        super(doubles.length * 8);
        dbuffer = buffer.asDoubleBuffer();
        dbuffer.put(doubles);
    }

    /** Create array with specified size */
    public DoubleArray(int size) {
        super(size * 8);
        dbuffer = buffer.asDoubleBuffer();
    }

    /** Wrap existing C++ array */
    public DoubleArray(long pointer) {
        super(pointer);
        dbuffer = buffer.asDoubleBuffer();
    }

    /**
     * Only for buffers received from C++
     *
     * @param size Size information (in #doubles)
     */
    public void setSize(int size) {
        getCSideBuffer(size * 8);
        dbuffer = buffer.asDoubleBuffer();
    }
}
