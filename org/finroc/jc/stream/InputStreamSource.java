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
package org.finroc.jc.stream;

import java.io.IOException;
import java.io.InputStream;

import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.jc.log.LogUser;
import org.finroc.jc.net.EOFException;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Wraps Java Input Stream as source
 */
@JavaOnly
public class InputStreamSource extends LogUser implements Source {

    /** Wrapped input stream */
    private final InputStream wrapped;

    /** Source state */
    public enum State { INITIAL, OPENED, CLOSED }
    State state = State.INITIAL;

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"stream\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("stream");

    public InputStreamSource(InputStream is) {
        wrapped = is;
    }

    @Override
    public void close(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        try {
            wrapped.close();
            buffer.reset();
            state = State.CLOSED;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void directRead(InputStreamBuffer inputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        try {
            byte[] tmp = new byte[MemoryBuffer.TEMP_ARRAY_SIZE];
            while (len > 0) {
                int r = wrapped.read(tmp, 0, Math.min(len, tmp.length));
                if (r < 0) {
                    throw new EOFException();
                }
                buffer.put(offset, tmp, 0, r);
                offset += r;
                len -= r;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean directReadSupport() {
        return true;
    }

    @Override
    public void read(InputStreamBuffer inputStreamBuffer, BufferInfo buffer, int len) {
        assert(len < buffer.buffer.capacity());
        try {
            int offset = 0;
            int av = wrapped.available();
            int read = Math.min(av, buffer.buffer.capacity() - offset);
            read = Math.max(read, len);
            byte[] tmp = new byte[2048];
            while (read > 0 || len > 0) {
                int r = wrapped.read(tmp, 0, Math.min(read, tmp.length));
                if (r < 0) {
                    throw new EOFException();
                }
                buffer.buffer.put(offset, tmp, 0, r);
                offset += r;
                read -= r;
                len -= r;
            }
            buffer.setRange(0, offset);
            buffer.position = 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void reset(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        assert(state == State.INITIAL);
        state = State.OPENED;
        assert(buffer.buffer == null);
        buffer.reset();
        buffer.buffer = new FixedBuffer(MemoryBuffer.DEFAULT_SIZE);
    }

    @Override
    public boolean moreDataAvailable(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        try {
            return wrapped.available() > 0;
        } catch (IOException e) {
            log(LogLevel.LL_ERROR, logDomain, e);
            return false;
        }
    }
}
