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

import org.rrlib.finroc_core_utils.jc.HasDestructor;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.jc.stampedptr.AtomicStampedPtrIdx64;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * This is a special concurrent non-blocking FIFO linked queue.
 * It should be real-time-capable, since it does not need to allocate memory.
 *
 * It allows concurrent enqueueing operations. There is an optional(!) size limit.
 * Only a single thread may read from this queue.
 * Multiple may write.
 *
 * In this variant, all elements can be dequeued.
 *
 * The implementation is quite "foxed-out" - non-blocking and very efficient.
 *
 *
 *
 * Implementation strategy/rationale:
 *
 * New elements are enqueued - by atomically setting last element to new element - next element of previous one is set delayed.
 *   To avoid ABA problem, a counter stamp is used.
 * First element may only be used after first variable has been successfully atomically set.
 *
 * Stamp (in first) contains the following information:
 *   [1bit sign dummy]
 *   [1bit flag: dq thread lock - do not adjust length]
 *   [1bit flag: signal that element has already been used/dequeued - do not use/recycle content anymore (enters this state when dequeueing last element]
 *   [29bit counter - wrapped around]
 */
public abstract class WonderQueueBounded<T, C extends BoundedQElementContainer> extends LogUser implements HasDestructor {

    /** See class comment for meanings */
    //private static final int DQ_THREAD_LOCK_FLAG = 0x40000000;
    private static final int DONT_USE_FLAG =       0x20000000;
    private static final int COUNTER_MASK =        0x1FFFFFFF;
    private static final int COUNTER_WRAP = COUNTER_MASK + 1;

    /** Last element in queue - the one that was most recently added - never null - stamp is element index*/
    protected final AtomicStampedPtrIdx64<C> last = new AtomicStampedPtrIdx64<C>();

    /** First/oldest element in queue - stamp is element index - negative stamp means that object has already been dequeued: do not recycle element */
    protected final AtomicStampedPtrIdx64<C> first = new AtomicStampedPtrIdx64<C>();

    /**
     * Maximum length of queue - due to threading/efficiency issues queue can sometimes
     * become slightly longer than this.
     * Negative values signal that there's no limit
     */
    private int maxLength = -1;

    /** Dummy object to signal that we need to retry an operation */
    private static final Object RETRY = new Object();

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("queue_impl");

    public WonderQueueBounded() {
        BoundedQElementContainer.staticInit();
    }

    /**
     * @param maxLength Maximum length of queue - Negative values signal that there's no limit
     */
    public WonderQueueBounded(int maxLength) {
        BoundedQElementContainer.staticInit();
        this.maxLength = maxLength;
    }

    public void delete() {
        clear(false);

        // recycle last element
        C last = first.getPointer();
        last.recycle(false);
    }

    /**
     * Needs to be called before queue can be used
     * This is not in the constructor, because C++ cannot handle virtual methods there :-/
     */
    public void init() {
        C c = getEmptyContainer();
        assert(c.stateChange((byte)(Reusable.UNKNOWN | Reusable.USED), Reusable.ENQUEUED, this));
        last.set(c, 1);
        first.set(last.getRaw());
        c.next2.set(BoundedQElementContainer.getDummy(0));
    }

    /**
     * (Should definetly be overriden by subclasses)
     * @return Returns empty container, as it is used in this queue
     */
    protected abstract C getEmptyContainer();

    /**
     * Add element to the end of the queue.
     * (Is thread safe and non-blocking in C++)
     *
     * @param pd Element to enqueue
     */
    public void enqueueWrapped(T o) {
        C c = getEmptyContainer();
        assert(c.element == null);
        assert(!c.isDummy());
        c.element = o;
        enqueueDirect(c);
    }

    /**
     * Add element to the end of the queue.
     * (Is thread safe and non-blocking)
     *
     * @param pd Element to enqueue
     * @return Element index in queue
     */
    public void enqueueDirect(C pd) {

        assert(pd.stateChange((byte)(Reusable.UNKNOWN | Reusable.USED), Reusable.ENQUEUED, this));

        // swap last pointer
        BoundedQElementContainer prev = null;
        long raw = 0;
        assert(pd.next2.get() == BoundedQElementContainer.getDummy(pd.reuseCounter));
        int count = 0;
        int lastCounter = 0;
        while (true) {
            raw = last.getRaw();
            prev = last.getPointer(raw);
            assert(prev != null); // forgot to call init?
            count = (last.getStamp(raw) + 1) & COUNTER_MASK;
            pd.prev = prev;
            assert(prev != pd); // this would be a desaster - prev could have been reused
            lastCounter = prev.reuseCounter;
            if (last.compareAndSet(raw, pd, count)) {
                break;
            }

            // ahh... fail (rare case)
            pd.prev = null;
        }

        // set "next" of previous element (keep in mind: is possibly delayed if thread is preempted before this instruction.)
        assert(pd != null);
//      boolean rec = prev.recycled;
//      if (rec) {
//          System.out.println("recycled... careful");
//      }
        boolean s = prev.next2.compareAndSet(BoundedQElementContainer.getDummy(lastCounter), pd); // only set, if still needed
//      assert(!rec || !s);
        if (!s) {
            log(LogLevel.LL_DEBUG_VERBOSE_1, logDomain, "Skipped setting next");
        }

        // adjust length - if size exceeds maximum length
        adjustLength(count);
    }

    /**
     * Adjust length if queue length is greater than maxLength
     *
     * @param currentElemIndex Index of element that was inserted last
     */
    @SuppressWarnings("unchecked")
    private void adjustLength(int currentElemIndex) {

        int maxLen = maxLength; // create local copy of maxLength
        if (maxLen <= 1) {
            return;
        }

        // check whether we might need to dequeue
        long rawFirst = first.getRaw();
        int firstCountRaw = first.getStamp(rawFirst);
        int firstCount = firstCountRaw & COUNTER_MASK;
        C firstElem = first.getPointer(rawFirst);
//      if ((firstCountRaw & DQ_THREAD_LOCK_FLAG) != 0) {
//          return; // okay... dequeue thread is currently active... do nothing
//      }

        while (currentElemIndex - firstCount + ((currentElemIndex >= firstCount) ? 0 : COUNTER_WRAP) >= maxLen) { // not thread-safe at all... but won't break anything... and good enough for estimate... worst case: queue might be a little too long
            BoundedQElementContainer next = firstElem.next2.get();
            if (next.isDummy()) {
                return; // wow! we're having some rare preemption delays... never mind... queue might be a little too long
            }

            long rawNext = first.merge((C)next, firstCount + 1);
            if (!first.compareAndSet(rawFirst, rawNext)) {
                return; // someone else is dealing with this... he will shorten the list... maybe not enough, but never mind... somebody will do it
            }

            firstElem.recycle((firstCountRaw & DONT_USE_FLAG) == 0);

            rawFirst = rawNext;
            firstCount++;
            firstCountRaw = firstCount;
            firstElem = (C)next;
        }
    }

    /**
     * @param maxLength New maximum queue length
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength; // length will possibly be reduced with next enqueueing operations
    }

    /**
     * @return maximum queue length
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Remove first element from queue and return it.
     * (May only be called by a single reader thread concurrently.)
     * (is slightly inefficient - use dequeueAll for better performance)
     *
     * @return Element that was dequeued - null if no element is available
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        Object result = null;
        do {
            result = dequeue2();
        } while (result == RETRY);
        return (T)result;
    }

    /**
     * Helper method for above
     */
    @SuppressWarnings("unchecked")
    private Object dequeue2() {

        T result = null;

        long rawFirst = first.getRaw();
        int firstCountRaw = first.getStamp(rawFirst);
        int firstCount = firstCountRaw & COUNTER_MASK;
        C firstElem = first.getPointer(rawFirst);

        // empty element at start that needs to be removed?
        if (firstElem.element == null || (firstCountRaw & DONT_USE_FLAG) != 0) {
            BoundedQElementContainer next = firstElem.next2.get();
            if (next.isDummy()) {
                // okay... queue is still empty
                return null;
            }

            long rawNext = first.merge((C)next, firstCount + 1);
            if (!first.compareAndSet(rawFirst, rawNext)) {
                //System.out.println("dqr2: Retry " + firstCount + 1);
                return RETRY; // ah... somebody else changed something... we need to retry
            }


            firstElem.recycle(false);
            firstElem = (C)next;
            rawFirst = rawNext;
            firstCount++;
        }

        // dequeue next element
        BoundedQElementContainer next = firstElem.next2.get();
        result = (T)firstElem.element;

        // last element in queue? - leave it there, but mark it as read
        if (next.isDummy()) {
            assert(!firstElem.isDummy());
            assert(firstCount > 0);
            if (!first.compareAndSet(rawFirst, firstElem, firstCount | DONT_USE_FLAG)) {
                //System.out.println("dqr2: Retry " + firstCount + 1);
                return RETRY; // ah... somebody else changed something... we need to retry
            }
            return result;
        }

        long rawNext = first.merge((C)next, firstCount + 1);
        if (!first.compareAndSet(rawFirst, rawNext)) {
            //System.out.println("dqr2: XRetryX " + firstCount + 1);
            return RETRY; // ah... somebody else changed something... we need to retry
        }

        assert(!firstElem.isDummy());
        firstElem.element = null;
        firstElem.recycle(false);
        return result;
    }

    /**
     * Remove all elements from queue and put them into specified fragment.
     *
     * @param wqf Empty fragment. Elements will dequeued and put into this fragment. There they can be handled very efficiently.
     */
    @SuppressWarnings("unchecked")
    public void dequeueAll(QueueFragment<T, C> wqf) {

        // Idea: last element remains in queue. It is marked "DONT_USE" and its object is passed to fragment.

        assert(wqf.dequeue() == null);

        long rawFirst = 0;
        long rawLast = 0;
        int lastStamp = 0;

        // adjust first and last pointers
        while (true) {

            // read current first and last elements
            rawFirst = first.getRaw();
            rawLast = last.getRaw();

            // decode last entry
            wqf.last = last.getPointer(rawLast);
            lastStamp = last.getStamp(rawLast);

            // store some values of last entry
            wqf.lastObject = (T)wqf.last.element;
            wqf.lastPrev = (C)wqf.last.prev;

            if (first.compareAndSet(rawFirst, wqf.last, lastStamp | DONT_USE_FLAG)) {
                break;
            }
        }

        wqf.skipFirst = (first.getStamp(rawFirst) & DONT_USE_FLAG) > 0;
        wqf.next = first.getPointer(rawFirst);
    }

    /**
     * Clear elements of queue
     *
     * @param recycleContents recycle queue's contents?
     */
    public void clear(boolean recycleContents) {
        QueueFragment<T, C> f = new QueueFragment<T, C>();
        dequeueAll(f);
        f.clear(recycleContents);
    }
}