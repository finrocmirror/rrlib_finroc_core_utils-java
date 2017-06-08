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

import org.rrlib.finroc_core_utils.jc.HasDestructor;
import org.rrlib.finroc_core_utils.jc.MutexLockOrder;
import org.rrlib.finroc_core_utils.jc.container.ReusablesPoolCR;
import org.rrlib.serialization.BinaryInputStream;
import org.rrlib.serialization.BinaryOutputStream;
import org.rrlib.serialization.BinarySerializable;
import org.rrlib.serialization.BufferInfo;
import org.rrlib.serialization.FixedBuffer;
import org.rrlib.serialization.Sink;
import org.rrlib.serialization.Source;
import org.rrlib.serialization.rtti.Clearable;
import org.rrlib.serialization.rtti.DataType;
import org.rrlib.serialization.rtti.DataTypeBase;

/**
 * @author Max Reichardt
 *
 * Buffer that increases its size (when necessary) by appending chunks
 * to form virtual contiguous memory block.
 * Quite efficient - however, memory is non-contiguous,
 * which is critical when passing raw pointers to the outside.
 *
 * This class is thread-safe for...
 *  1) one concurrent writer and several concurrent readers. (no new writers while there are readers and buffer is not empty!)
 *  2) one concurrent writer and one "destructive source". (no new writers while there is a reader and buffer is not empty!)
 *
 * The destructive source recycles chunks after reading them.
 *
 * There are two modes of operation: blocking-readers and non-blocking-readers
 *
 * blocking-readers:
 * When buffer boundary is reached, readers block and wait for data.
 * However, this costs some performance.
 *
 * non-blocking-readers:
 * Readers are non-blocking for performance reasons. If there are not enough bytes
 * in buffer an exception is thrown. So check with available() whether data is available
 * and only commit complete chunks.
 */
public class ChunkedBuffer implements BinarySerializable, Source, Sink, HasDestructor, Clearable {

    /** First chunk in buffer - only changed by reader - next ones can be determined following links through "next"-attributes*/
    protected BufferChunk first;

    /** Pool with chunks */
    private static ReusablesPoolCR<BufferChunk> chunks;

    /** Use blocking readers? */
    protected final boolean blockingReaders;

    /** Number of written bytes - only set by reader - increases monotonically with every "official" commit */
    protected volatile long writtenBytes = 0;

    /** "Destructive source" */
    protected DestructiveSource destructiveSource = new DestructiveSource();

    /** Must be locked before AllocationRegister */
    @SuppressWarnings("unused")
    private static final MutexLockOrder staticClassMutex = new MutexLockOrder(0x7FFFFFFF - 160);

    /** Data type of Chunked Buffer */
    public final static DataTypeBase TYPE = new DataType<ChunkedBuffer>(ChunkedBuffer.class);

    public static void staticInit() {
        chunks = new ReusablesPoolCR<BufferChunk>();
    }

    public ChunkedBuffer() {
        this(false);
    }

    public ChunkedBuffer(boolean blockingReaders) {
        first = getUnusedChunk();
        first.virtualPosition = 0;
//      last = first;
        this.blockingReaders = blockingReaders;
    }

    public void delete() {
        clear();
        first.recycle();
    }

    /**
     * @return Unused chunk
     */
    protected static BufferChunk getUnusedChunk() {
        BufferChunk result = chunks.getUnused();
        return result == null ? createChunk() : result;
    }

    /**
     * @return Newly created unused chunk
     */
    private static synchronized BufferChunk createChunk() {
        BufferChunk result = new BufferChunk();
        chunks.attach(result, false);
        return result;
    }

    /**
     * Clear buffer - Don't call this, while other threads are reading this buffer
     */
    public void clear() {
        writtenBytes = 0;
        first.curSize.set(0, 0);
        first.next = null;
        first.virtualPosition = 0;
        destructiveSource.readPos = 0;
        for (BufferChunk bc = first.next; bc != null; bc = bc.next) {
            bc.recycle();
        }
    }

    // Concurrent Source implementation

    @Override
    public void close(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
        // do nothing really
        buffer.reset();
    }

    @Override
    public void directRead(BinaryInputStream inputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        throw new RuntimeException("shouldn't be called");
    }

    @Override
    public boolean directReadSupport() {
        return false;
    }

    @Override
    public void read(BinaryInputStream inputStreamBuffer, BufferInfo buffer, int len) {
        readImpl(inputStreamBuffer, buffer, len);
    }

