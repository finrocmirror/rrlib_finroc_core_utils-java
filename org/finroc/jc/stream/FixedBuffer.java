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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.Unsigned;
import org.finroc.jc.jni.JNICalls;

/**
 * @author max
 *
 * This is a simple fixed-size memory buffer.
 *
 * In Java it wraps a java.nio.ByteBuffer (this allows sharing the buffer with C++).
 * Its methods are also very similar to ByteBuffer's - which makes ByteBuffer
 * a good source for documentation ;-)
 *
 * Writing arrays/buffers to the buffer is not thread-safe in Java. Everything else is.
 */
public class FixedBuffer {

    /** Use direct byte buffer? */
    @JavaOnly protected static final boolean USE_DIRECT_BUFFERS = JNICalls.JNI_AVAILABLE;

    /** Byte order for this buffer */
    @JavaOnly protected static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();

//  /** number of times buffer was (re-)allocated */
//  @JavaOnly protected volatile int allocationCount = 0;

    @JavaOnly protected final ThreadLocal<TempBuffer> tempBufs = new ThreadLocal<TempBuffer>();

    /** Actual (wrapped) buffer - may be replaced by subclasses */
    @InCpp( {"friend class ReadView;", "",
             "int8* buffer; // pointer to buffer start",
             "size_t capacityX; // buffer capacity",
             "bool ownsBuf; // owned buffers are deleted when this class is"
            })
    protected ByteBuffer buffer;

    /*Cpp
    // @param buffer_ pointer to buffer start
    // @param capacity_ capacity of wrapped buffer
    FixedBuffer(int8* buffer_, size_t capacity_) :
            buffer(buffer_),
            capacityX(capacity_),
            ownsBuf(false) {}

    FixedBuffer(size_t capacity_) :
            buffer(capacity_ > 0 ? new int8[capacity_] : NULL),
            capacityX(capacity_),
            ownsBuf(capacity_ > 0) {}

    FixedBuffer(ByteArray& array) :
            buffer(array.getPointer()),
            capacityX(array.getCapacity()),
            ownsBuf(false) {}

    FixedBuffer(const FixedBuffer& fb) : buffer(fb.buffer), capacityX(fb.capacity()), ownsBuf(false) {}

    FixedBuffer& operator=(const FixedBuffer& fb) {
        buffer = fb.buffer;
        capacityX = fb.capacity();
        ownsBuf = false;
        return *this;
    }

    virtual ~FixedBuffer() {
        checkDelete();
    }

    void checkDelete() {
        if (ownsBuf && buffer != NULL) {
            delete[] buffer;
        }
    }
     */

    /**
     * @return Capacity of buffer (in bytes)
     */
    @InCpp("return capacityX;")
    @ConstMethod public @SizeT int capacity() {
        return buffer.capacity();
    }

    @JavaOnly
    public FixedBuffer(@SizeT int capacity_) {
        this(USE_DIRECT_BUFFERS ? ByteBuffer.allocateDirect(capacity_) : ByteBuffer.allocate(capacity_));
    }

    /**
     * @param array Array to wrap
     */
    @JavaOnly
    public FixedBuffer(@Ref byte[] array) {
        this(ByteBuffer.wrap(array));
    }

    @JavaOnly public FixedBuffer(ByteBuffer bb) {
        buffer = bb;
        buffer.order(BYTE_ORDER);
    }

    /**
     * @return Wrapped ByteBuffer
     */
    @JavaOnly public ByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * @return Temporary buffer
     */
    @JavaOnly public ByteBuffer getTempBuffer() {
        TempBuffer tb = tempBufs.get();
        if (tb == null) {
            tb = new TempBuffer();
            tempBufs.set(tb);
        }
        tb.init();
        return tb.buffer;
    }

