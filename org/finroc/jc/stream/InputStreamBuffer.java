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
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.Friend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.OrgWrapper;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.Virtual;

/**
 * @author max
 *
 * Reasonably efficient, flexible, universal reading interface.
 * A manager class customizes its behaviour (whether it reads from file, memory block, chunked buffer, etc.)
 * It handles, where the data blocks actually come from.
 */
@Friend(OutputStreamBuffer.class)
public class InputStreamBuffer implements Source, HasDestructor {

    /*Cpp
    // for "locking" object source as long as this buffer exists
    std::tr1::shared_ptr<const Interface> sourceLock;
    */

    /** Buffer that is managed by source */
    @PassByValue protected BufferInfo sourceBuffer = new BufferInfo();

    /** Small buffer to enable reading data that crosses buffer boundaries */
    @PassByValue protected BufferInfo boundaryBuffer = new BufferInfo();

    /** Actual boundary buffer backend - symmetric layout: 7 bit old bytes - 7 bit new bytes */
    @PassByValue protected FixedBuffer boundaryBufferBackend = new FixedBuffer(14);

    /** Current buffer - either sourceBuffer or boundary buffer */
    @Ptr protected BufferInfo curBuffer = null;

    /** Manager that handles, where the data blocks come from etc. */
    @Ptr protected Source source = null;

    /** Manager that handles, where the data blocks come from etc. */
    @Const @Ptr protected ConstSource constSource = null;

    /** Current absolute buffer read position of buffer start - relevant when using Source; 64bit value, because we might transfer several GB over a stream */
    protected long absoluteReadPos = 0;

    /** (Absolute) skip offset target - if one has been read - otherwise -1 */
    protected long curSkipOffsetTarget = -1;

    /** Has stream/source been closed? */
    protected boolean closed = false;

    /** Is direct read support available with this sink? */
    protected boolean directReadSupport = false;

    @Init("sourceLock()")
    public InputStreamBuffer() {
        boundaryBuffer.buffer = boundaryBufferBackend;
    }

    @Init("sourceLock()")
    public InputStreamBuffer(@CppType("const ConstSource")  @OrgWrapper @SharedPtr ConstSource source_) {
        boundaryBuffer.buffer = boundaryBufferBackend;

        // JavaOnlyBlock
        reset(source_);

        //Cpp reset(source_);
    }

    /**
     * @param source Source that handles, where the data blocks come from etc.
     */
    @Init("sourceLock()")
    public InputStreamBuffer(@OrgWrapper @SharedPtr Source source_) {
        boundaryBuffer.buffer = boundaryBufferBackend;

        // JavaOnlyBlock
        reset(source_);

        //Cpp reset(source_);
    }

    public void delete() {
        close();
    }

//  /** Reference to stream buffer */
//  @Const @Ptr protected OutputStreamBuffer streamBuffer;
//
//  /** Reference to stream buffer - in case it has source and is not const (in C++) - otherwise null */
//  @Ptr protected OutputStreamBuffer streamBufferSrc;
//
//    /*Cpp
//    inline ReadView() :
//      streamBuffer(NULL),
//      streamBufferSrc(NULL),
//      readPos(0),
//      absoluteReadPos(0)
//    {}
//
//    inline ReadView(const StreamBuffer* streamBuffer_) :
//        streamBuffer(streamBuffer_),
//        streamBufferSrc(NULL),
//        readPos(0),
//        absoluteReadPos(0)
//    {}
//  */
//
//  /**
//   * @param streamBuffer
//   */
//  public InputStreamBuffer(@Ptr OutputStreamBuffer streamBuffer_) {
//      streamBuffer = streamBuffer_;
//      streamBufferSrc = streamBuffer.hasSource ? streamBuffer_ : null;
//  }

    /** ReadView of buffer - one for each thread */
    //@InCpp("FixedBuffer readView, ")
    //protected ByteBuffer readView;

    /** Version of buffer that is wrapped */
    //@JavaOnly private int allocationView = -1;

    /** TempView of buffer - one for each thread */
    //protected TempByteBuffer tempView;

