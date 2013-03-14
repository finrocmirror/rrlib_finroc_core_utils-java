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
package org.rrlib.finroc_core_utils.jc.jni;

import org.rrlib.finroc_core_utils.serialization.FixedBuffer;


public abstract class StructBase2 extends StructBase {
    public StructBase2() {}
    public StructBase2(FixedBuffer dbb) {
        super(dbb);
    }
    public StructBase2(StructBase parentStruct, int offset32, int offset64) {
        super(parentStruct, offset32, offset64);
    }

    public static class Signed8S extends Field0 {

        public Signed8S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public byte get(long ptr) {
            return JNICalls.getByte(ptr + offset);
        }

        public byte get(long ptr, boolean _64bit) {
            return JNICalls.getByte(ptr + (_64bit ? offset64 : offset32));
        }

        public byte get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public byte get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public byte getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().get(relPtr + offset);
        }

        public byte getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().get(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, byte val) {
            JNICalls.setByte(ptr + offset, val);
        }

        public void set(long ptr, byte val, boolean _64bit) {
            JNICalls.setByte(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, byte val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, byte val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, byte val) {
            dbb.getBuffer().put(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, byte val, boolean _64bit) {
            dbb.getBuffer().put(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Signed8 extends Field {

        protected final Signed8S ref;

        public Signed8(Signed8S ref) {
            this.ref = ref;
        }

        public byte get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public byte get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public byte get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public byte get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public byte getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public byte getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(byte val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(byte val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, byte val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, byte val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, byte val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, byte val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Unsigned8S extends Field0 {

        public Unsigned8S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public short get(long ptr) {
            return (short)(JNICalls.getByte(ptr + offset) & 0xFF);
        }

        public short get(long ptr, boolean _64bit) {
            return (short)(JNICalls.getByte(ptr + (_64bit ? offset64 : offset32)) & 0xFF);
        }

        public short get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public short get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public short getRel(FixedBuffer dbb, int relPtr) {
            return (short)(dbb.getBuffer().get(relPtr + offset) & 0xFF);
        }

        public short getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return (short)(dbb.getBuffer().get(relPtr + (_64bit ? offset64 : offset32)) & 0xFF);
        }

        public void set(long ptr, short val) {
            JNICalls.setByte(ptr + offset, (byte)val);
        }

        public void set(long ptr, short val, boolean _64bit) {
            JNICalls.setByte(ptr + (_64bit ? offset64 : offset32), (byte)val);
        }

        public void set(FixedBuffer dbb, long absPtr, short val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, short val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, short val) {
            dbb.getBuffer().put(relPtr + offset, (byte)val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, short val, boolean _64bit) {
            dbb.getBuffer().put(relPtr + (_64bit ? offset64 : offset32), (byte)val);
        }
    }

    public class Unsigned8 extends Field {

        protected final Unsigned8S ref;

        public Unsigned8(Unsigned8S ref) {
            this.ref = ref;
        }

        public short get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public short get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public short get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public short get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public short getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public short getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(short val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(short val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, short val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, short val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, short val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, short val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Signed16S extends Field0 {

        public Signed16S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public short get(long ptr) {
            return JNICalls.getShort(ptr + offset);
        }

        public short get(long ptr, boolean _64bit) {
            return JNICalls.getShort(ptr + (_64bit ? offset64 : offset32));
        }

        public short get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public short get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public short getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getShort(relPtr + offset);
        }

        public short getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getShort(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, short val) {
            JNICalls.setShort(ptr + offset, val);
        }

        public void set(long ptr, short val, boolean _64bit) {
            JNICalls.setShort(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, short val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, short val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, short val) {
            dbb.getBuffer().putShort(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, short val, boolean _64bit) {
            dbb.getBuffer().putShort(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Signed16 extends Field {

        protected final Signed16S ref;

        public Signed16(Signed16S ref) {
            this.ref = ref;
        }

        public short get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public short get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public short get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public short get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public short getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public short getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(short val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(short val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, short val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, short val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, short val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, short val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Unsigned16S extends Field0 {

        public Unsigned16S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public int get(long ptr) {
            return JNICalls.getShort(ptr + offset) & 0xFFFF;
        }

        public int get(long ptr, boolean _64bit) {
            return JNICalls.getShort(ptr + (_64bit ? offset64 : offset32)) & 0xFFFF;
        }

        public int get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public int get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public int getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getShort(relPtr + offset) & 0xFFFF;
        }

        public int getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getShort(relPtr + (_64bit ? offset64 : offset32)) & 0xFFFF;
        }

        public void set(long ptr, int val) {
            JNICalls.setShort(ptr + offset, (short)val);
        }

        public void set(long ptr, int val, boolean _64bit) {
            JNICalls.setShort(ptr + (_64bit ? offset64 : offset32), (short)val);
        }

        public void set(FixedBuffer dbb, long absPtr, int val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, int val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, int val) {
            dbb.getBuffer().putShort(relPtr + offset, (short)val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, int val, boolean _64bit) {
            dbb.getBuffer().putShort(relPtr + (_64bit ? offset64 : offset32), (short)val);
        }
    }

    public class Unsigned16 extends Field {

        protected final Unsigned16S ref;

        public Unsigned16(Unsigned16S ref) {
            this.ref = ref;
        }

        public int get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public int get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public int get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public int get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public int getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public int getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(int val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(int val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, int val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, int val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, int val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, int val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Signed32S extends Field0 {

        public Signed32S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public int get(long ptr) {
            return JNICalls.getInt(ptr + offset);
        }

        public int get(long ptr, boolean _64bit) {
            return JNICalls.getInt(ptr + (_64bit ? offset64 : offset32));
        }

        public int get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public int get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public int getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getInt(relPtr + offset);
        }

        public int getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getInt(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, int val) {
            JNICalls.setInt(ptr + offset, val);
        }

        public void set(long ptr, int val, boolean _64bit) {
            JNICalls.setInt(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, int val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, int val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, int val) {
            dbb.getBuffer().putInt(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, int val, boolean _64bit) {
            dbb.getBuffer().putInt(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Signed32 extends Field {

        protected final Signed32S ref;

        public Signed32(Signed32S ref) {
            this.ref = ref;
        }

        public int get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public int get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public int get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public int get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public int getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public int getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(int val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(int val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, int val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, int val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, int val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, int val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Unsigned32S extends Field0 {

        public Unsigned32S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public long get(long ptr) {
            return JNICalls.getInt(ptr + offset) & 0xFFFFFFFFL;
        }

        public long get(long ptr, boolean _64bit) {
            return JNICalls.getInt(ptr + (_64bit ? offset64 : offset32)) & 0xFFFFFFFFL;
        }

        public long get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public long get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public long getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getInt(relPtr + offset) & 0xFFFFFFFFL;
        }

        public long getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getInt(relPtr + (_64bit ? offset64 : offset32)) & 0xFFFFFFFFL;
        }

        public void set(long ptr, long val) {
            JNICalls.setInt(ptr + offset, (int)val);
        }

        public void set(long ptr, long val, boolean _64bit) {
            JNICalls.setInt(ptr + (_64bit ? offset64 : offset32), (int)val);
        }

        public void set(FixedBuffer dbb, long absPtr, long val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, long val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val) {
            dbb.getBuffer().putInt(relPtr + offset, (int)val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val, boolean _64bit) {
            dbb.getBuffer().putInt(relPtr + (_64bit ? offset64 : offset32), (int)val);
        }
    }

    public class Unsigned32 extends Field {

        protected final Unsigned32S ref;

        public Unsigned32(Unsigned32S ref) {
            this.ref = ref;
        }

        public long get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public long get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
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
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(long val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
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


    public static class Signed64S extends Field0 {

        public Signed64S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public long get(long ptr) {
            return JNICalls.getLong(ptr + offset);
        }

        public long get(long ptr, boolean _64bit) {
            return JNICalls.getLong(ptr + (_64bit ? offset64 : offset32));
        }

        public long get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public long get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public long getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getLong(relPtr + offset);
        }

        public long getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getLong(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, long val) {
            JNICalls.setLong(ptr + offset, val);
        }

        public void set(long ptr, long val, boolean _64bit) {
            JNICalls.setLong(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, long val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, long val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val) {
            dbb.getBuffer().putLong(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, long val, boolean _64bit) {
            dbb.getBuffer().putLong(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Signed64 extends Field {

        protected final Signed64S ref;

        public Signed64(Signed64S ref) {
            this.ref = ref;
        }

        public long get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public long get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
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
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(long val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
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


    public static class Float32S extends Field0 {

        public Float32S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public float get(long ptr) {
            return JNICalls.getFloat(ptr + offset);
        }

        public float get(long ptr, boolean _64bit) {
            return JNICalls.getFloat(ptr + (_64bit ? offset64 : offset32));
        }

        public float get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public float get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public float getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getFloat(relPtr + offset);
        }

        public float getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getFloat(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, float val) {
            JNICalls.setFloat(ptr + offset, val);
        }

        public void set(long ptr, float val, boolean _64bit) {
            JNICalls.setFloat(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, float val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, float val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, float val) {
            dbb.getBuffer().putFloat(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, float val, boolean _64bit) {
            dbb.getBuffer().putFloat(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Float32 extends Field {

        protected final Float32S ref;

        public Float32(Float32S ref) {
            this.ref = ref;
        }

        public float get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public float get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public float get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public float get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public float getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public float getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(float val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(float val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, float val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, float val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, float val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, float val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Float64S extends Field0 {

        public Float64S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public double get(long ptr) {
            return JNICalls.getDouble(ptr + offset);
        }

        public double get(long ptr, boolean _64bit) {
            return JNICalls.getDouble(ptr + (_64bit ? offset64 : offset32));
        }

        public double get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public double get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public double getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getDouble(relPtr + offset);
        }

        public double getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getDouble(relPtr + (_64bit ? offset64 : offset32));
        }

        public void set(long ptr, double val) {
            JNICalls.setDouble(ptr + offset, val);
        }

        public void set(long ptr, double val, boolean _64bit) {
            JNICalls.setDouble(ptr + (_64bit ? offset64 : offset32), val);
        }

        public void set(FixedBuffer dbb, long absPtr, double val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, double val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, double val) {
            dbb.getBuffer().putDouble(relPtr + offset, val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, double val, boolean _64bit) {
            dbb.getBuffer().putDouble(relPtr + (_64bit ? offset64 : offset32), val);
        }
    }

    public class Float64 extends Field {

        protected final Float64S ref;

        public Float64(Float64S ref) {
            this.ref = ref;
        }

        public double get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public double get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public double get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public double get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public double getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public double getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(double val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(double val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, double val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, double val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, double val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, double val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Bool8S extends Field0 {

        public Bool8S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public boolean get(long ptr) {
            return JNICalls.getByte(ptr + offset) != 0;
        }

        public boolean get(long ptr, boolean _64bit) {
            return JNICalls.getByte(ptr + (_64bit ? offset64 : offset32)) != 0;
        }

        public boolean get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public boolean get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public boolean getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().get(relPtr + offset) != 0;
        }

        public boolean getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().get(relPtr + (_64bit ? offset64 : offset32)) != 0;
        }

        public void set(long ptr, boolean val) {
            JNICalls.setByte(ptr + offset, val ? TRUE : FALSE);
        }

        public void set(long ptr, boolean val, boolean _64bit) {
            JNICalls.setByte(ptr + (_64bit ? offset64 : offset32), val ? TRUE : FALSE);
        }

        public void set(FixedBuffer dbb, long absPtr, boolean val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, boolean val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, boolean val) {
            dbb.getBuffer().put(relPtr + offset, val ? TRUE : FALSE);
        }

        public void setRel(FixedBuffer dbb, int relPtr, boolean val, boolean _64bit) {
            dbb.getBuffer().put(relPtr + (_64bit ? offset64 : offset32), val ? TRUE : FALSE);
        }
    }

    public class Bool8 extends Field {

        protected final Bool8S ref;

        public Bool8(Bool8S ref) {
            this.ref = ref;
        }

        public boolean get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public boolean get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public boolean get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public boolean get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public boolean getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public boolean getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(boolean val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(boolean val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, boolean val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, boolean val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, boolean val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, boolean val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }


    public static class Bool32S extends Field0 {

        public Bool32S(int offset32, int offset64) {
            super(offset32, offset64);
        }

        public boolean get(long ptr) {
            return JNICalls.getInt(ptr + offset) != 0;
        }

        public boolean get(long ptr, boolean _64bit) {
            return JNICalls.getInt(ptr + (_64bit ? offset64 : offset32)) != 0;
        }

        public boolean get(FixedBuffer dbb, long absPtr) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()));
        }

        public boolean get(FixedBuffer dbb, long absPtr, boolean _64bit) {
            return getRel(dbb, (int)(absPtr - dbb.getPointer()), _64bit);
        }

        public boolean getRel(FixedBuffer dbb, int relPtr) {
            return dbb.getBuffer().getInt(relPtr + offset) != 0;
        }

        public boolean getRel(FixedBuffer dbb, int relPtr, boolean _64bit) {
            return dbb.getBuffer().getInt(relPtr + (_64bit ? offset64 : offset32)) != 0;
        }

        public void set(long ptr, boolean val) {
            JNICalls.setInt(ptr + offset, val ? TRUE : FALSE);
        }

        public void set(long ptr, boolean val, boolean _64bit) {
            JNICalls.setInt(ptr + (_64bit ? offset64 : offset32), val ? TRUE : FALSE);
        }

        public void set(FixedBuffer dbb, long absPtr, boolean val) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void set(FixedBuffer dbb, long absPtr, boolean val, boolean _64bit) {
            setRel(dbb, (int)(absPtr - dbb.getPointer()), val);
        }

        public void setRel(FixedBuffer dbb, int relPtr, boolean val) {
            dbb.getBuffer().putInt(relPtr + offset, val ? TRUE : FALSE);
        }

        public void setRel(FixedBuffer dbb, int relPtr, boolean val, boolean _64bit) {
            dbb.getBuffer().putInt(relPtr + (_64bit ? offset64 : offset32), val ? TRUE : FALSE);
        }
    }

    public class Bool32 extends Field {

        protected final Bool32S ref;

        public Bool32(Bool32S ref) {
            this.ref = ref;
        }

        public boolean get() {
            return buffer == null ? ref.get(rootStruct.address + rootOffset) : ref.getRel(buffer, rootStruct.relAddress + rootOffset);
        }

        public boolean get(boolean _64bit) {
            return buffer == null ? ref.get(rootStruct.address + rootOffset, _64bit) : ref.getRel(buffer, rootStruct.relAddress + rootOffset, _64bit);
        }

        public boolean get(long absPtr) {
            return ref.get(buffer, absPtr + rootOffset);
        }

        public boolean get(long absPtr, boolean _64bit) {
            return ref.get(buffer, absPtr + rootOffset, _64bit);
        }

        public boolean getRel(int relPtr) {
            return ref.getRel(buffer, relPtr + rootOffset);
        }

        public boolean getRel(int relPtr, boolean _64bit) {
            return ref.getRel(buffer, relPtr + rootOffset, _64bit);
        }

        public void set(boolean val) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val);
            }
        }

        public void set(boolean val, boolean _64bit) {
            if (buffer == null) {
                ref.set(rootStruct.address + rootOffset, val, _64bit);
            } else {
                ref.setRel(buffer, rootStruct.relAddress + rootOffset, val, _64bit);
            }
        }

        public void set(long absPtr, boolean val) {
            ref.set(buffer, absPtr + rootOffset, val);
        }

        public void set(long absPtr, boolean val, boolean _64bit) {
            ref.set(buffer, absPtr + rootOffset, val, _64bit);
        }

        public void setRel(int relPtr, boolean val) {
            ref.setRel(buffer, relPtr + rootOffset, val);
        }

        public void setRel(int relPtr, boolean val, boolean _64bit) {
            ref.setRel(buffer, relPtr + rootOffset, val, _64bit);
        }
    }

}
