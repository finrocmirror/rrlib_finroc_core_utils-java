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
import org.finroc.jc.IntArrayWrapper;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.OrgWrapper;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.SizeT;

/**
 * @author Max Reichardt
 *
 * Reasonably efficient, flexible, universal writing interface.
 *
 * Flexible "all-in-one" output stream memory buffer interface that implements various interfaces.
 * (in Java it can be combined with Streams and ByteBuffers, in C++ with output streams and boost iostreams)
 *
 * This class provides a universal data writing interface for memory buffers.
 * A manager class needs to be specified, which will customize what is actually done with the data.
 *
 * The implementation is designed to be reasonably efficient (no virtual function calls in C++; except of committing/fetching
 * data chunks from streams... however, this doesn't happen often and should not harm performance) -
 * that's why no interfaces are used for serialization, but rather the class itself. Support for further
 * read and write methods can be easily added.
 *
 * Size checking is performed for every write and read operation. For maximum performance, arrays/buffers can be used to
 * write and read data to/from this class. Buffers can be forwarded to a sink directly (they don't need to be buffered) avoiding
 * additional copying operations.
 *
 * The Class is explicitly _not_ thread-safe for writing - meaning multiple threads may not write to the same object at any given
 * moment in time.
 *
 * There are two modes of operation with respect to print-methods:
 *  1) flush immediately
 *  2) flush when requested or full
 */
//@ForwardDecl(InputStreamBuffer.class)
//@PostInclude("ReadView.h")
//@CppPrepend({"ReadView StreamBuffer::getReadView(bool reset) {",
//    "    return ReadView(this);", "}"})
public class OutputStreamBuffer implements Sink, HasDestructor {

    /*Cpp
    // for "locking" object sink as long as this buffer exists
    std::tr1::shared_ptr<const Interface> sinkLock;
    */

//  /** UID */
//  private static final long serialVersionUID = 70L;
//
//  /** Thread-local read-related variables */
//  @JavaOnly protected final ReadThreadLocal readVars = new ReadThreadLocal();
//
//  /*Cpp
//  friend class ReadView;
//   */

    /** Committed buffers are buffered/copied (not forwarded directly), when smaller than 1/(2^n) of buffer capacity */
    @Const protected final static double BUFFER_COPY_FRACTION = 0.25;

//  /**
//   * Optional source stream specified?
//   *
//   * true implies that either source or blockSource is not null;
//   * false implies that source and blockSource are null;
//   *
//   * if false, no source is used and read operations may fail, when there's no more data
//   */
//  @Const protected final boolean hasSource;
//
//  /** Optional source stream */
//  protected final @SharedPtr Source source;
//
//  /**
//   * Optional block source stream
//   * Using such a source stream avoids extra copying.
//   * However, data (e.g. a double value) may not split between different blocks.
//   */
//  protected final @SharedPtr BlockSource blockSource;
//
//  /** Optional destination stream - if null, no sink is used and buffer size is increased when insufficient */
//  protected final @SharedPtr Sink sink;

    /** Source that determines where buffers that are written to come from and how they are handled */
    @Ptr protected Sink sink = null;

    /** Immediately flush buffer after printing? */
    @Const protected final boolean immediateFlush = false;

    /** Has stream been closed? */
    protected boolean closed = true;

    /** Buffer that is currently written to - is managed by sink */
    @PassByValue protected BufferInfo buffer = new BufferInfo();

    /** -1 by default - buffer position when a skip offset placeholder has been set/written */
    protected int curSkipOffsetPlaceholder = -1;

    /** hole Buffers are only buffered/copied, when they are smaller than this */
    protected @SizeT int bufferCopyFraction;

    /** Is direct write support available with this sink? */
    protected boolean directWriteSupport = false;

    public OutputStreamBuffer() {}

    public OutputStreamBuffer(@OrgWrapper @SharedPtr Sink sink_) {

        //JavaOnlyBlock
        reset(sink_);

        //Cpp reset(sink_);
    }

    public void delete() {
        close();
    }

