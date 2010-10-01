/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010 Robotics Research Lab, University of Kaiserslautern
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
package org.finroc.xml;

import java.util.ArrayList;

import org.finroc.jc.annotation.SizeT;

/**
 * @author max
 *
 * Vector of xml nodes
 * (mimimal std::vector interface)
 */
public class XMLNodeVector {

    /** wrapped arraylist */
    private ArrayList<XMLNode> wrapped = new ArrayList<XMLNode>();

    /**
     * Add XML node
     *
     * @param node Node
     */
    void addNode(XMLNode node) {
        wrapped.add(node);
    }

    /**
     * @return size of vector
     */
    public @SizeT int size() {
        return wrapped.size();
    }

    /**
     * @param i index
     * @return Element at index
     */
    public XMLNode at(@SizeT int i) {
        return wrapped.get(i);
    }
}
