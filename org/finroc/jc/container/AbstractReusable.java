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

import org.finroc.jc.HasDestructor;
import org.finroc.jc.annotation.HPrepend;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ptr;

/**
 * @author max
 *
 * Abstract base class for all reusables
 */
@Ptr @Inline
@HPrepend( {"#ifndef NDEBUG", "#define DEFINE_ADV_REUSABLE_DEBUGGING_ENABLED", "#endif"})
//@Include("jc/thread/sThreadUtil.h")
public abstract class AbstractReusable extends Queueable implements HasDestructor {

    /** Index in reusable register */
    private int registerIndex = -1;

    /*Cpp

    #ifdef DEFINE_ADV_REUSABLE_DEBUGGING_ENABLED

    //! entry for tracing operations on reusable object
    struct TraceEntry {
        int8 state; // New State at this step
        Object* partnerObject; // Object involved in state
        const char* partnerObjectClassName; // Class name of involved object (object is probably already deleted when leak report is printed)
        int64 timestamp; // Time when this occured
        int64 threadId; // Id of thread that did this
    };

    //! trace, since last recycle operation
    SimpleList<TraceEntry> trace;

    //! number of times object has been recycled
    int recycleCount;

    #endif
     */

    //Cpp #ifndef NDEBUG
    /** Current state (for debugging only - comment out, if not needed to save memory) */
    private byte state = UNKNOWN;
    //Cpp #endif

    /** Possible states (for debugging only) - these are bit flags */
    public static final byte UNKNOWN = 1, RECYCLED = 2, USED = 4, ENQUEUED = 8, POST_QUEUED = 16, DELETED = 32;

    /*Cpp

    AbstractReusable() :
            registerIndex(-1)
    #ifdef DEFINE_ADV_REUSABLE_DEBUGGING_ENABLED
            ,
            trace(),
            recycleCount(0)
    #endif
    #ifndef NDEBUG
            ,
            state(UNKNOWN)
    #endif
    {
        AllocationRegister::getInstance()->registerReusable(this);
    }

    */

    /**
     * Register element at Reusables register (optional - but required for use in some stamped pointers)
     */
    protected void register() {
        assert(registerIndex < 0);
        registerIndex = ReusablesRegister.register(this);
    }

    @JavaOnly
    protected AbstractReusable() {
        AllocationRegister.getInstance().registerReusable(this);
    }

    public void delete() {
//Cpp #ifndef NDEBUG
        state = DELETED;
//Cpp #endif
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
    public boolean stateChange(byte precondition, byte newState, @Ptr Object partnerObject) {

        /*Cpp
        #ifdef DEFINE_ADV_REUSABLE_DEBUGGING_ENABLED

        if (newState == RECYCLED && state != UNKNOWN) {
            recycleCount++;
        }

        if (newState == RECYCLED) {
            trace.clear();
        }
        TraceEntry te = {newState, partnerObject, partnerObject == NULL ? "null" : typeid(*partnerObject).name(), Time::getCoarse(), _sThreadUtil::getCurrentThreadId() };
        trace.add(te);

        #endif
         */

        //Cpp #ifndef NDEBUG
        if ((state & precondition) > 0) {
            state = newState;
            return true;
        }

        return false;
        /*Cpp
        #else
        return true;
        #endif
         */
    }


    /**
     * @return Index in reusable register
     */
    public int getRegisterIndex() {
        return registerIndex;
    }

    /** Called when reusable is deleted */
    protected void unregister() {
        if (registerIndex >= 0) {
            ReusablesRegister.unregister(this);
        }
    }

    /**
     * for C++-debugging only: Print trace of where object has been last used
     */
    public void printTrace(long startTime) {
        /*Cpp
        #ifdef DEFINE_ADV_REUSABLE_DEBUGGING_ENABLED
        printf("  trace: (recycle count: %d)\n", recycleCount);
        for (size_t i = 0; i < trace.size(); i++) {
            TraceEntry re = trace.get(i);
            printf("    state: %s  by/with %s %p (Thread: %lld, Time: %lld ms)\n", getStateString(re.state), re.partnerObjectClassName, re.partnerObject, re.threadId, re.timestamp - startTime);
        }
        #endif
         */
    }

    /*Cpp
    static const char* getStateString(int8 state) {
        switch(state) {
        case DELETED: return "DELETED";
        case ENQUEUED: return "ENQUEUED";
        case POST_QUEUED: return "POST_QUEUED";
        case RECYCLED: return "RECYCLED";
        case UNKNOWN: return "UNKNOWN";
        case USED: return "USED";
        }
        return "UNKNOWN/FISHY STATE";
    }
     */
}
