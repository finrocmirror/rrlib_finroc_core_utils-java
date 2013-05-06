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
 * This is the static thread-local ("TL")/not-thread-safe variant of ReusablesPoolTL.
 */
public class ReusablesPoolTL<T extends ReusableTL> extends AbstractReusablesPool<T> {

    /** Wrapped Queue */
    private WonderQueueTL<T> wrapped = new WonderQueueTL<T>();

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
        ReusableTL elem = lastCreated;
        while (elem != null) {
            elem.owner = null;
            ReusableTL temp = elem.nextInBufferPool;
            elem.nextInBufferPool = null; // safer and avoids unnecessary memory consumption in Java
            elem = temp;
        }

        this.delete(); // my favourite line :-)  Due to thread-local-nature we do not need safe delete using garbage collector
    }

    @Override
    public void delete() {
        wrapped.deleteEnqueued();
    }
}
