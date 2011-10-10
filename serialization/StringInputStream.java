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
package org.rrlib.finroc_core_utils.serialization;

import java.io.IOException;
import java.io.StringReader;

import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.CppDefault;
import org.rrlib.finroc_core_utils.jc.annotation.CppPrepend;
import org.rrlib.finroc_core_utils.jc.annotation.CppType;
import org.rrlib.finroc_core_utils.jc.annotation.HAppend;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.IncludeClass;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.PostInclude;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.SizeT;
import org.rrlib.finroc_core_utils.jc.annotation.SkipArgs;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author max
 *
 * String input stream.
 * Used for completely deserializing object from a string stream (UTF-8).
 */
@CppPrepend( {
    "int8_t StringInputStream::charMap[256];",
    "int stringInputStreamInitializer = StringInputStream::initCharMap();"
})
@HAppend( {
    "inline StringInputStream& operator>> (StringInputStream& is, char& t) { is.wrapped >> t; return is; }",
    "inline StringInputStream& operator>> (StringInputStream& is, int8_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, int16_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, int32_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, long int& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, long long int& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, uint8_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, uint16_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, uint32_t& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, unsigned long int& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, unsigned long long int& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, float& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, double& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, bool& t) { is.wrapped  >> t; return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, std::string& t) { t = is.readLine(); return is;  }",
    "inline StringInputStream& operator>> (StringInputStream& is, Serializable& t) { t.deserialize(is); return is;  }"
})
@IncludeClass(RRLibSerializableImpl.class)
@Include("<sstream>")
@PostInclude( {"detail/tInputStreamFallback.h", "detail/tStringInputStreamFallback.h"})
public class StringInputStream {

    /** Wrapped string stream */
    @CppType("std::istringstream")
    StringReader wrapped;

    /** Constants for character flags */
    public static final byte LCASE = 1, UCASE = 2, LETTER = 4, DIGIT = 8, WHITESPACE = 16;

    /** Map with flags of all 256 UTF Characters */
    @InCpp("static int8_t charMap[256];")
    private static byte[] charMap = new byte[256];

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"serialization\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("serialization");

    static {
        initCharMap();
    }

    @Init("wrapped(s)")
    public StringInputStream(@Const @Ref @CppType("std::string") String s) {
        wrapped = new StringReader(s);
    }

    /**
     * Initializes char map
     *
     * @return dummy value
     */
    public static int initCharMap() {
        for (int i = 0; i < 256; i++) {
            byte mask = 0;
            if (Character.isLowerCase(i)) {
                mask |= LCASE;
            }
            if (Character.isUpperCase(i)) {
                mask |= UCASE;
            }
            if (Character.isLetter(i)) {
                mask |= LETTER;
            }
            if (Character.isDigit(i)) {
                mask |= DIGIT;
            }
            if (Character.isWhitespace(i)) {
                mask |= WHITESPACE;
            }
            charMap[i] = mask;
        }
        return 0;
    }

    /**
     * @return String until end of stream
     */
    public @CppType("std::string") String readAll() {
        return readUntil("", 0, false);
    }

    /**
     * @return String util end of line
     */
    public @CppType("std::string") String readLine() {
        return readUntil("\n", 0, false);
    }

    /**
     * Read characters until a "stop character" is encountered
     *
     * @param stopAtChars List of "stop characters"
     * @param stopAtFlags Make all characters with specified flags "stop characters"
     * @param trimWhitespace Trim whitespace after reading?
     * @return String
     */
    public @CppType("std::string") String readUntil(@PassByValue @CppType("char*") String stopAtChars, @CppDefault("0") int stopAtFlags, @CppDefault("true") boolean trimWhitespace) {

        @CppType("std::ostringstream")
        StringBuilder sb = new StringBuilder();
        @InCpp("size_t validCharLen = strlen(stopAtChars);")
        int validCharLen = stopAtChars.length();
        @InCpp("const char* ca = stopAtChars;")
        char[] ca = stopAtChars.toCharArray();
        while (true) {
            int c = read();
            if (c == -1) {
                break;
            }

            if ((charMap[c] & stopAtFlags) != 0) {
                unget();
                break;
            }

            boolean stop = false;
            for (@SizeT int i = 0; i < validCharLen; i++) {
                if (c == ca[i]) {
                    stop = true;
                    break;
                }
            }
            if (stop) {
                unget();
                break;
            }

            //JavaOnlyBlock
            sb.append((char)c);

            //Cpp sb << ((char)c);

        }

        if (trimWhitespace) {

            //JavaOnlyBlock
            return sb.toString().trim();

            //Cpp return trim(sb._str());
        }

        //JavaOnlyBlock
        return sb.toString();

        //Cpp return sb._str();
    }

