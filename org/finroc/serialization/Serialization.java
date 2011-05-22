/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2010-2011 Max Reichardt,
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

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppPrepend;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.IncludeClass;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.PostInclude;
import org.finroc.jc.annotation.Prefix;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.Unsigned;
import org.finroc.serialization.RRLibSerializableImpl;
import org.finroc.xml.XMLNode;

/**
 * @author max
 *
 * Helper class:
 * Serializes binary CoreSerializables to hex string - and vice versa.
 */
@IncludeClass(RRLibSerializableImpl.class)
@Prefix("s")
@CppPrepend( {
    "char _sSerialization::TO_HEX[16] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };",
    "int _sSerialization::TO_INT[256];",
    "",
    "std::string _sSerialization::demangle(const char* mangled) {",
    "   int status = 0;",
    "   char* tmp = abi::__cxa_demangle(mangled, 0, 0, &status);",
    "   std::string result(tmp);",
    "   _free(tmp);",
    "   return result;",
    "}"
})
@CppInclude("<cxxabi.h>")
@PostInclude( {"deepcopy.h"})
public class Serialization {

    /** int -> hex char */
    @InCpp("static char TO_HEX[16];")
    private static final char[] TO_HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /** hex char -> int */
    @InCpp("static int TO_INT[256];")
    private static final int[] TO_INT = new int[256];

    /** Helper variable to trigger static initialization in C++ */
    @SuppressWarnings("unused")
    private static int INIT_HELPER = staticInit();

    /** may only be accessed in synchronized context */
    @JavaOnly private static final ThreadLocal<MemoryBuffer> buffer = new ThreadLocal<MemoryBuffer>();

    public static int staticInit() {
        for (@SizeT int i = 0; i < 256; i++) {
            TO_INT[i] = -1;
        }
        TO_INT['0'] = 0;
        TO_INT['1'] = 1;
        TO_INT['2'] = 2;
        TO_INT['3'] = 3;
        TO_INT['4'] = 4;
        TO_INT['5'] = 5;
        TO_INT['6'] = 6;
        TO_INT['7'] = 7;
        TO_INT['8'] = 8;
        TO_INT['9'] = 9;
        TO_INT['A'] = 0xA;
        TO_INT['B'] = 0xB;
        TO_INT['C'] = 0xC;
        TO_INT['D'] = 0xD;
        TO_INT['E'] = 0xE;
        TO_INT['F'] = 0xF;
        TO_INT['a'] = 0xA;
        TO_INT['b'] = 0xB;
        TO_INT['c'] = 0xC;
        TO_INT['d'] = 0xD;
        TO_INT['e'] = 0xE;
        TO_INT['f'] = 0xF;
        return 0;
    }

    /**
     * Serializes binary CoreSerializable to hex string
     *
     * @param cs CoreSerializable
     * @param os String output stream
     */
    @InCppFile
    public static void serializeToHexString(@Const @Ptr RRLibSerializable cs, @Ref StringOutputStream os) {
        @CppType("StackMemoryBuffer<65536>")
        @PassByValue MemoryBuffer cb = new MemoryBuffer();
        @PassByValue OutputStreamBuffer co = new OutputStreamBuffer(cb);
        cs.serialize(co);
        co.close();
        @PassByValue InputStreamBuffer ci = new InputStreamBuffer(cb);
        convertBinaryToHexString(ci, os);
        ci.close();
    }

