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
package org.rrlib.finroc_core_utils.jc.container;


/**
 * @author Max Reichardt
 *
 * This is the base class of a queueable object. It contains the pointer
 * to the next element in singly-linked queue.
 */
public class Queueable {

    /** Terminator (not null for efficiency reasons) */
    public final static Queueable terminator = new Queueable(true);

    /**
     * Pointer to next element in reuse queue... null if there's none
     * Does not need to be volatile... because only critical for reader
     * thread regarding terminator/null (and reader thread sets this himself)...
     * writer changes may be delayed without problem
     */
    public Queueable next = null;

    public Queueable() {}

    private Queueable(boolean isTerminator) {
        next = this;
    }

    /** Delete method */
    public void delete() {}
}
