//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.stream;

import org.rrlib.serialization.BinaryOutputStream;
import org.rrlib.serialization.BufferInfo;
import org.rrlib.serialization.FixedBuffer;
import org.rrlib.serialization.MemoryBuffer;
import org.rrlib.serialization.Sink;

/**
 * @author Max Reichardt
 *
 * A problem with ordinary stream buffers is that skip offsets often cannot be written
 * when the data in between is very large.
 * In this case, this class can be used as an intermediate buffer.
 * It will grow until data is flushed
 */
public class LargeIntermediateStreamBuffer extends MemoryBuffer {

    /** Sink for intermediate buffer */
    private final Sink sink;

    /** Dummy buffer info */
    private BufferInfo dummyInfo = new BufferInfo();

    public LargeIntermediateStreamBuffer(Sink sink) {
        this.sink = sink;
    }

    /**
     * Flush all contents of intermediate buffer to sink
     *
     * @param byteCount Number of bytes to write
     */
    private void flushContents(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        if (buffer.position > buffer.start) {
            sink.directWrite(outputStreamBuffer, backend, buffer.start, buffer.position);
            buffer.position = buffer.start;
        }
    }

    @Override
    public void close(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        flushContents(outputStreamBuffer, buffer);
        super.close(outputStreamBuffer, dummyInfo);
    }

    @Override
    public void directWrite(BinaryOutputStream outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        sink.directWrite(outputStreamBuffer, buffer, offset, len);
    }

    @Override
    public boolean directWriteSupport() {
        return sink.directWriteSupport();
    }

    @Override
    public void reset(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        super.reset(outputStreamBuffer, buffer);
        sink.reset(outputStreamBuffer, dummyInfo);
    }

    @Override
    public boolean write(BinaryOutputStream outputStreamBuffer, BufferInfo buffer, int writeSizeHint) {
        if (writeSizeHint < 0) { // ok... this was a manual flush... or one before a directWrite => write contents to output
            flushContents(outputStreamBuffer, buffer);
            buffer.position = buffer.start;
            return false;
        } else {
            return super.write(outputStreamBuffer, buffer, writeSizeHint);
        }
    }
}
