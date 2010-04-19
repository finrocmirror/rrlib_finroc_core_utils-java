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
package org.finroc.jc.container;

import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.Ptr;

/**
 * @author max
 *
 * This is the base class of all kinds of pools of reusable objects.
 */
@Inline @NoCpp @Include("AbstractReusable.h")
public class AbstractReusablesPool<T extends AbstractReusable> {

    /** Pointer to Last created reusable => linked list to all reusables */
    @Ptr
    protected T lastCreated;

    //Cpp #ifndef NDEBUG

    /** "Lock" to allocation register - ensures that report will be printed after pool has been deleted */
    protected AllocationRegister allocationRegisterLock = AllocationRegister.getInstance();

    /*Cpp
    #else

    AbstractReusablesPool() : lastCreated(NULL) {}

    #endif
     */

    /**
     * @return Pointer to Last created reusable => linked list to all reusables
     */
    @ConstMethod public @Ptr T getLastCreated() {
        return lastCreated;
    }
}
