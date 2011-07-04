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
package org.rrlib.finroc_core_utils.jc.container;

import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.Virtual;

/**
 * @author max
 *
 * Abstract base class for all reusables
 */
@Ptr @Inline
@Include( {"definitions.h", "rrlib/serialization/DataTypeBase.h"})
public abstract class AbstractReusable extends Queueable {

    /** Index in reusable register */
    private int registerIndex = -1;

    /*Cpp

    #ifdef __JC_DETAILED_REUSABLE_TRACING_ENABLED__

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

    //Cpp #ifdef __JC_BASIC_REUSABLE_TRACING_ENABLED__
    /** Current state (for debugging only - comment out, if not needed to save memory) */
    private byte state = UNKNOWN;
    //Cpp #endif
    //Cpp public:

    /** Possible states (for debugging only) - these are bit flags */
    public static final byte UNKNOWN = 1, RECYCLED = 2, USED = 4, ENQUEUED = 8, POST_QUEUED = 16, DELETED = 32;

    /*Cpp

    AbstractReusable() :
            registerIndex(-1)
    #ifdef __JC_DETAILED_REUSABLE_TRACING_ENABLED__
            ,
            trace(),
            recycleCount(0)
    #endif
    #ifdef __JC_BASIC_REUSABLE_TRACING_ENABLED__
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
    protected void registerForIndex() {
        assert(registerIndex < 0);
        registerIndex = AllocationRegister.getInstance().indexReusable(this);
    }

    @JavaOnly
    protected AbstractReusable() {
        AllocationRegister.getInstance().registerReusable(this);
    }

    public void delete() {
//Cpp #ifdef __JC_BASIC_REUSABLE_TRACING_ENABLED__
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
        #ifdef __JC_DETAILED_REUSABLE_TRACING_ENABLED__

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

        //Cpp #ifdef __JC_BASIC_REUSABLE_TRACING_ENABLED__
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

    /*Cpp
    virtual void customDelete(bool calledFromGc) {
        deleteThis();
    }
     */

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
    @Virtual
    protected void deleteThis() {
        this.delete();
    }

    /**
     * for C++-debugging only: Print trace of where object has been last used
     */
    public void printTrace(@Ref @CppType("rrlib::logging::LogStream") Object output, long startTime) {
        /*Cpp
        #ifdef __JC_DETAILED_REUSABLE_TRACING_ENABLED__
        output << "   trace: (recycle count: " << recycleCount << ")" << std::endl;
        //printf("  trace: (recycle count: %d)\n", recycleCount);
        for (size_t i = 0; i < trace.size(); i++) {
            TraceEntry re = trace.get(i);
            output << "    state: " << getStateString(re.state) << "  by/with " << rrlib::serialization::DataTypeBase::getDataTypeNameFromRtti(re.partnerObjectClassName) << " " << re.partnerObject << " (Thread: " << re.threadId << ", Time: " << (re.timestamp - startTime) << " ms)" << std::endl;
            //printf("    state: %s  by/with %s %p (Thread: %lld, Time: %lld ms)\n", getStateString(re.state), re.partnerObjectClassName, re.partnerObject, re.threadId, re.timestamp - startTime);
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
