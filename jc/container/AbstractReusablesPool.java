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
package org.rrlib.finroc_core_utils.jc.container;

import org.rrlib.finroc_core_utils.jc.HasDestructor;

/**
 * @author Max Reichardt
 *
 * This is the base class of all kinds of pools of reusable objects.
 */
public abstract class AbstractReusablesPool<T extends AbstractReusable> implements HasDestructor {

    /** Pointer to Last created reusable => linked list to all reusables */
    protected T lastCreated;

    /** "Lock" to allocation register - ensures that report will be printed after pool has been deleted */
    protected AllocationRegister allocationRegisterLock = AllocationRegister.getInstance();

    /**
     * @return Pointer to Last created reusable => linked list to all reusables
     */
    public T getLastCreated() {
        return lastCreated;
    }
}
