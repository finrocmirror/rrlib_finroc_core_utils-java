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
package org.finroc.jc.container;

import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.NoCpp;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;

/**
 * @author max
 *
 * This is the base class of a queueable object. It contains the pointer
 * to the next element in singly-linked queue.
 */
@Inline @NoCpp
public class Queueable {

    /** Terminator (not null for efficiency reasons) */
    public final static @PassByValue Queueable terminator = new Queueable(true);

    /**
     * Pointer to next element in reuse queue... null if there's none
     * Does not need to be volatile... because only critical for reader
     * thread regarding terminator/null (and reader thread sets this himself)...
     * writer changes may be delayed without problem
     */
    @Ptr public Queueable next = null;

    public Queueable() {}

    private Queueable(boolean isTerminator) {
        next = this;
    }
}
