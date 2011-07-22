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
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.jc.HasDestructor;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.CppDefault;
import org.rrlib.finroc_core_utils.jc.annotation.CppFilename;
import org.rrlib.finroc_core_utils.jc.annotation.CppName;
import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.HAppend;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.IncludeClass;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.OrgWrapper;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.SharedPtr;
import org.rrlib.finroc_core_utils.jc.annotation.SizeT;

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
@CppName("OutputStream") @CppFilename("OutputStream")
@HAppend( {
    "inline OutputStream& operator<< (OutputStream& os, char t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, int8_t t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, int16_t t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, int t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, long int t) { os.writeNumber<int64_t>(t); /* for 32/64-bit safety */ return os; }",
    "inline OutputStream& operator<< (OutputStream& os, long long int t) { os.writeNumber<int64_t>(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, uint8_t t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, uint16_t t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, uint32_t t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, long unsigned int t) { os.writeNumber<uint64_t>(t); /* for 32/64-bit safety */ return os; }",
    "inline OutputStream& operator<< (OutputStream& os, long long unsigned int t) { os.writeNumber(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, float t) { os.writeFloat(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, double t) { os.writeDouble(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, bool t) { os.writeBoolean(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, const char* t) { os.writeString(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, const std::string& t) { os.writeString(t); return os; }",
    "inline OutputStream& operator<< (OutputStream& os, const Serializable& t) { t.serialize(os); return os; }",
    "template <typename T>",
    "inline OutputStream& operator<< (OutputStream& os, const std::vector<T>& t) { os.writeSTLContainer<std::vector<T>, T>(t); return os; }",
    "template <typename T>",
    "inline OutputStream& operator<< (OutputStream& os, const std::list<T>& t) { os.writeSTLContainer<std::list<T>, T>(t); return os; }",
    "template <typename T>",
    "inline OutputStream& operator<< (OutputStream& os, const std::deque<T>& t) { os.writeSTLContainer<std::deque<T>, T>(t); return os; }"
})
@IncludeClass(RRLibSerializableImpl.class)
@Include( {"detail/tListElemInfo.h", "<vector>", "<list>", "<deque>", "<endian.h>"})
public class OutputStreamBuffer implements Sink, HasDestructor {

    /*Cpp
    // for "locking" object sink as long as this buffer exists
    std::shared_ptr<const Sink> sinkLock;
    */

    /** Committed buffers are buffered/copied (not forwarded directly), when smaller than 1/(2^n) of buffer capacity */
    @Const private /*TODO gcc >= 4.5: final*/ static double BUFFER_COPY_FRACTION = 0.25;

    /** Source that determines where buffers that are written to come from and how they are handled */
    @Ptr private Sink sink = null;

    /** Immediately flush buffer after printing? */
    @Const private final boolean immediateFlush = false;

    /** Has stream been closed? */
    private boolean closed = true;

    /** Buffer that is currently written to - is managed by sink */
    @PassByValue private BufferInfo buffer = new BufferInfo();

    /** -1 by default - buffer position when a skip offset placeholder has been set/written */
    private int curSkipOffsetPlaceholder = -1;

    /** hole Buffers are only buffered/copied, when they are smaller than this */
    private @SizeT int bufferCopyFraction;

    /** Is direct write support available with this sink? */
    private boolean directWriteSupport = false;

    /** Data type encoding */
    public enum TypeEncoding {
        LocalUids, // use local uids. fastest. Can, however, only be decoded in this runtime.
        Names, // use names. Can be decoded in any runtime that knows types.
        Custom // use custom type codec
    }

    /** Data type encoding that is used */
    private TypeEncoding encoding;

    /** Custom type encoder */
    private @SharedPtr TypeEncoder customEncoder;

    @JavaOnly
    public OutputStreamBuffer() {
        this(TypeEncoding.LocalUids);
    }

    @JavaOnly
    public OutputStreamBuffer(@OrgWrapper @PassByValue @SharedPtr Sink sink_) {
        this(sink_, TypeEncoding.LocalUids);
    }

    /**
     * @param encoding Data type encoding that is used
     */
    public OutputStreamBuffer(@CppDefault("_eLocalUids") TypeEncoding encoding) {
        this.encoding = encoding;
    }

    public OutputStreamBuffer(@PassByValue @SharedPtr TypeEncoder encoder) {
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;
    }

    /**
     * @param sink_ Sink to write to
     * @param encoding Data type encoding that is used
     */
    public OutputStreamBuffer(@OrgWrapper @PassByValue @SharedPtr Sink sink_, @CppDefault("_eLocalUids") TypeEncoding encoding) {
        this.encoding = encoding;

        //JavaOnlyBlock
        reset(sink_);

        //Cpp reset(sink_);
    }

    /**
     * @param sink_ Sink to write to
     * @param encoder Custom type encoder
     */
    public OutputStreamBuffer(@OrgWrapper @PassByValue @SharedPtr Sink sink_, @PassByValue @SharedPtr TypeEncoder encoder) {
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;

        //JavaOnlyBlock
        reset(sink_);

        //Cpp reset(sink_);
    }

