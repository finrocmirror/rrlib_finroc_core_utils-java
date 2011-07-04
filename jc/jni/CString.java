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

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * String with C++ (const char*) representation in memory
 */
@JavaOnly
public class CString extends SharedBuffer {

    /**
     * Creates String buffer for specified string
     */
    public CString(String s) {
        super(s.length() + 1);
        byte[] b = s.getBytes();
        for (int i = 0; i < b.length; i++) {
            super.buffer.put(i, b[i]);
        }
        super.buffer.put(b.length, (byte)0);
    }

    /**
     * Get String from specified pointer
     */
    public CString(long pointer) {
        super(pointer);
    }

    public String toString() {
        return JNICalls.toString(getPointer());
    }

    public int length() {
        return JNICalls.strlen(getPointer());
    }
}
