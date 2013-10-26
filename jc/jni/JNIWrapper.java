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

import org.rrlib.finroc_core_utils.jc.HasDestructor;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Proxy class/wrapper for an C++ object (with super class JNIWrappable).
 */
public abstract class JNIWrapper extends UsedInC implements HasDestructor {

    /** Pointer to class */
    private long pointer;

    /** Has Java class created C++ class and is responsible for deleting it? */
    private boolean owner;

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("jni");

    public JNIWrapper(long ptr) {
        this(ptr, false);
    }

    public JNIWrapper(long ptr, boolean owner) {
        pointer = ptr;
        this.owner = owner;
        JNICalls.setJavaObject(ptr, this, owner);
    }

    @Override
    protected void finalize() throws Throwable {

        // Automatically delete C++ object with garbage collection
        super.finalize();
        if (owner && pointer != 0) {
            //System.out.println("Deleting C++ object " + toString());
            log(LogLevel.DEBUG_VERBOSE_1, logDomain, "Deleting C++ object " + toString());
            delete();
            cppDelete();
            pointer = 0;
        }
    }

    public long getPointer() {
        return pointer;
    }

    /**
     * Deletes object in C++
     * (is automatically called - can be overridden by subclass)
     */
    protected void cppDelete() {
        JNICalls.deleteJNIWrappable(pointer);
    }

    // TODO: Called when C++ object is deleted
    public void delete() {}
}
