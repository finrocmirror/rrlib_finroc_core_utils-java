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

import org.rrlib.finroc_core_utils.jc.AtomicPtr;

/**
 * @author Max Reichardt
 *
 * This is a special concurrent non-blocking FIFO linked queue (untyped version).
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations and there is no size limit.
 * Unlike RawWonderQueueFastCR, only a single thread may read from this queue.
 *
 * This queue is faster than RawWonderQueue.
 * Drawback: the queue always contains at least one element...
 * (for efficiency reasons; problem
 * is that 'last' will point to reused element if last element is dequeued; maybe
 * there's a better way (?) )
 */
class RawWonderQueueFast extends Queueable {

    /** Pointer to last element in queue - never null */
    private final AtomicPtr<Queueable> last = new AtomicPtr<Queueable>(this);

    /** Atomic Pointer to next element in queue - only relevant for concurrent reading */
    protected final AtomicPtr<Queueable> nextCR = new AtomicPtr<Queueable>(this);

    /**
     * @param concurrentReaders Is this queue meant for concurrent reading? (appropriate dequeue method needs to be called depending on choice)
     */
    public RawWonderQueueFast(boolean concurrentDequeue) {
        next = concurrentDequeue ? null : terminator;
    }

    /**
     * Add element to the end of the queue.
     * (Is thread safe and non-blocking/non-waiting)
     *
     * @param pd Element to enqueue
     */
    public void enqueueRaw(Queueable pd) {

        // swap last pointer
        Queueable prev = last.getAndSet(pd);

        // set "next" of previous element
        //assert(!(pd instanceof RawWonderQueueTL));
        assert(prev != pd);
        prev.next = pd;
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no elements available
     */
    public Queueable dequeueRaw() {
        Queueable result = next;
        Queueable nextnext = result.next;
        if (nextnext == terminator || nextnext == null) {
            return null;
        }
        next = nextnext;
        result.next = null;
        return result;
    }

    /**
     * Remove first element from queue and return it.
     * (May be called by multiple threads concurrently.)
     *
     * @return Element that was dequeued - null if no elements available
     */
    public Queueable concurrentDequeueRaw() {
        while (true) {
            Queueable result = nextCR.get();
            Queueable nextnext = result.next;
            if (nextnext == this || nextnext == null) {
                return null;
            }
            if (nextCR.compareAndSet(result, nextnext)) {
                result.next = null;
                return (result == this) ? concurrentDequeueRaw() : result;
            }
        }
    }
}

/**
 * @author Max Reichardt
 *
 * This is a special concurrent non-blocking FIFO linked queue.
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations and there is no size limit.
 *
 * Only a single thread may read from this queue.
 *
 * This queue is faster than WonderQueue, but slower that WonderQueueTL.
 * Drawback: the queue always contains at least one element...
 * (for efficiency reasons; problem
 * is that 'last' will point to reused element if last element is dequeued; maybe
 * there's a better way (?) )
 */
public class WonderQueueFast<T extends Queueable> extends RawWonderQueueFast {

    public WonderQueueFast() {
        super(false);
    }

    /**
     * Add element to the end of the queue.
     * Is thread safe and non-blocking/non-waiting
     *
     * @param pd Element to enqueue
     */
    public void enqueue(T pd) {
        //System.out.println("enqueueing " + pd.toString());
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * May only be called by a single reader thread concurrently.
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        return (T)dequeueRaw();
    }

    /**
     * Remove first element from queue and return it.
     * May only be called by a single reader thread concurrently.
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    public T concurrentDequeue() {
        return (T)dequeueRaw();
    }

    /**
     * (meant for use in destructors)
     * Delete all elements that are enqueued in this list
     */
    public void deleteEnqueued() {
        while (true) {
            T r = dequeue();
            if (r == null) {
                break;
            }
            r.delete();
        }

        // delete last element
        if ((this.next != Queueable.terminator) && this.next != this) {
            this.next.delete();
        }
    }
}
