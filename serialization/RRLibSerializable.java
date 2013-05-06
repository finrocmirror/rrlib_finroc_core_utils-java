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
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.rtti.DataType;
import org.rrlib.finroc_core_utils.rtti.DataTypeBase;
import org.rrlib.finroc_core_utils.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * Classes that implement this interface can be serialized and deserialized from
 * a binary stream very efficiently (without unnecessary object allocation)
 *
 * Classes that cannot implement this interface should overload the >> and << operators.
 */
public interface RRLibSerializable {

    /** Data type of this class */
    public final static DataTypeBase TYPE = new DataType<RRLibSerializable>(RRLibSerializable.class);

    /**
     * @param stream Stream to serialize object to
     */
    public void serialize(OutputStreamBuffer stream);

    /**
     * Deserialize object. Object has to exists already.
     * Should be suitable for reusing old objects.
     *
     * @param stream Stream to deserialize from
     */
    public void deserialize(InputStreamBuffer stream);

    /**
     * Serialize object as string (e.g. for xml output)
     *
     * @param stream String output stream
     */
    public void serialize(StringOutputStream stream);

    /**
     * Deserialize object. Object has to exists already.
     * Should be suitable for reusing old objects.
     *
     * Parsing errors should throw an Exception - and set object to
     * sensible (default?) value
     *
     * @param stream String stream to deserialize from
     */
    public void deserialize(StringInputStream stream) throws Exception;

    /**
     * Serialize object to XML
     *
     * @param node XML node (name shouldn't be changed, attributes "name" and "type" neither)
     */
    public void serialize(XMLNode node) throws Exception;

    /**
     * Deserialize from XML Node
     *
     * @param node Node to deserialize from
     */
    public void deserialize(XMLNode node) throws Exception;

    /**
     * Empty Serializable
     */
    public class EmptySerialiazble extends RRLibSerializableImpl {

        public final static DataType<EmptySerialiazble> TYPE = new DataType<EmptySerialiazble>(EmptySerialiazble.class, "Empty Serializable");

        @Override
        public void serialize(OutputStreamBuffer stream) {
        }

        @Override
        public void deserialize(InputStreamBuffer stream) {
        }
    }
}
