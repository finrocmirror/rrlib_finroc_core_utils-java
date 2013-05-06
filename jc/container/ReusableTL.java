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
 * This is the static thread-local ("TL")/not-thread-safe variant of Reusable.
 * (Recycling and getting unused objects may only be done by one thread concurrently)
 */
public class ReusableTL extends AbstractReusable {

    /**
     * Pool that owns this data - null if doesn't exist anymore -
     * in this case this Reusable will be deleted when recycled
     */
    protected RawWonderQueueTL owner;

    /** Next element in this buffer pool - new elements are prepended  - set to null, when pool is deleted */
    protected ReusableTL nextInBufferPool;

    /**
     * Recycle object - after calling this method, object is available in ReusablesPool it originated from
     * Will only be called in while thread is running. And owner will exists during this time.
     */
    protected void recycle() {
        assert(stateChange((byte)(Reusable.UNKNOWN | Reusable.USED | POST_QUEUED), Reusable.RECYCLED, owner));
        assert(owner != null); // method should only be callable while owner exists (?)
        owner.enqueueRaw(this);
    }
}
