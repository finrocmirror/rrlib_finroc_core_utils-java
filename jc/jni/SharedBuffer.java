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
package org.rrlib.finroc_core_utils.jc.jni;

import java.nio.ByteBuffer;

/**
 * @author Max Reichardt
 *
 * Area of shared memory that can be shared between Java and C++
 */
public class SharedBuffer extends UsedInC {

    /** Buffer's address in memory */
    private long pointer = 0;

    /** nio.buffer reference */
    protected ByteBuffer buffer;

    /** Create new shared byte buffer */
    public SharedBuffer(int size) {
        buffer = ByteBuffer.allocateDirect(size);
        buffer.order(JNIInfo.getByteOrder());
    }

    /** Create from existing buffer (usually received from C++) */
    public SharedBuffer(long pointer) {
        this.pointer = pointer;
    }

    protected void getCSideBuffer(int size) {
        if (buffer != null) {
            return;
        }
        buffer = JNICalls.getCByteBuffer(getPointer(), size);
        buffer.order(JNIInfo.getByteOrder());
    }

    public long getPointer() {
        if (pointer == 0 && buffer != null) {
            pointer = JNICalls.getBufferPointer(buffer);
        }
        return pointer;
    }

    protected void setPointer(long ptr) {
        pointer = ptr;
    }

}