    /*Cpp

    // returns raw pointer to buffer start
    const int8* getPointer() const {
        return buffer;
    }

    // returns raw pointer to buffer start
    int8* getPointer() {
        return buffer;
    }

    // Generic put method... okay... this is MUCH more elegant than Java equivalent
    // @param offset absolute offset
    // @param t Data to write
    template <typename T>
    void putImpl(size_t offset, T t) {
        assert(offset + sizeof(T) <= capacityX);
        T* ptr = (T*)(buffer + offset);
        (*ptr) = t;
    }

    // Generic get method... okay... this is MUCH more elegant than Java equivalent again
    // @param offset absolute offset
    // @return t Data at offset
    template <typename T>
    T getImpl(size_t offset) const {
        assert(offset + sizeof(T) <= capacityX);
        T* ptr = (T*)(buffer + offset);
        return *ptr;
    }

    // Copy data from source buffer
    // @param off absolute offset
    // @param other source buffer
    // @param len number of bytes to copy
    void put(size_t off, const void* other, size_t len) {
        assert(off + len <= capacityX);
        memcpy(buffer + off, other, len);
    }

    // Copy data to destination buffer
    // @param off absolute offset
    // @param other destination buffer
    // @param len number of bytes to copy
    void get(size_t off, void* other, size_t len) const {
        assert(off + len <= capacityX);
        memcpy(other, buffer + off, len);
    }
     */

    /**
     * Copy Data to destination array
     *
     * @param offset Offset in this buffer
     * @param dst Destination array
     * @param off offset in destination array
     * @param length number of bytes to copy
     */
    @InCpp( {"assert(off + len <= dst.length);",
             "get(offset, dst.getPointer() + off, len);"
            })
    @ConstMethod public void get(@SizeT int offset, @Ref byte[] dst, @SizeT int off, @SizeT int len) {
        ByteBuffer buffer = getTempBuffer();
        buffer.position(offset);
        buffer.get(dst, off, len);
    }

    /**
     * Copy Data from source array
     *
     * @param offset Offset in this buffer
     * @param src Source array
     * @param off offset in source array
     * @param length number of bytes to copy
     */
    @InCpp( {"assert(off + len <= src.length);", "" +
             "put(offset, src.getPointer() + off, len);"
            })
    public void put(@SizeT int offset, @Const @Ref byte[] src, @SizeT int off, @SizeT int len) {
        buffer.position(offset);
        buffer.put(src, off, len);
    }

    /**
     * Copy Data to destination array
     *
     * @param offset Offset in this buffer
     * @param dst Destination array
     */
    @InCpp("get(offset, dst.getPointer(), dst.length);")
    @ConstMethod public void get(@SizeT int offset, @Ref byte[] dst) {
        ByteBuffer buffer = getTempBuffer();
        buffer.position(offset);
        buffer.get(dst);
    }

    /**
     * Copy Data from source array
     *
     * @param offset Offset in this buffer
     * @param src Source array
     */
    @InCpp("put(offset, src.getPointer(), src.length);")
    public void put(@SizeT int offset, @Const @Ref byte[] src) {
        buffer.position(offset);
        buffer.put(src);
    }

    /**
     * Copy Data to destination buffer
     *
     * @param offset Offset in this buffer
     * @param dst Destination array
     * @param off offset in source array
     * @param length number of bytes to copy
     */
    @JavaOnly public void get(int offset, ByteBuffer dst, int off, int len) {
        ByteBuffer buffer = getTempBuffer();
        dst.clear();
        dst.position(off);
        buffer.position(offset);
        int oldLimit = buffer.limit();
        buffer.limit(offset + len);
        dst.put(buffer);
        buffer.limit(oldLimit);
    }

    /**
     * Copy Data to destination buffer
     *
     * @param offset Offset in this buffer
     * @param dst Destination array
     * @param off offset in source array
     * @param length number of bytes to copy
     */
    @InCpp( {"assert(off + len <= dst.capacity());",
             "get(offset, dst.getPointer() + off, len);"
            })
    @ConstMethod public void get(@SizeT int offset, @Ref FixedBuffer dst, @SizeT int off, @SizeT int len) {
        get(offset, dst.buffer, off, len);
    }

