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
package org.rrlib.finroc_core_utils.serialization;

import java.io.IOException;
import java.io.OutputStream;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Wraps output stream as sink
 */
public class OutputStreamSink extends LogUser implements Sink {

    /** Wrapped output stream */
    private OutputStream wrapped;

    /** Source state */
    public enum State { INITIAL, OPENED, CLOSED }
    State state = State.INITIAL;

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("serialization");

    public OutputStreamSink(OutputStream is) {
        wrapped = is;
    }

    @Override
    public void close(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        try {
            wrapped.close();
            buffer.buffer = null;
            state = State.CLOSED;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void directWrite(OutputStreamBuffer outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        try {
            byte[] tmp = new byte[2048];
            while (len > 0) {
                int r = Math.min(len, tmp.length);
                buffer.get(offset, tmp, 0, r);
                wrapped.write(tmp, 0, r);
                len -= r;
                offset += r;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean directWriteSupport() {
        return true;
    }

    @Override
    public void reset(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        assert(state == State.INITIAL);
        state = State.OPENED;
        assert(buffer.buffer == null);
        buffer.reset();
        buffer.buffer = new FixedBuffer(MemoryBuffer.DEFAULT_SIZE);
        buffer.setRange(0, buffer.buffer.capacity());
        buffer.position = 0;
    }

    @Override
    public boolean write(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer, int hint) {
        try {
            byte[] tmp = new byte[2048];
            int len = buffer.getWriteLen();
            while (len > 0) {
                int r = Math.min(len, tmp.length);
                buffer.buffer.get(buffer.start, tmp, 0, r);
                wrapped.write(tmp, 0, r);
                len -= r;
                buffer.start += r;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        buffer.setRange(0, buffer.buffer.capacity());
        buffer.position = 0;
        return true;
    }

    @Override
    public void flush(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        try {
            wrapped.flush();
        } catch (IOException e) {
            log(LogLevel.LL_ERROR, logDomain, e);
        }
    }

}
