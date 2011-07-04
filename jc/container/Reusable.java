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

import org.rrlib.finroc_core_utils.jc.annotation.Friend;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.NonVirtual;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;

/**
 * @author max
 *
 * This is an element of a ReusablePool or a ReusablePoolCR
 */
@Inline @NoCpp @Friend( {ReusablesPool.class, ReusablesPoolCR.class}) @Ptr
public class Reusable extends AbstractReusable {

    /**
     * Pool that owns this data - null if doesn't exist anymore -
     * in this case this Reusable will be deleted when recycled
     * shouldn't need to be volatile, since owner deletion is deferred
     */
    @Ptr protected RawWonderQueueFast owner;

    /** Next element in this buffer pool - new elements are prepended  - set to null, when pool is deleted */
    @Ptr protected Reusable nextInBufferPool;

    /** Recycle object - after calling this method, object is available in ReusablesPool it originated from */
    @Inline @NonVirtual
    protected void recycle() {
        @Ptr RawWonderQueueFast o = owner;
        assert(stateChange((byte)(Reusable.UNKNOWN | Reusable.USED | POST_QUEUED), Reusable.RECYCLED, owner));
        if (o != null) {
            owner.enqueueRaw(this);
        } else { // Owner pool has been deleted... no longer needed

            deleteThis(); // hehe... taking everything into account, this seems a good and safe choice (IMPORTANT: last statement in method)
        }
    }
}