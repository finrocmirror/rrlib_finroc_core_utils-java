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

import java.nio.ByteBuffer;

/**
 * @author Max Reichardt
 *
 * This class contains all JNI calls from this package
 *
 * Using these directly is potentially unsafe (You are able to ruin memory - just as in C++ code)
 */
public class JNICalls {

    /** True, if JNI library is available */
    public static final boolean JNI_AVAILABLE;

    static {
        boolean temp = false;
        try {
            //System.load(RuntimeSettings.getRootDir() + File.separator + "libjnibase.so");
            System.loadLibrary("rrlib_finroc_core_utils_jni_base");
            temp = true;
        } catch (Exception e) {
        } catch (UnsatisfiedLinkError e) {}
        JNI_AVAILABLE = temp;
    }

    /** @return Size of pointers on this platform */
    public static native int sizeOfPointer();

    /**
     * Set Pointer in memory
     *
     * @param address Memory address at which pointer is located
     * @param pointer Pointer that is written to memory address
     */
    public static void setPointer(long address, long pointer) {
        setPointer(address, 0, pointer);
    }

    /**
     * Set Pointer in memory
     *
     * @param arraypointer Memory address at which pointer array is located
     * @param index Index in Pointer array
     * @param pointer Pointer that is written to memory address
     */
    public static native void setPointer(long arraypointer, int index, long pointer);

    /**
     * @param buf Java ByteBuffer that was allocated using allocateDirect
     * @return Pointer ByteBuffer's contents
     */
    public static native long getBufferPointer(ByteBuffer buf);

    /**
     * Get Pointer in memory
     *
     * @param address Address to read pointer from
     * @return Pointer read
     */
    public static long getPointer(long address) {
        return getPointer(address, 0);
    }

    /**
     * Get Pointer in memory
     *
     * @param address Array Address to read pointer from
     * @param index Index in array
     * @return Pointer read
     */
    public static native long getPointer(long address, int index);


    //static native void createCString(long pointer, String s);

    /**
     * Gets Java String from const char*
     *
     * @param pointer const char*
     * @return String at this pointer
     */
    public static native String toString(long pointer);

    /**
     * Get ByteBuffer created in C++ - wrapping some existing buffer
     *
     * @param ptr Pointer at which to create the buffer
     * @param size Size the Java Buffer should have
     * @return ByteBuffer object
     */
    static native ByteBuffer getCByteBuffer(long ptr, int size);

    /**
     * Get (and possibly init global) pointer to this JavaVM
     */
    public static native long getJavaVM();

    /**
     * C++ memcpy operation :-)
     *
     * @param dest Destination pointer
     * @param src Source pointer
     * @param length Number of bytes to copy
     */
    public static native void memcpy(long dest, long src, int length);

    /**
     * C++ strlen operation
     *
     * @param ptr Memory const char*
     * @return String length
     */
    public static native int strlen(long ptr);

    // Getters and setters for all elementary data types (ptr is memory address) - anywhere in memory
    public static native byte getByte(long ptr);
    public static native short getShort(long ptr);
    public static native int getInt(long ptr);
    public static native long getLong(long ptr);
    public static native float getFloat(long ptr);
    public static native double getDouble(long ptr);
    public static native void setByte(long ptr, byte val);
    public static native void setShort(long ptr, short val);
    public static native void setInt(long ptr, int val);
    public static native void setLong(long ptr, long val);
    public static native void setFloat(long ptr, float val);
    public static native void setDouble(long ptr, double val);

    /** Delete C++ object that inherits from finroc::util::JNIWrappable */
    public static native void deleteJNIWrappable(long pointer);

    /**
     * Set Pointer to java object in C++ JNIWrappable
     *
     * @param pointer Pointer of C++ object
     * @param object Java Object
     * @param javaResponsible Is object owned by Java?
     */
    public static native void setJavaObject(long pointer, JNIWrapper object, boolean javaResponsible);
}