    public void delete() {
        close();
    }

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
    void reset(std::shared_ptr<Sink> sink) {
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

    /*Cpp
    void writeString(const char* s) {
        write(const_cast<char*>(s), strlen(s) + 1);
    }
     */

    /**
     * Write null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param s String
     */
    @Inline public void writeString(@CppType("std::string") @Const @Ref String s) {
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
    public void writeString(@CppType("std::string") @Const @Ref String s, boolean terminate) {

        // JavaOnlyBlock
        write(s.getBytes());
        if (terminate) {
            writeByte(0);
        }

        /*Cpp
        size_t len = terminate ? (s._size() + 1) : s._size();
        write(FixedBuffer((char*)s.c_str(), len));
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
            buffer.buffer.put(buffer.position, b, off, write);
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

    /*Cpp
    inline void write(const void* address, size_t size) {
        FixedBuffer fb((char*)address, size);
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

    /**
     * Immediately flush buffer if appropriate option is set
     * Used in print methods
     */
    @Inline protected void checkFlush() {
        if (immediateFlush) {
            flush();
        }
    }

    /*Cpp
    template <typename T>
    void writeNumber(T t) {
        ensureAdditionalCapacity(sizeof(T));

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

        buffer.buffer->putImpl<T>(buffer.position, t);
        buffer.position += sizeof(T);
    }
     */

    /**
     * @param v (1-byte) boolean
     */
    @Inline public void writeBoolean(boolean v) {
        writeByte(v ? 1 : 0);
    }

    /**
     * @param v 8 bit integer
     */
    @InCpp("writeNumber<int8_t>(static_cast<int8_t>(v));")
    @Inline public void writeByte(int v) {
        ensureAdditionalCapacity(1);
        buffer.buffer.putByte(buffer.position, v);
        buffer.position++;
    }

    /**
     * @param v Character
     */
    @JavaOnly
    @Inline public void writeChar(char v) {
        ensureAdditionalCapacity(2);
        buffer.buffer.putChar(buffer.position, v);
        buffer.position += 2;
    }

    /**
     * @param v 32 bit integer
     */
    @InCpp("writeNumber(v);")
    @Inline public void writeInt(int v) {
        ensureAdditionalCapacity(4);
        buffer.buffer.putInt(buffer.position, v);
        buffer.position += 4;
    }

    /**
     * @param v 64 bit integer
     */
    @InCpp("writeNumber(v);")
    @Inline public void writeLong(long v) {
        ensureAdditionalCapacity(8);
        buffer.buffer.putLong(buffer.position, v);
        buffer.position += 8;
    }

    /**
     * @param v 16 bit integer
     */
    @InCpp("writeNumber<int16_t>(static_cast<int16_t>(v));")
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
    public void println(@Const @Ref @CppType("std::string") String s) {
        writeString(s, false);
        writeByte('\n');
        checkFlush();
    }

    /**
     * Print String to StreamBuffer.
     *
     * @param s Line to print
     */
    public void print(@Const @Ref @CppType("std::string") String s) {
        writeString(s, false);
        checkFlush();
    }

    @JavaOnly
    public String toString() {
        return "OutputStreamBuffer - " + buffer.toString();
    }

    public void close() {
        if (!closed) {
            flush();
            sink.close(this, buffer);
            //Cpp sinkLock._reset();
        }
        closed = true;
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

        //JavaOnlyBlock
        writeInt(Integer.MIN_VALUE);

        //Cpp writeInt(0x80000000);
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


    /*Cpp
    // Serialize STL container (must either have pass-by-value type or shared pointers)
    template <typename C, typename T>
    void writeSTLContainer(const C& container) {
        typedef detail::ListElemInfo<T> info;

        writeInt(container._size());
        const bool constType = !info::isSharedPtr;
        writeBoolean(constType); // const type?
        typename C::const_iterator it;
        for (it = container._begin(); it != container._end(); it++) {
            if (!constType) {
                DataTypeBase type = info::getType(*it);
                writeType(type);
                if (type == NULL) {
                    continue;
                }
                if (type != info::getTypeT()) {
                    type.serialize(*this, &info::getElem(*it));
                    continue;
                }
            }
            *this << info::getElem(*it);
        }
    }
     */

    @Override
    public void flush(OutputStreamBuffer outputStreamBuffer, BufferInfo info) {
        flush();
    }

    /**
     * @param type Data type to write/reference (using encoding specified in constructor)
     */
    public void writeType(DataTypeBase type) {

        //JavaOnlyBlock
        type = type == null ? DataTypeBase.getNullType() : type;

        if (encoding == TypeEncoding.LocalUids) {
            writeShort(type.getUid());
        } else if (encoding == TypeEncoding.Names) {
            writeString(type.getName());
        } else {
            customEncoder.writeType(this, type);
        }
    }

    /**
     * Serialize Object of arbitrary type to stream
     * (including type information)
     *
     * @param to Object to write (may be null)
     */
    public void writeObject(@Const GenericObject to) {
        if (to == null) {
            writeType(null);
            return;
        }

        //writeSkipOffsetPlaceholder();
        writeType(to.getType());
        to.serialize(this);
        //skipTargetHere();
    }
}