    /**
     * Converts binary to hex string
     *
     * @param src Input stream that contains binary data
     * @param co Output stream to write hex string to
     */
    public static void convertBinaryToHexString(@Ref InputStreamBuffer src, @Ref StringOutputStream os) {
        while (src.moreDataAvailable()) {
            @Unsigned byte b = src.readByte();
            @Unsigned int b1 = b >>> 4;
            @Unsigned int b2 = b & 0xF;
            try {
                os.append(TO_HEX[b1]);
                os.append(TO_HEX[b2]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deserializes binary CoreSerializable from hex string
     *
     * @param cs CoreSerializable
     * @param s Hex String to deserialize from
     */
    @InCppFile
    public static void deserializeFromHexString(@Ptr RRLibSerializable cs, @Ref StringInputStream s) throws Exception {
        @CppType("StackMemoryBuffer<65536>")
        @PassByValue MemoryBuffer cb = new MemoryBuffer();
        @PassByValue OutputStreamBuffer co = new OutputStreamBuffer(cb);
        convertHexStringToBinary(s, co);
        co.close();
        @PassByValue InputStreamBuffer ci = new InputStreamBuffer(cb);
        cs.deserialize(ci);
        ci.close();
    }

    /**
     * Converts hex string from StringInputStream to binary
     *
     * @param src Input stream that contains hex string
     * @param co Output stream to write binary data to
     */
    public static void convertHexStringToBinary(@Ref StringInputStream src, @Ref OutputStreamBuffer co) throws Exception {
        int c1;
        while ((c1 = src.read()) != -1) {
            int c2 = src.read();
            if (c2 == -1) {

                //JavaOnlyBlock
                throw new Exception("not a valid hex string (should have even number of chars)");

                //Cpp throw std::runtime_error("not a valid hex string (should have even number of chars)");
            }
            if (TO_INT[c1] < 0 || TO_INT[c2] < 0) {

                //JavaOnlyBlock
                throw new Exception("invalid hex chars: " + c1 + c2);

                //Cpp throw std::runtime_error("invalid hex chars");;
            }
            int b = (TO_INT[c1] << 4) | TO_INT[c2];
            co.writeByte((byte)b);
        }
    }

    /**
     * Standard XML serialization fallback implementation
     * (for Java, because we don't have multiple inheritance here)
     *
     * @param node XML node
     * @param rs Serializable object
     */
    @JavaOnly
    public static void serialize(@Ref XMLNode node, @Const @Ref RRLibSerializable rs) throws Exception {
        node.setContent(serialize(rs));
    }

    /**
     * Serializes string stream serializable object to string
     * (convenience function)
     *
     * @param cs Serializable
     * @return String
     */
    public static @CppType("std::string") String serialize(@Const @Ref RRLibSerializable rs) {
        StringOutputStream os = new StringOutputStream();
        rs.serialize(os);
        return os.toString();
    }

    /**
     * Serializes generic object to string
     * (convenience function)
     *
     * @param cs Serializable
     * @return String
     */
    public static @CppType("std::string") String serialize(@Const @Ref GenericObject go) {
        StringOutputStream os = new StringOutputStream();
        go.serialize(os);
        return os.toString();
    }

    /**
     * Creates deep copy of serializable object
     *
     * @param src Object to be copied
     * @param dest Object to copy to
     */
    @HAppend( {})
    @InCpp( {"DefaultFactory df;",
             "detail::deepCopy(src, dest, f != NULL ? f : (Factory*)&df);"
            })
    public static <T extends RRLibSerializable> void deepCopy(@Const @Ref T src, @Ref T dest, @CppDefault("NULL") @Ptr Factory f) {
        MemoryBuffer buf = buffer.get();
        if (buf == null) {
            buf = new MemoryBuffer(16384);
            buffer.set(buf);
        }
        deepCopyImpl(src, dest, f, buf);
    }

    /**
     * Creates deep copy of serializable object using serialization to and from specified memory buffer
     *
     * @param src Object to be copied
     * @param dest Object to copy to
     * @param buf Memory buffer to use
     */
    @Inline
    public static <T extends RRLibSerializable> void deepCopyImpl(@Const @Ref T src, @Ref T dest, @Ptr Factory f, @Ref MemoryBuffer buf) {
        buf.clear();
        @PassByValue OutputStreamBuffer os = new OutputStreamBuffer(buf);

        //JavaOnlyBlock
        src.serialize(os);

        //Cpp os << src;

        os.close();
        @PassByValue InputStreamBuffer ci = new InputStreamBuffer(buf);
        ci.setFactory(f);

        //JavaOnlyBlock
        dest.deserialize(ci);

        //Cpp ci >> dest;

        ci.close();
    }

    /**
     * Serialization-based equals()-method
     * (not very efficient/RT-capable - should therefore not be called regular loops)
     *
     * @param obj1 Object1
     * @param obj2 Object2
     * @returns true if both objects are serialized to the same binary data (usually they are equal then)
     */
    public static boolean equals(@Const @Ref GenericObject obj1, @Const @Ref GenericObject obj2) {
        if (obj1.getType() != obj2.getType()) {
            return false;
        }

        @CppType("StackMemoryBuffer<32768>")
        @PassByValue MemoryBuffer buf1 = new MemoryBuffer();
        @CppType("StackMemoryBuffer<32768>")
        @PassByValue MemoryBuffer buf2 = new MemoryBuffer();
        @PassByValue OutputStreamBuffer os1 = new OutputStreamBuffer(buf1);
        @PassByValue OutputStreamBuffer os2 = new OutputStreamBuffer(buf2);
        obj1.serialize(os1);
        obj2.serialize(os2);
        os1.close();
        os2.close();

        if (buf1.getSize() != buf2.getSize()) {
            return false;
        }

        @PassByValue InputStreamBuffer is1 = new InputStreamBuffer(buf1);
        @PassByValue InputStreamBuffer is2 = new InputStreamBuffer(buf2);


        for (int i = 0; i < buf1.getSize(); i++) {
            if (is1.readByte() != is2.readByte()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Resize vector (also works for vectors with noncopyable types)
     *
     * @param vector Vector to resize
     * @param newSize New Size
     */
    @InCpp("detail::Resize<std::vector<T>, T, !boost::is_base_of<boost::noncopyable, T>::value>::resize(vector, newSize);") @HAppend( {})
    static public <T> void resizeVector(@CppType("std::vector<T>") @Ref PortDataList<?> vector, @SizeT int newSize) {
        vector.resize(newSize);
    }

    /*Cpp

    // demangle mangled type name
    static std::string demangle(const char* mangled);

     */
}
