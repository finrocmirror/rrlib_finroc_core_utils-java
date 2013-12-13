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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Max Reichardt
 *
 * this class represents an atomic pointer
 */
public class AtomicPtr<T> {


    AtomicReference<T> wrapped;

    public AtomicPtr() {
        wrapped = new AtomicReference<T>();
    }

    /**
     * @param pointTo initiallay point to this object
     */
    public AtomicPtr(T pointTo) {
        wrapped = new AtomicReference<T>(pointTo);
    }

    public final T get() {
        return wrapped.get();
    }

    public final T getAndSet(T newValue) {
        return wrapped.getAndSet(newValue);
    }

    public final void set(T newValue) {
        wrapped.set(newValue);
    }

    public final boolean compareAndSet(T expect, T update) {
        return wrapped.compareAndSet(expect, update);
    }


}
