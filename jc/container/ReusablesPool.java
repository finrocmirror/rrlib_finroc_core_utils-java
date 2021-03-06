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

import org.rrlib.finroc_core_utils.jc.GarbageCollector;

/**
 * @author Max Reichardt
 *
 * This class manages a pool of reusable objects.
 * Reusables are returned to their pool when they are recycled.
 *
 * This may happen concurrently. However, at most one thread at a time
 * may retrieve objects from the pool (for concurrently doing this,
 * use ReusablesPoolCR).
 *
 * Reusable elements know which pool they belong and are recycled to.
 * However, they may exists longer than the pool itself.
 *
 * The pool should not be deleted directly. Rather controlledDelete()
 * should be called (the pool will take care of the rest).
 * - The owningPool field of its element will be set to NULL
 * - It will be deleted deferred (by the garbage collector to avoid dangerous race conditions)
 */
public class ReusablesPool<T extends Reusable> extends AbstractReusablesPool<T> {

    /** Wrapped Queue */
    private WonderQueueFast<T> wrapped = new WonderQueueFast<T>();

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
    public void attach(T r, boolean enqueueNow) {
        assert r.nextInBufferPool == null;
        assert r.owner == null;
        r.nextInBufferPool = lastCreated;
        lastCreated = r;
        r.owner = wrapped;
        allocationRegisterLock.trackReusable(r);
        assert(r.stateChange(AbstractReusable.UNKNOWN, enqueueNow ? AbstractReusable.RECYCLED : AbstractReusable.USED, wrapped));
        if (enqueueNow) {
            wrapped.enqueue(r);
        }
    }

    /**
     * @return Element from pool - or null, if all are currently in use
     */
    public T getUnused() {
        T result = wrapped.dequeue();
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
    public void controlledDelete() {

        // Set pool pointers of all elements to null
        Reusable elem = lastCreated;
        while (elem != null) {
            elem.owner = null;
            Reusable temp = elem.nextInBufferPool;
            elem.nextInBufferPool = null; // safer and avoids unnecessary memory consumption in Java
            elem = temp;
        }
        GarbageCollector.deleteDeferred(this); // should be last instruction
    }

    @Override
    public void delete() {
        wrapped.deleteEnqueued();
    }
}