    /**
     * Copy Data from source buffer
     *
     * @param offset Offset in this buffer
     * @param src Source Buffer
     * @param off offset in source buffer
     * @param length number of bytes to copy
     */
    @JavaOnly public void put(int offset, ByteBuffer src, int off, int len) {
        if (len <= 0) {
            return;
        }
        src.position(off);
        int oldLimit = src.limit();
        //int oldLimit2 = buffer.limit();
        src.limit(off + len);
        buffer.rewind();
        buffer.position(offset);
        //buffer.limit(buffer.capacity());
        buffer.put(src);
        src.limit(oldLimit);
        //buffer.limit(oldLimit2);
    }

    /**
     * Copy Data from source buffer
     *
     * @param offset Offset in this buffer
     * @param src Source Buffer
     * @param off offset in source array
     * @param length number of bytes to copy
     */
    @InCpp( {"assert(off + len <= src.capacity());",
             "put(offset, src.getPointer() + off, len);"
            })
    public void put(@SizeT int offset, @Const @Ref FixedBuffer src, @SizeT int off, @SizeT int len) {
        put(offset, src.getTempBuffer(), off, len);
    }

    /**
     * Copy Data to destination buffer
     * (whole buffer is filled)
     *
     * @param offset Offset in this buffer
     * @param dst Destination array
     */
    @InCpp("get(offset, dst.getPointer(), dst.capacity());")
    @ConstMethod public void get(@SizeT int offset, @Ref FixedBuffer dst) {
        ByteBuffer buffer = getTempBuffer();
        dst.buffer.clear();
        buffer.position(offset);
        int oldLimit = buffer.limit();
        buffer.limit(offset + dst.buffer.capacity());
        dst.buffer.put(buffer);
        buffer.limit(oldLimit);
    }

    /**
     * Copy Data from source buffer
     * (whole buffer is copied)
     *
     * @param offset Offset in this buffer
     * @param src Source Buffer
     */
    @InCpp("put(offset, src.getPointer(), src.capacity());")
    public void put(@SizeT int offset, @Const @Ref FixedBuffer src) {
        src.buffer.clear();
        buffer.reset();
        buffer.position(offset);
        buffer.put(src.buffer);
    }


    /**
     * @param offset absolute offset
     * @param v 8 bit integer
     */
    @InCpp("putImpl<int8>(offset, v);")
    @Inline public void putByte(@SizeT int offset, int v) {
        buffer.put(offset, (byte)(v & 0xFF));
    }
    /**
     * @param offset absolute offset
     * @return 8 bit integer
     */
    @InCpp("return getImpl<int8>(offset);")
    @ConstMethod @Inline public byte getByte(@SizeT int offset) {
        return buffer.get(offset);
    }

    /**
     * @param offset absolute offset
     * @param v (1-byte) boolean
     */
    @Inline public void putBoolean(@SizeT int offset, boolean v) {
        putByte(offset, v ? 1 : 0);
    }
    /**
     * @param offset absolute offset
     * @return (1-byte) boolean
     */
    @ConstMethod @Inline public boolean getBoolean(@SizeT int offset) {
        return getByte(offset) != 0;
    }

    /**
     * @param offset absolute offset
     * @param v Character
     */
    @InCpp("putImpl<jchar>(offset, v);")
    @Inline public void putChar(@SizeT int offset, char v) {
        buffer.putChar(offset, v);
    }
    /**
     * @param offset absolute offset
     * @return Character
     */
    @InCpp("return getImpl<jchar>(offset);")
    @ConstMethod @Inline public char getChar(@SizeT int offset) {
        return buffer.getChar(offset);
    }

    /**
     * @param offset absolute offset
     * @param v 16 bit integer
     */
    @InCpp("putImpl<int16>(offset, v);")
    @Inline public void putShort(@SizeT int offset, int v) {
        buffer.putShort(offset, (short)v);
    }
    /**
     * @param offset absolute offset
     * @return 16 bit integer
     */
    @InCpp("return getImpl<int16>(offset);")
    @ConstMethod @Inline public short getShort(@SizeT int offset) {
        return buffer.getShort(offset);
    }

