/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2011 Max Reichardt,
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

import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.IncludeClass;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.NonVirtual;
import org.finroc.jc.annotation.PostInclude;

/**
 * @author max
 *
 * String output stream.
 * Used for completely serializing object to a string stream (UTF-8).
 */
@HAppend( {
    "inline StringOutputStream& operator<< (StringOutputStream& os, char t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, int8_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, int16_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, int32_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, long int t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, long long int t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, uint8_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, uint16_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, uint32_t t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, unsigned long int t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, unsigned long long int t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, float t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, double t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, bool t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, const char* t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, const std::string& t) { os.wrapped << t; return os; }",
    "inline StringOutputStream& operator<< (StringOutputStream& os, const Serializable& t) { t.serialize(os); return os; }"
})
@IncludeClass(RRLibSerializableImpl.class)
@Include("<sstream>")
@NoCpp @Inline
@PostInclude( {"detail/tOutputStreamFallback.h", "detail/tStringOutputStreamFallback.h"})
public class StringOutputStream {

    /** Wrapped string stream */
    @CppType("std::ostringstream")
    StringBuilder wrapped;

    /*Cpp
    template <typename T>
    StringOutputStream& append(const T& t) {
        *this << t;
        return *this;
    }
     */

    @Init("wrapped()")
    public StringOutputStream() {
    }

    /**
     * @param length Initial length of buffer (TODO: in C++ this currently has now effect)
     */
    @Init("wrapped()")
    public StringOutputStream(int length) {

        //JavaOnlyBlock
        wrapped.setLength(length);
    }

    @JavaOnly
    public StringBuilder append(String str) {
        return wrapped.append(str);
    }

    @JavaOnly
    public StringBuilder append(StringBuffer sb) {
        return wrapped.append(sb);
    }

    @JavaOnly
    public StringBuilder append(CharSequence s) {
        return wrapped.append(s);
    }

    @JavaOnly
    public StringBuilder append(CharSequence s, int start, int end) {
        return wrapped.append(s, start, end);
    }

    @JavaOnly
    public StringBuilder append(char[] str) {
        return wrapped.append(str);
    }

    @JavaOnly
    public StringBuilder append(char[] str, int offset, int len) {
        return wrapped.append(str, offset, len);
    }

    @JavaOnly
    public StringBuilder append(boolean b) {
        return wrapped.append(b);
    }

    @JavaOnly
    public StringBuilder append(char c) {
        return wrapped.append(c);
    }

    @JavaOnly
    public StringBuilder append(int i) {
        return wrapped.append(i);
    }

    @JavaOnly
    public StringBuilder append(long lng) {
        return wrapped.append(lng);
    }

    @JavaOnly
    public StringBuilder append(float f) {
        return wrapped.append(f);
    }

    @JavaOnly
    public StringBuilder append(double d) {
        return wrapped.append(d);
    }

    @NonVirtual @InCpp("return wrapped._str();") @CppType("std::string")
    public String toString() {
        return super.toString();
    }

    /**
     * Clear contents and reset
     */
    @InCpp("wrapped._str(std::_string());")
    public void clear() {
        wrapped.delete(0, wrapped.length());
    }
}
