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

import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Abstract Data Source that can be used with InputStreamBuffer.
 *
 * Somewhat similar to boost iostreams input devices.
 * Is responsible for buffer management.
 *
 * Same as source, but with const-methods.
 * Typically, only const-sources allow concurrent reading
 */
@Ptr @Include("<cstddef>")
public interface ConstSource {

    /**
     * Reset input stream buffer for reading.
     * This is called initially when associating source with InputStreamBuffer.
     *
     * Supporting multiple reset operations is optional.
     * Streaming buffers typically won't support this (typically an assert will fail)
     *
     * @param inputStreamBuffer InputStreamBuffer that requests reset operation.
     * @param buffer BufferInfo object that will contain result - about buffer to initially operate on
     */
    @ConstMethod public void reset(@Ptr InputStreamBuffer inputStreamBuffer, @Ref BufferInfo buffer);

    /**
     * Fetch next bytes for reading.
     *
     * Source is responsible for managing buffers that is writes/creates in buffer object
     *
     * if len is <= zero, method will not block
     * if len is greater, method may block until number of bytes in available
     *
     * @param inputStreamBuffer InputStreamBuffer that requests fetch operation.
     * @param buffer BufferInfo object that contains result of read operation (buffer managed by Source)
     * @param len Minimum number of bytes to read
     */
    @ConstMethod public void read(@Ptr InputStreamBuffer inputStreamBuffer, @Ref BufferInfo buffer, @CppDefault("0") @SizeT int len);

    /**
     * @return Does source support reading directly into target buffer?
     * (optional optimization - does not have to make sense, depending on source)
     */
    @ConstMethod public boolean directReadSupport();

    /**
     * (Optional operation)
     * Fetch next bytes for reading - and copy them directly to target buffer.
     *
     * @param inputStreamBuffer InputStreamBuffer that requests fetch operation.
     * @param buffer Buffer to copy data to (buffer provided and managed by client)
     * @param len Minimum number of bytes to read
     */
    @ConstMethod public void directRead(@Ptr InputStreamBuffer inputStreamBuffer, @Ref FixedBuffer buffer, @SizeT int offset, @CppDefault("0") @SizeT int len);

    /**
     * Close stream/source.
     *
     * Possibly clean up buffer(s).
     *
     * @param inputStreamBuffer InputStreamBuffer that requests fetch operation.
     * @param buffer BufferInfo object that may contain buffer that needs to be deleted
     */
    @ConstMethod public void close(@Ptr InputStreamBuffer inputStreamBuffer, @Ref BufferInfo buffer);

    /**
     * Is any more data available?
     *
     * @param inputStreamBuffer Buffer that requests operation
     * @param buffer Current buffer (managed by source)
     * @return Answer
     */
    @ConstMethod public boolean moreDataAvailable(@Ptr InputStreamBuffer inputStreamBuffer, @Ref BufferInfo buffer);
}
