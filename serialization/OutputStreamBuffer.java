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
import org.rrlib.finroc_core_utils.rtti.DataTypeBase;
import org.rrlib.finroc_core_utils.rtti.GenericObject;

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
public class OutputStreamBuffer implements Sink, HasDestructor {

    /** Committed buffers are buffered/copied (not forwarded directly), when smaller than 1/(2^n) of buffer capacity */
    private static double BUFFER_COPY_FRACTION = 0.25;

    /** Source that determines where buffers that are written to come from and how they are handled */
    private Sink sink = null;

    /** Immediately flush buffer after printing? */
    private final boolean immediateFlush = false;

    /** Has stream been closed? */
    private boolean closed = true;

    /** Buffer that is currently written to - is managed by sink */
    private BufferInfo buffer = new BufferInfo();

    /** -1 by default - buffer position when a skip offset placeholder has been set/written */
    private int curSkipOffsetPlaceholder = -1;

    /** hole Buffers are only buffered/copied, when they are smaller than this */
    private int bufferCopyFraction;

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
    private TypeEncoder customEncoder;

    public OutputStreamBuffer() {
        this(TypeEncoding.LocalUids);
    }

    public OutputStreamBuffer(Sink sink_) {
        this(sink_, TypeEncoding.LocalUids);
    }

    /**
     * @param encoding Data type encoding that is used
     */
    public OutputStreamBuffer(TypeEncoding encoding) {
        this.encoding = encoding;
    }

    public OutputStreamBuffer(TypeEncoder encoder) {
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;
    }

    /**
     * @param sink_ Sink to write to
     * @param encoding Data type encoding that is used
     */
    public OutputStreamBuffer(Sink sink_, TypeEncoding encoding) {
        this.encoding = encoding;
        reset(sink_);
    }

    /**
     * @param sink_ Sink to write to
     * @param encoder Custom type encoder
     */
    public OutputStreamBuffer(Sink sink_, TypeEncoder encoder) {
        customEncoder = encoder;
        encoding = TypeEncoding.Custom;
        reset(sink_);
    }

    public void delete() {
        close();
    }

    /**
     * @return Size of data that was written to buffer
     * (typically useless - because of flushing etc. - only used by some internal stuff)
     */
    public int getWriteSize() {
        return buffer.position - buffer.start;
    }

    /**
     * @return Bytes remaining (for writing) in this buffer
     */
    public int remaining() {
        return buffer.remaining();
    }

