/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2009-2011 Max Reichardt,
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

import org.finroc.jc.HasDestructor;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.CppFilename;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppName;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.Friend;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.IncludeClass;
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
import org.finroc.jc.annotation.VoidPtr;

/**
 * @author max
 *
 * Reasonably efficient, flexible, universal reading interface.
 * A manager class customizes its behaviour (whether it reads from file, memory block, chunked buffer, etc.)
 * It handles, where the data blocks actually come from.
 */
@CppName("InputStream") @CppFilename("InputStream")
@Friend(OutputStreamBuffer.class)
@HAppend( {
    "inline InputStream& operator>> (InputStream& is, char& t) { t = is.readNumber<char>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, int8_t& t) { t = is.readNumber<int8_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, int16_t& t) { t = is.readNumber<int16_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, int32_t& t) { t = is.readNumber<int32_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, long int& t) { t = static_cast<long int>(is.readNumber<int64_t>()); /* for 32/64-bit safety */ return is; }",
    "inline InputStream& operator>> (InputStream& is, long long int& t) { t = is.readNumber<long long int>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, uint8_t& t) { t = is.readNumber<uint8_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, uint16_t& t) { t = is.readNumber<uint16_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, uint32_t& t) { t = is.readNumber<uint32_t>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, long unsigned int& t) { t = static_cast<long unsigned int>(is.readNumber<uint64_t>()); /* for 32/64-bit safety */ return is; }",
    "inline InputStream& operator>> (InputStream& is, long long unsigned int& t) { t = is.readNumber<long long unsigned int>(); return is; }",
    "inline InputStream& operator>> (InputStream& is, float& t) { t = is.readFloat(); return is; }",
    "inline InputStream& operator>> (InputStream& is, double& t) { t = is.readDouble(); return is; }",
    "inline InputStream& operator>> (InputStream& is, bool& t) { t = is.readBoolean(); return is; }",
    "inline InputStream& operator>> (InputStream& is, std::string& t) { t = is.readString(); return is; }",
    "inline InputStream& operator>> (InputStream& is, Serializable& t) { t.deserialize(is); return is; }",
    "template <typename T>",
    "inline InputStream& operator>> (InputStream& is, std::vector<T>& t) { is.readSTLContainer<std::vector<T>, T>(t); return is; }",
    "template <typename T>",
    "inline InputStream& operator>> (InputStream& is, std::list<T>& t) { is.readSTLContainer<std::list<T>, T>(t); return is; }",
    "template <typename T>",
    "inline InputStream& operator>> (InputStream& is, std::deque<T>& t) { is.readSTLContainer<std::deque<T>, T>(t); return is; }"
})
@IncludeClass(RRLibSerializableImpl.class)
@Include( {"detail/tListElemInfo.h", "<vector>", "<list>", "<deque>", "<endian.h>", "sStaticFactory.h"})
@CppInclude("<unistd.h>")
public class InputStreamBuffer implements Source, HasDestructor {

    /*Cpp
    // for "locking" object source as long as this buffer exists
    std::shared_ptr<const void> sourceLock;
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

    /** timeout for blocking calls (<= 0 when disabled) */
    protected int timeout = -1;

    /** Default Factory to use for creating objects. */
    protected @PassByValue DefaultFactory defaultFactory = new DefaultFactory();;

    /** Factory to use for creating objects. */
    protected @Ptr Factory factory = defaultFactory;

    /** Data type encoding */
    public enum TypeEncoding {
        LocalUids, // use local uids. fastest. Can, however, only be used with streams encoded by this runtime.
        Names, // use names. Can be decoded in any runtime that knows types.
        Custom // use custom type codec
    }

    /** Data type encoding that is used */
    protected TypeEncoding encoding;

    /** Custom type encoder */
    protected @SharedPtr TypeEncoder customEncoder;

    @JavaOnly
    public InputStreamBuffer() {
        this(TypeEncoding.LocalUids);
    }

    @JavaOnly
    public InputStreamBuffer(@CppType("const ConstSource") @OrgWrapper @SharedPtr ConstSource source_) {
        this(source_, TypeEncoding.LocalUids);
    }