    /** Temporary byte array for copy operations */
    //protected final byte[] tempArray = new byte[TEMP_ARRAY_SIZE];
//
//  public void init(boolean reset) {
//      /*if (allocationView != allocationCount) {
//          readView
//      }
//      if (reset) {
//          rv.readView.position(0);
//          rv.readView.limit(buffer.limit());
//      }
//      readView = buffer.duplicate();
//      allocationView = allocationCount;*/
//      if (reset) {
//          readPos = 0;
//          absoluteReadPos = 0;
//      }
//  }

//  /**
//   * @return Size of data that can be read from buffer
//   */
//  public @SizeT int getSize() {
//      //return writePos > 0 ? writePos : capacity();
//      return this.streamBuffer.writePos;
//  }

    /**
     * @return Remaining size of data in wrapped current intermediate buffer.
     * There might actually be more to read in following buffers though.
     * (Therefore, this is usually pretty useless - some internal implementations use it though)
     */
    public @SizeT int remaining() {
        return curBuffer.remaining();
    }

    /**
     * Resets buffer for reading - may not be supported by all managers
     */
    public void reset() {
        if (source != null) {
            source.reset(this, sourceBuffer);
            directReadSupport = source.directReadSupport();
            closed = false;
        } else if (constSource != null) {
            constSource.reset(this, sourceBuffer);
            directReadSupport = constSource.directReadSupport();
            closed = false;
        }
        curBuffer = sourceBuffer;
        absoluteReadPos = 0;
    }

    /*Cpp
    void reset(std::tr1::shared_ptr<ConstSource> source) {
        sourceLock = source;
        reset(source._get());
    }

    void reset(std::tr1::shared_ptr<const ConstSource> source) {
        sourceLock = source;
        reset(source._get());
    }

    void reset(std::tr1::shared_ptr<Source> source) {
        sourceLock = source;
        reset(source._get());
    }
     */

    /**
     * Use this object with different source.
     * Current source will be closed.
     *
     * @param source New Source
     */
    public void reset(@Const @Ptr ConstSource source) {
        close();
        this.source = null;
        this.constSource = source;
        reset();
    }

    /**
     * Use this object with different source.
     * Current source will be closed.
     *
     * @param source New Source
     */
    public void reset(@Ptr Source source) {
        close();
        this.source = source;
        this.constSource = null;
        reset();
    }

    /**
     * In case of source change: Cleanup
     */
    public void close() {
        if (!closed) {
            if (source != null) {
                source.close(this, sourceBuffer);
            } else if (constSource != null) {
                constSource.close(this, sourceBuffer);
            }
            //Cpp sourceLock._reset();
        }
        closed = true;
    }

