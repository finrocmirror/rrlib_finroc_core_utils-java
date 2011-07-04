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

import java.nio.ByteOrder;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * This class provides general information about C++ code on this machine
 */
@JavaOnly
public class JNIInfo {

    /** Size of Pointers on this platform (only valid when JNI is available) (*/
    private static final int ptrSize;

    /** True, if running on 64 Bit platfom */
    public static final boolean IS_64_BIT_PLATFORM;

    static {
        /*if (RuntimeSettings.isRunningInApplet() > 0) {
            ptrSize = RuntimeSettings.getNativePointerSize();
            IS_64_BIT_PLATFORM = (ptrSize == 8);
        } else if (JNICalls.JNI_AVAILABLE) {
            ptrSize = JNICalls.sizeOfPointer();
            IS_64_BIT_PLATFORM = (ptrSize == 8);
        } else {
            IS_64_BIT_PLATFORM = false; // shouldn't matter in this case
        }*/

        //
        ptrSize = JNICalls.JNI_AVAILABLE ? JNICalls.sizeOfPointer() : 4;
        IS_64_BIT_PLATFORM = (ptrSize == 8);
    }

    /** Size of Pointers on this platform (only valid when JNI is available) */
    public static int getPointerSize()  {
        return ptrSize;
    }

    /** True, if running on 64 Bit platfom */
    public static boolean is64BitPlatform() {
        return IS_64_BIT_PLATFORM;
    }

    /**
     * @return ByteOrder to use for this platform
     */
    public static ByteOrder getByteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }
}