    @Init("sourceLock()")
    public InputStreamBuffer(@CppDefault("_eLocalUids") TypeEncoding encoding) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        this.encoding = encoding;
    }

    @Init("sourceLock()")
    public InputStreamBuffer(@PassByValue @SharedPtr TypeEncoder encoder) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;
    }

    //Cpp template <typename T>
    @Init("sourceLock()") @Inline
    public InputStreamBuffer(@PassByValue @CppType("T") ConstSource source_, @CppDefault("_eLocalUids") TypeEncoding encoding) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        this.encoding = encoding;

        // JavaOnlyBlock
        reset(source_);

        //Cpp reset(source_);
    }

    //Cpp template <typename T>
    @Init("sourceLock()") @Inline
    public InputStreamBuffer(@PassByValue @CppType("T") ConstSource source_, @PassByValue @SharedPtr TypeEncoder encoder) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;

        // JavaOnlyBlock
        reset(source_);

        //Cpp reset(source_);
    }


    /**
     * @param source Source that handles, where the data blocks come from etc.
     */
    @JavaOnly
    @Init("sourceLock()")
    public InputStreamBuffer(@OrgWrapper @SharedPtr Source source_) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        reset(source_);
    }

    /**
     * @param source Source that handles, where the data blocks come from etc.
     */
    @JavaOnly
    @Init("sourceLock()")
    public InputStreamBuffer(@OrgWrapper @SharedPtr Source source_, @SharedPtr TypeEncoder encoder) {
        boundaryBuffer.buffer = boundaryBufferBackend;
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;
        reset(source_);
    }

    public void delete() {
        close();
    }

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
    template <typename T>
    void reset(std::shared_ptr<T> source) {
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
    @InCppFile
    public @CppType("std::string") String readString() {
        @CppType("StringOutputStream")
        StringBuilder sb = new StringBuilder(); // no shortcut in C++, since String could be in this chunk only partly
        readStringImpl(sb);
        return sb.toString();
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringOutputStream object to write result to
     */
    @InCpp("sb.clear(); readStringImpl(sb);") @InCppFile
    public void readString(@Ref StringOutputStream sb) {
        sb.clear();
        readStringImpl(sb.wrapped);
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringBuilder object to write result to
     */
    @JavaOnly
    public void readString(@Ref StringBuilder sb) {
        sb.delete(0, sb.length());
        readStringImpl(sb);
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringBuilder object to write result to
     */
    @Inline
    public <T extends StringBuilder> void readStringImpl(@Ref T sb) {
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
    public @CppType("std::string") String readString(@SizeT int length) {
        StringOutputStream sb = new StringOutputStream(); // no shortcut in C++, since String could be in this chunk only partly
        readString(sb, length);
        return sb.toString();
    }

    /**
     * Read string (8 Bit Characters - Suited for ASCII). Stops at null-termination or specified length.
     *
     * @param sb StringBuilder object to write result to
     * @param length Length of string to read (including possible termination)
     */
    public void readString(@Ref StringOutputStream sb, @SizeT int length) {
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
     * Ensures that the specified number of bytes is available for reading
     */
    @Inline protected void ensureAvailable(@SizeT int required) {
        assert(!closed);
        @SizeT int available = remaining();
        if (available < required) {
            // copy rest to beginning and get next bytes from input
            fetchNextBytes(required - available);
            assert(remaining() >= required);
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

        // if we have a timeout set - wait until more data is available
        // TODO: this doesn't ensure that there are minRequired2 bytes available. However, it should be sufficient in 99.9% of the cases.
        if (timeout > 0) {
            int initialSleep = 20; // timeout-related
            int slept = 0; // timeout-related
            while (timeout > 0 && (!(source != null ? source.moreDataAvailable(this, sourceBuffer) : constSource.moreDataAvailable(this, sourceBuffer)))) {
                initialSleep *= 2;

                //JavaOnlyBlock
                try {
                    Thread.sleep(initialSleep);
                } catch (InterruptedException e) {}

                //Cpp _usleep(initialSleep * 1000);

                slept += initialSleep;
                if (slept > timeout) {

                    //JavaOnlyBlock
                    throw new RuntimeException("Read Timeout");

                    //Cpp throw std::runtime_error("Read Timeout");
                }
            }
        }

        // read next block
        if (source != null) {
            source.read(this, sourceBuffer, minRequired2);
        } else {
            constSource.read(this, sourceBuffer, minRequired2);
        }
        assert(sourceBuffer.remaining() >= minRequired2);

        //JavaOnlyBlock
        assert(sourceBuffer.position >= 0);

        // (possibly) fill up boundary buffer
        if (remain > 0) {
            //boundaryBuffer.buffer.put(7, sourceBuffer.buffer, 0, minRequired2);
            sourceBuffer.buffer.get(0, boundaryBuffer.buffer, 7, minRequired2);
            boundaryBuffer.end = 7 + minRequired2;
            sourceBuffer.position += minRequired2;
        }
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
    @JavaOnly
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

    /**
     * Fill destination array with the next n bytes (possibly blocks with streams)
     *
     * @param b destination array
     */
    @JavaOnly
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
    @JavaOnly
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
    }

    /*Cpp
    void readFully(void* address, size_t size) {
        FixedBuffer tmp((char*)address, size);
        readFully(tmp);
    }
     */

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
    @InCpp("return readNumber<int32_t>();")
    public int readInt() {
        ensureAvailable(4);
        int i = curBuffer.buffer.getInt(curBuffer.position);
        curBuffer.position += 4;
        return i;
    }

    /**
     * @return String/Line from stream (ends either at line delimiter or 0-character)
     */
    public @CppType("std::string") String readLine() {
        StringOutputStream sb = new StringOutputStream();
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
    @InCpp("return readNumber<int64_t>();")
    public long readLong() {
        ensureAvailable(8);
        long l = curBuffer.buffer.getLong(curBuffer.position);
        curBuffer.position += 8;
        return l;
    }

    /**
     * @return 2 byte integer
     */
    @InCpp("return readNumber<int16_t>();")
    public short readShort() {
        ensureAvailable(2);
        short s = curBuffer.buffer.getShort(curBuffer.position);
        curBuffer.position += 2;
        return s;
    }

    /**
     * @return unsigned 1 byte integer
     */
    @InCpp("return readNumber<uint8_t>();")
    public int readUnsignedByte() {
        ensureAvailable(1);
        int i = curBuffer.buffer.getUnsignedByte(curBuffer.position);
        curBuffer.position += 1;
        return i;
    }

    /**
     * @return unsigned 2 byte integer
     */
    @InCpp("return readNumber<uint16_t>();")
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
    T readNumber() {
        ensureAvailable(sizeof(T));
        T t = curBuffer->buffer->getImpl<T>(curBuffer->position);
        curBuffer->position += sizeof(T);

    #if __BYTE_ORDER == __ORDER_BIG_ENDIAN
        T tmp = t;
        char* dest = reinterpret_cast<char*>(&t);
        char* src = reinterpret_cast<char*>(&tmp);
        src += sizeof(T);
        for (size_t i = 0; i < sizeof(T); i++) {
            src--;
            *dest = *src;
            dest++;
        }
    #endif

        return t;
    }
     */

    /*public int read(FixedBuffer buffer, int offset) {
        ensureAvailable(1);
        int size = Math.min(buffer.capacity() - offset, remaining());
        readFully(buffer, offset, size);
        return size;
    }*/

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
        long pos = curBuffer.position;
        assert(curSkipOffsetTarget >= absoluteReadPos + pos);
        skip((int)(curSkipOffsetTarget - absoluteReadPos - pos));
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

    @JavaOnly
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

    /**
     * @return Number of bytes ever read from this stream
     */
    public long getAbsoluteReadPosition() {
        return absoluteReadPos + curBuffer.position;
    }

    /**
     * @return timeout for blocking calls (<= 0 when disabled)
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout for blocking calls (<= 0 when disabled)
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /*Cpp
    // Deserialize STL container (must either have pass-by-value type or shared pointers)
    template <typename C, typename T>
    void readSTLContainer(C& container) {
        typedef detail::ListElemInfo<T> info;

        size_t size = readInt();
        bool constType = readBoolean();
        if (constType == info::isSharedPtr) {
            throw std::runtime_error("Wrong list type");
        }

        // container.resize(size);
        while(container._size() < size) {
          container.push_back(_sStaticFactory<T>::createByValue());
        }
        while(container._size() > size) {
          container.pop_back();
        }

        typename C::iterator it;
        for (it = container._begin(); it != container._end(); it++) {
            if (!constType) {
                DataTypeBase needed = readType();
                DataTypeBase current = info::getType(*it);
                if (needed != current) {
                    if (needed == NULL) {
                        info::reset(*it);
                    } else {
                        info::changeBufferType(factory, *it, needed);
                    }
                }
                if (needed != info::getTypeT()) {
                    needed.deserialize(*this, &info::getElem(*it));
                    continue;
                }
            }
            *this >> info::getElem(*it);
        }
    }
     */

    /**
     * @return Reads type from stream
     */
    public DataTypeBase readType() {
        if (encoding == TypeEncoding.LocalUids) {
            return DataTypeBase.getType(readShort());
        } else if (encoding == TypeEncoding.Names) {
            return DataTypeBase.findType(readString());
        } else {
            return customEncoder.readType(this);
        }
    }

    /**
     * @return Factory to use for creating objects.
     */
    public Factory getFactory() {
        return factory;
    }

    /**
     * When deserializing pointer list, for example, buffers are needed.
     * This factory provides them.
     *
     * It may be reset for more efficient buffer management.
     *
     * @param factory Factory to use for creating objects. (will not be deleted by this class)
     */
    public void setFactory(Factory factory) {
        this.factory = factory == null ? defaultFactory : factory;
    }

    /**
     * Deserialize object with yet unknown type from stream
     * (should have been written to stream with OutputStream.WriteObject() before; typeencoder should be of the same type)
     *
     * @param inInterThreadContainer Deserialize "cheap copy" data in interthread container?
     * @param expectedType expected type (optional, may be null)
     * @param factoryParameter Custom parameter for possibly user defined factory
     * @return Buffer with read object (caller needs to take care of deleting it)
     */
    public GenericObject readObject(@Const @Ref @CppDefault("NULL") DataTypeBase expectedType, @VoidPtr @CppDefault("NULL") Object factoryParameter) {
        //readSkipOffset();
        DataTypeBase dt = readType();
        if (dt == null) {
            return null;
        }

        //JavaOnlyBlock
        if (expectedType != null && (!dt.isConvertibleTo(expectedType))) {
            dt = expectedType; // fix to cope with mca2 legacy blackboards
        }

        GenericObject buffer = factory.createGenericObject(dt, factoryParameter);
        buffer.deserialize(this);
        return buffer;
    }
}
