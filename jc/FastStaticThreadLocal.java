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
package org.rrlib.finroc_core_utils.jc;

/**
 * @author Max Reichardt
 *
 * This class implements a fast static thread local variable.
 *
 * It may only be used for static variables and the User template
 * parameter must be different for every user class ... typically
 * this should be the declaring class.
 *
 * In C++ this class is very fast (~10x faster than an ordinary thread local).
 */
public class FastStaticThreadLocal<T, User> extends ThreadLocal<T> {

    /**
     * Same as get, but without checking for initialization (in C++)
     * May be called in places where it is guaranteed that the value
     * has been initialized or where it may be null.
     */
    public T getFast() {
        return get();
    }
}