    /*
    // stream operator
    template <typename T>
    ByteBuffer& operator<<(const T& t) {
        ensureAdditionalCapacity(sizeof(T));
        putImpl<T>(writePos, t);
        writePos += sizeof(T);
    }
     */

//  /**
//   * Reallocate buffers with the specified size
//   * Old buffer storage area needs to be deallocated manually in C++ (!!)
//   *
//   * @param size Size
//   */
//  @JavaOnly protected synchronized void allocateBuffer(int size) {
//      buffer = USE_DIRECT_BUFFERS ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
//      buffer.order(BYTE_ORDER);
//      allocationCount++;
//  }
//
//  //Cpp ReadView getReadView(bool reset);
//
//  /**
//   * @param reset Reset position and limit to match write buffer?
//   * @return Thread-safe read view of buffer (Note: it is only thread-safe when no streams(source/sink) are used)
//   * It is somewhat expensive to call this regularly, so users should rather continue using the read view object.
//   * When buffer is reallocated (can happen when writing), the read view becomes invalid and has to be retrieved again
//   * using this method.
//   */
//  @InCpp("return ReadView(this);") @InCppFile @ConstMethod @NonVirtual
//  public InputStreamBuffer getReadView(boolean reset) {
//      InputStreamBuffer rv = readVars.get();
//      rv.init(reset);
//      return rv;
//  }

    /**
     * @return Size of data that was written to buffer
     * (typically useless - because of flushing etc. - only used by some internal stuff)
     */
    @ConstMethod public @SizeT int getWriteSize() {
        return buffer.position - buffer.start;
    }

    /**
     * @return Bytes remaining (for writing) in this buffer
     */
    @ConstMethod public @SizeT int remaining() {
        return buffer.remaining();
    }

    /*Cpp
    void reset(std::tr1::shared_ptr<Sink> sink) {
        sinkLock = sink;
        reset(sink._get());
    }
     */

    /**
     * Use buffer with different sink (closes old one)
     *
     * @param sink New Sink to use
     */
    public void reset(@Ptr Sink sink) {
        close();
        this.sink = sink;
        reset();
    }

    /**
     * Resets/clears buffer for writing
     */
    public void reset() {
        sink.reset(this, buffer);
        assert(buffer.remaining() >= 8);
        closed = false;
        bufferCopyFraction = (int)(buffer.capacity() * BUFFER_COPY_FRACTION);
        directWriteSupport = sink.directWriteSupport();
    }

