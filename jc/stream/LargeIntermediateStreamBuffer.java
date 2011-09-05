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

import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.SharedPtr;
import org.rrlib.finroc_core_utils.serialization.BufferInfo;
import org.rrlib.finroc_core_utils.serialization.FixedBuffer;
import org.rrlib.finroc_core_utils.serialization.MemoryBuffer;
import org.rrlib.finroc_core_utils.serialization.OutputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.Sink;

/**
 * @author max
 *
 * A problem with ordinary stream buffers is that skip offsets often cannot be written
 * when the data in between is very large.
 * In this case, this class can be used as an intermediate buffer.
 * It will grow until data is flushed
 */
public class LargeIntermediateStreamBuffer extends MemoryBuffer {

    /*Cpp
    // for "locking" object sink as long as this buffer exists
    std::shared_ptr<const rrlib::serialization::Sink> sinkLock;
    */

    /** Sink for intermediate buffer */
    private final Sink sink;

    /** Dummy buffer info */
    @PassByValue private BufferInfo dummyInfo = new BufferInfo();

    /*Cpp
    LargeIntermediateStreamBuffer(Sink* sink_) : sinkLock(), sink(sink_), dummyInfo()
    {}
     */

    @Init("sinkLock(sink_)")
    public LargeIntermediateStreamBuffer(@PassByValue @SharedPtr Sink sink) {
        this.sink = sink;
    }

    /**
     * Flush all contents of intermediate buffer to sink
     *
     * @param byteCount Number of bytes to write
     */
    private void flushContents(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        if (buffer.position > buffer.start) {
            sink.directWrite(outputStreamBuffer, backend, buffer.start, buffer.position);
            buffer.position = buffer.start;
        }
    }

    @Override
    public void close(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        flushContents(outputStreamBuffer, buffer);
        super.close(outputStreamBuffer, dummyInfo);
    }

    @Override
    public void directWrite(OutputStreamBuffer outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        sink.directWrite(outputStreamBuffer, buffer, offset, len);
    }

    @Override
    public boolean directWriteSupport() {
        return sink.directWriteSupport();
    }

    @Override
    public void reset(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        super.reset(outputStreamBuffer, buffer);
        sink.reset(outputStreamBuffer, dummyInfo);
    }

    @Override
    public boolean write(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer, int writeSizeHint) {
        if (writeSizeHint < 0) { // ok... this was a manual flush... or one before a directWrite => write contents to output
            flushContents(outputStreamBuffer, buffer);
            buffer.position = buffer.start;
            return false;
        } else {
            return super.write(outputStreamBuffer, buffer, writeSizeHint);
        }
    }
}
