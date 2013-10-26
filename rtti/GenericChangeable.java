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
package org.rrlib.finroc_core_utils.rtti;

/**
 * @author Max Reichardt
 *
 * Interface for data types that can be changed using transactions T.
 */
public interface GenericChangeable<T> {

    /**
     * @param t Change/Transaction to apply
     * @param parameter1 Custom parameter (e.g. start index)
     * @param parameter2 Custom parameter 2 (e.g. length)
     */
    public void applyChange(T t, long parameter1, long parameter2);
}
