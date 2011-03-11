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
import org.finroc.serialization.FixedBuffer;

/**
 * @author max
 *
 * This class is similar to Javolution's Struct class.
 * It can be used to access C++ Classes and Structs from Java
 * (it is possible without using JNI).
 *
 * It is used for automatic code generation.
 */
@JavaOnly
public abstract class Struct extends StructBase2 {

    private static final boolean _64 = JNIInfo.is64BitPlatform();

    public Struct() {}

    public Struct(FixedBuffer dbb) {
        super(dbb);
    }

    public Struct(StructBase parentStruct, int offset32, int offset64) {
        super(parentStruct, offset32, offset64);
    }

    public static class Unsigned32orSigned64S extends Field0 {

        protected final Unsigned32S u32;
        protected final Signed64S u64;

        public Unsigned32orSigned64S(int offset32, int offset64) {
            super(offset32, offset64);
            u32 = new Unsigned32S(offset32, offset64);
            u64 = new Signed64S(offset32, offset64);
        }

        public long get(long ptr) {
            return _64 ? u64.get(ptr, true) : u32.get(ptr, false);
        }

        public long get(long ptr, boolean _64bit) {
            return _64bit ? u64.get(ptr, true) : u32.get(ptr, false);
        }

        public long get(FixedBuffer dbb, long absPtr) {
            return _64 ? u64.get(dbb, absPtr, true) : u32.get(dbb, absPtr, false);
        }

        public long get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return _64bit ? u64.get(dbb, absPtr, true) : u32.get(dbb, absPtr, false);
        }

        public long getRel(FixedBuffer dbb, int relPtr) {
            return _64 ? u64.getRel(dbb, relPtr, true) : u32.getRel(dbb, relPtr, false);
        }

        public long getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return _64bit ? u64.getRel(dbb, relPtr, true) : u32.getRel(dbb, relPtr, false);
        }

        public void set(long ptr, long val) {
            if (_64) {
                u64.set(ptr, val, true);
            } else {
                u32.set(ptr, val, false);
            }
        }

        public void set(long ptr, long val, boolean _64bit) {
            if (_64bit) {
                u64.set(ptr, val, true);
            } else {
                u32.set(ptr, val, false);
            }
        }

        public void set(FixedBuffer dbb, long absPtr, long val) {
            if (_64) {
                u64.set(dbb, absPtr, val, true);
            } else {
                u32.set(dbb, absPtr, val, false);
            }
        }

        public void set(FixedBuffer dbb, long absPtr, long val, boolean _64bit) {
            if (_64bit) {
                u64.set(dbb, absPtr, val, true);
            } else {
                u32.set(dbb, absPtr, val, false);
            }
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val) {
            if (_64) {
                u64.setRel(dbb, relPtr, val, true);
            } else {
                u32.setRel(dbb, relPtr, val, false);
            }
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val, boolean _64bit) {
            if (_64bit) {
                u64.setRel(dbb, relPtr, val, true);
            } else {
                u32.setRel(dbb, relPtr, val, false);
            }
        }
    }

    public class Unsigned32orSigned64 extends Field {

        protected final Unsigned32orSigned64S ref;

        public Unsigned32orSigned64(Unsigned32orSigned64S ref) {
            this.ref = ref;
        }

        public long get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.get(buffer, rootStruct.relAddress + rootOffset);
        }

        public long get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.get(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public long get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public long get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public long getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public long getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(long val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.set(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(long val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.set(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, long val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, long val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, long val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, long val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }

    public static class PointerS extends Unsigned32orSigned64S {

        public PointerS(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public long getOffsetInBuffer(FixedBuffer dbb, long absPtr) {
            return get(dbb, absPtr) - dbb.getPointer();
        }

        public long getOffsetInBuffer(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return get(dbb, absPtr, _64bit) - dbb.getPointer();
        }

        public long getOffsetInBufferRel(FixedBuffer dbb, int relPtr) {
            return getRel(dbb, relPtr) - dbb.getPointer();
        }

        public long getOffsetInBufferRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return getRel(dbb, relPtr, _64bit) - dbb.getPointer();
        }
    }

    public class Pointer extends Unsigned32orSigned64 {

        public Pointer(PointerS ref) {
            super(ref);
        }

        public long getOffsetInBuffer() {
            return ref.get(buffer, rootStruct.relAddress + rootOffset) - buffer.getPointer();
        }

        public long getOffsetInBuffer(boolean _64bit) {
            return ref.get(buffer, rootStruct.relAddress + rootOffset, _64bit) - buffer.getPointer();
        }

        public long getOffsetInBuffer(FixedBuffer dbb, long absPtr) {
            return ref.get(buffer, absPtr + rootOffset) - buffer.getPointer();
        }

        public long getOffsetInBuffer(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit) - buffer.getPointer();
        }

        public long getOffsetInBufferRel(FixedBuffer dbb, int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset) - buffer.getPointer();
        }

        public long getOffsetInBufferRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset) - buffer.getPointer();
        }
    }

    public static class InnerStruct extends Field0 {

        public InnerStruct(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public long getAddress(long ptr) {
            return ptr + offset;
        }

        public int getAddress(int ptr) {
            return ptr + offset;
        }

        public long getAddress(long ptr, boolean _64bit) {
            return _64bit ? ptr + offset64 : ptr + offset32;
        }

        public int getAddress(int ptr, boolean _64bit) {
            return _64bit ? ptr + offset64 : ptr + offset32;
        }
    }
}
