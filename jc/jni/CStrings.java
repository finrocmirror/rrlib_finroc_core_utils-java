//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.jni;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Max Reichardt
 *
 * String array in C++ representation (char **)
 */
public class CStrings extends PointerArray {

    public CStrings(String[] strings) {
        super(toCStrings(strings));
    }

    public CStrings(long pointer) {
        super(pointer);
    }

    private static List<UsedInC> toCStrings(String[] strings) {
        List<UsedInC> list = new ArrayList<UsedInC>();
        for (String s : strings) {
            list.add(new CString(s));
        }
        return list;
    }

    public String getString(int index) {
        return JNICalls.toString(getPointer(index));
    }

    public CString getCString(int index) {
        return new CString(getPointer(index));
    }

    public String[] toStrings(int elemCount) {
        String[] result = new String[elemCount];
        for (int i = 0; i < elemCount; i++) {
            result[i] = getString(i);
        }
        return result;
    }
}