    /**
     * @param offset absolute offset
     * @param v 32 bit integer
     */
    @InCpp("putImpl<int>(offset, v);")
    @Inline public void putInt(@SizeT int offset, int v) {
        buffer.putInt(offset, v);
    }
    /**
     * @param offset absolute offset
     * @return 32 bit integer
     */
    @InCpp("return getImpl<int>(offset);")
    @ConstMethod @Inline public int getInt(@SizeT int offset) {
        return buffer.getInt(offset);
    }

    /**
     * @param offset absolute offset
     * @param v 64 bit integer
     */
    @InCpp("putImpl<int64>(offset, v);")
    @Inline public void putLong(@SizeT int offset, long v) {
        buffer.putLong(offset, v);
    }
    /**
     * @param offset absolute offset
     * @return 64 bit integer
     */
    @InCpp("return getImpl<int64>(offset);")
    @ConstMethod @Inline public long getLong(@SizeT int offset) {
        return buffer.getLong(offset);
    }

    /**
     * @param offset absolute offset
     * @param v 32 bit floating point
     */
    @InCpp("putImpl<float>(offset, v);")
    @Inline public void putFloat(@SizeT int offset, float v) {
        buffer.putFloat(offset, v);
    }

    /**
     * @param offset absolute offset
     * @return 32 bit floating point
     */
    @InCpp("return getImpl<float>(offset);")
    @ConstMethod @Inline public float getFloat(@SizeT int offset) {
        return buffer.getFloat(offset);
    }

    /**
     * @param offset absolute offset
     * @param v 64 bit floating point
     */
    @InCpp("putImpl<double>(offset, v);")
    @Inline public void putDouble(@SizeT int offset, double v) {
        buffer.putDouble(offset, v);
    }
    /**
     * @param offset absolute offset
     * @return 64 bit floating point
     */
    @InCpp("return getImpl<double>(offset);")
    @ConstMethod @Inline public double getDouble(@SizeT int offset) {
        return buffer.getDouble(offset);
    }

    /**
     * @param offset absolute offset
     * @return unsigned 1 byte integer
     */
    @InCpp("return getImpl<uint8>(offset);") @ConstMethod
    public @Unsigned int getUnsignedByte(@SizeT int offset) {
        int b = getByte(offset);
        return b >= 0 ? b : b + 256;
    }

    /**
     * @param offset absolute offset
     * @return unsigned 2 byte integer
     */
    @InCpp("return getImpl<uint16>(offset);") @ConstMethod
    public @Unsigned int getUnsignedShort(@SizeT int offset) {
        short s = getShort(offset);
        return s >= 0 ? s : s + 65536;
    }

    /**
     * Write null-terminated string (16 Bit Characters)
     *
     * @param offset absolute offset in buffer
     * @param s String
     * @param terminate Terminate string with zero?
     */
    @JavaOnly public void putUnicode(@SizeT int offset, String s, boolean terminate) {
        int len = s.length();
        int off = offset;
        for (int i = 0; i < len; i++) {
            putChar(off, s.charAt(i));
            off += 2;
        }
        if (terminate) {
            putChar(off, (char)0);
        }
    }

