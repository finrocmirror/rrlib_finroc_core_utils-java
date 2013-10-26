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
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * Default implementation of Serializable
 */
public abstract class RRLibSerializableImpl extends LogUser implements RRLibSerializable {

    /**
     * @param os Stream to serialize object to
     */
    @Override
    public abstract void serialize(OutputStreamBuffer os);

    /**
     * Deserialize object. Object has to already exists.
     * Should be suited for reusing old objects.
     *
     * @param readView Stream to deserialize from
     */
    @Override
    public abstract void deserialize(InputStreamBuffer is);

    /**
     * Serialize object as string (e.g. for xml output)
     *
     * @param os String output stream
     */
    @Override
    public void serialize(StringOutputStream os) {
        Serialization.serializeToHexString(this, os);
    }

    /**
     * Deserialize object. Object has to already exists.
     * Should be suited for reusing old objects.
     *
     * Parsing errors should throw an Exception - and set object to
     * sensible (default?) value
     *
     * @param s String to deserialize from
     */
    @Override
    public void deserialize(StringInputStream s) throws Exception {
        Serialization.deserializeFromHexString(this, s);
    }

    /**
     * Serialize object to XML
     *
     * @param node Empty XML node (name shouldn't be changed)
     */
    @Override
    public void serialize(XMLNode node) throws Exception {
        node.setContent(Serialization.serialize(this));
    }

    /**
     * Deserialize from XML Node
     *
     * @param node Node to deserialize from
     */
    @Override
    public void deserialize(XMLNode node) throws Exception {
        StringInputStream is = new StringInputStream(node.hasTextContent() ? node.getTextContent() : "");
        deserialize(is);
    }
}
