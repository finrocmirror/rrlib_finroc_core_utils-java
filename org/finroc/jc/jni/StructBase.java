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
package org.finroc.jc.jni;

import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.stream.FixedBuffer;

@JavaOnly
public abstract class StructBase {

    /** bool constants */
    public static final byte TRUE = 1, FALSE = 0;

    /** Constant to AND unsigned variables with */
    public static final long UNSIGNED_INT_GET = 0xFFFFFFFFL;
    public static final int UNSIGNED_SHORT_GET = 0xFFFF;
    public static final short UNSIGNED_BYTE_GET = 0xFF;

    /** Pointer to instance to read/write */
    protected long address;

    /** Buffer to read or write from (if not available memory is accessed directly) */
    protected FixedBuffer buffer;

    /** Pointer to instance relative to buffer - or to parent struct */
    protected int relAddress;

    /** for inner structs */
    protected final StructBase rootStruct;
    protected final int rootOffset, rootOffset32, rootOffset64;

    /** size on 32 bit systems */
    public abstract int getSize32();

    /** size on 64 bit systems */
    public abstract int getSize64();

    public StructBase() {
        this(null);
    }

    public StructBase(FixedBuffer dbb) {
        buffer = dbb;
        rootStruct = this;
        rootOffset32 = 0;
        rootOffset64 = 0;
        rootOffset = 0;
    }

    public StructBase(StructBase parentStruct, int offset32, int offset64) {
        buffer = parentStruct.buffer;
        rootStruct = parentStruct.rootStruct;
        rootOffset32 = parentStruct.rootOffset32 + offset32;
        rootOffset64 = parentStruct.rootOffset64 + offset64;
        rootOffset = JNIInfo.is64BitPlatform() ? rootOffset64 : rootOffset32;
    }

    /** Field of class - variant that always takes a pointer */
    protected abstract static class Field0 {

        /** Offset in class/struct (platform default) */
        protected final int offset;

        /** Offset on 32 and 64 Bit machines */
        protected final int offset32, offset64;

        public Field0(int offset32, int offset64) {
            this.offset32 = offset32;
            this.offset64 = offset64;
            this.offset = JNIInfo.is64BitPlatform() ? offset64 : offset32;
        }

        public int getOffset() {
            return offset;
        }

        public int getOffset32() {
            return offset32;
        }

        public int getOffset64() {
            return offset64;
        }
    }

    /** Field of class - variant that is instantiated with C-Class and uses its pointer */
    protected abstract class Field {}

    /**
     * @return Size on current system
     */
    public int getSize() {
        return JNIInfo.is64BitPlatform() ? getSize32() : getSize64();
    }

    /**
     * @return the address
     */
    public long getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(long address) {
        this.address = address;
    }

    /**
     * @return the relAddress
     */
    public int getRelAddress() {
        return relAddress;
    }

    /**
     * @param relAddress the relAddress to set
     */
    public void setRelAddress(int relAddress) {
        this.relAddress = relAddress;
    }

    /**
     * @return Buffer that is read from/written to (if not available memory is accessed directly)
     */
    public FixedBuffer getBuffer() {
        return buffer;
    }

    /**
     * @param buffer Buffer that is read from/written to (if not available memory is accessed directly)
     */
    public void setBuffer(FixedBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * Set Buffers of specified structs
     *
     * @param b Buffer
     * @param structs Structs to set buffer of
     */
    public static void setBuffer(FixedBuffer b, StructBase... structs) {
        for (StructBase struct : structs) {
            struct.setBuffer(b);
        }
    }
}