    /**
     * Read implementation for source and destructive source
     *
     * @param inputStreamBuffer see above
     * @param buffer see above
     * @param len see above
     * @param destructive Implementation for destructive source?
     * @return Has a buffer switch occured?
     */
    public boolean readImpl(BinaryInputStream inputStreamBuffer, BufferInfo buffer, int len) {

        BufferChunk bc = (BufferChunk)buffer.customData;

        // any changes? if not, wait for change...
        long written = writtenBytes;
        if ((long)(bc.virtualPosition + buffer.position + len) > written) {
            assert((long)(bc.virtualPosition + buffer.position) <= written) : "Programming error as it seems";

            if (!blockingReaders) {
                throw new RuntimeException("Attempt to read outside of buffer with non-blocking readers");
            } else {
                synchronized (this) {
                    while (true) {
                        written = writtenBytes;
                        if ((long)(bc.virtualPosition + buffer.position + len) <= written) {
                            break;
                        }
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            // continue;
                        }
                    }
                }
            }
        }

        // Get current buffer status
        int raw = bc.curSize.getRaw();
        boolean nextAv = bc.curSize.getVal1(raw) != 0;
        int curSize = bc.curSize.getVal2(raw);

        // Are more bytes in current buffer?
        //assert(bc.virtualPosition + buffer.position <= writtenBytes) : "Programming error as it seeems " + bc.virtualPosition + " " + buffer.position + " " + writtenBytes;
        if (buffer.end < curSize) {

            // Keep buffer and perform size increase
            int currentWritePosInBuffer = (int)(written - bc.virtualPosition);
            buffer.end = minSizeT(curSize, currentWritePosInBuffer);
            assert(curSize - buffer.position >= len) : "Minimal buffer size increase causes problem - probably incorrectly aligned data (eliminating obsolete flushes might also help)?";
        } else {

            // Use next buffer
            assert(nextAv && bc.next != null) : "Programming error: Another chunk should be available here";
            BufferChunk next = bc.next;
//          if (destructive) {
//              first = next;
//              //System.out.println("Recycling " + bc.hashCode());
//              bc.recycle();
//              destructiveSource.readPos = next.virtualPosition;
//          }
            bc = next;
            buffer.buffer = bc.buffer;
            int nextSize = minSizeT(bc.curSize.getVal2(), (int)(written - bc.virtualPosition));
            buffer.setRange(0, nextSize);
            buffer.customData = bc;
            buffer.position = 0;
            assert(nextSize >= len) : "Buffer still too empty => programming error";
            return true;
        }
        return false;
    }

    @Override
    public void reset(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
        buffer.buffer = first.buffer;
        buffer.setRange(0, minSizeT(first.curSize.getVal2(), (int)(writtenBytes - first.virtualPosition)));
        buffer.position = 0;
        buffer.customData = first;
    }

    @Override
    public boolean moreDataAvailable(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
        BufferChunk bc = (BufferChunk)buffer.customData;
        //System.out.println(bc.virtualPosition +" + " + buffer.position +" < "+ writtenBytes);
        return ((long)(bc.virtualPosition + buffer.end) < writtenBytes);
    }

    // Sink implementation

    @Override
    public void close(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        // do nothing really
        buffer.reset();
    }

    @Override
    public void directWrite(BinaryOutputStream outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        throw new RuntimeException("shouldn't be called");
    }

    @Override
    public boolean directWriteSupport() {
        return false;
    }

    @Override
    public void reset(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        clear();
        writtenBytes = 0;
        first.virtualPosition = 0;
        buffer.buffer = first.buffer;
        buffer.setRange(0, buffer.buffer.capacity());
        buffer.position = 0;
        buffer.customData = first;
    }

    @Override
    public boolean write(BinaryOutputStream outputStreamBuffer, BufferInfo buffer, int writeSizeHint) {
        BufferChunk bc = (BufferChunk)buffer.customData;
        if (buffer.remaining() > 8) {

            // keep buffer, but commit new write position
            assert(writtenBytes <= (long)(bc.virtualPosition + buffer.position));
            bc.curSize.set(0, buffer.position);
            return true;
        }

        // commit buffer
        int newSize = buffer.position;
        BufferChunk next = getUnusedChunk();
        bc.next = next;
        next.virtualPosition = bc.virtualPosition + buffer.position;
        buffer.buffer = next.buffer;
        buffer.setRange(0, next.buffer.capacity());
        buffer.position = 0;
        buffer.customData = next;
        bc.curSize.set(1, newSize); // set this last, so that everything is ready, before reader thread starts...
        return true;
    }

    /**
     * @return Current Size of chunked buffer
     */
    public int getCurrentSize() {
        long relevantPos = Math.max(destructiveSource.readPos, first.virtualPosition);
        return (int)(writtenBytes - relevantPos);
    }

    @Override
    public void deserialize(BinaryInputStream is) {

        // read Size
        int size = is.readInt();

        // number of required chunks
        int chunkCount = (size + BufferChunk.CHUNK_SIZE - 1) / BufferChunk.CHUNK_SIZE;

        // the following is a little more complicated, but it is more efficient than recycling everything

        // ensure we have the minimum number of chunks
        BufferChunk current = first;
        for (int i = 1; i < chunkCount; i++) {
            BufferChunk next = current.next;
            if (next == null) {
                next = getUnusedChunk();
                current.next = next;
            }
            current = next;
        }

        // recycle chunks that are too much
        current = current.next;
        while (current != null) {
            BufferChunk next = current.next;
            current.recycle();
            current = next;
        }

        // read chunks
        current = first;
        first.virtualPosition = 0;
        for (int i = 0; i < chunkCount; i++) {
            int read = minSizeT(size - ((int)current.virtualPosition), BufferChunk.CHUNK_SIZE);
            is.readFully(current.buffer, 0, read);
            current.curSize.set(current.next == null ? 0 : 1, read);
            if (current.next == null) {
                break;
            }

            current.next.virtualPosition = current.virtualPosition + read;
            current = current.next;
        }

        destructiveSource.readPos = 0;
        writtenBytes = size;
    }

    @Override
    public void serialize(BinaryOutputStream os) {
        long relevantPos = Math.max(destructiveSource.readPos, first.virtualPosition);

        // write size
        int size = getCurrentSize();
        os.writeInt(size);

        // write first chunk
        int firstPos = (int)(relevantPos - first.virtualPosition);
        int write = minSizeT(size, first.curSize.getVal2() - firstPos);
        os.write(first.buffer, firstPos, write);
        size -= write;

        // write further chunks
        BufferChunk bc = first;
        while (size > 0) {
            bc = bc.next;
            write = minSizeT(size, bc.curSize.getVal2());
            os.write(bc.buffer, 0, write);
            size -= write;
        }
    }

    @Override
    public void flush(BinaryOutputStream outputStreamBuffer, BufferInfo buffer) {
        BufferChunk bc = (BufferChunk)buffer.customData;
        //System.out.println("Flushing " + bc.hashCode());
        assert(bc.curSize.getVal2() == buffer.position) : "please commit before flush";
        writtenBytes = bc.virtualPosition + buffer.position;
        if (blockingReaders) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * @return "Destructive source"
     */
    public DestructiveSource getDestructiveSource() {
        return destructiveSource;
    }

    /**
     * "Desctructive Source"
     */
    private class DestructiveSource implements Source {

        /** Current read position; */
        private long readPos;

        /** Current user of Source */
        private BinaryInputStream user = null;

        @Override
        public void close(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
            assert(inputStreamBuffer == user);
            // do nothing really
            readPos = ((BufferChunk)buffer.customData).virtualPosition + buffer.position;
            buffer.reset();
            user = null;
        }

        @Override
        public void directRead(BinaryInputStream inputStreamBuffer, FixedBuffer buffer, int offset, int len) {
            throw new RuntimeException("Should not be called");
        }

        @Override
        public boolean directReadSupport() {
            return false;
        }

        @Override
        public boolean moreDataAvailable(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
            assert(inputStreamBuffer == user);
            BufferChunk bc = (BufferChunk)buffer.customData;
            return (long)(bc.virtualPosition + buffer.end) < writtenBytes;
        }

        @Override
        public void read(BinaryInputStream inputStreamBuffer, BufferInfo buffer, int len) {
            if (readImpl(inputStreamBuffer, buffer, len)) {

                // buffer switch: recycle old one
                BufferChunk old = first;
                BufferChunk next = first.next;
                first = next;
                //System.out.println("Recycling " + bc.hashCode());
                old.recycle();
                destructiveSource.readPos = next.virtualPosition;
            }
        }

        @Override
        public void reset(BinaryInputStream inputStreamBuffer, BufferInfo buffer) {
            assert(user == null);
            user = inputStreamBuffer;
            buffer.buffer = first.buffer;
            buffer.setRange((int)(readPos - first.virtualPosition), minSizeT(first.curSize.getVal2(), (int)(writtenBytes - first.virtualPosition)));
            buffer.position = buffer.start;
            buffer.customData = first;
        }
    }

    /**
     * Helper function for convenience (without, we have all these casting issues...)
     */
    private static int minSizeT(int a, int b) {
        return Math.min(a, b);
    }

    @Override
    public void clearObject() {
        clear();
    }
}
