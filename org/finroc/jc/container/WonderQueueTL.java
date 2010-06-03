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

import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.RawTypeArgs;

/**
 * @author max
 *
 * This is the thread-local ("TL")/not-thread-safe variant of RawWonderQueue.
 * (non-blocking FIFO linked queue - real-time-capable, since it does not need
 * to allocate memory.)
 *
 * In this variant, all elements are dequeued.
 * It's also the fastest variant.
 */
@Inline @NoCpp
class RawWonderQueueTL extends Queueable {

    /** Pointer to last element in queue - never null */
    private @Ptr Queueable last = this;

    public RawWonderQueueTL() {
        next = this;
    }

    /**
     * Add element to the end of the queue.
     * (May only be called by a single writer thread concurrently.)
     *
     * @param pd Element to enqueue
     */
    @Inline public void enqueueRaw(@Ptr Queueable pd) {
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
    @Inline public @Ptr Queueable dequeueRaw() {
        @Ptr Queueable result = next;
        if (result == this) {
            return null;
        }
        @Ptr Queueable nextnext = result.next;
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
    @Inline public @Ptr Queueable peekRaw() {
        @Ptr Queueable result = next;
        if (result == this) {
            return null;
        }
        return result;
    }
}

/**
 * @author max
 *
 * This is the thread-local ("TL")/not-thread-safe variant of WonderQueue.
 * (non-blocking FIFO linked queue - real-time-capable, since it does not need
 * to allocate memory.)
 *
 * In this variant, all elements are dequeued.
 * It's also the fastest variant.
 */
@Inline @NoCpp @RawTypeArgs
public class WonderQueueTL<T extends Queueable> extends RawWonderQueueTL {

    /**
     * Add element to the end of the queue.
     * (May only be called by a single writer thread concurrently.)
     *
     * @param pd Element to enqueue
     */
    @NonVirtual @Inline public void enqueue(@Ptr T pd) {
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    @NonVirtual @Inline public @Ptr T dequeue() {
        return (T)dequeueRaw();
    }

    /**
     * Return first element in queue with dequeueing it.
     *
     * @return First element in queue - null if there's none
     */
    @SuppressWarnings("unchecked")
    @NonVirtual @Inline public @Ptr T peek() {
        return (T)peekRaw();
    }

    /**
     * (meant for use in destructors)
     * Delete all elements that are enqueued in this list
     */
    public void deleteEnqueued() {
        while (true) {
            @Ptr T r = dequeue();
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