    /**
     * Use buffer with different sink (closes old one)
     *
     * @param sink New Sink to use
     */
    public void reset(Sink sink) {
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
    public void writeUnicode(String s, boolean terminate) {
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
    public void writeString(String s) {
        writeString(s, true);
    }

    /**
     * Write null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param sb StringBuilder object
     */
    public void writeString(StringBuilder sb) {
        writeString(sb.toString());
    }

    /**
     * Write string (8 Bit Characters - Suited for ASCII)
     *
     * @param s String
     * @param terminate Terminate string with zero?
     */
    public void writeString(String s, boolean terminate) {
        write(s.getBytes());
        if (terminate) {
            writeByte(0);
        }
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
            assert(addSizeHint < 0 || buffer.remaining() >= 8);
            bufferCopyFraction = (int)(buffer.capacity() * BUFFER_COPY_FRACTION);
        }
    }

    /**
     * Writes all bytes from array to buffer
     *
     * @param b Array
     */
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    /**
     * @return Whole Buffers are only buffered/copied, when they are smaller than this
     */
    protected int getCopyFraction() {
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
    public void write(byte[] b, int off, int len) {
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

    /**
     * Writes specified byte buffer contents to stream
     * Regarding streams:
     *   Large buffers are directly copied to output device
     *   avoiding an unnecessary copying operation.
     *
     * @param bb ByteBuffer (whole buffer will be copied)
     */
    public void write(FixedBuffer bb) {
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
    public void write(FixedBuffer bb, int off, int len) {
        if ((remaining() >= len) && (len < getCopyFraction() || curSkipOffsetPlaceholder >= 0)) {
            buffer.buffer.put(buffer.position, bb, off, len);
            buffer.position += len;
        } else {
            if (directWriteSupport && curSkipOffsetPlaceholder < 0) {
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
    public void ensureAdditionalCapacity(int c) {
        if (remaining() < c) {
            commitData(c - remaining());
        }
    }

    /**
     * Immediately flush buffer if appropriate option is set
     * Used in print methods
     */
    protected void checkFlush() {
        if (immediateFlush) {
            flush();
        }
    }

    /**
     * @param v (1-byte) boolean
     */
    public void writeBoolean(boolean v) {
        writeByte(v ? 1 : 0);
    }

    /**
     * @param v 8 bit integer
     */
    public void writeByte(int v) {
        ensureAdditionalCapacity(1);
        buffer.buffer.putByte(buffer.position, v);
        buffer.position++;
    }

    /**
     * @param v Character
     */
    public void writeChar(char v) {
        ensureAdditionalCapacity(2);
        buffer.buffer.putChar(buffer.position, v);
        buffer.position += 2;
    }

    /**
     * @param v 32 bit integer
     */
    public void writeInt(int v) {
        ensureAdditionalCapacity(4);
        buffer.buffer.putInt(buffer.position, v);
        buffer.position += 4;
    }

    /**
     * @param v 64 bit integer
     */
    public void writeLong(long v) {
        ensureAdditionalCapacity(8);
        buffer.buffer.putLong(buffer.position, v);
        buffer.position += 8;
    }

    /**
     * @param v 16 bit integer
     */
    public void writeShort(int v) {
        ensureAdditionalCapacity(2);
        buffer.buffer.putShort(buffer.position, (short)v);
        buffer.position += 2;
    }

    /**
     * @param v 64 bit floating point
     */
    public void writeDouble(double v) {
        ensureAdditionalCapacity(8);
        buffer.buffer.putDouble(buffer.position, v);
        buffer.position += 8;
    }

    /**
     * @param v 32 bit floating point
     */
    public void writeFloat(float v) {
        ensureAdditionalCapacity(4);
        buffer.buffer.putFloat(buffer.position, v);
        buffer.position += 4;
    }

    /**
     * @param e Enum constant
     */
    public void writeEnum(Enum<?> e) {
        writeEnum(e.ordinal(), e.getDeclaringClass().getEnumConstants());
    }

    /**
     * @param value Enum value (ordinal)
     * @param constants All enum constants
     */
    public void writeEnum(int value, Object[] constants) {
        if (constants.length == 0) {
            assert(value < 0x7FFFFFFF) : "What?";
            writeInt(value);
        } else if (constants.length <= 0x100) {
            writeByte((byte) value);
        } else if (constants.length <= 0x1000) {
            writeShort((short) value);
        } else {
            assert(constants.length < 0x7FFFFFFF) : "What?";
            writeInt(value);
        }
    }

    /**
     * Print line to StreamBuffer.
     *
     * @param s Line to print
     */
    public void println(String s) {
        writeString(s, false);
        writeByte('\n');
        checkFlush();
    }

    /**
     * Print String to StreamBuffer.
     *
     * @param s Line to print
     */
    public void print(String s) {
        writeString(s, false);
        checkFlush();
    }

    public String toString() {
        return "OutputStreamBuffer - " + buffer.toString();
    }

    public void close() {
        if (!closed) {
            flush();
            sink.close(this, buffer);
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
    public void writeAllAvailable(InputStreamBuffer inputStream) {
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

    /**
     * @param type Data type to write/reference (using encoding specified in constructor)
     */
    public void writeType(DataTypeBase type) {
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
     * @param enc Data encoding to use
     */
    public void writeObject(GenericObject to, Serialization.DataEncoding enc) {
        if (to == null) {
            writeType(null);
            return;
        }

        //writeSkipOffsetPlaceholder();
        writeType(to.getType());
        to.serialize(this, enc);
        //skipTargetHere();
    }

    /**
     * @param duration Duration in ms
     */
    public void writeDuration(long duration) {
        writeLong(duration * 1000000);
    }
}
