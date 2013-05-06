//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Max Reichardt
 *
 */
public class AtomicInt64 extends AtomicLong {

    /**
     * UID
     */
    private static final long serialVersionUID = 3450814650623733796L;

    public AtomicInt64(long i) {
        super(i);
    }

    public AtomicInt64() {
        this(0);
    }
}