    /**
     * Write null-terminated string
     *
     * @param s String
     * @param terminate Terminate string with zero?
     */
    @JavaOnly public void writeUnicode(String s, boolean terminate) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            writeChar(s.charAt(i));
        }
        if (terminate) {
            writeChar((char)0);
        }
    }

    /**
     * Write null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param s String
     */
    @Inline public void writeString(@CppType("AbstractString") @Const @Ref String s) {
        writeString(s, true);
    }

    /**
     * Write null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringBuilder object
     */
    @JavaOnly
    @Inline public void writeString(@Const @Ref StringBuilder sb) {
        writeString(sb.toString());
    }

    /**
     * Write string (8 Bit Characters - Suited for ASCII)
     *
     * @param s String
     * @param terminate Terminate string with zero?
     */
    public void writeString(@CppType("AbstractString") @Const @Ref String s, boolean terminate) {
        // JavaOnlyBlock
        int len = s.length();
        for (int i = 0; i < len; i++) {
            writeByte((byte)s.charAt(i));
        }
        if (terminate) {
            writeByte(0);
        }

        /*Cpp
        size_t len = terminate ? (s.length() + 1) : s.length();
        write(FixedBuffer((int8*)s.getCString(), len));
         */
    }

    /**
     * Flush current buffer contents to sink and clear buffer.
     * (with no immediate intent to write further data to buffer)
     */
    public void flush() {
        commitData(-1);
        sink.flush(this, buffer);
    }

    /**
     * Write current buffer contents to sink and clear buffer.
     *
     * @param addSizeHint Hint at how many additional bytes we want to write; -1 indicates manual flush without need for size increase
     */
    protected void commitData(int addSizeHint) {
        if (getWriteSize() > 0) {
            if (sink.write(this, buffer, addSizeHint)) {
                assert(curSkipOffsetPlaceholder < 0);
            }
            assert(buffer.remaining() >= 8);
            bufferCopyFraction = (int)(buffer.capacity() * BUFFER_COPY_FRACTION);
        }
    }

    /**
     * Writes all bytes from array to buffer
     *
     * @param b Array
     */
    @JavaOnly public void write(@Const @Ref byte[] b) {
        write(b, 0, b.length);
    }

    /**
     * @return Whole Buffers are only buffered/copied, when they are smaller than this
     */
    @Inline @ConstMethod protected @SizeT int getCopyFraction() {
        assert(bufferCopyFraction > 0);
        return bufferCopyFraction;
    }

    /**
     * Writes specified bytes from array to buffer
     *
     * @param b Array
     * @param off Offset in array
     * @param len Number of bytes to copy
     */
    @JavaOnly
    public void write(@Const @Ref byte[] b, @SizeT int off, @SizeT int len) {
        while (len > 0) {
            int write = Math.min(len, remaining());
            buffer.buffer.put(buffer.position, b, off, len);
            buffer.position += len;
            len -= write;
            off += write;
            assert(len >= 0);
            if (len == 0) {
                return;
            }
            commitData(len);
        }
    }

    /*Cpp
    inline void write(void* address, size_t size) {
        FixedBuffer fb((int8*)address, size);
        write(fb);
    }
     */

    /**
     * Writes specified byte buffer contents to stream
     * Regarding streams:
     *   Large buffers are directly copied to output device
     *   avoiding an unnecessary copying operation.
     *
     * @param bb ByteBuffer (whole buffer will be copied)
     */
    public void write(@Const @Ref FixedBuffer bb) {
        write(bb, 0, bb.capacity());
    }

    /**
     * (Non-virtual variant of above)
     * Writes specified byte buffer contents to stream
     * Regarding streams:
     *   Large buffers are directly copied to output device
     *   avoiding an unnecessary copying operation.
     *
     * @param bb ByteBuffer
     * @param off Offset in buffer
     * @param len Number of bytes to write
     */
    public void write(@Const @Ref FixedBuffer bb, @SizeT int off, @SizeT int len) {
        if ((remaining() >= len) && (len < getCopyFraction() || curSkipOffsetPlaceholder >= 0)) {
            buffer.buffer.put(buffer.position, bb, off, len);
            buffer.position += len;
        } else {
            if (directWriteSupport) {
                commitData(-1);
                sink.directWrite(this, bb, off, len);
            } else {
                while (len > 0) {
                    int write = Math.min(len, remaining());
                    buffer.buffer.put(buffer.position, bb, off, write);
                    buffer.position += write;
                    len -= write;
                    off += write;
                    assert(len >= 0);
                    if (len == 0) {
                        return;
                    }
                    commitData(len);
                }
            }
        }
    }

    /**
     * Ensure that the specified number of bytes is available in buffer.
     * Possibly resize or flush.
     *
     * @param c Number of Bytes.
     */
    @Inline public void ensureAdditionalCapacity(@SizeT int c) {
        if (remaining() < c) {
            commitData(c - remaining());
        }
    }

