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


/**
 * @author Max Reichardt
 *
 * This is the thread-local ("TL")/not-thread-safe variant of RawWonderQueue.
 * (non-blocking FIFO linked queue - real-time-capable, since it does not need
 * to allocate memory.)
 *
 * In this variant, all elements are dequeued.
 * It's also the fastest variant.
 */
class RawWonderQueueTL extends Queueable {

    /** Pointer to last element in queue - never null */
    private Queueable last = this;

    public RawWonderQueueTL() {
        next = this;
    }

    /**
     * Add element to the end of the queue.
     * (May only be called by a single writer thread concurrently.)
     *
     * @param pd Element to enqueue
     */
    public void enqueueRaw(Queueable pd) {
        last.next = pd;
//      pd.next = this;
        last = pd;
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no elements available
     */
    public Queueable dequeueRaw() {
        Queueable result = next;
        if (result == this) {
            return null;
        }
        Queueable nextnext = result.next;
        if (nextnext == null) { // now empty
            last = this;
            nextnext = this;
        }
        next = nextnext;
        result.next = null;
        return result;
    }

    /**
     * Return first element in queue with dequeueing it.
     *
     * @return First element in queue - null if there's none
     */
    public Queueable peekRaw() {
        Queueable result = next;
        if (result == this) {
            return null;
        }
        return result;
    }
}

/**
 * @author Max Reichardt
 *
 * This is the thread-local ("TL")/not-thread-safe variant of WonderQueue.
 * (non-blocking FIFO linked queue - real-time-capable, since it does not need
 * to allocate memory.)
 *
 * In this variant, all elements are dequeued.
 * It's also the fastest variant.
 */
public class WonderQueueTL<T extends Queueable> extends RawWonderQueueTL {

    /**
     * Add element to the end of the queue.
     * (May only be called by a single writer thread concurrently.)
     *
     * @param pd Element to enqueue
     */
    public void enqueue(T pd) {
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        return (T)dequeueRaw();
    }

    /**
     * Return first element in queue with dequeueing it.
     *
     * @return First element in queue - null if there's none
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        return (T)peekRaw();
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
    }

    public boolean isEmpty() {
        return peekRaw() == null;
    }
}
