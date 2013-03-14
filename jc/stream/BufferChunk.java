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
package org.rrlib.finroc_core_utils.jc.stream;

import org.rrlib.finroc_core_utils.jc.AtomicDoubleInt;
import org.rrlib.finroc_core_utils.jc.container.Reusable;
import org.rrlib.finroc_core_utils.serialization.FixedBuffer;

/**
 * @author Max Reichardt
 *
 * Single chunk that is used in a chunk buffer
 */
public class BufferChunk extends Reusable {

    /** Size of a single buffer chunk */
    public static final int CHUNK_SIZE = 8192;

    /** Next chunk in singly-linked list */
    public volatile BufferChunk next = null;

    /** Buffer that this object provides */
    public final FixedBuffer buffer;

    /**
     * [1 bit] Is a following chunk available? (Updated _after_ next has been set)
     * [30bit] Number of bytes written to this chunk? (Updated _after_ data was written => it's available for reader)
     *
     * Updated before writtenBytes of ChunkedBuffer
     */
    public AtomicDoubleInt curSize = new AtomicDoubleInt(1, 30);

    /** (Virtual) absolute start position in ChunkedBuffer */
    public long virtualPosition = -1;

    public BufferChunk() {
        buffer = new FixedBuffer(CHUNK_SIZE);
    }

    /**
     * Recycle Chunk
     */
    public void recycle() {
        next = null;
        curSize.set(0, 0);
        virtualPosition = 0;
        super.recycle();
    }
}
