/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2009-2010 Max Reichardt,
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
package org.finroc.serialization;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NoSuperclass;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.Struct;
import org.finroc.jc.annotation.VoidPtr;

/**
 * Buffer information
 * (can be passed to and modified by Manager (by reference))
 */
@NoSuperclass @Struct @NoCpp @Inline
@Include("<cstddef>")
public class BufferInfo {

    /** Buffer that read view currently operates on */
    @Ptr public FixedBuffer buffer = null;

    /** Start of buffer */
    public @SizeT int start = 0;

    /** End of buffer */
    public @SizeT int end = 0;

    /** Current read or write position */
    public @SizeT int position = 0;

    /** Custom data that can be filled by source/sink that manages this buffer */
    @VoidPtr public Object customData = null;

    /**
     * @param other Other Buffer Info to copy values from
     */
    public void assign(@Const @Ref BufferInfo other) {
        buffer = other.buffer;
        start = other.start;
        end = other.end;
        position = other.position;
        customData = other.customData;
    }

    /**
     * Reset info to default/null values
     */
    public void reset() {
        buffer = null;
        start = 0;
        end = 0;
        position = 0;
        customData = null;
    }

    /**
     * @return Remaining bytes in buffer
     */
    @ConstMethod public @SizeT int remaining() {
        return end - position;
    }

    /**
     * @return Number of bytes to write (for Sinks)
     */
    @ConstMethod public @SizeT int getWriteLen() {
        return position - start;
    }

    /**
     * Set start and end position in buffer backend
     *
     * @param start Start position (inclusive)
     * @param end End position (exclusive
     */
    public @SizeT void setRange(@SizeT int start, @SizeT int end) {
        this.start = start;
        this.end = end;
    }

    @JavaOnly
    public String toString() {
        if (buffer == null) {
            return "no buffer backend";
        } else {
            return "start: " + start + " position: " + position + " end: " + end;
        }
    }

    /**
     * @return Total size of buffer - as described by this BufferInfo object: end - start
     */
    @ConstMethod public @SizeT int capacity() {
        return end - start;
    }
}