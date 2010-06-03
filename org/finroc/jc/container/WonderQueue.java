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

import org.finroc.jc.ArrayWrapper;
import org.finroc.jc.AtomicPtr;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.RawTypeArgs;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.thread.SpinLock;

/**
 * @author max
 *
 * Implementation of class below
 */
@Include( {"Atomic.h"}) @Inline @NoCpp
class RawWonderQueue extends Queueable {

    /** Spin lock for concurrent access */
    @InCpp("SpinMutex mutex;")
    private final SpinLock lock = new SpinLock();

    /**
     * Pointer to last element in queue - never null
     * This variable us used for "communication" among writers
     */
    private final AtomicPtr<Queueable> last = new AtomicPtr<Queueable>(this);

    /**
     * Temporary last object (for dequeueing) - this way all elements can be dequeued.
     * points to this, if no element is available that has not been read already.
     */
    private @Ptr Queueable readLast = this;

    /**
     * Next element after readLast
     *
     * This variable is used for "communication" between writer and reader.
     */
    private final AtomicPtr<Queueable> nextFirst = new AtomicPtr<Queueable>(null);

    @Init("mutex()")
    public RawWonderQueue() {
        next = null;
    }

    /**
     * Add element to the end of the queue.
     * (Is thread safe and non-blocking/non-waiting)
     *
     * @param pd Element to enqueue
     */
    @Inline public void enqueueRaw(@Ptr Queueable pd) {

        // swap last pointer
        @Ptr Queueable prev = last.getAndSet(pd);

        if (prev == this) {
            assert(nextFirst.get() == null);
            nextFirst.set(pd);
        } else {
            // set "next" of previous element
            prev.next = pd;
        }
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     *
     * @return Element that was dequeued - null if no element is available
     */
    @Inline public @Ptr Queueable dequeueRaw() {
        @Ptr Queueable next = this.next;
        if (next == null) { // does readLast need updating?
            next = nextFirst.getAndSet(null);
            if (next == null) {
                return null; // queue empty
            }
            readLast = last.getAndSet(this);
        }

        if (next == readLast) {
            this.next = null;
            return next;
        }
        @Ptr Queueable nextnext = next.next;
        if (nextnext == null) { // can occur with delayed/preempted enqueue operations (next is set later and is not volatile)
            return null; // queue is not empty, but elements are not fully available yet
        }
        this.next = nextnext;
        next.next = null;
        return next;
    }

    /**
     * Remove first element from queue and return it.
     * (May be called by multiple threads concurrently. Uses locks => not non-blocking)
     *
     * @return Element that was dequeued - null if no element is available
     */
    @InCpp( {"SpinLock(mutex);", "return dequeueRaw();"})
    @Inline public @Ptr Queueable concurrentDequeueRaw() {
        lock.lock();
        @Ptr Queueable result = dequeueRaw();
        lock.release();
        return result;
    }

    /**
     * Dequeue multiple elements at once
     *
     * @param buffer Buffer to write result to
     * @param maxElements Maximum number of elements to dequeue
     * @return Actual number of elements dequeued (can be less if queue has lass elements)
     */
    @SuppressWarnings("unchecked")
    public @SizeT int dequeueRaw(@Ptr ArrayWrapper buffer, @SizeT int maxElements) {
        @SizeT int pos = 0;

        // first run: dequeue remaining elements in current chunk
        // second run: dequeue remaining elements in updated chunk
        for (int i = 0;; i++) {
            while (next != null && pos < maxElements) {
                if (next == readLast) {
                    next = null;
                }
                @Ptr Queueable nextnext = next.next;
                if (nextnext == null) { // can occur with delayed/preempted enqueue operations (next is set later and is not volatile)
                    return pos; // queue is not empty, but elements are not fully available yet
                }
                next = nextnext;
                next.next = null;

                // JavaOnlyBlock
                buffer.set(pos, next);

                //Cpp buffer->set(pos, next);
                pos++;
            }

            if (i == 1 || pos == maxElements) { // terminate after second run
                return pos;
            }

            // fetch updated chunk
            next = nextFirst.getAndSet(null);
            if (next == null) {
                return pos; // queue empty
            }
            readLast = last.getAndSet(this);
        }
    }
}

/**
 * @author max
 *
 * This is a special concurrent non-blocking FIFO linked queue.
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations and there is no size limit.
 * Unlike RawWonderQueueFastCR, only a single thread may read from this queue.
 *
 * In this variant, all elements can be dequeued.
 * Internally, it does this by dequeueing all elements internally and then returning
 * them one by one.
 */
@Inline @NoCpp @RawTypeArgs
public class WonderQueue<T extends Queueable> extends RawWonderQueue {

    /**
     * Add element to the end of the queue.
     * Is thread safe and non-blocking/non-waiting
     *
     * @param pd Element to enqueue
     */
    @NonVirtual @Inline public void enqueue(@Ptr T pd) {
        enqueueRaw(pd);
    }

    /**
     * Remove first element from queue and return it.
     * May only be called by a single reader thread concurrently.
     *
     * @return Element that was dequeued - null if no elements available
     */
    @SuppressWarnings("unchecked")
    @NonVirtual @Inline public @Ptr T dequeue() {
        return (T)dequeueRaw();
    }

    /**
     * Dequeue multiple elements at once
     *
     * @param buffer Buffer to write result to
     * @param maxElements Maximum number of elements to dequeue
     * @return Actual number of elements dequeued (can be less if queue has lass elements)
     */
    public int dequeue(@Ptr ArrayWrapper<T> buffer, int maxElements) {
        return dequeueRaw(buffer, maxElements);
    }

    /**
     * Remove first element from queue and return it.
     * (May be called by multiple threads concurrently. Uses locks => not non-blocking)
     *
     * @return Element that was dequeued - null if no element is available
     */
    @SuppressWarnings("unchecked")
    @Inline public @Ptr T concurrentDequeue() {
        return (T)concurrentDequeueRaw();
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
}