    /**
     * Read null-terminated string (16 Bit Characters)
     *
     * @param s String
     */
    @JavaOnly public String readUnicode() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = readChar();
            if (c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Read string (16 Bit Characters). Stops at null-termination or specified length.
     *
     * @param length Length of string to read (including possible termination)
     */
    @JavaOnly public String readUnicode(@SizeT int length) {
        StringBuilder sb = new StringBuilder(length);
        int count = 0;
        while (true) {
            char c = readChar();
            if (c == 0 || count == length) {
                break;
            }
            sb.append(c);
            count++;
        }
        return sb.toString();
    }

    /**
     * Skips null-terminated string (16 Bit Characters)
     */
    @JavaOnly public void skipUnicode() {
        while (true) {
            char c = readChar();
            if (c == 0) {
                break;
            }
        }
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @return String
     */
    @Inline
    public String readString() {
        StringBuilder sb = new StringBuilder(); // no shortcut in C++, since String could be in this chunk only partly
        readString(sb);
        return sb.toString();
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringBuilder object to write result to
     */
    public void readString(@Ref StringBuilder sb) {
        sb.delete(0, sb.length());
        while (true) {
            byte b = readByte();
            if (b == 0) {
                break;
            }
            sb.append((char)b);
        }
    }

    /**
     * Read string (8 Bit Characters - Suited for ASCII). Stops at null-termination or specified length.
     *
     * @param length Length of string to read (including possible termination)
     * @return String
     */
    public String readString(@SizeT int length) {
        StringBuilder sb = new StringBuilder(); // no shortcut in C++, since String could be in this chunk only partly
        readString(sb, length);
        return sb.toString();
    }

    /**
     * Read string (8 Bit Characters - Suited for ASCII). Stops at null-termination or specified length.
     *
     * @param sb StringBuilder object to write result to
     * @param length Length of string to read (including possible termination)
     */
    public void readString(@Ref StringBuilder sb, @SizeT int length) {
        @SizeT int count = 0;
        while (true) {
            byte b = readByte();
            if (b == 0 || count == length) {
                break;
            }
            sb.append(b);
            count++;
        }
    }

    /**
     * Skips null-terminated string (8 Bit Characters)
     */
    public void skipString() {
        while (true) {
            byte c = readByte();
            if (c == 0) {
                break;
            }
        }
    }

    /**
     * Read custom serializable object from stream
     *
     * @param writeTo Already allocated object to write data to (if null, new object will be allocated)
     * @return Deserialized object
     */
    /*@SuppressWarnings("unchecked")
    public <K extends CustomSerialization> K readCSObject(K writeTo) throws Exception {
        short uid = readShort();
        if (writeTo == null || writeTo.getUid() != uid) {
            K cs = (K)DataTypeRegister2.getInstance().getDataType(uid).createInstance();
            cs.deserialize(this);
            return cs;
        } else {
            writeTo.deserialize(this);
            return writeTo;
        }
        return CoreDataCommon.readCSObject(this, writeTo);
    }*/

    /**
     * Ensures that the specified number of bytes is available for reading
     */
    @Inline protected void ensureAvailable(@SizeT int required) {
        assert(!closed);
        @SizeT int available = remaining();
        if (available < required) {
            // copy rest to beginning and get next bytes from input
            fetchNextBytes(required - available);
            //  throw new RuntimeException("Attempt to read outside of buffer");
        }
    }

    /**
     * Fills buffer with contents from input stream
     *
     * @param minRequired2 Minimum number of bytes to read (block otherwise)
     */
    @Virtual protected void fetchNextBytes(@SizeT int minRequired2) {
        assert(minRequired2 <= 8);
        assert(source != null || constSource != null);

        // are we finished using boundary buffer?
        if (usingBoundaryBuffer() && boundaryBuffer.position >= 7) {
            absoluteReadPos += 7;
            curBuffer = sourceBuffer;
            ensureAvailable(minRequired2);
            return;
        }

        // put any remaining bytes in boundary buffer
        int remain = remaining();
        absoluteReadPos += (curBuffer.end - curBuffer.start);
        if (remain > 0) {
            boundaryBuffer.position = 7 - remain;
            boundaryBuffer.start = 0;
            absoluteReadPos -= 7;
            //boundaryBuffer.buffer.put(boundaryBuffer.position, sourceBuffer.buffer, sourceBuffer.position, remain);
            sourceBuffer.buffer.get(sourceBuffer.position, boundaryBuffer.buffer, boundaryBuffer.position, remain); // equivalent, but without problem that SourceBuffer limit is changed in java
            curBuffer = boundaryBuffer;
        }

        // read next block
        if (source != null) {
            source.read(this, sourceBuffer, minRequired2);
        } else {
            constSource.read(this, sourceBuffer, minRequired2);
        }
        assert(sourceBuffer.remaining() >= minRequired2);

        // (possibly) fill up boundary buffer
        if (remain > 0) {
            //boundaryBuffer.buffer.put(7, sourceBuffer.buffer, 0, minRequired2);
            sourceBuffer.buffer.get(0, boundaryBuffer.buffer, 7, minRequired2);
            boundaryBuffer.end = 7 + minRequired2;
            sourceBuffer.position += minRequired2;
        }

//      int minRequired = minRequired2; // int, because we also need to be able to handle negative numbers
//      if (streamBufferSrc.source != null) { // "ordinary" source
//          assert minRequired <= streamBufferSrc.capacity();
//
//          // preserve rest ?
//          int remain = remaining();
//          if (remain > 0) {
//
//              // JavaOnlyBlock
//              ByteBuffer bb = streamBufferSrc.getTempBuffer();
//              streamBufferSrc.put(0, bb, readPos, remain);
//
//              /*Cpp
//              streamBufferSrc->put(0, streamBufferSrc->buffer + readPos, remain); // shouldn't overlap since we typically fetch a few bytes
//               */
//          }
//
//          absoluteReadPos += readPos;
//          readPos = 0;
//          streamBufferSrc.writePos = remain;
//          minRequired -= remain;
//
//          //@SizeT int maxRemaining = streamBufferSrc.capacity() - remain; // don't need that because source.read already takes care of this
//          @SizeT int lastRead = 0;
//          do {
//              lastRead = streamBufferSrc.source.read(streamBufferSrc, streamBufferSrc.writePos);
//              //maxRemaining -= lastRead;
//              minRequired -= lastRead;
//              streamBufferSrc.writePos += lastRead;
//          } while (lastRead > 0 && minRequired > 0);
//
//          if (minRequired > 0) { // switch to blocking get
//              streamBufferSrc.source.readFully(streamBufferSrc, streamBufferSrc.writePos, minRequired);
//          }
//      } else { // block source
//          assert remaining() <= 0 : "Data on block boundary";
//          FixedBuffer fb = streamBufferSrc.blockSource.readNextBlock();
//          absoluteReadPos += readPos;
//          readPos = 0;
//          streamBufferSrc.writePos = fb.capacity();
//
//          // JavaOnlyBlock
//          streamBufferSrc.buffer = fb.buffer.duplicate();
//          streamBufferSrc.allocationCount++;
//
//          /*Cpp
//          streamBufferSrc->checkDelete();
//          streamBufferSrc->ownsBuf = false;
//          streamBufferSrc->buffer = fb.buffer;
//          streamBufferSrc->capacityX = fb.capacityX;
//           */
//
//          /*readView = buffer.duplicate();
//          allocationView = allocationCount;
//          readView.position(0);
//          readView.limit(buffer.limit());*/
//      }
    }

    /**
     * @return boolean value (byte is read from stream and compared against zero)
     */
    public boolean readBoolean() {
        return readByte() != 0;
    }

    /**
     * @return 8 bit integer
     */
    public byte readByte() {
        ensureAvailable(1);
        byte b = curBuffer.buffer.getByte(curBuffer.position);
        curBuffer.position++;
        return b;
    }

    /**
     * @return Next byte - without forwarding read position though
     */
    public byte peek() {
        ensureAvailable(1);
        byte b = curBuffer.buffer.getByte(curBuffer.position);
        return b;
    }

    /**
     * @return 16 bit character
     */
    public char readChar() {
        ensureAvailable(2);
        char c = curBuffer.buffer.getChar(curBuffer.position);
        curBuffer.position += 2;
        return c;
    }

    /**
     * @return 64 bit floating point
     */
    public double readDouble() {
        ensureAvailable(8);
        double d = curBuffer.buffer.getDouble(curBuffer.position);
        curBuffer.position += 8;
        return d;
    }

    /**
     * @return 32 bit floating point
     */
    public float readFloat() {
        ensureAvailable(4);
        float f = curBuffer.buffer.getFloat(curBuffer.position);
        curBuffer.position += 4;
        return f;
    }

    /*Cpp
    void readFully(void* address, size_t size) {
        ByteArray tmp((int8*)address, size);
        readFully(tmp);
    }
     */

    /**
     * Fill destination array with the next n bytes (possibly blocks with streams)
     *
     * @param b destination array
     */
    public void readFully(@Ref byte[] b) {
        readFully(b, 0, b.length);
    }

    /**
     * Fill destination array with the next n bytes (possibly blocks with streams)
     *
     * @param b destination array
     * @param off Offset
     * @param len Number of bytes to read
     */
    @InCpp( {"FixedBuffer fb(b.getPointer(), b.length);",
             "readFully(fb, off, len);"
            }) // may avoid unnecessary copying in C++
    public void readFully(@Ref byte[] b, @SizeT int off, @SizeT int len) {
        while (true) {
            int read = Math.min(curBuffer.remaining(), len);
            curBuffer.buffer.get(curBuffer.position, b, off, read);
            len -= read;
            off += read;
            curBuffer.position += read;
            assert(len >= 0);
            if (len == 0) {
                break;
            }
            fetchNextBytes(1);
        }
//
//      if (!this.streamBuffer.hasSource) {
//          this.streamBuffer.get(readPos, b, off, len);
//          readPos += len;
//      } else {
//          int curOff = off;
//          while(len > 0) {
//              int read = Math.min(len, remaining());
//              this.streamBuffer.get(readPos, b, curOff, read);
//              readPos += read;
//              len -= read;
//              curOff += read;
//              if (len > 0) { // possibly fetch new bytes
//                  fetchNextBytes(Math.min(len, this.streamBuffer.capacity()));
//              }
//          }
//      }
    }

    /**
     * Fill destination buffer (complete buffer)
     *
     * @param b destination buffer
     */
    public void readFully(@Ref FixedBuffer bb) {
        readFully(bb, 0, bb.capacity());
    }

    /**
     * Fill destination buffer
     *
     * @param bb destination buffer
     * @param offset offset in buffer
     * @param len number of bytes to copy
     */
    public void readFully(@Ref FixedBuffer bb, @SizeT int off, @SizeT int len) {
        while (true) {
            int read = Math.min(curBuffer.remaining(), len);
            curBuffer.buffer.get(curBuffer.position, bb, off, read);
            len -= read;
            off += read;
            curBuffer.position += read;
            assert(len >= 0);
            if (len == 0) {
                break;
            }
            if (usingBoundaryBuffer() || (!directReadSupport)) {
                fetchNextBytes(1);
            } else {
                source.directRead(this, bb, off, len); // shortcut
                absoluteReadPos += len;
                assert(curBuffer.position == curBuffer.end);
                break;
            }
        }

//      if (streamBufferSrc == null) {
//          streamBuffer.get(readPos, bb, off, len);
//          readPos += len;
//      } else {
//          int read = Math.min(len, remaining());
//          streamBufferSrc.get(readPos, bb, off, read);
//          readPos += read;
//          len -= read;
//          off += read;
//          if (len > 0) {
//              streamBufferSrc.source.readFully(bb, off, len); // shortcut
//              streamBufferSrc.writePos = 0; // invalidate buffer contents
//              readPos = 0;
//          }
//      }
    }

    /**
     * @return Is current buffer currently set to boundaryBuffer?
     */
    private boolean usingBoundaryBuffer() {
        return curBuffer.buffer == boundaryBuffer.buffer;
    }

    /**
     * @return 32 bit integer
     */
    public int readInt() {
        ensureAvailable(4);
        int i = curBuffer.buffer.getInt(curBuffer.position);
        curBuffer.position += 4;
        return i;
    }

    /**
     * @return String/Line from stream (ends either at line delimiter or 0-character)
     */
    public String readLine() {
        StringBuilder sb = new StringBuilder();
        while (true) {
            byte b = readByte();
            if (b == 0 || b == '\n') {
                break;
            }
            sb.append((char)b);
        }
        return sb.toString();
    }

    /**
     * @return 8 byte integer
     */
    public long readLong() {
        ensureAvailable(8);
        long l = curBuffer.buffer.getLong(curBuffer.position);
        curBuffer.position += 8;
        return l;
    }

    /**
     * @return 2 byte integer
     */
    public short readShort() {
        ensureAvailable(2);
        short s = curBuffer.buffer.getShort(curBuffer.position);
        curBuffer.position += 2;
        return s;
    }

    /**
     * @return unsigned 1 byte integer
     */
    public int readUnsignedByte() {
        ensureAvailable(1);
        int i = curBuffer.buffer.getUnsignedByte(curBuffer.position);
        curBuffer.position += 1;
        return i;
    }

    /**
     * @return unsigned 2 byte integer
     */
    public int readUnsignedShort() {
        ensureAvailable(2);
        int i = curBuffer.buffer.getUnsignedShort(curBuffer.position);
        curBuffer.position += 2;
        return i;
    }

    /**
     * Skip specified number of bytes
     */
    public void skip(@SizeT int n) {
        /*      if (this.streamBuffer.source == null) {
                    readPos += n;
                } else {*/

        while (true) {
            if (remaining() >= n) {
                curBuffer.position += n;
                return;
            }
            n -= remaining();
            curBuffer.position = curBuffer.end;
            fetchNextBytes(1);
        }
    }

    /*Cpp
    // stream operator
    template <typename T>
    InputStreamBuffer& operator>>(T& t) {
        ensureAvailable(sizeof(T));
        t = curBuffer->buffer->getImpl<T>(curBuffer->position);
        curBuffer->position += sizeof(T);
        return *this;
    }
     */

    /*public int read(FixedBuffer buffer, int offset) {
        ensureAvailable(1);
        int size = Math.min(buffer.capacity() - offset, remaining());
        readFully(buffer, offset, size);
        return size;
    }*/

    /**
     * Read IntArrayWrapper from stream
     *
     * @param array Array to fill
     */
    public void read(@Ref IntArrayWrapper array) {
        array.setSize(0);
        int size = readInt();
        for (int i = 0; i < size; i++) {
            array.add(readInt());
        }
    }

    /**
     * Read "skip offset" at current position and store it internally
     */
    public void readSkipOffset() {
        curSkipOffsetTarget = absoluteReadPos + curBuffer.position;
        curSkipOffsetTarget += readInt();
        curSkipOffsetTarget += 4; // from readInt()
    }

    /**
     * Move to target of last read skip offset
     */
    public void toSkipTarget() {
        assert(curSkipOffsetTarget >= absoluteReadPos + curBuffer.position);
        skip((int)(curSkipOffsetTarget - absoluteReadPos - curBuffer.position));
        curSkipOffsetTarget = 0;
    }

    // Source methods for efficient chaining of buffers

    @Override
    public void close(InputStreamBuffer buf, BufferInfo buffer) {
        close();
    }

    @Override
    public void read(InputStreamBuffer buf, BufferInfo buffer, int len) {

        // read next chunk - and copy this info to chained input stream buffer
        curBuffer.position = curBuffer.end; // move marker to end so that ensureAvailable fetches next bytes
        ensureAvailable(len);
        buffer.assign(curBuffer);
    }

    @Override
    public void reset(InputStreamBuffer buf, BufferInfo buffer) {
        reset();
        buffer.assign(curBuffer);
    }

    @Override
    public void directRead(InputStreamBuffer inputStreamBuffer, FixedBuffer buffer, int offset, int len) {
        directRead(this, buffer, offset, len);
    }

    @Override
    public boolean directReadSupport() {
        return source != null ? source.directReadSupport() : constSource.directReadSupport();
    }

    public String toString() {
        if (curBuffer.buffer != null) {
            return "InputStreamBuffer - position: " + curBuffer.position + " start: " + curBuffer.start + " end: " + curBuffer.end;
        } else {
            return "InputStreamBuffer - no buffer backend";
        }
    }

    @Override
    public boolean moreDataAvailable(InputStreamBuffer inputStreamBuffer, BufferInfo buffer) {
        if (source != null) {
            return source.moreDataAvailable(this, buffer);
        } else {
            return constSource.moreDataAvailable(this, buffer);
        }
    }

    /**
     * @return Is further data available?
     */
    public boolean moreDataAvailable() {
        if (remaining() > 0) {
            return true;
        }
        //System.out.println("Not here");
        return source != null ? source.moreDataAvailable(this, sourceBuffer) : constSource.moreDataAvailable(this, sourceBuffer);
    }
}