    /**
     * Read null-terminated string (16 Bit Characters)
     *
     * @param offset absolute offset in buffer
     * @param s String
     */
    @JavaOnly public String getUnicode(@SizeT int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, n = buffer.capacity() - 1; i < n; i += 2) {
            char c = buffer.getChar(i);
            if (c == 0) {
                return sb.toString();
            }
            sb.append(c);
        }
        throw new RuntimeException("String not terminated");
    }

    /**
     * Read string (16 Bit Characters). Stops at null-termination or specified length.
     *
     * @param offset absolute offset
     * @param s String
     * @param length Length of string to read (including possible termination)
     */
    @JavaOnly public String getUnicode(@SizeT int offset, @SizeT int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, n = Math.max(i + (length * 2), buffer.capacity() - 1); i < n; i += 2) {
            char c = buffer.getChar(i);
            if (c == 0) {
                return sb.toString();
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Write null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param offset absolute offset in buffer
     * @param s String
     */
    @Inline public void putString(@SizeT int offset, @Const @Ref String s) {
        putString(offset, s, true);
    }

    /**
     * Write string (8 Bit Characters - Suited for ASCII)
     *
     * @param offset absolute offset in buffer
     * @param s String
     * @param terminate Terminate string with zero?
     */
    @InCpp("put(offset, s.getCString(), terminate ? s.length() + 1 : s.length());")
    public void putString(@SizeT int offset, @Const @Ref String s, boolean terminate) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            putByte(offset + i, (byte)s.charAt(i));
        }
        if (terminate) {
            putByte(offset + len, 0);
        }
    }

    /**
     * Read null-terminated string (8 Bit Characters - Suited for ASCII)
     *
     * @param offset absolute offset in buffer
     */
    @InCpp("return String(buffer + offset);")
    @ConstMethod public String getString(@SizeT int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, n = capacity(); i < n; i++) {
            char c = (char)getByte(i);
            if (c == 0) {
                return sb.toString();
            }
            sb.append(c);
        }
        throw new RuntimeException("String not terminated");
    }

    /**
     * Read String/Line from stream (ends either at line delimiter or 0-character - 8bit)
     *
     * @param offset absolute offset in buffer
     */
    @ConstMethod public String getLine(@SizeT int offset) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset, n = capacity(); i < n; i++) {
            char c = (char)getByte(i);
            if (c == 0 || c == '\n') {
                return sb.toString();
            }
            sb.append(c);
        }
        throw new RuntimeException("String not terminated");
    }

    /**
     * Read string (8 Bit Characters - Suited for ASCII). Stops at null-termination or specified length.
     *
     * @param offset absolute offset
     * @param length Length of string to read
     */
    @ConstMethod public String getString(@SizeT int offset, @SizeT int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = offset, n = Math.min(capacity(), offset + length); i < n; i++) {
            char c = (char)getByte(i);
            if (c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Zero out specified bytes
     *
     * @param offset Offset in buffer to start at
     * @param length Length of area to zero out
     */
    @Inline
    public void zeroOut(@SizeT int offset, @SizeT int length) {
        // JavaOnlyBlock
        for (int i = offset, n = offset + length; i < n; i++) {
            buffer.put(i, (byte)0);
        }

        /*Cpp
        assert(offset + length <= capacityX);
        memset(buffer + offset, 0, length);
         */
    }

    /**
     * Set wrapped buffer to buffer contained in other fixed buffer
     * (only call, when FixedBuffer doesn't own a buffer himself)
     *
     * @param fb
     */
    protected void setCurrentBuffer(@Ptr FixedBuffer fb) {
        //JavaOnlyBlock
        buffer = fb.buffer;

        /*Cpp
        assert(!ownsBuf);
        buffer = fb->buffer;
        capacityX = fb->capacityX;
        ownsBuf = false;
         */
    }

    @JavaOnly
    class TempBuffer {
        ByteBuffer buffer;
//      private int allocationView = -1;

        void init() {
            //if (allocationView != allocationCount) {
            buffer = FixedBuffer.this.buffer.duplicate();
            //}
        }
    }

    @JavaOnly
    long pointer = -1;

    @JavaOnly
    public long getPointer() {
        if (pointer == -1) {
            if (buffer.isDirect() && JNICalls.JNI_AVAILABLE) {
                pointer = JNICalls.getBufferPointer(buffer);
            } else {
                pointer = 0;
            }
        }
        return pointer;
    }

    @JavaOnly
    public void dumpToFile(String filename, int size) {
        try {
            BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(filename));
            int n = size <= 0 ? capacity() : size;
            for (int i = 0; i < n; i++) {
                fos.write(getByte(i));
            }
            fos.close();
        } catch (Exception e) {
        }
    }
}
