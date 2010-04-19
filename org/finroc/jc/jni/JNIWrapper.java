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

import org.finroc.jc.HasDestructor;
import org.finroc.jc.annotation.JavaOnly;

/**
 * @author max
 *
 * Proxy class/wrapper for an C++ object (with super class JNIWrappable).
 */
@JavaOnly
public abstract class JNIWrapper extends UsedInC implements HasDestructor {

    /** Pointer to class */
    private long pointer;

    /** Has Java class created C++ class and is responsible for deleting it? */
    private boolean owner;

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
            System.out.println("deleting " + toString());
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