    /**
     * Read "valid" characters. Stops at "invalid" character
     *
     * @param validChars List of "valid characters"
     * @param validFlags Make all characters with specified flags "valid characters"
     * @param trimWhitespace Trim whitespace after reading?
     * @return String
     */
    public @CppType("std::string") String readWhile(@PassByValue @CppType("char*") String validChars, @CppDefault("0") int validFlags, @CppDefault("true") boolean trimWhitespace) {

        @CppType("std::ostringstream")
        StringBuilder sb = new StringBuilder();
        @InCpp("size_t validCharLen = strlen(validChars);")
        int validCharLen = validChars.length();
        @InCpp("const char* ca = validChars;")
        char[] ca = validChars.toCharArray();
        while (true) {
            int c = read();
            if (c == -1) {
                break;
            }

            if ((charMap[c] & validFlags) == 0) {
                boolean valid = false;
                for (@SizeT int i = 0; i < validCharLen; i++) {
                    if (c == ca[i]) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    unget();
                    break;
                }
            }

            //JavaOnlyBlock
            sb.append((char)c);

            //Cpp sb << ((char)c);

        }

        if (trimWhitespace) {

            //JavaOnlyBlock
            return sb.toString().trim();

            //Cpp return trim(sb._str());
        }

        //JavaOnlyBlock
        return sb.toString();

        //Cpp return sb._str();
    }

    /*Cpp
    std::string trim(const std::string& s) {
      std::string result;
      size_t len = s._size();
      size_t st = 0;

      while ((st < len) && (_isspace(s[st])))
      {
        st++;
      }
      while ((st < len) && (_isspace(s[len - 1])))
      {
        len--;
      }
      return ((st > 0) || (len < s._size())) ? s._substr(st, len - st) : s;
    }
     */

    /**
     * @return next character in stream. -1 when end of stream is reached
     */
    @InCpp( {"char result; wrapped._get(result);",
             "if (wrapped._eof()) { return -1; }",
             "return result;"
            })
    public int read() {
        try {
            wrapped.mark(256);
            return wrapped.read();
        } catch (IOException e) {
        }
        return -1;
    }

    /**
     * @return next character in stream (without advancing in stream). -1 when end of stream is reached
     */
    @InCpp( {"char result = wrapped._peek();",
             "if (wrapped._eof()) { return -1; }",
             "return result;"
            })
    public int peek() {
        try {
            wrapped.mark(256);
            int result = wrapped.read();
            wrapped.reset();
            return result;
        } catch (IOException e) {
        }
        return -1;
    }

    /**
     * Put read character back to stream
     */
    @InCpp("wrapped._unget();")
    public void unget() {
        try {
            wrapped.reset();
        } catch (IOException e) {
        }
    }

    public String getLogDescription() {
        return "StringInputStream()";
    }

    /**
     * @return Enum value
     */
    @SuppressWarnings( { "rawtypes", "unchecked" })
    @SkipArgs("1")
    public <E extends Enum> E readEnum(Class<E> eclass) {
        // parse input
        String enumString = readWhile("", DIGIT | LETTER | WHITESPACE, true).trim();
        int c1 = read();
        String numString = "";
        if (c1 == '(') {
            numString = readUntil(")", 0, true).trim();
            int c2 = read();
            if (c2 != ')') {
                throw new RuntimeException("Did not read expected bracket");
            }
        }

        // deal with input
        Object[] constants = eclass.getEnumConstants();
        if (enumString.length() > 0) {
            for (int i = 0; i < constants.length; i++) {
                if (enumString.equalsIgnoreCase(constants[i].toString())) {
                    return (E) constants[i];
                }
            }
        }

        if (enumString.length() > 0) {
            logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "Could not find enum constant for string '" + enumString + "'. Trying number '" + numString + "'");
        }
        if (numString.length() == 0) {
            throw new RuntimeException("No Number String specified");
        }
        int n = Integer.parseInt(numString);
        if (n >= constants.length) {
            logDomain.log(LogLevel.LL_ERROR, getLogDescription(), "Number " + n + " out of range for enum (" + constants.length + ")");
            throw new RuntimeException("Number out of range");
        }
        return (E) constants[n];
    }
}
