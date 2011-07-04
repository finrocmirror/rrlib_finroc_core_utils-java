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

import org.rrlib.finroc_core_utils.jc.annotation.ConvertInterfaceOnly;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.RawTypeArgs;

/**
 * @author max
 *
 * This class implements a fast static thread local variable.
 *
 * It may only be used for static variables and the User template
 * parameter must be different for every user class ... typically
 * this should be the declaring class.
 *
 * In C++ this class is very fast (~10x faster than an ordinary thread local).
 */
@ConvertInterfaceOnly @JavaOnly
@RawTypeArgs
public class FastStaticThreadLocal<T, User> extends ThreadLocal<T> {

    /**
     * Same as get, but without checking for initialization (in C++)
     * May be called in places where it is guaranteed that the value
     * has been initialized or where it may be null.
     */
    public @Ptr T getFast() {
        return get();
    }
}
