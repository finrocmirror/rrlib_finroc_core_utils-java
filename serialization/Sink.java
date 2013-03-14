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
package org.rrlib.finroc_core_utils.serialization;

/**
 * @author Max Reichardt
 *
 * Like boost output devices.
 * Somewhat simpler variant of Java output streams.
 *
 * Is responsible for buffer management.
 *
 * Sinks may only be written to by one OutputStream at the same time.
 */
public interface Sink {

    /**
     * Close stream/sink
     *
     * @param outputStreamBuffer Buffer that requests operation
     * @param buffer Buffer that is managed - and was possibly allocated by - sink
     */
    void close(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer);

    /**
     * Reset sink for writing content (again)
     * (may only be supported once - typically the case with streams)
     *
     * @param outputStreamBuffer Buffer that requests operation
     * @param buffer Buffer that is managed - and was possibly allocated by - sink
     */
    void reset(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer);

    /**
     * Write/flush data to sink/"device".
     * Bytes from buffer "limit" to buffer position are written.
     *
     * @param outputStreamBuffer Buffer that requests operation
     * @param buffer Buffer that is managed and contains data. Needs to be cleared/reset/replaced by this method.
     * @param writeSizeHint Hint about how much data we plan to write additionally (mostly makes sense, when there's no direct read support); -1 indicates manual flush without need for size increase
     * @return Invalidate any Placeholder? (usually true, when buffer changes)
     */
    boolean write(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer, int writeSizeHint);

    /**
     * @return Does Sink support direct writing
     * (optional optimization for reduction of copying overhead)
     */
    boolean directWriteSupport();

    /**
     * Directly write buffer to sink
     * (optional optimization for reduction of copying overhead)
     * (will only be called after flush() operation)
     *
     * @param outputStreamBuffer Buffer that requests operation
     * @param buffer Buffer that contains data to write - managed by client
     * @param offset Offset to start reading in buffer
     * @param len Number of bytes to write
     */
    void directWrite(OutputStreamBuffer outputStreamBuffer, FixedBuffer buffer, int offset, int len);

    /**
     * Flush/Commit data written to sink
     *
     * @param outputStreamBuffer Buffer that requests operation
     * @param buffer Buffer that contains data to write - managed by client
     */
    void flush(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer);
}