//  /**
//   * Ensure that the specified number of bytes is available in buffer.
//   * Possibly resize or flush.
//   * (Internal implementation)
//   *
//   * @param c Number of Bytes.
//   * @param keepOldContents preserve contents already in (smaller) buffer => additional copy operation (irrelevant for streamed buffers)
//   */
//  public void ensureCapacity(@SizeT int c, boolean keepOldContents) {
//      ensureCapacity(c, keepOldContents, resizeReserveFactor);
//  }
//
//  /**
//   * Ensure that the specified number of bytes is available in buffer.
//   * Possibly resize or flush.
//   * (Internal implementation)
//   *
//   * @param c Number of Bytes.
//   * @param keepOldContents preserve contents already in (smaller) buffer => additional copy operation (irrelevant for streamed buffers)
//   * @param resizeFactor Factor to multiply required capacity with to have some space in reserve (irrelevant for streamed buffers)
//   */
//  @Inlin
//  protected void ensureCapacity(@SizeT int c, boolean keepOldContents, double resizeFactor) {
//      if (capacity)
//      if (capacity() >= c) {
//          return;
//      }
//      curSkipOffsetPlaceholder = -1;
//
//      if (sink == null || resizeReserveFactor > 1) {
//          if (resizeReserveFactor < 1) {
//              throw new RuntimeException("This buffer is not supposed to be resized");
//          }
//
//          // JavaOnlyBlock
//          ByteBuffer old = buffer;
//          allocateBuffer((int)((c * resizeReserveFactor) + 1));
//          if (keepOldContents) {
//              old.position(0);
//              old.limit(writePos);
//              buffer.put(old);
//          }
//
//          /*Cpp
//          int8* old = buffer;
//          size_t newCap = (size_t)((c * resizeReserveFactor) + 1);
//          buffer = new int8[newCap];
//          if (keepOldContents) {
//              memcpy(buffer, old, capacity());
//          }
//          capacityX = newCap;
//          delete[] old;
//          //allocationCount++;
//           */
//
//      } else {
//          assert c < capacity();
//          if (remaining() < c) {
//              flush();
//          }
//      }
//  }

    /**
     * Immediately flush buffer if appropriate option is set
     * Used in print methods
     */
    @Inline protected void checkFlush() {
        if (immediateFlush) {
            flush();
        }
    }

    /**
     * @param v (1-byte) boolean
     */
    @Inline public void writeBoolean(boolean v) {
        writeByte(v ? 1 : 0);
    }

    /**
     * @param v 8 bit integer
     */
    @Inline public void writeByte(int v) {
        ensureAdditionalCapacity(1);
        buffer.buffer.putByte(buffer.position, v);
        buffer.position++;
    }


    /**
     * @param v Character
     */
    @Inline public void writeChar(char v) {
        ensureAdditionalCapacity(2);
        buffer.buffer.putChar(buffer.position, v);
        buffer.position += 2;
    }

    /**
     * @param v 32 bit integer
     */
    @Inline public void writeInt(int v) {
        ensureAdditionalCapacity(4);
        buffer.buffer.putInt(buffer.position, v);
        buffer.position += 4;
    }

    /**
     * @param v 64 bit integer
     */
    @Inline public void writeLong(long v) {
        ensureAdditionalCapacity(8);
        buffer.buffer.putLong(buffer.position, v);
        buffer.position += 8;
    }

    /**
     * @param v 16 bit integer
     */
    @Inline public void writeShort(int v) {
        ensureAdditionalCapacity(2);
        buffer.buffer.putShort(buffer.position, (short)v);
        buffer.position += 2;
    }

    /**
     * @param v 64 bit floating point
     */
    @Inline public void writeDouble(double v) {
        ensureAdditionalCapacity(8);
        buffer.buffer.putDouble(buffer.position, v);
        buffer.position += 8;
    }

    /**
     * @param v 32 bit floating point
     */
    @Inline public void writeFloat(float v) {
        ensureAdditionalCapacity(4);
        buffer.buffer.putFloat(buffer.position, v);
        buffer.position += 4;
    }

    /**
     * Print line to StreamBuffer.
     *
     * @param s Line to print
     */
    public void println(@Const @Ref String s) {
        writeString(s, false);
        writeByte('\n');
        checkFlush();
    }

    /**
     * Print String to StreamBuffer.
     *
     * @param s Line to print
     */
    public void print(@Const @Ref String s) {
        writeString(s, false);
        checkFlush();
    }

    // Serialization related

    /*@Override
    @InCpp("return 70;")
    public short getUid() {
        return (short)serialVersionUID;
    }*/

//  /**
//   * Does specified Buffer fit in the remaining capacity of this buffer?
//   *
//   * @param cbb Buffer
//   * @param writeSize Will size be written, too?
//   * @return Answer
//   */
//  public boolean fits(@Const @Ref OutputStreamBuffer cbb, boolean writeSize) {
//      int diff = remaining() - cbb.getWriteSize();
//      return writeSize ? diff - 4 >= 0 : diff >= 0;
//  }

    public String toString() {
        return "OutputStreamBuffer - " + buffer.toString();
    }

//  /**
//   * Reads the specified number of bytes from the source (possibly blocks)
//   *
//   * @param source Source to read from
//   * @param len Size of data to read
//   * @param startAt0 Write to this buffer at position 0 or rather continue at current position?
//   */
//  public void write(Source source, @SizeT int len, boolean startAt0) {
//      if (sink == null) {
//          if (startAt0) {
//              writePos = 0;
//              ensureCapacity(len, false, resizeReserveFactor);
//          } else {
//              ensureAdditionalCapacity(len);
//          }
//          source.readFully(this, writePos, len);
//          writePos += len;
//      } else {
//          while(len > 0) {
//              @SizeT int read = Math.min(len, remaining());
//              source.readFully(this, writePos, len);
//              writePos += read;
//              len -= read;
//              if (len > 0) {
//                  flush();
//              }
//          }
//      }
//  }

//  /**
//   * Reads from the specified stream until it is finished (possibly blocks)
//   *
//   * @param source Source to read from
//   */
//  public void writeFully(Source source) {
//      writeFully(source, 0, resizeReserveFactor, false);
//  }

