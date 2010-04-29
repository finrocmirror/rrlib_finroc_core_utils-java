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

import org.finroc.jc.HasDestructor;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Memory buffer that can be used as source and concurrent sink.
 *
 * When used as sink, it can grow when required. A resizeFactor <= 1 indicates that this should not happen.
 * The buffer size is limited to 2GB with respect to serialization.
 *
 * Writing and reading concurrently is not supported - due to resize.
 */
public class MemoryBuffer implements ConstSource, Sink, CustomSerialization, HasDestructor {

    /** Size of temporary array */
    @Const @SizeT public final static int TEMP_ARRAY_SIZE = 2048;

    /** Default size of temp buffer */
    @Const @SizeT public final static int DEFAULT_SIZE = 8192;

    /** Default factor for buffer size increase */
    @Const public final static float DEFAULT_RESIZE_FACTOR = 2;

    /** Wrapped memory buffer */
    @Ptr protected FixedBuffer backend;

    /** When buffer needs to be reallocated, new size is multiplied with this factor to have some bytes in reserve */
    protected float resizeReserveFactor;

    /** Current size of buffer */
    @SizeT protected int curSize;

    @JavaOnly
    public MemoryBuffer() {
        this(DEFAULT_SIZE);
    }

    @JavaOnly
    public MemoryBuffer(@SizeT int size) {
        this(size, DEFAULT_RESIZE_FACTOR);
    }

    /**
     * @param size Initial buffer size
     * @param resizeFactor When buffer needs to be reallocated, new size is multiplied with this factor to have some bytes in reserve
     */
    public MemoryBuffer(@CppDefault("DEFAULT_SIZE") @SizeT int size, @CppDefault("DEFAULT_RESIZE_FACTOR") float resizeFactor) {
        backend = new FixedBuffer(size);
        resizeReserveFactor = resizeFactor;
    }

    /**
     * @return the resizeReserveFactor
     */
    public float getResizeReserveFactor() {
        return resizeReserveFactor;
    }

    /**
     * @param resizeReserveFactor the resizeReserveFactor to set
     */
    public void setResizeReserveFactor(float resizeReserveFactor) {
        this.resizeReserveFactor = resizeReserveFactor;
    }

    /**
     * Clear buffer
     */
    public void clear() {
        curSize = 0;
    }

    // ConstSource implementation

    @Override
    public void close(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        // do nothing, really
        buffer.reset();
    }

    @Override
    public void directRead(InputStreamBuffer inputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        throw new RuntimeException("Unsupported - shouldn't be called");
    }

    @Override
    public boolean directReadSupport() {
        return false;
    }

    @Override
    public void read(InputStreamBuffer inputStreamBuffer, BufferInfo buffer, int len) {
        buffer.setRange(0, curSize);
        if (buffer.position >= curSize) {
            throw new RuntimeException("Attempt to read outside of buffer");
        }
    }

    @Override
    public void reset(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        buffer.buffer = backend;
        buffer.position = 0;
        buffer.setRange(0, curSize);
    }

    // Sink implementation

    @Override
    public void close(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        // do nothing, really
        buffer.reset();
    }

    @Override
    public void directWrite(OutputStreamBuffer outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        throw new RuntimeException("Unsupported - shouldn't be called");
    }

    @Override
    public boolean directWriteSupport() {
        return false;
    }

    @Override
    public void reset(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        buffer.buffer = backend;
        buffer.position = 0;
        buffer.setRange(0, backend.capacity());
    }

    /**
     * Ensure that memory buffer has at least this size.
     * If not, backend will be reallocated.
     *
     * @param newSize New Size in bytes
     * @param keepContents Keep contents when reallocating?
     * @param oldSize Old Size (only relevant if contents are to be kept)
     */
    protected void ensureCapacity(int newSize, boolean keepContents, @SizeT int oldSize) {
        if (resizeReserveFactor <= 1) {
            throw new RuntimeException("Attempt to write outside of buffer");
        }
        if (resizeReserveFactor <= 1.2) {
            System.out.println("warning: small resizeReserveFactor");
        }

        reallocate(newSize, keepContents, oldSize);
    }

    @Override
    public boolean write(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer, int hint) {

        // do we need size increase?
        if (buffer.position + hint >= backend.capacity()) {
            @SizeT int newSize = Math.max(8, (int)((backend.capacity() + hint) * resizeReserveFactor));
            ensureCapacity(newSize, true, buffer.position);
            buffer.buffer = backend;
        }
        buffer.end = backend.capacity(); // don't modify buffer start
        return false;
    }

    /**
     * Reallocate backend
     *
     * @param newSize New size
     * @param keepContents Keep contents of backend?
     * @param oldSize Old Size (only relevant of contents are to be kept)
     */
    protected void reallocate(@SizeT int newSize, boolean keepContents, @SizeT int oldSize) {
        if (newSize <= backend.capacity()) {
            return;
        }

        @Ptr FixedBuffer newBuffer = new FixedBuffer(newSize);

        if (keepContents) {

            // Copy old contents
            newBuffer.put(0, backend, 0, oldSize);
        }

        //Cpp delete backend;
        backend = newBuffer;
    }

    // CustomSerializable implementation

    @Override
    public void deserialize(InputStreamBuffer rv) {
        int size = rv.readInt(); // Buffer size is limited to 2 GB
        curSize = 0;
        reallocate(size, false, -1);
        rv.readFully(backend, 0, size);
        curSize = size;
    }

    @Override
    public void serialize(OutputStreamBuffer sb) {
        sb.writeInt(curSize);
        sb.write(backend, 0, curSize);
    }

    @Override
    public void delete() {
        //Cpp delete backend;
        backend = null;
    }

    @Override
    public boolean moreDataAvailable(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        return buffer.end < curSize;
    }

    /**
     * @return Buffer size
     */
    @ConstMethod public int getSize() {
        return curSize;
    }

    /**
     * @return Buffer capacity
     */
    @ConstMethod public int getCapacity() {
        return backend.capacity();
    }

    /*Cpp

    //! returns buffer backend
    inline FixedBuffer* getBuffer() {
        return backend;
    }

    //! returns pointer to buffer backend - with specified offset in bytes
    inline int8* getBufferPointer(int offset) {
        return getBuffer()->getPointer() + offset;
    }

    //! returns pointer to buffer backend - with specified offset in bytes
    inline const int8* getBufferPointer(int offset) const {
        return getBuffer()->getPointer() + offset;
    }
     */

    /**
     * @return Raw buffer backend
     */
    @ConstMethod public @Ptr @Const FixedBuffer getBuffer() {
        return backend;
    }

    @Override
    public void flush(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        curSize = buffer.position; // update buffer size
    }

    @JavaOnly
    public void dumpToFile(String filename) {
        backend.dumpToFile(filename, curSize);
    }
}
