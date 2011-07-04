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
package org.rrlib.finroc_core_utils.jc;

import java.util.concurrent.atomic.AtomicReference;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.RawTypeArgs;

/**
 * @author max
 *
 * this class represents an atomic pointer
 */
@JavaOnly @RawTypeArgs
public class AtomicPtr<T> {

    /** UID */
    private static final long serialVersionUID = 5117825501316865000L;

    AtomicReference<T> wrapped;

    public AtomicPtr() {
        wrapped = new AtomicReference<T>();
    }

    /**
     * @param pointTo initiallay point to this object
     */
    public AtomicPtr(@Ptr T pointTo) {
        wrapped = new AtomicReference<T>(pointTo);
    }

    public final @Ptr T get() {
        return wrapped.get();
    }

    public final @Ptr T getAndSet(@Ptr T newValue) {
        return wrapped.getAndSet(newValue);
    }

    public final void set(@Ptr T newValue) {
        wrapped.set(newValue);
    }

    public final boolean compareAndSet(@Ptr T expect, @Ptr T update) {
        return wrapped.compareAndSet(expect, update);
    }


}
