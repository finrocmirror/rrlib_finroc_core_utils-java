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

import org.finroc.jc.GarbageCollector;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.ForwardDecl;
import org.finroc.jc.annotation.Friend;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.RawTypeArgs;
import org.finroc.jc.annotation.Virtual;

/**
 * @author max
 *
 * This is the static thread-local ("TL")/not-thread-safe variant of ReusablesPoolTL.
 */
@ForwardDecl( {ReusableTL.class, GarbageCollector.class})
@Friend(GarbageCollector.class)
@CppInclude("ReusableTL.h")
@Ptr @RawTypeArgs
public class ReusablesPoolTL<T extends ReusableTL> extends AbstractReusablesPool<T> {

    /** Wrapped Queue */
    @PassByValue WonderQueueTL<T> wrapped = new WonderQueueTL<T>();

    /**
     * Attaches (and enqueues) a Reusable object to this pool.
     * The object will be returned to the pool when recycled.
     *
     * The method is typically called during initialization of a Reusable.
     *
     * Should only be called by owner thread.
     *
     * @param r Reusable to attach and enqueue
     * @param enqueueNow Enqueue attached element or use directly?
     */
    public void attach(@Ptr T r, boolean enqueueNow) {
        assert r.nextInBufferPool == null;
        assert r.owner == null;
        r.nextInBufferPool = lastCreated;
        lastCreated = r;
        r.owner = wrapped;
        //Cpp #ifndef NDEBUG
        allocationRegisterLock.trackReusable(r);
        //Cpp #endif
        if (enqueueNow) {
            wrapped.enqueue(r);
        }
    }

    /**
     * @return Element from pool - or null, if all are currently in use
     */
    public @Ptr T getUnused() {
        @Ptr T result = wrapped.dequeue();
        assert(result == null || result.stateChange((byte)(Reusable.UNKNOWN | Reusable.RECYCLED), Reusable.USED, wrapped));
        return result;
    }

    /**
     * Controlled deletion of this pool.
     * No more objects will be recycled to this pool and the pool
     * will be deleted with a delay.
     *
     * Should only be called by owner thread.
     */
    @Virtual public void controlledDelete() {

        // Set pool pointers of all elements to null
        @Ptr ReusableTL elem = lastCreated;
        while (elem != null) {
            elem.owner = null;
            @Ptr ReusableTL temp = elem.nextInBufferPool;
            elem.nextInBufferPool = null; // safer and avoids unnecessary memory consumption in Java
            elem = temp;
        }

        //Cpp delete this; // my favourite line :-)  Due to thread-local-nature we do not need safe delete using garbage collector
    }

    /*Cpp
    protected:
    // destructor is intentionally protected: call controlledDelete() instead
    virtual ~ReusablesPoolTL() {
        wrapped.deleteEnqueued();
    }
     */
}