//  /**
//   * Reads from the specified stream until it is finished (possibly blocks)
//   * (the last three parameters are only relevant for self-growing buffers)
//   *
//   * @param source Source to read from
//   * @param sizeHint Approximation what size this stream might have (this size will be allocated instantly)
//   * @param resizeFactor Alternative Resize reserver factor
//   * @param startAt0 Write to this buffer at position 0 or rather continue at current position?
//   */
//  public void writeFully(Source source, int sizeHint, double resizeFactor, boolean startAt0) {
//      if (sink == null) {
//          if (startAt0) {
//              writePos = 0;
//              ensureCapacity(sizeHint, false, resizeFactor);
//          } else {
//              ensureCapacity(getWriteSize() + sizeHint, true, resizeFactor);
//          }
//
//          while(true) {
//              int read = source.read(this, writePos);
//              if (read == -1) {
//                  return;
//              }
//              writePos += read;
//              if (writePos == capacity()) {
//                  if (resizeReserveFactor < 1.1) {
//                      System.out.println("warning: small resize factor");
//                  }
//                  ensureCapacity(capacity() + 1, true, resizeReserveFactor);
//              }
//          }
//      } else { // streamed
//          while(true) {
//              if (writePos > capacity() - getCopyFraction()) {
//                  flush();
//              }
//              int read = source.read(this, writePos);
//              if (read == -1) {
//                  return;
//              }
//              writePos += read;
//          }
//      }
//  }

    public void close() {
        if (!closed) {
            flush();
            sink.close(this, buffer);
            //Cpp sinkLock._reset();
        }
        closed = true;
    }

//  @JavaOnly
//  protected InputStreamBuffer createReadView() {
//      return new InputStreamBuffer(this);
//  }
//
//  @JavaOnly
//  class ReadThreadLocal extends ThreadLocal<InputStreamBuffer> {
//
//      @Override
//      protected InputStreamBuffer initialValue() {
//          return createReadView();
//      }
//  }

    public void write(@Const @Ref IntArrayWrapper array) {
        writeInt(array.size());
        for (int i = 0, n = array.size(); i < n; i++) {
            writeInt(array.get(i));
        }
    }

    /**
     * A "skip offset" will be written to this position in the stream.
     *
     * (only one such position can be set/remembered at a time)
     *
     * As soon as the stream has reached the position to which are reader might want to skip
     * call setSkipTargetHere()
     */
    public void writeSkipOffsetPlaceholder() {
        assert(curSkipOffsetPlaceholder < 0);
        curSkipOffsetPlaceholder = buffer.position;
        writeInt(Integer.MIN_VALUE);
    }

    /**
     * Set target for last "skip offset" to this position.
     */
    public void skipTargetHere() {
        assert(curSkipOffsetPlaceholder >= 0);
        buffer.buffer.putInt(curSkipOffsetPlaceholder, buffer.position - curSkipOffsetPlaceholder - 4);
        curSkipOffsetPlaceholder = -1;
    }

    // Sink implementation - for chaining OutputStreamBuffers together

    @Override
    public void close(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        sink.close(this, buffer);
    }

    @Override
    public void directWrite(OutputStreamBuffer outputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        sink.directWrite(this, buffer, offset, len);
    }

    @Override
    public boolean directWriteSupport() {
        return sink.directWriteSupport();
    }

    @Override
    public void reset(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer) {
        sink.reset(this, this.buffer);
        buffer.assign(this.buffer);
    }

    @Override
    public boolean write(OutputStreamBuffer outputStreamBuffer, BufferInfo buffer, int sizeHint) {
        boolean result = sink.write(this, buffer, sizeHint);
        this.buffer.assign(buffer); // synchronize
        return result;
    }

    /**
     * Write all available data from input stream to this output stream buffer
     *
     * @param inputStream Input Stream
     */
    public void writeAllAvailable(@Ptr InputStreamBuffer inputStream) {
        while (inputStream.moreDataAvailable()) {
            inputStream.ensureAvailable(1);
            write(inputStream.curBuffer.buffer, inputStream.curBuffer.position, inputStream.curBuffer.remaining());
            inputStream.curBuffer.position = inputStream.curBuffer.end;
        }
    }

    @Override
    public void flush(OutputStreamBuffer outputStreamBuffer, BufferInfo info) {
        flush();
    }
}
