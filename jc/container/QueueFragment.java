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

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Queue fragment.
 *
 * Used to dequeue all elements of bounded WonderQueue at once
 */
public class QueueFragment<T, C extends BoundedQElementContainer> extends LogUser {

    /** Next element in fragment */
    protected C next = null;

    /** Last element in fragment - null if no more objects are left - will not be recycled, since it's still in queue */
    protected C last = null;

    /** Object of last element */
    protected T lastObject = null;

    /** "Previous" entry of last element */
    protected C lastPrev = null;

    /** Skip first/next element */
    public boolean skipFirst = false;

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("queue_impl");

    /**
     * Dequeue one element
     *
     * @return Next element in QueueFragment - null when there's none
     */
    @SuppressWarnings("unchecked")
    public T dequeue() {
        C n2 = next;
        if (last == null) {
            return null;
        } else if (n2 == last) {
            last = null;
            return skipFirst ? null : lastObject;
        } else {
            assert(n2 != null);
            BoundedQElementContainer nextX = n2.next2.get();
            if (nextX.isDummy()) {
                // rare preemption case: find next element from the back

                if (n2 == lastPrev) {
                    next = last;
                } else {
                    BoundedQElementContainer current = lastPrev;
                    while (current.prev != n2) {
                        current = current.prev;
                    }
                    //System.out.println("Rare preemption case: " + n2.toString() + " " + current.toString());
                    log(LogLevel.LL_DEBUG_VERBOSE_1, logDomain, "Rare preemption case: " + n2.toString() + " " + current.toString());
                    next = (C)current;
                }
                //assert(n2.count == current.count - 1);
                //n2 = current;
            } else {
                next = (C)nextX;
            }
        }
        T result = (T)n2.element;

        n2.recycle(false);
        if (result == null || skipFirst) {
            skipFirst = false;
            result = (T)dequeue();
        }
        return result;
    }

    /**
     * @param recycleContents Recycle contents?
     */
    @SuppressWarnings("unchecked")
    public void clear(boolean recycleContents) {

        if (last == next) {
            if (!skipFirst && lastObject != null) {
                last.recycleContent(lastObject);  // last can be null and this (pseudo-static) method still works
            }
            return;
        }

        if (lastObject != null) {
            last.recycleContent(lastObject);  // last can be null and this (pseudo-static) method still works
        }

        // from back to start - so we won't have any issues with any "next"s that are set too late
        if (lastPrev != null) {
            C ec = lastPrev;
            while (true) {
                C prev = (C)ec.prev;
                ec.recycle(recycleContents && (!(skipFirst && ec == next)));
                if (ec == next) {
                    next = null;
                    last = null;
                    return;
                }
                ec = prev;
            }
        }
    }

}
