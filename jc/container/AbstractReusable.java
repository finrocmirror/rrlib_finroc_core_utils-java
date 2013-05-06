//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.container;

/**
 * @author Max Reichardt
 *
 * Abstract base class for all reusables
 */
public abstract class AbstractReusable extends Queueable {

    /** Index in reusable register */
    private int registerIndex = -1;

    /** Current state (for debugging only - comment out, if not needed to save memory) */
    private byte state = UNKNOWN;

    /** Possible states (for debugging only) - these are bit flags */
    public static final byte UNKNOWN = 1, RECYCLED = 2, USED = 4, ENQUEUED = 8, POST_QUEUED = 16, DELETED = 32;

    /**
     * Register element at Reusables register (optional - but required for use in some stamped pointers)
     */
    protected void registerForIndex() {
        assert(registerIndex < 0);
        registerIndex = AllocationRegister.getInstance().indexReusable(this);
    }

    protected AbstractReusable() {
        AllocationRegister.getInstance().registerReusable(this);
    }

    public void delete() {
        state = DELETED;
        AllocationRegister.unregisterReusable(this);
    }

    /**
     * for debugging only - usage: assert(r.stateChange(UNKNOWN | RECYCLED, USED));
     *
     * @param precondition Valid states before this state transition
     * @param newState New state to enter
     * @param partnerObject Object (queue, pool) that has to do with this operation (for tracing object use)
     * @return Was precondition true and state change performed?
     */
    public boolean stateChange(byte precondition, byte newState, Object partnerObject) {
        if ((state & precondition) > 0) {
            state = newState;
            return true;
        }

        return false;
    }

    /**
     * @return Index in reusable register
     */
    public int getRegisterIndex() {
        return registerIndex;
    }

    /**
     * Deletes this object.
     * Called when this object is no longer needed.
     * May be overridden.
     */
    protected void deleteThis() {
        this.delete();
    }

    /**
     * for C++-debugging only: Print trace of where object has been last used
     */
    public void printTrace(Object output, long startTime) {
    }
}
